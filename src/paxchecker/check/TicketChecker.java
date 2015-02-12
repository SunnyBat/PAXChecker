package paxchecker.check;

import java.util.*;
import java.util.concurrent.*;

/**
 *
 * @author Sunny
 */
public final class TicketChecker {

  private static final ArrayList<Check> checks = new ArrayList<>();
  private static final ArrayList<String> openedLinks = new ArrayList<>();
  private static paxchecker.gui.Status status;
  private static String linkFound = "";
  private static ExecutorService threadPool;
  private static Phaser threadWait;

  /**
   * Initializes the TicketChecker class.
   *
   * @param s The Status to update, or null if none
   */
  public static void init(paxchecker.gui.Status s) {
    if (threadPool != null) {
      throw new IllegalStateException("Program has already been initialized.");
    }
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
  public static void addChecker(Check c) {
    c.init(status, threadWait); // MOVE LATER
    checks.add(c);
  }

  /**
   * Checks whether or not the registered Checkers have an update. This method may block for several seconds.
   *
   * @return True if an update is found, false if not
   */
  public static boolean isUpdated() {
    for (Check c : checks) {
      threadPool.submit(c);
    }
    System.out.println("Waiting: TC");
    threadWait.arriveAndAwaitAdvance();
    System.out.println("Finished waiting");
    for (Check c : checks) {
      //c.updateLink();
      if (status != null) {
        c.updateGUI(status);
      }
      if (c.ticketsFound()) {
        if (!hasOpenedLink(c.getLink())) {
          System.out.println("FOUND LINK: " + c.getLink());
          setLinkFound(c.getLink());
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
    return false;
  }

  /**
   * Sets the link found.
   *
   * @param link The link to set
   */
  private static synchronized void setLinkFound(String link) {
    linkFound = link;
  }

  /**
   * Gets the last link found, or a zero-length String if a link has not been found or has been reset.
   *
   * @return The last link found
   */
  public static synchronized String getLinkFound() {
    return linkFound;
  }

  /**
   * Resets the last link found. This automatically adds the last link found to the list.
   */
  public static synchronized void resetLinkFound() {
    addLinkFound(linkFound);
    linkFound = "";
  }

  /**
   * Adds a last link found to the list of links found.
   *
   * @param link The link to add to the list
   */
  public static synchronized void addLinkFound(String link) {
    openedLinks.add(link);
  }

  /**
   * Checks whether or not a link has already been opened.
   *
   * @param s The link to check
   * @return True if the link has already been opened, false if not
   */
  public static synchronized boolean hasOpenedLink(String s) {
    for (String t : openedLinks) {
      if (t.equalsIgnoreCase(s)) {
        System.out.println("Already found link.");
        return true;
      }
    }
    return false;
  }

  /**
   * Checks whether or not the program is currently checking the PAX website for updates.
   *
   * @return True if checking the PAX website, false if not
   */
  public static boolean isCheckingPaxsite() {
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
  public static boolean isCheckingShowclix() {
    for (Check c : checks) {
      if (CheckShowclix.class.isInstance(c)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Checks whether or not the program is currently checking the Twitter website for updates.
   *
   * @return True if checking Twitter, false if not
   */
  public static boolean isCheckingTwitter() {
//    for (Check c : checks) {
//      if (CheckTwitter.class.isInstance(c)) {
//        return true;
//      }
//    }
//    return false;
    return TwitterStreamer.isStreamingTwitter();
  }
}
