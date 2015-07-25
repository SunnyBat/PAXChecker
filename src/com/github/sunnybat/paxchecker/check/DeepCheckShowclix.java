package com.github.sunnybat.paxchecker.check;

import com.github.sunnybat.paxchecker.browser.ShowclixReader;
import java.util.Set;

/**
 *
 * @author SunnyBat
 * @deprecated Inefficient check method
 */
public class DeepCheckShowclix extends CheckShowclix { // Such a complex class. =/

  @Override
  protected Set<String> getLinks() {
    return ShowclixReader.getAllRelevantURLs();
  }

}
