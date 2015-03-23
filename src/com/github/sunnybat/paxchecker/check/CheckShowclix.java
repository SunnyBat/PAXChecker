package com.github.sunnybat.paxchecker.check;

import com.github.sunnybat.paxchecker.browser.ShowclixReader;
import com.github.sunnybat.paxchecker.browser.Browser;
import java.util.Set;
import java.util.TreeSet;
// Could replace Browser with a class variable, since all this is using is getExpo(), and then be able to have multiple instances of this running
// But then if we change the expo later on...

/**
 *
 * @author Sunny
 */
public class CheckShowclix extends Check {

  static final String BASE_SHOWCLIX_LINK = "http://www.showclix.com/event/";
  Set<String> alreadyChecked = new TreeSet<>();
  String currentLink;

  public CheckShowclix() {
    super();
  }

  @Override
  public synchronized void init(com.github.sunnybat.paxchecker.gui.Status s, java.util.concurrent.Phaser cB) {
    super.init(s, cB);
    updateLabel(s, "Showclix initialized.");
  }

  @Override
  public synchronized boolean ticketsFound() {
    if (currentLink == null) { // NEVER return true on null. That's how NPEs happen in TicketChecker!
      return false;
    }
    return !alreadyChecked.contains(currentLink);
  }

  @Override
  public synchronized void updateLink() {
    Set<String> mySet = ShowclixReader.getAllEventURLs(Browser.getExpo());
    for (String i : mySet) {
      if (!mySet.contains(i)) {
        System.out.println("Not checked: " + i);
        if (ShowclixReader.isPaxPage(i)) {
          System.out.println("Is PAX Page!");
          currentLink = i;
          return;
        }
      }
    }
  }

  @Override
  public synchronized String getLink() {
    if (currentLink == null) {
      return "[No API Connection]";
    }
    return currentLink;
  }

  @Override
  public synchronized void updateGUI(com.github.sunnybat.paxchecker.gui.Status s) {
    updateLabel(s, "Current Showclix Link: " + getLink());
  }

  @Override
  public synchronized void reset() {
    if (currentLink == null) {
      Set<String> mySet = ShowclixReader.getAllEventURLs(Browser.getExpo());
      for (String i : mySet) {
        alreadyChecked.add(i);
        currentLink = i;
      }
    } else {
      alreadyChecked.add(currentLink);
    }
  }

}
