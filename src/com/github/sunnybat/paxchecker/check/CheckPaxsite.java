/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.sunnybat.paxchecker.check;

import com.github.sunnybat.paxchecker.browser.PaxsiteReader;
import com.github.sunnybat.paxchecker.browser.Browser;

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
  public synchronized void init(com.github.sunnybat.paxchecker.gui.Status s, java.util.concurrent.Phaser cB) {
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
  public synchronized void updateGUI(com.github.sunnybat.paxchecker.gui.Status s) {
    updateLabel(s, "Current Website Link: " + getLink());
  }

  @Override
  public synchronized void reset() {
    lastLinkFound = PaxsiteReader.getCurrentButtonLink(Browser.getExpo());
  }

}
