/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package paxchecker.check;

import paxchecker.Browser;

/**
 *
 * @author Sunny
 */
public class CheckPaxsite extends Check {

  private String lastLinkFound;
  private boolean hasFoundLink;

  public CheckPaxsite() {
    super();
  }

  @Override
  public void init(paxchecker.gui.Status s) {
    super.init(s);
    s.updateJLabel(linkLabel, "PAXSite");
  }

  @Override
  public boolean ticketsFound() {
    if (hasFoundLink) {
      return false;
    } else if (lastLinkFound == null) {
      return false;
    } else if (lastLinkFound.equals("IOException") || lastLinkFound.equals("NoConnection")) {
      return false;
    } else if (lastLinkFound.equals("NoFind")) {
      return false;
    } else if (!lastLinkFound.contains("\"" + PaxsiteReader.getWebsiteLink(Browser.getExpo()) + "\"")) {
      System.out.println("OMG IT'S UPDATED: " + lastLinkFound);
      hasFoundLink = true;
      return true;
    }
    return false;
  }

  @Override
  public void updateLink() {
    lastLinkFound = PaxsiteReader.parseHRef(PaxsiteReader.getCurrentButtonLinkLine(Browser.getExpo()));
  }

  @Override
  public String getLink() {
    return lastLinkFound;
  }

  @Override
  public void updateGUI(paxchecker.gui.Status s) {
    s.updateJLabel(linkLabel, "Current Website Link: " + getLink());
  }

}
