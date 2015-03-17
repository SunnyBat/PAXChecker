package com.github.sunnybat.paxchecker.check;

import java.util.Set;
import java.util.TreeSet;
import com.github.sunnybat.paxchecker.browser.Browser;
import com.github.sunnybat.paxchecker.browser.ShowclixReader;

/**
 *
 * @author SunnyBat
 */
public class DeepCheckShowclix extends CheckShowclix {

  private Set<Integer> alreadyChecked = new TreeSet<>();

  @Override
  public synchronized void init(com.github.sunnybat.paxchecker.gui.Status s, java.util.concurrent.Phaser cB) {
    super.init(s, cB);
    Set<Integer> mySet = ShowclixReader.getAllRelatedIDs();
    for (int i : mySet) {
      alreadyChecked.add(i); // Ignore pages already in Showclix API when starting up
    }
  }

  @Override
  public synchronized void updateLink() {
    Set<Integer> mySet = ShowclixReader.getAllRelatedIDs();
    for (int i : mySet) {
      if (!mySet.contains(i)) {
        System.out.println("Not checked: " + i);
        alreadyChecked.add(i);
        if (ShowclixReader.isPaxPage(i)) {
          System.out.println("Is PAX Page!");
          currentShowclixEventID = i;
          return;
        }
      }
    }
    currentShowclixEventID = ShowclixReader.getLatestEventID(Browser.getExpo()); // QUESTION: What if PAX makes a new event with a lower ID on their Seller page than on their Partner page?
  }

}
