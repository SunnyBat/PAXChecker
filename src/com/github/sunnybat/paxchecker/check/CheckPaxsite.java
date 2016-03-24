package com.github.sunnybat.paxchecker.check;

import com.github.sunnybat.paxchecker.Expo;
import com.github.sunnybat.paxchecker.browser.PaxsiteReader;
import com.github.sunnybat.paxchecker.status.CheckerInfoOutput;

/**
 *
 * @author Sunny
 */
public class CheckPaxsite extends Check {

  private String lastLinkFound;
  private String currentLinkFound;
  private Expo expoToCheck;
  private PaxsiteReader siteReader;

  /**
   * Creates a new PaxsiteChecker.
   *
   * @param expo The expo to check
   */
  public CheckPaxsite(Expo expo) {
    super();
    expoToCheck = expo;
    siteReader = new PaxsiteReader(expoToCheck);
  }

  @Override
  public synchronized void init(CheckerInfoOutput s, java.util.concurrent.Phaser cB) {
    super.init(s, cB);
    updateWithInfo("Paxsite initialized.");
  }

  @Override
  public synchronized boolean ticketsFound() {
    if (currentLinkFound.equals(lastLinkFound)) {
      return false;
    } else if (currentLinkFound == null) {
      return false;
    } else if (currentLinkFound.equals("[IOException]") || currentLinkFound.equals("[NoConnection]")) {
      return false;
    } else if (currentLinkFound.equals("[NoFind]") || currentLinkFound.equals("[Button Parsing Error]")) {
      return false;
    } else if (!currentLinkFound.toLowerCase().contains("\"" + siteReader.getWebsiteLink(expoToCheck.toString()) + "\"")) {
      System.out.println("OMG IT'S UPDATED: " + currentLinkFound);
      return true;
    }
    return false;
  }

  @Override
  public synchronized final void updateLink() {
    updateLink("[Checking]");
    currentLinkFound = siteReader.getCurrentButtonLink();
    updateLink(getLink());
  }

  @Override
  public synchronized String getLink() {
    return currentLinkFound;
  }

  @Override
  public synchronized void reset() {
    lastLinkFound = siteReader.getCurrentButtonLink();
  }

}
