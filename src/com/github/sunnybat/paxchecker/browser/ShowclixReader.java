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
  private static ExecutorService threadPool = Executors.newFixedThreadPool(5); // TODO: Make this only initialize when Deep Showclix Checking is enabled
  private static int maxPartnerID = 100;

  /**
   * Gets the complete Showclix Event Page link.
   *
   * @param showclixID The Showclix Event ID
   * @return The complete link
   */
  public static String getEventLink(int showclixID) {
    return API_LINK_BASE + API_EXTENSION_EVENT + showclixID;
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

  public static Set<String> getAllEventURLs(String expo) {
    Set<String> retSet = ShowclixReader.getAllSellerEventURLs(expo);
    retSet.addAll(ShowclixReader.getAllPartnerEventURLs(expo));
    return retSet;
  }

  public static Set<String> getAllRelevantURLs() {
    Set<Integer> sellerIDs = getAllRelevantSellerIDs();
    Set<Integer> partnerIDs = getAllPartners(sellerIDs);
    final Set<String> retSet = new TreeSet<>();
    for (int partnerID : partnerIDs) {
      retSet.addAll(getAllPartnerEventURLs(partnerID));
    }
    for (int sellerID : sellerIDs) {
      retSet.addAll(getAllSellerEventURLs(sellerID));
    }
    return retSet;
  }

  private static Set<String> getAllPartnerEventURLs(String expo) {
    return getAllPartnerEventURLs(getPartnerID(expo));
  }

  private static Set<String> getAllSellerEventURLs(String expo) {
    return getAllSellerEventURLs(getSellerID(expo));
  }

  private static Set<String> getAllEventURLs(JSONObject obj) {
    Set<String> retSet = new TreeSet<>();
    for (String s : (Iterable<String>) obj.keySet()) { // Parse through Event IDs
      try {
        JSONObject obj2 = ((JSONObject) obj.get(s)); // Will throw CCE if it's not a JSONObject
        if (obj2.get("listing_url") == null) {
          System.out.println("Listing URL is null!");
          retSet.add("http://www.showclix.com/Event/" + s);
        } else {
          System.out.println("URL Found: " + obj2.get("listing_url"));
          retSet.add((String) obj2.get("listing_url"));
        }
      } catch (ClassCastException cce) {
        System.out.println("Unable to read event from key " + s + " -- CCE: object is " + obj.get(s).getClass().getSimpleName());
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
      String jsonText = parseJSON(new URL(API_LINK_BASE + API_EXTENSION_SELLER + sellerID + "/events"));
      if (jsonText == null) {
        return retSet;
      }
      //System.out.println("JSON Text: " + jsonText);
      JSONParser mP = new JSONParser();
      //JSONArray array = (JSONArray) mP.parse(jsonText);
      try {
        retSet.addAll(getAllEventURLs((JSONObject) mP.parse(jsonText)));
      } catch (ClassCastException cce) {
        System.out.println("ClassCastException from " + mP.parse(jsonText).getClass().getSimpleName() + ": " + mP.parse(jsonText));
      }
    } catch (IOException iOException) {
      System.out.println("ERROR connecting to Seller " + sellerID);
    } catch (ParseException parseException) {
    }
    return retSet;
  }

  private static Set<String> getAllPartnerEventURLs(int partnerID) {
    Set<String> retSet = new TreeSet<>();
    try {
      String jsonText = parseJSON(new URL(API_LINK_BASE + API_EXTENSION_PARTNER + partnerID + "/events"));
      if (jsonText == null) {
        return retSet;
      }
      //System.out.println("JSON Text: " + jsonText);
      JSONParser mP = new JSONParser();
      //JSONArray array = (JSONArray) mP.parse(jsonText);
      try {
        retSet.addAll(getAllEventURLs((JSONObject) mP.parse(jsonText)));
      } catch (ClassCastException cce) {
        System.out.println("ClassCastException from " + mP.parse(jsonText).getClass().getName() + ": " + mP.parse(jsonText));
      }
    } catch (IOException iOException) {
      System.out.println("Error connecting to partner " + partnerID);
    } catch (ParseException parseException) {
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
            if (obj2.get("organization") == null) {
              System.out.println("Null.");
            } else if (((String) obj2.get("organization")).toLowerCase().contains("pax")) {
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

  private static String parseJSON(URL url) {
    try {
      HttpURLConnection httpCon = Browser.setUpConnection(url);
      httpCon.setConnectTimeout(500);
      BufferedReader reader = new BufferedReader(new InputStreamReader(httpCon.getInputStream()));
      StringBuilder build = new StringBuilder();
      String line;
      while ((line = reader.readLine()) != null) {
        DataTracker.addDataUsed(line.length());
        build.append(line);
      }
      reader.close();
      return build.toString();
    } catch (IOException iOException) {
      return null;
    }
  }
}
