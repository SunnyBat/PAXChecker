package paxchecker.check;

import paxchecker.Browser;

/**
 *
 * @author Sunny
 */
public class CheckShowclix extends Check {

  private int lastShowclixEventID = -1;
  private int currentShowclixEventID = -1;
  private static final String BASE_SHOWCLIX_LINK = "http://www.showclix.com/event/";

  public CheckShowclix() {
    super();
  }

  @Override
  public void init(paxchecker.gui.Status s) {
    super.init(s);
    lastShowclixEventID = ShowclixReader.getLatestSellerEventID(Browser.getExpo());
    s.updateJLabel(linkLabel, "Showclix");
  }

  @Override
  public boolean ticketsFound() {
    return currentShowclixEventID > lastShowclixEventID;
  }

  @Override
  public void updateLink() {
    currentShowclixEventID = ShowclixReader.getLatestSellerEventID(Browser.getExpo());
  }

  @Override
  public String getLink() {
    return getLink(currentShowclixEventID);
  }

  @Override
  public void updateGUI(paxchecker.gui.Status s) {
    s.updateJLabel(linkLabel, "Current Showclix Link: " + getLink());
  }

  private static String getLink(int showclixID) {
    return BASE_SHOWCLIX_LINK + showclixID;
  }

}
