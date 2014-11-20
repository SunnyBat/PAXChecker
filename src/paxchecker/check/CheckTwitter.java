/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package paxchecker.check;

import java.util.List;
import paxchecker.browser.TwitterReader;
import twitter4j.Paging;
import twitter4j.Status;

/**
 *
 * @author Sunny
 */
public class CheckTwitter extends Check {

  private long lastTweetID = -1;
  private long currentTweetID = -1;
  private String linkFound = "[Twitter Not Updated]";

  public CheckTwitter() {
    super();
  }

  @Override
  public synchronized void init(paxchecker.gui.Status s, java.util.concurrent.Phaser cB) {
    super.init(s, cB);
    s.updateJLabel(linkLabel, "Twitter");
  }

  @Override
  public synchronized boolean ticketsFound() {
    return lastTweetID != currentTweetID;
  }

  @Override
  public synchronized void updateLink() {
    currentTweetID = TwitterReader.getLatestTweetID();
    if (currentTweetID != lastTweetID) {
      linkFound = TwitterReader.getLinkFromTweet(currentTweetID);
    }
  }

  @Override
  public synchronized String getLink() {
    return linkFound;
  }

  @Override
  public synchronized void reset() {
    lastTweetID = TwitterReader.getLatestTweetID();
    linkFound = "[Twitter Not Updated]";
  }

  @Override
  public synchronized void updateGUI(paxchecker.gui.Status s) {
    s.updateJLabel(linkLabel, "Current Tweet ID: " + currentTweetID);
  }
}
