package paxchecker.check;

import java.util.*;

/**
 *
 * @author Sunny
 */
public final class TicketChecker {

  private static final ArrayList<Check> checks = new ArrayList<>();
  private static paxchecker.gui.Status status;
  private static String linkFound;

  public static void init(paxchecker.gui.Status s) {
    status = s;
  }

  public static void addChecker(Check c) {
    checks.add(c);
    c.init(status); // MOVE LATER
  }

  public static boolean isUpdated() {
    for (Check c : checks) {
      c.updateLink();
      c.updateGUI(status);
      if (c.ticketsFound()) {
        System.out.println("FOUND LINK: " + c.getLink());
        setLinkFound(c.getLink());
        c.reset();
        return true;
      } else {
        System.out.println("Link: " + c.getLink());
      }
    }
    return false;
  }

  private static void setLinkFound(String link) {
    linkFound = link;
  }

  public static String getLinkFound() {
    return linkFound;
  }

  public static void resetLinkFound() {
    linkFound = "";
  }
}
