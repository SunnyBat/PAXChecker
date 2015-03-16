package com.github.sunnybat.paxchecker.browser;

import com.github.sunnybat.commoncode.error.ErrorDisplay;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.ArrayList;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import com.github.sunnybat.paxchecker.DataTracker;

/**
 *
 * @author Sunny
 */
public class ShowclixReader {

  private static final String API_LINK_BASE = "http://api.showclix.com/";
  private static final String API_EXTENSION_SELLER = "Seller/";
  private static final String API_EXTENSION_PARTNER = "Partner/"; // Partner IDs -- Prime, East, South = 48 -- Aus = 75
  private static final String API_EXTENSION_EVENT = "Event/";
  private static final String API_EXTENSION_PRIME_ID = "16886/"; // Also for PAX Dev
  private static final String API_EXTENSION_EAST_ID = "17792/";
  private static final String API_EXTENSION_SOUTH_ID = "19042/";
  private static final String API_EXTENSION_AUS_ID = "15374/";

  /**
   * Gets the complete Showclix Event Page link.
   *
   * @param showclixID The Showclix Event ID
   * @return The complete link
   */
  public static String getLink(int showclixID) {
    return API_LINK_BASE + API_EXTENSION_EVENT + showclixID;
  }

  /**
   * Returns the link to the ShowclixReader API of the given expo. The expo should be the name of the expo (Prime, East) or have PAX in front of it
   * (PAX Prime, PAX East, etc). Any other name (PAX, PAX Invalid, etc) will return the PAX Prime link.
   *
   * @param expo The name of the expo
   * @return The ShowclixReader API link to Seller Events
   */
  public static String getAPISellerEventLink(String expo) {
    return getAPISellerLink(expo) + "events";
  }

  /**
   * Gets the latest ShowclixReader ID for PAX events. Note that this checks PAX Prime, East, South and Aus events. If you want to check a specific
   * event's ShowclixReader ID, see {@link #getLatestSellerEventID(java.lang.String)}.
   *
   * @return The most recent ShowclixReader ID
   */
  public static int getLatestEventID() {
    int maxId;
    maxId = Math.max(getLatestSellerEventID("Prime"), getLatestSellerEventID("East"));
    maxId = Math.max(maxId, getLatestSellerEventID("South"));
    maxId = Math.max(maxId, getLatestSellerEventID("Aus"));
    maxId = Math.max(maxId, getLatestPartnerEventID(48));
    maxId = Math.max(maxId, getLatestPartnerEventID(75));
    return maxId;
  }

  /**
   * Gets the latest Event ID for the given expo. Checks both the seller events and partner events.
   *
   * @param expo The expo to check
   * @return The latest Event ID
   */
  public static int getLatestEventID(String expo) {
    return Math.max(getLatestSellerEventID(expo), getLatestPartnerEventID(getSellerID(expo)));
  }

  /**
   * Gets the latest Event ID from the Seller events for a given PAX expo.
   *
   * @param expo The expo to check
   * @return The most recent ShowclixReader ID
   */
  public static int getLatestSellerEventID(String expo) {
    try {
      return getLatestID(new URL(getAPISellerEventLink(expo)));
    } catch (MalformedURLException ex) {
      return -1;
    }
  }

  /**
   * Gets the latest Event ID from the given Partner events for a given PAX expo.
   *
   * @param partnerID The Seller ID to check
   * @return The most recent ShowclixReader ID
   */
  public static int getLatestPartnerEventID(int partnerID) {
    try {
      return getLatestID(new URL(API_LINK_BASE + API_EXTENSION_PARTNER + partnerID + "/events"));
    } catch (MalformedURLException ex) {
      return -1;
    }
  }

  /**
   * Gets the latest Event ID from the given PAX expo's Seller events.
   *
   * @param expo The expo to check
   * @return The latest Event ID
   */
  public static int getLatestPartnerEventID(String expo) {
    return getLatestPartnerEventID(getSellerID(expo));
  }

  /**
   * Checks whether or not the page associated with the given ShowclixID is a PAX ticket page.
   *
   * @param showclixID The Showclix ID to check
   * @return True if it is, false if not
   */
  public static boolean isPaxPage(int showclixID) {
    try {
      HttpURLConnection connect = Browser.setUpConnection(new URL("http://www.showclix.com/event/" + showclixID));
      BufferedReader reader = new BufferedReader(new InputStreamReader(connect.getInputStream()));
      String text = "";
      String line;
      while ((line = reader.readLine()) != null) {
        DataTracker.addDataUsed(line.length());
        text += line.toLowerCase();
      }
      if (text.contains(Browser.getExpo().toLowerCase())) {
        System.out.println("Expo found on page.");
        return true; // This blocks the data from the BufferedReader from being fully added to the total data
      }
    } catch (IOException iOException) {
    }
    return false;
  }

  private static int getLatestID(URL url) {
    try {
      HttpURLConnection httpCon = Browser.setUpConnection(url);
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
      int maxID = 0;
      for (String s : (Iterable<String>) obj.keySet()) {
        try {
          maxID = Math.max(maxID, Integer.parseInt((String) s));
        } catch (NumberFormatException nfe) {
          System.out.println("Error parsing ID number from String: " + s);
        }
      }
      return maxID;
    } catch (java.net.SocketTimeoutException ste) { // Return maxId for this?
      System.out.println("Unable to complete information download -- connection timed out");
    } catch (IOException | ParseException e) {
      e.printStackTrace();
    }
    return -1;
  }

  private static String getAPISellerLink(String expo) {
    String link = API_LINK_BASE + API_EXTENSION_SELLER;
    if (expo == null) {
      return link += API_EXTENSION_PRIME_ID;
    }
    switch (expo.toLowerCase()) {
      case "prime":
      case "pax prime":
      case "dev":
      case "pax dev":
        link += API_EXTENSION_PRIME_ID;
        break;
      case "east":
      case "pax east":
        link += API_EXTENSION_EAST_ID;
        break;
      case "south":
      case "pax south":
        link += API_EXTENSION_SOUTH_ID;
        break;
      case "aus":
      case "pax aus":
        link += API_EXTENSION_AUS_ID;
        break;
      default:
        System.out.println("Unknown expo: " + expo);
        link += API_EXTENSION_PRIME_ID;
        break;
    }
    return link;
  }

  private static int getSellerID(String expo) {
    if (expo == null) {
      return 48;
    }
    switch (expo.toLowerCase()) {
      case "prime":
      case "pax prime":
      case "dev":
      case "pax dev":
      case "east":
      case "pax east":
      case "south":
      case "pax south":
        return 48;
      case "aus":
      case "pax aus":
        return 75;
      default:
        System.out.println("Unknown expo: " + expo);
        return 48;
    }
  }

  // Need to use http://api.showclix.com/Partner/48/sellers?follow[]=events instead.
  public static List<Integer> getAllRelatedIDs() {
    List<Integer> myList = new ArrayList<>();
    int maxPartnerID = 99;
    for (int i = 0; i <= maxPartnerID; i++) {
      try {
        HttpURLConnection httpCon = Browser.setUpConnection(new URL(API_LINK_BASE + API_EXTENSION_PARTNER + i + "/sellers"));
        httpCon.connect();
        if (i == maxPartnerID) {
          if (httpCon.getResponseCode() < 300) { // Is 2XX request (page found, or a variation of it)
            maxPartnerID++;
            System.out.println("Max PartnerID increased by 1!");
            ErrorDisplay.showErrorWindow("DEBUG: Partner Found", "This is not an error. This is to let you know that a new Partner has been found. "
                + "You may close this window at any time.", null);
            i--;
          }
        } else {
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
          for (String s : (Iterable<String>) obj.keySet()) {
            try {
              int sellerID = Integer.parseInt(s);
              // TODO: Verify seller info, get event info if found
            } catch (NumberFormatException nfe) {
              System.out.println("Error parsing ID number from String: " + s);
            }
          }
        }
      } catch (IOException | ParseException e) {
        e.printStackTrace();
      }
    }
    return myList;
  }
}
