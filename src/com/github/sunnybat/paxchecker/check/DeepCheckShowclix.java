package com.github.sunnybat.paxchecker.check;

import java.util.Set;
import com.github.sunnybat.paxchecker.browser.ShowclixReader;

/**
 *
 * @author SunnyBat
 */
public class DeepCheckShowclix extends CheckShowclix {

  @Override
  public synchronized void init(com.github.sunnybat.paxchecker.gui.Status s, java.util.concurrent.Phaser cB) {
    super.init(s, cB);
  }

  @Override
  public synchronized void updateLink() {
    Set<String> mySet = ShowclixReader.getAllRelevantURLs();
    updateLinkFromSet(mySet);
  }

  @Override
  public synchronized void reset() {
    if (currentLink == null) {
      Set<String> mySet = ShowclixReader.getAllRelevantURLs();
      for (String url : mySet) {
        alreadyChecked.add(url); // Ignore pages already in Showclix API when starting up
        currentLink = url; // Set to last one added
      }
    } else {
      alreadyChecked.add(currentLink);
    }
  }

}
