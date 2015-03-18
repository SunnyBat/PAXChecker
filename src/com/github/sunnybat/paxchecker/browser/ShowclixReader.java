package com.github.sunnybat.paxchecker.browser;

import com.github.sunnybat.commoncode.error.ErrorDisplay;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import com.github.sunnybat.paxchecker.DataTracker;
import com.github.sunnybat.paxchecker.PAXChecker;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Phaser;

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
  private static ExecutorService threadPool = Executors.newFixedThreadPool(5); // TODO: Make this only initialize when Deep Showclix Checking is enabled
  private static int maxPartnerID = 100;

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
  @Deprecated
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
  @Deprecated
  public static int getLatestEventID(String expo) {
    return Math.max(getLatestSellerEventID(expo), getLatestPartnerEventID(getSellerID(expo)));
  }

  /**
   * Gets the latest Event ID from the Seller events for a given PAX expo.
   *
   * @param expo The expo to check
   * @return The most recent ShowclixReader ID
   */
  @Deprecated
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
  @Deprecated
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
  @Deprecated
  public static int getLatestPartnerEventID(String expo) {
    return getLatestPartnerEventID(getSellerID(expo));
  }

  /**
   * Gets all Event URLs from the given URL. Note that the page should be JSON-formatted.
   *
   * @param sellerID The Seller ID to read
   * @return The Set of all the Event URLs listed on the given page. This is guaranteed to be non-null.
   */
  public static Set<String> getAllSellerEventURLs(int sellerID) {
    Set<String> retList = new TreeSet<>();
    try {
      final HttpURLConnection httpCon = Browser.setUpConnection(new URL(API_LINK_BASE + API_EXTENSION_SELLER + sellerID + "/events"));
      httpCon.setConnectTimeout(500);
      try {
        BufferedReader reader = new BufferedReader(new InputStreamReader(httpCon.getInputStream()));
        String jsonText = "";
        String line;
        while ((line = reader.readLine()) != null) {
          DataTracker.addDataUsed(line.length());
          jsonText += line;
        }
        reader.close();
        //System.out.println("JSON Text: " + jsonText);
        JSONParser mP = new JSONParser();
        //JSONArray array = (JSONArray) mP.parse(jsonText);
        try {
          JSONObject obj = (JSONObject) mP.parse(jsonText);
          System.out.println(obj);
          for (String s : (Iterable<String>) obj.keySet()) { // Parse through Event IDs
            try {
              JSONObject obj2 = ((JSONObject) obj.get(s)); // Will throw CCE if it's not a JSONObject
              if (obj2.get("listing_url") == null) {
                System.out.println("Listing URL is null!");
              } else {
                System.out.println("URL Found: " + obj2.get("listing_url"));
                addToSet(retList, (String) obj2.get("listing_url"));
              }
            } catch (ClassCastException cce) {
              System.out.println("CCE: " + s);
            }
          }
        } catch (ClassCastException cce) {
          cce.printStackTrace();
          System.out.println("ClassCastException from " + mP.parse(jsonText).getClass().getSimpleName() + ": " + mP.parse(jsonText));
        }
      } catch (IOException iOException) {
      } catch (ParseException parseException) {
      }
    } catch (IOException iOException) {
      System.out.println("ERROR connecting to Seller " + sellerID);
    }
    return retList;
  }

  public static Set<String> getAllPartnerEventURLs(int partnerID) {
    Set<String> retSet = new TreeSet<>();
    try {
      HttpURLConnection httpCon = Browser.setUpConnection(new URL(API_LINK_BASE + API_EXTENSION_PARTNER + partnerID + "/sellers?follow[]=events"));
      httpCon.setConnectTimeout(500);
      BufferedReader reader = new BufferedReader(new InputStreamReader(httpCon.getInputStream()));
      StringBuilder build = new StringBuilder();
      String line;
      while ((line = reader.readLine()) != null) {
        DataTracker.addDataUsed(line.length());
        build.append(line);
      }
      reader.close();
      String jsonText = build.toString();
      //System.out.println("JSON Text: " + jsonText);
      JSONParser mP = new JSONParser();
      //JSONArray array = (JSONArray) mP.parse(jsonText);
      try {
        JSONObject obj = (JSONObject) mP.parse(jsonText);
        for (String s : (Iterable<String>) obj.keySet()) { // Parse through Seller IDs
          try {
            JSONObject obj2 = ((JSONObject) obj.get(s)); // Will throw CCE if it's not a JSONObject
            if (obj2.get("organization") == null) {
              System.out.println("Null.");
            } else if (((String) obj2.get("organization")).toLowerCase().contains("pax")) {
              System.out.println("PAX Seller: " + obj2.get("organization"));
              JSONObject events = (JSONObject) obj2.get("events"); // Will throw CCE if it's no a JSONObject
              System.out.println(events);
              for (String s2 : (Iterable<String>) events.keySet()) {
                //System.out.println("KEY: " + s2);
                if ((String) ((JSONObject) events.get(s2)).get("listing_url") != null) {
                  addToSet(retSet, (String) ((JSONObject) events.get(s2)).get("listing_url")); // SO MANY CLASS CASTS
                } else {
                  System.out.println("NULL, resorting to Listing ID");
                  addToSet(retSet, "http://www.showclix.com/Event/" + s2);
                }
              }
            }
          } catch (ClassCastException e) {
            e.printStackTrace();
          }
        }
      } catch (ClassCastException cce) {
        System.out.println("ClassCastException from " + mP.parse(jsonText).getClass().getName() + ": " + mP.parse(jsonText));
      }
    } catch (IOException iOException) {
      System.out.println("Error connecting to partner " + partnerID);
    } catch (ParseException parseException) {
    }
    return retSet;
  }

  /**
   * Checks whether or not the page associated with the given Showclix EventID is a PAX ticket page.
   *
   * @param showclixID The Showclix ID to check
   * @return True if it is, false if not
   */
  public static boolean isPaxPage(int showclixID) {
    return isPaxPage("http://www.showclix.com/Event/" + showclixID);
  }

  /**
   * Checks whether or not the given URL is a PAX ticket page.
   *
   * @param URL The URL to check
   * @return True if it is, false if not
   */
  public static boolean isPaxPage(String URL) {
    try {
      HttpURLConnection connect = Browser.setUpConnection(new URL(URL));
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

  @Deprecated
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
    } catch (java.net.SocketTimeoutException ste) {
      System.out.println("Unable to complete information download -- connection timed out (URL: " + url + ")");
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

  /**
   * A currently inefficiently-coded method to check all Partners and Sellers for new PAX events.
   *
   * @return A Set of all Showclix IDs to check
   */
  public static Set<String> getAllRelatedIDs() {
    final Phaser threadWait = new Phaser();
    threadWait.bulkRegister(maxPartnerID); // Includes registering this Thread
    final Set<String> myList = new TreeSet<>();
    for (int partnerID = 1; partnerID <= maxPartnerID; partnerID++) {
      if (partnerID == maxPartnerID) {
        try {
          HttpURLConnection httpCon = Browser.setUpConnection(new URL(API_LINK_BASE + API_EXTENSION_PARTNER + partnerID + "/sellers?follow[]=events"));
          httpCon.setConnectTimeout(500);
          httpCon.connect();
          if (httpCon.getResponseCode() < 300) { // Is 2XX request (page found, or a variation of it)
            maxPartnerID++;
            partnerID--;
            threadWait.register();
            if (!PAXChecker.isCommandLine()) {
              ErrorDisplay.showErrorWindow("DEBUG: Partner Found", "This is not an error. This is to let you know that a new Partner has been found. "
                  + "You may close this window at any time.", null);
            } else {
              System.out.println("DEBUG: Partner Found -- This is not an error. This is to let you know that a new Partner has been found.");
            }
          }
        } catch (IOException e) {
        }
      } else {
        final int finalPID = partnerID;
        Runnable r = new Runnable() {
          @Override
          public void run() {
            System.out.println("Checking Partner ID " + finalPID);
            myList.addAll(getAllPartnerEventURLs(finalPID));
            threadWait.arriveAndDeregister();
          }
        };
        threadPool.submit(r);
      }
    }
    threadWait.awaitAdvance(threadWait.arriveAndDeregister());
    System.out.println("LIST!");
    return myList;
  }

  private static final Object OBJ = new Object();

  private static void addToSet(Set<String> mySet, String add) {
    synchronized (OBJ) {
      if (add.endsWith("/")) {
        add = add.substring(0, add.length() - 1);
      }
      mySet.add(add);
    }
  }
}
