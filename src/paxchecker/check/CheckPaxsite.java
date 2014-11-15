/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package paxchecker.check;

/**
 *
 * @author Sunny
 */
public class CheckPaxsite extends Check {

  private String lastLinkFound;

  public CheckPaxsite(String e) {
    super(e);
    expoC = e;
  }

  @Override
  public void init(paxchecker.gui.Status s) {
    super.init(s);
    s.updateJLabel(linkLabel, "PAXSite");
  }

  @Override
  public boolean ticketsFound() {
    if (lastLinkFound == null) {
      return false;
    } else if (lastLinkFound.equals("IOException") || lastLinkFound.equals("NoConnection")) {
      return false;
    } else if (lastLinkFound.equals("NoFind")) {
      return false;
    } else if (!lastLinkFound.contains("\"" + PaxsiteReader.getWebsiteLink(expoC) + "\"")) {
      System.out.println("OMG IT'S UPDATED: " + lastLinkFound);
      return true;
    }
    return false;
  }

  @Override
  public void updateLink() {
    lastLinkFound = PaxsiteReader.getCurrentButtonLinkLine(expoC);
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
