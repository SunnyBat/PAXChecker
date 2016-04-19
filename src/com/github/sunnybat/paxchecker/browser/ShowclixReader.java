package com.github.sunnybat.paxchecker.browser;

import com.github.sunnybat.paxchecker.DataTracker;
import com.github.sunnybat.paxchecker.Expo;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
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
  private static final String API_EXTENSION_PARTNER = "Partner/";
  private static final String API_EXTENSION_VENUE = "Venue/";
  private static final String API_EXTENSION_EVENT = "Event/";
  private static final String EVENT_LINK_BASE = "http://www.showclix.com/event/";
  private static final String EVENTS_ATTRIBUTE_LINK = "?follow[]=events";
  private boolean strictFiltering;
  private Expo expoToCheck;

  public ShowclixReader(Expo expo) {
    expoToCheck = expo;
  }

  /**
   * Sets the isPaxPage() checks to strictly filter for PAX pages.
   */
  public void strictFilter() {
    strictFiltering = true;
  }

  /**
   * Gets the best URL to use for the event. The returned URL should be used instead of the URL passed in.
   *
   * @param url The Showclix URL to finalize
   * @return The final URL to check, or null if url is null
   */
  public String getNamedURL(String url) {
    if (url == null) {
      return null;
    }
    if (!url.contains(EVENT_LINK_BASE)) {
      return url;
    }
    int id;
    try {
      id = Integer.parseInt(url.substring(EVENT_LINK_BASE.length())); // Throws NFE if not number
      String json = readJSONFromURL(new URL(API_LINK_BASE + API_EXTENSION_EVENT + id));
      JSONParser mP = new JSONParser();
      JSONObject listing = (JSONObject) mP.parse(json);
      if (listing.containsKey("listing_url")) {
        if (listing.get("listing_url") instanceof String) {
          System.out.println("Final URL: " + listing.get("listing_url"));
          return (String) listing.get("listing_url");
        } else {
          System.out.println("listing_url !instanceOf String");
        }
      } else if (listing.containsKey("short_name")) {
        if (listing.get("short_name") instanceof String) {
          System.out.println("Final URL: " + EVENT_LINK_BASE + listing.get("short_name"));
          return EVENT_LINK_BASE + listing.get("short_name");
        } else {
          System.out.println("short_name !instanceOf String");
        }
      } else {
        System.out.println("Unknown URL from JSON " + json);
      }
    } catch (NumberFormatException nfe) {
      System.out.println("Unable to parse number from event URL");
    } catch (MalformedURLException | ClassCastException e) {
      e.printStackTrace();
    } catch (ParseException | NullPointerException e) {
      System.out.println("Unable to parse JSON (" + url + ")");
      e.printStackTrace();
    }
    return url;
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
   * Checks whether or not the given URL is a PAX ticket page or a queue page. Note that this does NOT follow any redirects.
   *
   * @param link The URL to check
   * @return True if it is, false if not
   */
  public boolean isPaxPage(String link) { // CHECK: Move this (and strict filtering) to somewhere else?
    try {
      URLConnection connect = new URL(link).openConnection();
      BufferedReader reader = new BufferedReader(new InputStreamReader(connect.getInputStream()));
      String line;
      while ((line = reader.readLine().toLowerCase()) != null) {
        DataTracker.addDataUsed(line.length());
        if (line.contains("pax")) {
          System.out.println("Found PAX in page -- is PAX page.");
          return true;
        } else if (line.contains("queue")) {
          if (strictFiltering) {
            System.out.println("Found queue in page, but strict filtering is enabled");
          } else {
            System.out.println("Found queue in page -- is PAX page.");
            return true;
          }
        }
      }
    } catch (IOException iOException) {
      System.out.println("IOException in ShowclixReader.isPaxPage() -- returning strictFiltering");
      return !strictFiltering;
    }
    System.out.println("Is not PAX page.");
    return false;
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
    try {
      String jsonText = readJSONFromURL(new URL(API_LINK_BASE + API_EXTENSION_SELLER + sellerID + EVENTS_ATTRIBUTE_LINK));
      return parseEvents(jsonText);
    } catch (IOException iOException) {
      System.out.println("ERROR connecting to Seller " + sellerID);
    }
    return new TreeSet<>();
  }

  private Set<String> getAllPartnerEventURLs(int partnerID) {
    try {
      String jsonText = readJSONFromURL(new URL(API_LINK_BASE + API_EXTENSION_PARTNER + partnerID + EVENTS_ATTRIBUTE_LINK));
      return parseEvents(jsonText);
    } catch (IOException iOException) {
      System.out.println("Error connecting to Partner " + partnerID);
    }
    return new TreeSet<>();
  }

  private Set<String> getAllVenueEventURLs(int venueID) {
    try {
      String jsonText = readJSONFromURL(new URL(API_LINK_BASE + API_EXTENSION_VENUE + venueID + EVENTS_ATTRIBUTE_LINK));
      return parseEvents(jsonText);
    } catch (IOException iOException) {
      System.out.println("ERROR connecting to Venue " + venueID);
    }
    return new TreeSet<>();
  }

  private Set<String> parseEvents(String jsonText) {
    Set<String> retSet = new TreeSet<>();
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
      cce.printStackTrace();
    } catch (ParseException pe) {
      System.out.println("Error parsing JSON: " + jsonText);
    }
    return retSet;
  }

  private static int getPartnerID(Expo expo) {
    if (expo == null) {
      return 48;
    }
    switch (expo) {
      case PRIME:
      case EAST:
      case SOUTH:
        return 48;
      case AUS:
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
    switch (expo) {
      case PRIME:
        return 16886;
      case EAST:
        return 17792;
      case SOUTH:
        return 19042;
      case AUS:
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
    switch (expo) {
      case PRIME:
        return 13961;
      case EAST:
        return 16418;
      case SOUTH:
        return 20012;
      case AUS:
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
  private static String readJSONFromURL(URL url) {
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
