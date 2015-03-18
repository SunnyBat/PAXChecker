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

  private Set<String> alreadyChecked = new TreeSet<>();
  private String currentLink;

  @Override
  public synchronized void init(com.github.sunnybat.paxchecker.gui.Status s, java.util.concurrent.Phaser cB) {
    super.init(s, cB);
    Set<String> mySet = ShowclixReader.getAllRelatedIDs();
    for (String i : mySet) {
      alreadyChecked.add(i); // Ignore pages already in Showclix API when starting up
      currentLink = i; // Set to last one added
    }
  }

  @Override
  public synchronized void updateLink() {
    Set<String> mySet = ShowclixReader.getAllRelatedIDs();
    for (String i : mySet) {
      if (!mySet.contains(i)) {
        System.out.println("Not checked: " + i);
        alreadyChecked.add(i);
        if (ShowclixReader.isPaxPage(i)) {
          System.out.println("Is PAX Page!");
          currentLink = i;
          return;
        }
      }
    }
  }

  @Override
  public String getLink() {
    return currentLink;
  }

}
