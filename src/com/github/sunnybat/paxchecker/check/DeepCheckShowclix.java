package com.github.sunnybat.paxchecker.check;

import java.util.Set;
import com.github.sunnybat.paxchecker.browser.ShowclixReader;

/**
 *
 * @author SunnyBat
 */
public class DeepCheckShowclix extends CheckShowclix { // Such a complex class. =/

  @Override
  protected Set<String> getLinks() {
    return ShowclixReader.getAllRelevantURLs();
  }

}
