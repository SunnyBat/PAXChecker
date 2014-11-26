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
  public synchronized void init(paxchecker.gui.Status s, java.util.concurrent.Phaser cB) {
    super.init(s, cB);
    updateLabel(s, "Showclix");
  }

  @Override
  public synchronized boolean ticketsFound() {
    return currentShowclixEventID > lastShowclixEventID;
  }

  @Override
  public synchronized void updateLink() {
    currentShowclixEventID = ShowclixReader.getLatestEventID(Browser.getExpo()); // QUESTION: What if PAX makes a new event with a lower ID on their Seller page than on their Partner page?
  }

  @Override
  public synchronized String getLink() {
    return getLink(currentShowclixEventID);
  }

  @Override
  public synchronized void updateGUI(paxchecker.gui.Status s) {
    updateLabel(s, "Current Showclix Link: " + getLink());
  }

  @Override
  public synchronized void reset() {
    lastShowclixEventID = ShowclixReader.getLatestEventID(Browser.getExpo());
  }

  private static String getLink(int showclixID) {
    return BASE_SHOWCLIX_LINK + showclixID;
  }

}
