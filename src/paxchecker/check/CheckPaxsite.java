/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package paxchecker.check;

import paxchecker.browser.PaxsiteReader;
import paxchecker.browser.Browser;

/**
 *
 * @author Sunny
 */
public class CheckPaxsite extends Check {

  private String lastLinkFound;
  private String currentLinkFound;

  public CheckPaxsite() {
    super();
  }

  @Override
  public synchronized void init(paxchecker.gui.Status s, java.util.concurrent.Phaser cB) {
    super.init(s, cB);
    updateLabel(s, "Initializing Paxsite...");
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
    } else if (!currentLinkFound.contains("\"" + PaxsiteReader.getWebsiteLink(Browser.getExpo()) + "\"")) {
      System.out.println("OMG IT'S UPDATED: " + currentLinkFound);
      return true;
    }
    return false;
  }

  @Override
  public synchronized final void updateLink() {
    currentLinkFound = PaxsiteReader.getCurrentButtonLink(Browser.getExpo());
  }

  @Override
  public synchronized String getLink() {
    return currentLinkFound;
  }

  @Override
  public synchronized void updateGUI(paxchecker.gui.Status s) {
    updateLabel(s, "Current Website Link: " + getLink());
  }

  @Override
  public synchronized void reset() {
    lastLinkFound = PaxsiteReader.getCurrentButtonLink(Browser.getExpo());
  }

}
