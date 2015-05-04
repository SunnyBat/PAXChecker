package com.github.sunnybat.paxchecker.browser;

import com.github.sunnybat.paxchecker.DataTracker;
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
  private static ExecutorService threadPool = Executors.newFixedThreadPool(5); // TODO: Make this only initialize when Deep Showclix Checking is enabled
  private static int maxPartnerID = 100;

  /**
   * Checks whether or not the page associated with the given Showclix EventID is a PAX ticket page.
   *
   * @param showclixID The Showclix ID to check
   * @return True if it is, false if not
   */
  public static boolean isPaxPage(int showclixID) {
    return isPaxPage(EVENT_LINK_BASE + showclixID);
  }

  /**
   * Checks whether or not the given URL is a PAX ticket page or a queue page.
   *
   * @param URL The URL to check
   * @return True if it is, false if not
   */
  public static boolean isPaxPage(String URL) {
    try {
      HttpURLConnection connect = Browser.setUpConnection(new URL(URL));
      BufferedReader reader = new BufferedReader(new InputStreamReader(connect.getInputStream())); // Throws IOException if 404
      String text = "";
      String line;
      while ((line = reader.readLine()) != null) {
        DataTracker.addDataUsed(line.length());
        text += line.toLowerCase();
      }
      if (text.contains("pax") || text.contains("queue")) {
        System.out.println("PAX page found!");
        return true;
      } else {
        return false;
      }
    } catch (IOException iOException) {
      System.out.println("IOException in ShowclixReader.isPaxPage() -- returning true");
      return true; // Not sure if it does or not, so open just in case
    }
  }

  public static Set<String> getAllEventURLs(String expo) {
    Set<String> retSet = getAllSellerEventURLs(expo);
    retSet.addAll(getAllPartnerEventURLs(expo));
    retSet.addAll(getAllVenueEventURLs(expo));
    System.out.println(retSet);
    return retSet;
  }

  public static Set<String> getAllRelevantURLs() {
    Set<Integer> sellerIDs = getAllRelevantSellerIDs();
    sellerIDs.add(getSellerID("Prime"));
    sellerIDs.add(getSellerID("East"));
    sellerIDs.add(getSellerID("South"));
    sellerIDs.add(getSellerID("Aus"));
    Set<Integer> partnerIDs = getAllPartners(sellerIDs);
    partnerIDs.add(getPartnerID("Prime"));
    partnerIDs.add(getPartnerID("East"));
    partnerIDs.add(getPartnerID("South"));
    partnerIDs.add(getPartnerID("Aus"));
    final Set<String> retSet = new TreeSet<>();
    System.out.println("Seller IDs: " + sellerIDs);
    System.out.println("Partner IDs: " + partnerIDs);
    for (int partnerID : partnerIDs) {
      retSet.addAll(getAllPartnerEventURLs(partnerID));
    }
    for (int sellerID : sellerIDs) {
      retSet.addAll(getAllSellerEventURLs(sellerID));
    }
    int[] venues = {13961, 16418, 20012, 15820};
    for (int venueID : venues) {
      retSet.addAll(getAllVenueEventURLs(venueID));
    }
    return retSet;
  }

  private static Set<String> getAllPartnerEventURLs(String expo) {
    return getAllPartnerEventURLs(getPartnerID(expo));
  }

  private static Set<String> getAllSellerEventURLs(String expo) {
    return getAllSellerEventURLs(getSellerID(expo));
  }

  private static Set<String> getAllVenueEventURLs(String expo) {
    return getAllVenueEventURLs(getVenueID(expo));
  }

  private static Set<String> getAllEventURLs(JSONObject obj) {
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
  private static Set<String> getAllSellerEventURLs(int sellerID) {
    Set<String> retSet = new TreeSet<>();
    try {
      String jsonText = parseJSON(new URL(API_LINK_BASE + API_EXTENSION_SELLER + sellerID + EVENTS_ATTRIBUTE_LINK));
      if (jsonText == null) {
        return retSet;
      }
      //System.out.println("JSON Text: " + jsonText);
      JSONParser mP = new JSONParser();
      //JSONArray array = (JSONArray) mP.parse(jsonText);
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

  private static Set<String> getAllPartnerEventURLs(int partnerID) {
    Set<String> retSet = new TreeSet<>();
    try {
      String jsonText = parseJSON(new URL(API_LINK_BASE + API_EXTENSION_PARTNER + partnerID + EVENTS_ATTRIBUTE_LINK));
      if (jsonText == null) {
        return retSet;
      }
      //System.out.println("JSON Text: " + jsonText);
      JSONParser mP = new JSONParser();
      //JSONArray array = (JSONArray) mP.parse(jsonText);
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

  private static Set<String> getAllVenueEventURLs(int venueID) {
    Set<String> retSet = new TreeSet<>();
    try {
      String jsonText = parseJSON(new URL(API_LINK_BASE + API_EXTENSION_VENUE + venueID + EVENTS_ATTRIBUTE_LINK));
      if (jsonText == null) {
        return retSet;
      }
      //System.out.println("JSON Text: " + jsonText);
      JSONParser mP = new JSONParser();
      //JSONArray array = (JSONArray) mP.parse(jsonText);
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
      System.out.println("ERROR connecting to Seller " + venueID);
    } catch (ParseException parseException) {
      System.out.println("ERROR parsing JSON text in Venue events!");
      parseException.printStackTrace();
    }
    return retSet;
  }

  private static Set<Integer> getAllPartners(Set<Integer> sellerIDs) {
    final Set<Integer> retSet = new TreeSet<>();
    for (int i : sellerIDs) {
      try {
        String jsonText = parseJSON(new URL(API_LINK_BASE + API_EXTENSION_SELLER + i + "/partner"));
        if (jsonText == null) {
          return retSet;
        }
        JSONParser mP = new JSONParser();
        JSONObject obj = (JSONObject) mP.parse(jsonText);
        if (obj.get("partner_id") != null) {
          try {
            retSet.add(Integer.parseInt((String) obj.get("partner_id")));
          } catch (NumberFormatException nfe) {
            System.out.println("Error parsing number: " + obj.get("partner_id"));
          }
        }
      } catch (MalformedURLException mue) {
      } catch (ParseException pe) {
      }
    }
    return retSet;
  }

  private static Set<Integer> getAllRelevantSellerIDs() {
    final Phaser threadWait = new Phaser();
    threadWait.bulkRegister(maxPartnerID); // Includes registering this Thread
    final Set<Integer> relevantSellerIDs = new TreeSet<>();
    final Object LOCK = new Object();
    for (int pID = 1; pID < 100; pID++) {
      final int partnerID = pID;
      Runnable r = new Runnable() {
        @Override
        public void run() {
          Set<Integer> mySet = getRelevantSellerIDs(partnerID);
          synchronized (LOCK) {
            relevantSellerIDs.addAll(mySet);
          }
          threadWait.arriveAndDeregister();
        }
      };
      threadPool.submit(r);
    }
    threadWait.awaitAdvance(threadWait.arriveAndDeregister());
    return relevantSellerIDs;
  }

  private static Set<Integer> getRelevantSellerIDs(int partnerID) {
    Set<Integer> retSet = new TreeSet<>();
    try {
      String jsonText = parseJSON(new URL(API_LINK_BASE + API_EXTENSION_PARTNER + partnerID + "/sellers"));
      if (jsonText == null) {
        return retSet;
      }
      //System.out.println("JSON Text: " + jsonText);
      JSONParser mP = new JSONParser();
      //JSONArray array = (JSONArray) mP.parse(jsonText);
      try {
        JSONObject obj = (JSONObject) mP.parse(jsonText);
        for (String s : (Iterable<String>) obj.keySet()) { // Parse through Seller IDs
          try {
            JSONObject obj2 = ((JSONObject) obj.get(s)); // Will throw CCE if it's not a JSONObject
            String seller = (String) obj2.get("organization");
            if (seller == null) {
              System.out.println("Null (" + s + ")");
            } else if (seller.toLowerCase().contains("pax") || seller.toLowerCase().contains("penny")) {
              System.out.println("PAX Seller: " + obj2.get("organization"));
              retSet.add(Integer.parseInt(s));
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

  private static int getPartnerID(String expo) {
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

  private static int getSellerID(String expo) {
    if (expo == null) {
      return 16886;
    }
    switch (expo.toLowerCase()) {
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

  private static int getVenueID(String expo) {
    if (expo == null) {
      return 13961;
    }
    switch (expo.toLowerCase()) {
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
