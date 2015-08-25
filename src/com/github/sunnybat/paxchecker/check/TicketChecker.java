package com.github.sunnybat.paxchecker.check;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Phaser;

/**
 *
 * @author Sunny
 */
public final class TicketChecker {

  private final ArrayList<Check> checks = new ArrayList<>();
  private final ArrayList<String> openedLinks = new ArrayList<>();
  private com.github.sunnybat.paxchecker.gui.Status status;
  private String linkFound = "";
  private ExecutorService threadPool;
  private Phaser threadWait;

  /**
   * Initializes the TicketChecker class.
   *
   * @param s The Status to update, or null if none
   */
  public TicketChecker(com.github.sunnybat.paxchecker.gui.Status s) {
    status = s;
    threadPool = Executors.newCachedThreadPool();
    threadWait = new Phaser();
    threadWait.register();
  }

  /**
   * Adds a new Checker instance to the program.
   *
   * @param c The Checker to add
   */
  public void addChecker(Check c) {
    //c.init(status, threadWait); // MOVE LATER
    checks.add(c);
  }

  /**
   * Initializes the Checkers registered with the program.
   */
  public void initCheckers() {
    for (Check c : checks) {
      c.init(status, threadWait);
    }
  }

  /**
   * Checks whether or not the registered Checkers have an update. This method may block for several seconds.
   *
   * @return True if an update is found, false if not
   */
  public boolean isUpdated() {
    if (status != null) {
      status.setForceCheckEnabled(false);
    }
    for (Check c : checks) {
      threadPool.submit(c);
    }
    System.out.println("Waiting: TC");
    threadWait.arriveAndAwaitAdvance();
    System.out.println("Finished waiting");
    for (Check c : checks) {
      //c.updateLink();
      if (c.ticketsFound()) {
        if (!hasOpenedLink(c.getLink())) {
          System.out.println("FOUND LINK: " + c.getLink());
          linkFound(c.getLink());
          c.reset();
          return true;
        } else {
          System.out.println("Link found, but already opened: " + c.getLink());
          c.reset();
        }
      } else {
        System.out.println("Link: " + c.getLink());
      }
    }
    if (status != null) {
      status.setForceCheckEnabled(true);
    }
    return false;
  }

  /**
   * Sets the link found.
   *
   * @param link The link to set
   */
  private synchronized void linkFound(String link) {
    linkFound = link;
    addLinkFound(link);
  }

  /**
   * Gets the last link found, or a zero-length String if a link has not been found or has been reset.
   *
   * @return The last link found
   */
  public synchronized String getLinkFound() {
    return linkFound;
  }

  /**
   * Adds a last link found to the list of links found.
   *
   * @param link The link to add to the list
   */
  public synchronized void addLinkFound(String link) {
    if (link.endsWith("/") || link.endsWith("\\")) {
      link = link.substring(0, link.length() - 1);
    }
    openedLinks.add(link);
  }

  /**
   * Checks whether or not a link has already been opened.
   *
   * @param s The link to check
   * @return True if the link has already been opened, false if not
   */
  public synchronized boolean hasOpenedLink(String s) {
    for (String link : openedLinks) {
      if (link.endsWith("/") || link.endsWith("\\")) {
        link = link.substring(0, link.length() - 1);
      }
      if (link.equalsIgnoreCase(s)) {
        System.out.println("Already found link.");
        return true;
      }
    }
    return false;
  }

  /**
   * Checks whether or not the program is currently checking anything.
   *
   * @return True if checking anything, false if not
   */
  public boolean isCheckingAnything() {
    return !checks.isEmpty();
  }

  /**
   * Checks whether or not the program is currently checking the PAX website for updates.
   *
   * @return True if checking the PAX website, false if not
   */
  public boolean isCheckingPaxsite() {
    for (Check c : checks) {
      if (CheckPaxsite.class.isInstance(c)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Checks whether or not the program is currently checking the Showclix website for updates.
   *
   * @return True if checking the Showclix website, false if not
   */
  public boolean isCheckingShowclix() {
    for (Check c : checks) {
      if (CheckShowclix.class.isInstance(c)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Checks whether or not the program is currently checking known Showclix pages for updates.
   *
   * @return True if checking known Showclix pages, false if not
   */
  public boolean isCheckingKnownPages() {
    for (Check c : checks) {
      if (CheckShowclixEventPage.class.isInstance(c)) {
        return true;
      }
    }
    return false;
  }
}
