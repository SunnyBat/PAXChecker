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
  private String currentLink; // Null until the API returns events found
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
    return currentLink != null && !currentLink.startsWith("["); // currentLink is not null if a new link has been found
  }

  @Override
  public synchronized void updateLink() {
    updateLink("[Checking]");
    Set<String> allLinksFound = getLinks();
    if (allLinksFound == null) {
      updateLink("[Error Connecting]");
      return;
    }
    if (currentLink == null) { // In case there was no API connection before, we don't want to alert for ALL the events found
      System.out.println("currentLink is null, adding all links");
      alreadyChecked.addAll(allLinksFound);
      currentLink = "[No New Events]";
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
        String tempURL = Browser.unshortenURL(link);
        if (tempURL != null) { // Followed redirects, we're happy
          if (!tempURL.equals(link)) { // Different URL found
            alreadyChecked.add(link); // So we don't check this URL again (every time)
            link = tempURL;
          }
        } else { // Didn't follow redirects, need to use Showclix API to get event URL
          String temp2 = showReader.getNamedURL(link);
          if (temp2 == null) {
            System.out.println("CS: Unable to update URL " + link);
          } else if (Browser.unshortenURL(temp2) == null) { // 404'd again
            System.out.println("CS: URL " + link + " and updated URL " + temp2 + " unable to resolve -- checking again later");
            continue;
          } else if (!temp2.equalsIgnoreCase(link)) {
            alreadyChecked.add(link); // So we don't check this URL again (every time)
            link = temp2;
          }
        }
        if (!alreadyChecked.contains(link)) {
          currentLink = link;
          System.out.println("CS: PAX page found: " + currentLink);
          break;
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
      System.out.println("CS: Adding " + currentLink + " to alreadyChecked");
      alreadyChecked.add(currentLink);
      currentLink = "[No New Events]";
    }
  }

}
