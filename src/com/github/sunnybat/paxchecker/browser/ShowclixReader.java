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
      if (listing.get("listing_url") != null) {
        if (listing.get("listing_url") instanceof String) {
          System.out.println("SR: Final URL: " + listing.get("listing_url"));
          return (String) listing.get("listing_url");
        } else {
          System.out.println("SR: listing_url !instanceOf String");
        }
      } else if (listing.get("short_name") != null) {
        if (listing.get("short_name") instanceof String) {
          System.out.println("SR: Final URL: " + EVENT_LINK_BASE + listing.get("short_name"));
          return EVENT_LINK_BASE + listing.get("short_name");
        } else {
          System.out.println("SR: short_name !instanceOf String");
        }
      } else {
        System.out.println("SR: Unknown URL from JSON " + json);
      }
    } catch (NumberFormatException nfe) {
      System.out.println("SR: Unable to parse number from event URL");
    } catch (MalformedURLException | ClassCastException e) {
      e.printStackTrace();
    } catch (ParseException | NullPointerException e) {
      System.out.println("SR: Unable to parse JSON (" + url + ")");
      e.printStackTrace();
    }
    return url;
  }

  /**
   * Gets all relevant event URLs. These will not be sorted in any particular order.
   *
   * @return All relevant event URLs
   */
  public Set<String> getAllEventURLs() {
    Set<String> retSet = new TreeSet<>();
    Set<String> sellerEvents = getAllSellerEventURLs(expoToCheck);
    if (sellerEvents == null) {
      System.out.println("SR: Error downloading Seller Event URLs");
      return null;
    }
    retSet.addAll(sellerEvents);
    Set<String> partnerEvents = getAllPartnerEventURLs(expoToCheck);
    if (partnerEvents == null) {
      System.out.println("SR: Error downloading Partner Event URLs");
      return null;
    }
    retSet.addAll(partnerEvents);
    Set<String> venueEvents = getAllVenueEventURLs(expoToCheck);
    if (venueEvents == null) {
      System.out.println("SR: Error downloading Partner Event URLs");
      return null;
    }
    retSet.addAll(venueEvents);
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
    for (String eventID : (Iterable<String>) obj.keySet()) { // Parse through Event IDs
      try {
        if (obj.get(eventID) instanceof JSONObject) {
          JSONObject jObj = (JSONObject) obj.get(eventID);
          if (jObj.containsKey("event")) {
            if (jObj.get("event") == null) {
              System.out.println("SR: Event " + eventID + " is null, ignoring");
            } else {
              String eventName = jObj.get("event").toString().toLowerCase();
              if (eventName.contains("pax")) {
                System.out.println("SR: PAX event found: " + eventID + " (" + jObj.get("event") + ")");
                if (!strictFiltering || eventName.contains(expoToCheck.toString().toLowerCase())) {
                  retSet.add(EVENT_LINK_BASE + eventID);
                } else {
                  System.out.println("SR: Strict Filtering is on, ignoring");
                }
              }
            } // else event is not PAX, ignoring
          } else {
            System.out.println("SR: Event " + eventID + " does not contain an event title -- ignoring");
          }
        } else if (((String) obj.get(eventID)).equals("HIDDEN")) {
          System.out.println("SR: Event " + eventID + " is currently hidden");
        } else {
          System.out.println("SR: Unknown event: " + obj);
        }
      } catch (Exception e) {
        e.printStackTrace();
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
      if (jsonText == null) {
        return null;
      }
      return parseEvents(jsonText);
    } catch (IOException iOException) {
      System.out.println("SR: ERROR connecting to Seller " + sellerID);
    }
    return null;
  }

  private Set<String> getAllPartnerEventURLs(int partnerID) {
    try {
      String jsonText = readJSONFromURL(new URL(API_LINK_BASE + API_EXTENSION_PARTNER + partnerID + EVENTS_ATTRIBUTE_LINK));
      if (jsonText == null) {
        return null;
      }
      return parseEvents(jsonText);
    } catch (IOException iOException) {
      System.out.println("SR: Error connecting to Partner " + partnerID);
    }
    return null;
  }

  private Set<String> getAllVenueEventURLs(int venueID) {
    try {
      String jsonText = readJSONFromURL(new URL(API_LINK_BASE + API_EXTENSION_VENUE + venueID + EVENTS_ATTRIBUTE_LINK));
      if (jsonText == null) {
        return null;
      }
      return parseEvents(jsonText);
    } catch (IOException iOException) {
      System.out.println("SR: ERROR connecting to Venue " + venueID);
    }
    return null;
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
      System.out.println("SR: Error parsing JSON: " + jsonText);
    }
    return retSet;
  }

  private static int getPartnerID(Expo expo) {
    if (expo == null) {
      return 48;
    }
    switch (expo) {
      case WEST:
      case EAST:
      case SOUTH:
        return 48;
      case AUS:
        return 75;
      default:
        System.out.println("SR: Unknown expo: " + expo);
        return 48;
    }
  }

  private static int getSellerID(Expo expo) {
    if (expo == null) {
      return 16886;
    }
    switch (expo) {
      case WEST:
        return 16886;
      case EAST:
        return 17792;
      case SOUTH:
        return 19042;
      case AUS:
        return 15374;
      default:
        System.out.println("SR: Unknown expo: " + expo);
        return 16886;
    }
  }

  private static int getVenueID(Expo expo) {
    if (expo == null) {
      return 13961;
    }
    switch (expo) {
      case WEST:
        return 13961;
      case EAST:
        return 16418;
      case SOUTH:
        return 20012;
      case AUS:
        return 15820;
      default:
        System.out.println("SR: Unknown expo: " + expo);
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
      iOException.printStackTrace();
      return null;
    }
  }
}
