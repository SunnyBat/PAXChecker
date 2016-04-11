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
  private String currentLink; // When new link found, this will not be null
  private ShowclixReader showReader;

  /**
   * Creates a new CheckShowclix.
   *
   * @param expo The expo to check
   * @param shouldFilterShowclix True to use strict Showclix filtering, false to not
   */
  public CheckShowclix(Expo expo, boolean shouldFilterShowclix) {
    super();
    showReader = new ShowclixReader(expo);
    if (shouldFilterShowclix) {
      showReader.strictFilter();
    }
  }

  @Override
  public synchronized void init(CheckerInfoOutput s, java.util.concurrent.Phaser cB) {
    super.init(s, cB);
    updateWithInfo("Showclix initialized.");
  }

  @Override
  public synchronized boolean ticketsFound() {
    return currentLink != null; // currentLink is not null if a new link has been found
  }

  @Override
  public synchronized void updateLink() {
    updateLink("[Checking]");
    Set<String> allLinksFound = getLinks();
    if (alreadyChecked.isEmpty()) { // In case there was no API connection before, we don't want to alert for ALL the events found
      alreadyChecked.addAll(allLinksFound);
    } else {
      updateLinkFromSet(allLinksFound);
    }
    updateLink(getLink());
  }

  protected Set<String> getLinks() {
    return showReader.getAllEventURLs();
  }

  private void updateLinkFromSet(Set<String> allLinksFound) {
    for (String link : allLinksFound) {
      if (!alreadyChecked.contains(link)) {
        link = Browser.unshortenURL(link);
        if (!alreadyChecked.contains(link)) {
          System.out.println("Not checked: " + link);
          if (showReader.isPaxPage(link)) {
            currentLink = link;
            System.out.println("PAX page found: " + currentLink);
            break;
          } else {
            System.out.println("Link is not pax page. Ignoring.");
            alreadyChecked.add(link);
          }
        }
      }
    }
  }

  @Override
  public synchronized String getLink() {
    if (currentLink == null) {
      return "[No New Events]";
    }
    return currentLink;
  }

  @Override
  public synchronized void reset() {
    if (currentLink != null) {
      System.out.println("Adding " + currentLink + " to alreadyChecked");
      alreadyChecked.add(currentLink);
      currentLink = null;
    }
  }

}