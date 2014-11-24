package paxchecker.check;

import java.util.*;
import java.util.concurrent.*;
import paxchecker.browser.TwitterReader;

/**
 *
 * @author Sunny
 */
public final class TicketChecker {

  private static final ArrayList<Check> checks = new ArrayList<>();
  private static final ArrayList<String> openedLinks = new ArrayList<>();
  private static paxchecker.gui.Status status;
  private static String linkFound;
  private static ExecutorService threadPool;
  private static Phaser threadWait;

  private TicketChecker() {
  }

  public static void init(paxchecker.gui.Status s) {
    status = s;
    threadPool = Executors.newCachedThreadPool();
    threadWait = new Phaser();
    threadWait.register();
  }

  public static void addChecker(Check c) {
    c.init(status, threadWait); // MOVE LATER
    checks.add(c);
  }

  public static void preRun() {
    TwitterReader.runTwitterStream();
  }

  public static boolean isUpdated() {
    for (Check c : checks) {
      threadPool.submit(c);
    }
    System.out.println("Waiting: TC");
    threadWait.arriveAndAwaitAdvance();
    System.out.println("Finished waiting");
    for (Check c : checks) {
      //c.updateLink();
      c.updateGUI(status);
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

  private static synchronized void setLinkFound(String link) {
    linkFound = link;
  }

  public static synchronized String getLinkFound() {
    return linkFound;
  }

  public static synchronized void resetLinkFound() {
    addLinkFound(linkFound);
    linkFound = "";
  }

  public static synchronized void addLinkFound(String link) {
    openedLinks.add(link);
  }

  public static synchronized boolean hasOpenedLink(String s) {
    for (String t : openedLinks) {
      if (t.equalsIgnoreCase(s)) {
        System.out.println("Already found link.");
        return true;
      }
    }
    return false;
  }

  public static boolean isCheckingPaxsite() {
    for (Check c : checks) {
      if (CheckPaxsite.class.isInstance(c)) {
        return true;
      }
    }
    return false;
  }

  public static boolean isCheckingShowclix() {
    for (Check c : checks) {
      if (CheckShowclix.class.isInstance(c)) {
        return true;
      }
    }
    return false;
  }

  public static boolean isCheckingTwitter() {
    for (Check c : checks) {
      if (CheckTwitter.class.isInstance(c)) {
        return true;
      }
    }
    return false;
  }
}
