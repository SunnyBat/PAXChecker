package com.github.sunnybat.paxchecker.check;

import com.github.sunnybat.paxchecker.browser.Browser;
import java.io.IOException;
import java.net.*;
import java.util.*;

/**
 *
 * @author SunnyBat
 */
public class CheckShowclixEventPage extends Check {

  private static final String EVENT_BASE_URL = "http://www.showclix.com/event/";
  private List<String> eventCheckList = new ArrayList<>();
  private String validPageID = null;

  public CheckShowclixEventPage() {
    eventCheckList.add("3925916"); // "Prime 2015"
    eventCheckList.add("3926134"); // "Prime 2015 BYOC"
    //eventCheckList.add("3926157"); // "Dev 2015"
  }

  @Override
  public synchronized void init(com.github.sunnybat.paxchecker.gui.Status s, java.util.concurrent.Phaser cB) {
    super.init(s, cB);
    updateLabel(s, "Event Checking initialized.");
  }

  @Override
  public synchronized boolean ticketsFound() {
    return validPageID != null;
  }

  @Override
  public synchronized final void updateLink() {
    for (String s : eventCheckList) {
      HttpURLConnection conn = null;
      try {
        conn = Browser.setUpConnection(new URL(EVENT_BASE_URL + s));
        if (conn == null) { // In case it fails to set up correctly
          continue;
        }
        conn.getInputStream(); // Will throw IOException if 404 -- simplest way to force it
        System.out.println(conn.getURL());
        validPageID = s;
        return; // Found valid page, stop checking
      } catch (IOException ioe) { // getInputStream() threw exception -- 404 or unable to connect
        if (conn != null) {
          System.out.println("Link redirected to: " + conn.getURL());
        } else {
          System.out.println("Unable to find link from " + s);
        }
      }
    }
  }

  @Override
  public synchronized String getLink() {
    if (validPageID == null) {
      return "[None Found]";
    } else {
      return EVENT_BASE_URL + validPageID;
    }
  }

  @Override
  public synchronized void updateGUI(com.github.sunnybat.paxchecker.gui.Status s) {
    updateLabel(s, "Known Pages Found: " + getLink());
  }

  @Override
  public synchronized void reset() {
    if (validPageID != null) {
      eventCheckList.remove(validPageID);
      validPageID = null;
    }
  }

}
