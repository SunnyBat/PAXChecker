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

  public CheckTwitter(String e) {
    super(e);
    expoC = e;
  }

  @Override
  public void init(paxchecker.gui.Status s) {
    super.init(s);
    s.updateJLabel(linkLabel, "Twitter");
  }

  @Override
  public boolean ticketsFound() {
    return false;
  }

  @Override
  public void updateLink() {
  }

  @Override
  public String getLink() {
    return null;
  }

  @Override
  public void updateGUI(paxchecker.gui.Status s) {
    s.setTwitterLink(getLink());
  }

}
