package com.github.sunnybat.paxchecker.check;

import com.github.sunnybat.paxchecker.browser.PaxsiteReader;

/**
 *
 * @author Sunny
 */
public class CheckPaxsite extends Check {

  private String lastLinkFound;
  private String currentLinkFound;
  private final String expoToCheck;

  /**
   * Creates a new PaxsiteChecker.
   *
   * @param expo The expo to check
   */
  public CheckPaxsite(String expo) {
    super();
    expoToCheck = expo;
  }

  @Override
  public synchronized void init(com.github.sunnybat.paxchecker.status.StatusGUI s, java.util.concurrent.Phaser cB) {
    super.init(s, cB);
    updateLabel(s, "Paxsite initialized.");
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
    } else if (!currentLinkFound.contains("\"" + PaxsiteReader.getWebsiteLink(expoToCheck) + "\"")) {
      System.out.println("OMG IT'S UPDATED: " + currentLinkFound);
      return true;
    }
    return false;
  }

  @Override
  public synchronized final void updateLink() {
    updateLink("[Checking]");
    currentLinkFound = PaxsiteReader.getCurrentButtonLink(expoToCheck);
    updateLink(getLink());
  }

  @Override
  public synchronized String getLink() {
    return currentLinkFound;
  }

  @Override
  public synchronized void reset() {
    lastLinkFound = PaxsiteReader.getCurrentButtonLink(expoToCheck);
  }

}
