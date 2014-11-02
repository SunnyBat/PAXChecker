/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package paxchecker;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 *
 * @author Sunny
 */
public class Showclix {

  private static boolean checkShowclix;
  private static int lastShowclixEventID = 3852445;
  private static final String SHOWCLIX_API_LINK_PRIME = "http://api.showclix.com/Seller/16886/events"; // Also for PAX Dev
  private static final String SHOWCLIX_API_LINK_EAST = "http://api.showclix.com/Seller/17792/events";
  private static final String SHOWCLIX_API_LINK_SOUTH = "http://api.showclix.com/Seller/19042/events";
  private static final String SHOWCLIX_API_LINK_AUS = "http://api.showclix.com/Seller/15374/events";

  /**
   * Sets whether or not to check the Showclix website for updates.
   *
   * @param check True to check, false to not
   */
  public static void setCheckShowclix(boolean check) {
    checkShowclix = check;
  }

  /**
   * Checks whether or not the program is checking the Showclix website. If true, the program uses the Showclix API (api.showclix.com) to check for
   * new events.
   *
   * @return True if it is checking the Showclix website, false if not
   */
  public static boolean isCheckingShowclix() {
    return checkShowclix;
  }

  /**
   * Checks whether or not the Showclix website has a new event. This uses the Showclix API to get the latest Event ID
   * (https://www.showclix.com/Event/######) from the API. This uses the {@link #getLatestShowclixID(java.lang.String)} method to get the most recent
   * ID.
   *
   * @return True if there is a new Showclix event, false if not
   */
  public static boolean isShowclixUpdated() {
    if (!isCheckingShowclix()) {
      return false;
    }
    int currEvent = getLatestShowclixID(Browser.getExpo());
    if (currEvent == -1) {
      Checker.setStatusShowclixLink("Unable to connect to the Showclix website!");
      return false;
    }
    String eventUrl = "https://showclix.com/event/" + currEvent;
    Checker.setStatusShowclixLink(eventUrl);
    return currEvent != lastShowclixEventID;
  }

  /**
   * Gets the latest Showclix ID for PAX events. Note that this checks PAX Prime, East, South and Aus events. If you want to check a specific event's
   * Showclix ID, see {@link #getLatestShowclixID(java.lang.String)}.
   *
   * @return The most recent Showclix ID
   */
  public static int getLatestShowclixID() {
    int maxId;
    maxId = Math.max(getLatestShowclixID("Prime"), getLatestShowclixID("East"));
    maxId = Math.max(maxId, getLatestShowclixID("South"));
    maxId = Math.max(maxId, getLatestShowclixID("Aus"));
    return maxId;
  }

  /**
   * Gets the latest Showclix ID for a specific PAX event. This uses the Showclix API to find all of the events listed under a specific seller. In
   * this case, each PAX event (Prime, East, etc) has a separate Showclix seller account associated with it. This uses the fact that every new
   * Showclix event has a larger ID than before, and simply returns the largest Event ID for the specific expo given.
   *
   * @param expo The expo to check
   * @return The most recent Showclix ID
   */
  public static int getLatestShowclixID(String expo) {
    try {
      URL url = new URL(getShowclixAPILink(expo));
      HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
      httpCon.addRequestProperty("User-Agent", "Mozilla/4.0");
      BufferedReader reader = new BufferedReader(new InputStreamReader(httpCon.getInputStream()));
      String jsonText = "";
      String line;
      while ((line = reader.readLine()) != null) {
        DataTracker.addDataUsed(line.length());
        jsonText += line;
      }
      reader.close();
      JSONParser mP = new JSONParser();
      JSONObject obj = (JSONObject) mP.parse(jsonText);
      int maxId = 0;
      for (String s : (Iterable<String>) obj.keySet()) {
        maxId = Math.max(maxId, Integer.parseInt((String) s));
      }
      return maxId;
    } catch (Exception e) {
      e.printStackTrace();
      return -1;
    }
  }

  /**
   * Checks the link given against the most recent Showclix link. This uses the currently-set expo link. Note that if this returns false, it opens up
   * an ErrorHandler window with a NOTE-style warning, and opens the most recent Showclix link in the browser.
   *
   * @param link The link to check
   * @return True if it matches, false if not
   */
  public static boolean checkShowclixLink(String link) {
    String showLink = getShowclixLink();
    if (!showLink.equals(link)) {
      ErrorHandler.showErrorWindow("NOTE", "NOTE: The last found Showclix link has changed! New link:\n" + showLink + "\nThe link will be opened in case the queue has been found.", null);
      Browser.openLinkInBrowser(showLink);
      return false;
    }
    return true;
  }

  /**
   * Returns the link of the most recent Showclix event. Note that this gets the most recent from the current expo.
   *
   * @return The link of the most recent Showclix event
   */
  public static String getShowclixLink() {
    try {
      return "https://showclix.com/event/" + getLatestShowclixID(Browser.getExpo());
    } catch (Exception e) {
      ErrorHandler.showErrorWindow("ERORR checking the Showclix website for updates!", e);
      return null;
    }
  }

  /**
   * Sets the Showclix ID to check against. Note that this makes the program check to see if the showclix ID found is DIFFERENT than the one it has,
   * not if it's bigger. If the ID is -1, it will not be set.
   *
   * @param ID The Showclix Event ID to set
   */
  public static void setShowclixID(int ID) {
    if (ID == -1) {
      System.out.println("Unable to set most recent Showclix Event ID -- invalid ShowclixID!");
      return;
    }
    lastShowclixEventID = ID;
    System.out.println("ShowclixID set to " + ID);
  }

  /**
   * Returns the link to the Showclix API of the given expo. The expo should be the name of the expo (Prime, East) or have PAX in front of it (PAX
   * Prime, PAX East, etc). Any other name (PAX, PAX Invalid, etc) will return the PAX Prime link.
   *
   * @param expo The name of the expo
   * @return The Showclix API link to Seller Events
   */
  public static String getShowclixAPILink(String expo) {
    if (expo == null) {
      return SHOWCLIX_API_LINK_PRIME;
    }
    switch (expo.toLowerCase()) {
      case "prime":
      case "pax prime":
        return SHOWCLIX_API_LINK_PRIME;
      case "east":
      case "pax east":
        return SHOWCLIX_API_LINK_EAST;
      case "south":
      case "pax south":
        return SHOWCLIX_API_LINK_SOUTH;
      case "aus":
      case "pax aus":
        return SHOWCLIX_API_LINK_AUS;
      case "dev":
      case "pax dev":
        return SHOWCLIX_API_LINK_PRIME;
      default:
        return SHOWCLIX_API_LINK_PRIME;
    }
  }

}
