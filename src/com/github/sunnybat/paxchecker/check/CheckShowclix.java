package com.github.sunnybat.paxchecker.check;

import com.github.sunnybat.paxchecker.Expo;
import com.github.sunnybat.paxchecker.browser.Browser;
import com.github.sunnybat.paxchecker.browser.ShowclixReader;
import com.github.sunnybat.paxchecker.status.CheckerInfoOutput;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 * @author Sunny
 */
public class CheckShowclix extends Check {

  private Set<String> alreadyChecked = new TreeSet<>();
  private String currentLink; // When new link found, this will not be null. This will be the final link to check, AKA the final redirect link
  private String originalLink; // The original link found to add to alreadyChecked when finished
  private Expo expoToCheck;

  /**
   * Creates a new CheckShowclix.
   *
   * @param expo The expo to check
   */
  public CheckShowclix(Expo expo) {
    super();
    expoToCheck = expo;
  }

  @Override
  public synchronized void init(CheckerInfoOutput s, java.util.concurrent.Phaser cB) {
    super.init(s, cB);
    updateWithInfo("Showclix initialized.");
  }

  @Override
  public synchronized boolean ticketsFound() {
    if (currentLink == null) { // NEVER return true on null. That's how NPEs happen in TicketChecker!
      return false;
    }
    return !alreadyChecked.contains(currentLink);
  }

  @Override
  public synchronized void updateLink() {
    updateLink("[Checking]");
    Set<String> mySet = getLinks();
    updateLinkFromSet(mySet);
    updateLink(getLink());
  }

  protected Set<String> getLinks() {
    return ShowclixReader.getAllEventURLs(expoToCheck.toString()); // TODO: Replace with Expo object
  }

  private void updateLinkFromSet(Set<String> mySet) {
    for (String i : mySet) {
      if (!alreadyChecked.contains(i)) {
        System.out.println("Not checked: " + i);
        if (ShowclixReader.isPaxPage(i)) {
          originalLink = i;
          currentLink = Browser.unshortenURL(i);
          System.out.println("PAX page found! OL = " + originalLink + " :: CL = " + currentLink);
          break;
        } else {
          System.out.println("Link is not pax page. Ignoring.");
          alreadyChecked.add(i);
        }
      }
    }
  }

  @Override
  public synchronized String getLink() {
    if (currentLink == null) {
      return "[No API Connection]";
    }
    return currentLink;
  }

  @Override
  public synchronized void reset() {
    if (currentLink == null) {
      Set<String> mySet = getLinks();
      for (String i : mySet) {
        alreadyChecked.add(i);
        currentLink = i;
      }
    } else {
      System.out.println("Adding " + originalLink + " to alreadyChecked");
      alreadyChecked.add(originalLink);
    }
  }

}
