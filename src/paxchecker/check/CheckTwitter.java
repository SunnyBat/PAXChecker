/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package paxchecker.check;

import paxchecker.browser.TwitterReader;

/**
 *
 * @author Sunny
 * @deprecated
 */
public class CheckTwitter extends Check {

  private long lastTweetID = -1;
  private long currentTweetID = -1;
  private String linkFound = "[Twitter Not Updated]";
  private TwitterReader twitter;
  private final String twitterHandle;

  public CheckTwitter() {
    super();
    twitterHandle = "@Official_PAX";
  }

  public CheckTwitter(String handle) {
    super();
    twitterHandle = handle;
  }

  @Override
  public synchronized void init(paxchecker.gui.Status s, java.util.concurrent.Phaser cB) {
    twitter = new TwitterReader(twitterHandle);
    super.init(s, cB);
    s.updateJLabel(linkLabel, "Twitter");
  }

  @Override
  public synchronized boolean ticketsFound() {
    return lastTweetID != currentTweetID;
  }

  @Override
  public synchronized void updateLink() {
    currentTweetID = twitter.getLatestTweetID();
    if (currentTweetID != lastTweetID) {
      linkFound = twitter.getLinkFromTweet(currentTweetID);
    }
  }

  @Override
  public synchronized String getLink() {
    return linkFound;
  }

  @Override
  public synchronized void reset() {
    lastTweetID = twitter.getLatestTweetID();
    linkFound = "[Twitter Not Updated]";
  }

  @Override
  public synchronized void updateGUI(paxchecker.gui.Status s) {
    s.updateJLabel(linkLabel, "Current Tweet ID: " + currentTweetID);
  }
}
