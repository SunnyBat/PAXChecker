/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package paxchecker.check;

/**
 *
 * @author Sunny
 */
public class CheckTwitter extends Check {

  private String lastTweet;
  private String lastTweetID;

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
    return false;
  }

  @Override
  public synchronized void updateLink() {
  }

  @Override
  public synchronized String getLink() {
    return null;
  }

  @Override
  public synchronized void reset() {
  }

  @Override
  public synchronized void updateGUI(paxchecker.gui.Status s) {
  }

}
