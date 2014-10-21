package paxchecker;

import java.awt.Desktop;
import java.io.*;
import java.net.*;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 *
 * @author SunnyBat
 */
public class Browser {

  private static boolean checkPAXWebsite;
  private static boolean checkShowclix;
  private static int lastShowclixEventID = 3852445;
  private static String Expo;
  private static String websiteLink;
  private static final String SHOWCLIX_API_LINK_PRIME = "http://api.showclix.com/Seller/16886/events"; // Also for PAX Dev
  private static final String SHOWCLIX_API_LINK_EAST = "http://api.showclix.com/Seller/17792/events";
  private static final String SHOWCLIX_API_LINK_SOUTH = "http://api.showclix.com/Seller/19042/events";
  private static final String SHOWCLIX_API_LINK_AUS = "http://api.showclix.com/Seller/15374/events";

  /**
   * Sets the current expo. This should adhere to the format of "PAX [expo]" or just "[expo]". Using a different format may result in Browser or
   * program inoperability. The expo set is used multiple times throughout the program for user feedback, so it's recommended to capitalize it
   * correctly.
   *
   * @param e The String to set as the expo
   */
  public static void setExpo(String e) {
    Expo = e;
    setShowclixID(getLatestShowclixID(e));
    setWebsiteLink(getWebsiteLink(getExpo()));
  }

  /**
   * Returns the expo currently set. This should adhere to the format of "PAX [expo]" or just "[expo]".
   *
   * @return The expo currently set
   * @see #setExpo(java.lang.String)
   */
  public static String getExpo() {
    return Expo;
  }

  /**
   * Enables the checking of the [expo].paxsite.com/registration page for the Register Online button.
   *
   * @see #isCheckingPaxWebsite()
   */
  public static void enablePaxWebsiteChecking() {
    checkPAXWebsite = true;
  }

  /**
   * Checks whether or not the program should check the PAX Registration website.
   *
   * @return True if should check, false if not
   */
  public static boolean isCheckingPaxWebsite() {
    return checkPAXWebsite;
  }

  /**
   * Checks whether or not the PAX website is updated. This checks for the Register Online button on the [expo].paxsite.com/registration page. If
   * found, it reads the current href (hyperlink) for the button, and if it doesn't link to the [expo].paxsite.com website (when tickets are all sold
   * out, PAX switches it to that), it returns true. This method also sets the Status window website link text. Note that this returns false if
   * {@link #enablePaxWebsiteChecking() enablePaxWebsiteChecking()} has not been called.
   *
   * @return True if the Register Now button link is updated, false if not
   */
  public static boolean isPAXWebsiteUpdated() {
    if (!isCheckingPaxWebsite()) {
      return false;
    }
    String lineText = getCurrentButtonLinkLine();
    if (lineText == null) {
      if (PAXChecker.status != null) {
        PAXChecker.status.setWebsiteLink("ERROR connecting to the PAX website!");
      } else {
        System.out.println("ERROR connecting to the PAX website!");
      }
      return false;
    } else if (lineText.equals("IOException") || lineText.equals("NoConnection")) {
      if (PAXChecker.status != null) {
        PAXChecker.status.setWebsiteLink("Unable to connect: " + lineText);
      } else {
        System.out.println("Unable to connect: " + lineText);
      }
      return false;
    } else if (lineText.equals("NoFind")) {
      if (PAXChecker.status != null) {
        PAXChecker.status.setWebsiteLink("Unable to find the Register Online button!");
      } else {
        System.out.println("Unable to find the Register Online button!");
      }
      return false;
    } else if (!lineText.contains("\"" + websiteLink + "\"")) {
      System.out.println("OMG IT'S UPDATED: " + lineText);
      return true;
    } else {
      if (PAXChecker.status != null) {
        PAXChecker.status.setWebsiteLink(parseHRef(lineText));
      } else {
        System.out.println("");
      }
      return false;
    }
  }

  /**
   * Finds the link of the Register Now button on the PAX website. This scans EVENT.paxsite.com/registration for the Register Now button link, and
   * returns the ENTIRE line, HTML and all.
   *
   * @return The line (HTML included) that the Register Now button link is on
   * @see #parseHRef(java.lang.String)
   */
  public static String getCurrentButtonLinkLine() {
    URL url;
    InputStream is = null;
    BufferedReader br;
    String line;
    try {
      url = new URL(websiteLink + "/registration");
      //is = url.openStream();
      HttpURLConnection httpCon1 = (HttpURLConnection) url.openConnection();
      httpCon1.addRequestProperty("User-Agent", "Mozilla/4.0");
      is = httpCon1.getInputStream();
      br = new BufferedReader(new InputStreamReader(is));
      while ((line = br.readLine()) != null) {
        DataTracker.addDataUsed(line.length());
        line = line.trim();
        if (line.contains("class=\"btn red\"") && line.contains("title=\"Register Online\"")) {
          return line;
        }
      }
    } catch (UnknownHostException | MalformedURLException uhe) {
      return "NoConnection";
    } catch (IOException ioe) {
      return "IOException";
    } catch (Exception e) {
      ErrorHandler.showErrorWindow("ERROR", "An unknown error has occurred while attempting to read the PAX website.", e);
      System.out.println("ERROR");
      return null;
    } finally {
      try {
        if (is != null) {
          is.close();
        }
      } catch (IOException ioe) {
        // nothing to see here
        System.out.println("Note: Unable to close InputStream for getCurrentButtonLinkLine()");
        ioe.printStackTrace();
      }
    }
    System.out.println("Website \"Register Now\" button not found!");
    return "NoFind";
  }

  /**
   * Parses the link out of the given line of HTML. Note that this requires an href with the link surrounded by quotation marks for this to work.
   * Furthermore, the link can't have any quotation marks in it (though if it does, you have bigger problems).
   *
   * @param parse The HTML to parse
   * @return The link from the HTML, or the address to the current expo site
   * @see #getExpo()
   * @see #getWebsiteLink(java.lang.String)
   * @see #getCurrentButtonLinkLine()
   */
  public static String parseHRef(String parse) {
    if (parse == null) {
      System.out.println("ERROR: parseHRef arg parse is null!");
      return websiteLink;
    }
    try {
      parse = parse.trim(); // Remove white space
      parse = parse.substring(parse.indexOf("href=") + 6); // Get index of link
      parse = parse.substring(0, parse.indexOf("\"")); // Remove everything after the link
      if (parse.startsWith("\"") && parse.endsWith("\"")) {
        parse = parse.substring(1, parse.length() - 1);
      } else if (parse.length() < 10) {
        System.out.println("Unable to correctly parse link from button HTML.");
        return websiteLink;
      }
      //System.out.println("Link parsed from Register Online button: " + parse);
      return parse.trim(); // PAX Aus currently has a space at the end of the registration button link... It doesn't sit well with Browser.java
    } catch (Exception e) {
      System.out.println("ERROR: Unable to parse link from button");
      e.printStackTrace();
      return websiteLink;
    }
  }

  /**
   * Sets the website link used for the program. Note that this does NOT check for invalid links, and therefore should be used with caution. Invalid
   * links will result in failure to check the proper PAX website. Ex: http://prime.paxsite.com
   *
   * @param link The FULL address (http:// as well!) to check for updates.
   */
  public static void setWebsiteLink(String link) {
    websiteLink = link;
  }

  /**
   * Returns the HTTP address of the given PAX Expo. Be sure to only use the name of the expo (ex: prime) OR the full name (ex: pax prime) as the
   * argument.
   *
   * @param expo The PAX expo to get the website link for
   * @return The website link of the specified expo, or the PAX Prime link if invalid.
   */
  public static String getWebsiteLink(String expo) {
    if (expo == null) {
      return "http://prime.paxsite.com";
    }
    switch (expo.toLowerCase()) { // toLowerCase to lower the possibilities (and readability)
      case "prime":
      case "pax prime":
        return "http://prime.paxsite.com";
      case "east":
      case "pax east":
        return "http://east.paxsite.com";
      case "south":
      case "pax south":
        return "http://south.paxsite.com";
      case "aus":
      case "pax aus":
        return "http://aus.paxsite.com";
      case "dev":
      case "pax dev":
        return "http://dev.paxsite.com";
      default:
        return "http://prime.paxsite.com";
    }
  }

  /**
   * Tells the program to check the Showclix website when checking for ticket sales. Note that this uses the Showclix API (api.showclix.com) to check
   * for events.
   */
  public static void enableShowclixWebsiteChecking() {
    checkShowclix = true;
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
    int currEvent = getLatestShowclixID(getExpo());
    if (currEvent == -1) {
      if (PAXChecker.status != null) {
        PAXChecker.status.setShowclixLink("Unable to to connect to the Showclix website!");
      }
      return false;
    }
    String eventUrl = "https://showclix.com/event/" + currEvent;
    if (PAXChecker.status != null) {
      PAXChecker.status.setShowclixLink(eventUrl);
    }
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
      System.out.println("Max ID = " + maxId);
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
      openLinkInBrowser(showLink);
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
      return "https://showclix.com/event/" + getLatestShowclixID(getExpo());
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

  /**
   * Opens the link given in the computer's default browser. Note that this will NOT work if the desktop environment isn't supported (generally a
   * non-issue).
   *
   * @param link The link to open in the computer's default browser
   */
  public static void openLinkInBrowser(String link) {
    Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
    if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
      if (link.startsWith("/") || link.startsWith("\\")) {
        link = websiteLink + link;
      }
      try {
        desktop.browse(new URI(link));
      } catch (Exception e) {
        ErrorHandler.showErrorWindow("ERROR opening browser window", "Unable to open link in browser window!", e);
      }
    } else {
      System.out.println("Unable to open link in default browser.");
      ErrorHandler.showErrorWindow("ERROR", "Unable to open link in default browser.", null);
    }
  }

  /**
   * Opens the URL given in the computer's default browser. Note that this will NOT work if the desktop environment isn't supported (generally a
   * non-issue). Also note that this will simply open the URL -- it will not parse through it to make sure it is valid!
   *
   * @param url The URL to open in the computer's default browser
   */
  public static void openLinkInBrowser(URL url) {
    Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
    if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
      try {
        desktop.browse(url.toURI());
      } catch (Exception e) {
        ErrorHandler.showErrorWindow("ERROR opening browser window", "Unable to open link in browser window!", e);
      }
    } else {
      System.out.println("Unable to open link in default browser.");
      ErrorHandler.showErrorWindow("ERROR", "Unable to open link in default browser.", null);
    }
  }
}
