package com.github.sunnybat.paxchecker.browser;

import com.github.sunnybat.paxchecker.DataTracker;
import com.github.sunnybat.paxchecker.Expo;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Set;
import java.util.TreeSet;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 *
 * @author Sunny
 */
public class ShowclixReader {

  private static final String API_LINK_BASE = "http://api.showclix.com/";
  private static final String API_EXTENSION_SELLER = "Seller/";
  private static final String API_EXTENSION_PARTNER = "Partner/"; // Partner IDs -- Prime, East, South = 48 -- Aus = 75
  private static final String API_EXTENSION_VENUE = "Venue/";
  private static final String EVENT_LINK_BASE = "http://www.showclix.com/event/";
  private static final String EVENTS_ATTRIBUTE_LINK = "?follow[]=events";
  private boolean strictFiltering; // TODO: This is state and should be moved to non-static somehow
  private Expo expoToCheck;

  public ShowclixReader(Expo expo) {
    expoToCheck = expo;
  }

  /**
   * Sets the isPaxPage() checks to strictly filter for PAX pages.
   */
  public void strictFilter() { // TODO: Refactor so this is non-static elsewhere
    strictFiltering = true;
  }

  /**
   * Checks whether or not the page associated with the given Showclix EventID is a PAX ticket page.
   *
   * @param showclixID The Showclix ID to check
   * @return True if it is, false if not
   */
  public boolean isPaxPage(int showclixID) {
    return isPaxPage(EVENT_LINK_BASE + showclixID);
  }

  /**
   * Checks whether or not the given URL is a PAX ticket page or a queue page.
   *
   * @param URL The URL to check
   * @return True if it is, false if not
   */
  public boolean isPaxPage(String URL) {
    try {
      HttpURLConnection connect = Browser.setUpConnection(new URL(URL));
      BufferedReader reader = new BufferedReader(new InputStreamReader(connect.getInputStream())); // Throws IOException if 404
      String text = "";
      String line;
      while ((line = reader.readLine()) != null) {
        DataTracker.addDataUsed(line.length());
        text += line.toLowerCase();
      }
      if (text.contains("pax")) {
        System.out.println("Found PAX in page -- is PAX page.");
        return true;
      } else if (text.contains("queue")) {
        if (strictFiltering) {
          System.out.println("Found queue in page, but strict filtering is enabled -- returning false");
          return false;
        } else {
          System.out.println("Found queue in page -- is PAX page.");
          return true;
        }
      } else {
        System.out.println("Is not PAX page.");
        return false;
      }
    } catch (IOException iOException) {
      System.out.println("IOException in ShowclixReader.isPaxPage() -- returning strictFiltering");
      return !strictFiltering;
    }
  }

  /**
   * Gets all relevant event URLs. These will not be sorted in any particular order.
   *
   * @return All relevant event URLs
   */
  public Set<String> getAllEventURLs() {
    Set<String> retSet = getAllSellerEventURLs(expoToCheck);
    retSet.addAll(getAllPartnerEventURLs(expoToCheck));
    retSet.addAll(getAllVenueEventURLs(expoToCheck));
    return retSet;
  }

  private Set<String> getAllPartnerEventURLs(Expo expo) {
    return getAllPartnerEventURLs(getPartnerID(expo));
  }

  private Set<String> getAllSellerEventURLs(Expo expo) {
    return getAllSellerEventURLs(getSellerID(expo));
  }

  private Set<String> getAllVenueEventURLs(Expo expo) {
    return getAllVenueEventURLs(getVenueID(expo));
  }

  private Set<String> getAllEventURLs(JSONObject obj) {
    Set<String> retSet = new TreeSet<>();
    for (String s : (Iterable<String>) obj.keySet()) { // Parse through Event IDs
      if (obj.get(s) instanceof JSONObject || !((String) obj.get(s)).equals("HIDDEN")) {
        retSet.add(EVENT_LINK_BASE + s);
      } else {
        System.out.println("Event " + s + " is currently hidden");
      }
    }
    return retSet;
  }

  /**
   * Gets all Event URLs from the given Seller ID. Note that the page should be JSON-formatted.
   *
   * @param sellerID The Seller ID to read
   * @return The Set of all the Event URLs listed on the given page. This is guaranteed to be non-null.
   */
  private Set<String> getAllSellerEventURLs(int sellerID) {
    Set<String> retSet = new TreeSet<>();
    try {
      String jsonText = parseJSON(new URL(API_LINK_BASE + API_EXTENSION_SELLER + sellerID + EVENTS_ATTRIBUTE_LINK));
      if (jsonText == null) {
        return retSet;
      }
      JSONParser mP = new JSONParser();
      try {
        JSONObject obj = (JSONObject) (JSONObject) mP.parse(jsonText);
        if (obj.containsKey("events")) {
          retSet.addAll(getAllEventURLs((JSONObject) obj.get("events")));
        } else {
          retSet.addAll(getAllEventURLs(obj));
        }
      } catch (ClassCastException cce) {
        System.out.println("ClassCastException from " + mP.parse(jsonText).getClass().getSimpleName() + ": " + mP.parse(jsonText));
      }
    } catch (IOException iOException) {
      System.out.println("ERROR connecting to Seller " + sellerID);
    } catch (ParseException parseException) {
      System.out.println("ERROR parsing JSON text in Seller events!");
      parseException.printStackTrace();
    }
    return retSet;
  }

  private Set<String> getAllPartnerEventURLs(int partnerID) {
    Set<String> retSet = new TreeSet<>();
    try {
      String jsonText = parseJSON(new URL(API_LINK_BASE + API_EXTENSION_PARTNER + partnerID + EVENTS_ATTRIBUTE_LINK));
      if (jsonText == null) {
        return retSet;
      }
      JSONParser mP = new JSONParser();
      try {
        JSONObject obj = (JSONObject) (JSONObject) mP.parse(jsonText);
        if (obj.containsKey("events")) {
          retSet.addAll(getAllEventURLs((JSONObject) obj.get("events")));
        } else {
          retSet.addAll(getAllEventURLs(obj));
        }
      } catch (ClassCastException cce) {
        System.out.println("ClassCastException from " + mP.parse(jsonText).getClass().getName() + ": " + mP.parse(jsonText));
      }
    } catch (IOException iOException) {
      System.out.println("Error connecting to partner " + partnerID);
    } catch (ParseException parseException) {
      System.out.println("ERROR parsing JSON text in Partner events!");
      parseException.printStackTrace();
    }
    return retSet;
  }

  private Set<String> getAllVenueEventURLs(int venueID) {
    Set<String> retSet = new TreeSet<>();
    try {
      String jsonText = parseJSON(new URL(API_LINK_BASE + API_EXTENSION_VENUE + venueID + EVENTS_ATTRIBUTE_LINK));
      if (jsonText == null) {
        return retSet;
      }
      JSONParser mP = new JSONParser();
      try {
        JSONObject obj = (JSONObject) (JSONObject) mP.parse(jsonText);
        if (obj.containsKey("events")) {
          retSet.addAll(getAllEventURLs((JSONObject) obj.get("events")));
        } else {
          retSet.addAll(getAllEventURLs(obj));
        }
      } catch (ClassCastException cce) {
        System.out.println("ClassCastException from " + mP.parse(jsonText).getClass().getSimpleName() + ": " + mP.parse(jsonText));
      }
    } catch (IOException iOException) {
      System.out.println("ERROR connecting to Venue " + venueID);
    } catch (ParseException parseException) {
      System.out.println("ERROR parsing JSON text in Venue events!");
      parseException.printStackTrace();
    }
    return retSet;
  }

  private static int getPartnerID(Expo expo) {
    if (expo == null) {
      return 48;
    }
    switch (expo.toString().toLowerCase()) {
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

  private static int getSellerID(Expo expo) {
    if (expo == null) {
      return 16886;
    }
    switch (expo.toString().toLowerCase()) {
      case "prime":
      case "pax prime":
      case "dev":
      case "pax dev":
        return 16886;
      case "east":
      case "pax east":
        return 17792;
      case "south":
      case "pax south":
        return 19042;
      case "aus":
      case "pax aus":
        return 15374;
      default:
        System.out.println("Unknown expo: " + expo);
        return 16886;
    }
  }

  private static int getVenueID(Expo expo) {
    if (expo == null) {
      return 13961;
    }
    switch (expo.toString().toLowerCase()) {
      case "prime":
      case "pax prime":
      case "dev":
      case "pax dev":
        return 13961;
      case "east":
      case "pax east":
        return 16418;
      case "south":
      case "pax south":
        return 20012;
      case "aus":
      case "pax aus":
        return 15820;
      default:
        System.out.println("Unknown expo: " + expo);
        return 13961;
    }
  }

  /**
   * Reads the JSON from the given URL. Note that this does NOT check whether or not this page contains valid JSON text. This method will also attempt
   * to fix any invalid JSON found. This only fixes known JSON parsing errors.
   *
   * @param url The URL to parse from
   * @return The (fixed) text from the page
   */
  private static String parseJSON(URL url) {
    try {
      HttpURLConnection httpCon = Browser.setUpConnection(url);
      httpCon.setConnectTimeout(500);
      BufferedReader reader = new BufferedReader(new InputStreamReader(httpCon.getInputStream()));
      StringBuilder build = new StringBuilder();
      String line;
      while ((line = reader.readLine()) != null) {
        DataTracker.addDataUsed(line.length());
        // Yea, this is a somewhat hacked-together fix. Oh well, it works!
        // Perhaps I should try and make this dynamic instead of specific fixes.
        line = line.replaceAll(":,", ":\"HIDDEN\","); // Showclix, fix your JSON please. It's invalid.
        line = line.replaceAll(":}", ":\"HIDDEN\"}"); // I'm guessing it's from you guys trying to fix your follows[] code too hastily. Woops.
        build.append(line);
      }
      reader.close();
      return build.toString();
    } catch (IOException iOException) {
      return null;
    }
  }
}
