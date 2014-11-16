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
  public void init(paxchecker.gui.Status s) {
    super.init(s);
    reset();
    s.updateJLabel(linkLabel, getLink());
  }

  @Override
  public boolean ticketsFound() {
    if (currentLinkFound.equals(lastLinkFound)) {
      return false;
    } else if (currentLinkFound == null) {
      return false;
    } else if (currentLinkFound.equals("IOException") || currentLinkFound.equals("NoConnection")) {
      return false;
    } else if (currentLinkFound.equals("NoFind")) {
      return false;
    } else if (!currentLinkFound.contains("\"" + PaxsiteReader.getWebsiteLink(Browser.getExpo()) + "\"")) {
      System.out.println("OMG IT'S UPDATED: " + currentLinkFound);
      return true;
    }
    return false;
  }

  @Override
  public void updateLink() {
    currentLinkFound = PaxsiteReader.getCurrentButtonLink(Browser.getExpo());
  }

  @Override
  public String getLink() {
    return currentLinkFound;
  }

  @Override
  public void reset() {
    lastLinkFound = PaxsiteReader.getCurrentButtonLink(Browser.getExpo());
  }

  @Override
  public void updateGUI(paxchecker.gui.Status s) {
    s.updateJLabel(linkLabel, "Current Website Link: " + getLink());
  }

}
