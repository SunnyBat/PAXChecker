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

  private static final String EVENT_BASE_URL = "http://www.showclix.com/Event/";
  private List<String> eventCheckList = new ArrayList<>();
  private String validPage = null;

  public CheckShowclixEventPage() {
    eventCheckList.add("3925916");
  }

  @Override
  public synchronized void init(com.github.sunnybat.paxchecker.gui.Status s, java.util.concurrent.Phaser cB) {
    super.init(s, cB);
    updateLabel(s, "Event Checking initialized.");
  }

  @Override
  public synchronized boolean ticketsFound() {
    return validPage != null;
  }

  @Override
  public synchronized final void updateLink() {
    for (String s : eventCheckList) {
      try {
        HttpURLConnection conn = Browser.setUpConnection(new URL(EVENT_BASE_URL + s));
        if (conn == null) { // In case it fails to set up correctly
          continue;
        }
        conn.getInputStream(); // Will throw IOException if 404 -- simplest way to force it
        validPage = s;
        return; // Found valid page, stop checking
      } catch (IOException ioe) {
        System.out.println("IOException");
        ioe.printStackTrace();
      }
    }
  }

  @Override
  public synchronized String getLink() {
    if (validPage == null) {
      return "[None]";
    } else {
      return EVENT_BASE_URL + validPage;
    }
  }

  @Override
  public synchronized void updateGUI(com.github.sunnybat.paxchecker.gui.Status s) {
    updateLabel(s, "Known Pages Found: " + getLink());
  }

  @Override
  public synchronized void reset() {
    if (validPage != null) {
      eventCheckList.remove(validPage);
      validPage = null;
    }
  }

}
