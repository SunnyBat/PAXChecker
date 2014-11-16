package paxchecker.check;

import paxchecker.browser.ShowclixReader;
import paxchecker.browser.Browser;

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
    reset();
    s.updateJLabel(linkLabel, getLink());
  }

  @Override
  public boolean ticketsFound() {
    return currentShowclixEventID > lastShowclixEventID;
  }

  @Override
  public void updateLink() {
    currentShowclixEventID = ShowclixReader.getLatestEventID(Browser.getExpo()); // QUESTION: What if PAX makes a new event with a lower ID on their Seller page than on their Partner page?
  }

  @Override
  public String getLink() {
    return getLink(currentShowclixEventID);
  }

  @Override
  public void updateGUI(paxchecker.gui.Status s) {
    s.updateJLabel(linkLabel, "Current Showclix Link: " + getLink());
  }

  @Override
  public void reset() {
    lastShowclixEventID = ShowclixReader.getLatestEventID(Browser.getExpo());
  }

  private static String getLink(int showclixID) {
    return BASE_SHOWCLIX_LINK + showclixID;
  }

}
