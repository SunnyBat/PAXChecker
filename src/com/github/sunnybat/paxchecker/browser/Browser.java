package com.github.sunnybat.paxchecker.browser;

import com.github.sunnybat.paxchecker.check.CheckSetup;
import java.awt.Desktop;
import java.io.*;
import java.net.*;
import java.util.Scanner;
import com.github.sunnybat.commoncode.error.ErrorDisplay;

/**
 *
 * @author SunnyBat
 */
public class Browser {

  private static volatile String Expo;
  private static final String UNSHORTEN_API_LINK = "http://api.longurl.org/v2/expand?url=";

  /**
   * Sets the current expo. This should adhere to the format of "PAX [expo]" or just "[expo]". Using a different format may result in Browser or
   * program inoperability. The expo set is used multiple times throughout the program for user feedback, so it's recommended to capitalize it
   * correctly.
   *
   * @param e The String to set as the expo
   */
  public static void setExpo(String e) {
    if (e != null) {
      if (!e.toLowerCase().startsWith("pax ")) {
        e = "PAX " + e;
      }
      Expo = e;
    }
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
   * Opens the link given in the computer's default browser. Note that this will NOT work if the desktop environment isn't supported (generally a
   * non-issue).
   *
   * @param link The link to open in the computer's default browser
   */
  public static void openLinkInBrowser(String link) {
    if (link == null) {
      return;
    }
    try {
      openLinkInBrowser(new URL(link));
    } catch (MalformedURLException mue) {
    }
  }

  /**
   * Opens the URL given in the computer's default browser. Note that this will NOT work if the desktop environment isn't supported (generally a
   * non-issue). Also note that this will simply open the URL -- it will not parse through it to make sure it is valid!
   *
   * @param url The URL to open in the computer's default browser
   */
  public static void openLinkInBrowser(URL url) {
    if (url == null) {
      ErrorDisplay.showErrorWindow("ERROR", "Unable to open link in default browser -- link is null!", null);
      return;
    }
    Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
    if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
      try {
        desktop.browse(url.toURI());
      } catch (URISyntaxException | IOException e) {
        ErrorDisplay.showErrorWindow("ERROR opening browser window", "Unable to open link in browser window!", e);
      }
    } else {
      ErrorDisplay.showErrorWindow("ERROR", "Unable to open link in default browser -- desktop is not supported", null);
    }
  }

  public static HttpURLConnection setUpConnection(URL url) {
    try {
      HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
      httpCon.addRequestProperty("User-Agent", "Mozilla/4.0");
      httpCon.setConnectTimeout(Math.min(CheckSetup.getRefreshTime() * 1000, 15000));
      httpCon.setReadTimeout(2000);
      return httpCon;
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  /**
   * Gets the actual link from the given shortened URL.
   *
   * @param toShorten The URL to unshorten
   * @return The actual URL that will be served
   */
  public static String unshortenURL(String toShorten) {
    URL fullURL;
    URLConnection myConn;
    try {
      fullURL = new URL(UNSHORTEN_API_LINK + toShorten);
      myConn = fullURL.openConnection();
      myConn.setConnectTimeout(3000);
      myConn.connect();
      Scanner scan = new Scanner(myConn.getInputStream());
      String line;
      while (scan.hasNext()) {
        line = scan.nextLine().trim();
        if (line.contains("<long-url>") && line.contains("]]></l")) {
          System.out.print("URL Unshortened: ");
          String found = line.substring(line.indexOf("http"), line.indexOf("]]></l"));
          System.out.println(found);
          return found;
        }
      }
    } catch (Exception e) {
      System.out.println("ERROR: Unable to shorten link: " + toShorten);
      e.printStackTrace();
    }
    return toShorten;
  }

  /**
   * Parses the link from the given String. Note that this cannot parse links with spaces.
   *
   * @param link The String with a link to extract
   * @return The link
   */
  public static String parseLink(String link) {
    if (link == null) {
      return "";
    }
    if (link.contains("http://")) { // Trim link to start of address
      link = link.substring(link.indexOf("http://"));
    } else if (link.contains("https://")) {
      link = link.substring(link.indexOf("https://"));
    } else if (link.contains("t.co/")) {
      link = link.substring(link.indexOf("t.co/"));
    } else {
      return null; // Link not recognized
    }
    if (link.contains(" ")) { // There are words after the link, so remove them
      link = link.substring(0, link.indexOf(" "));
    }
    link = link.trim();
//    if (link.contains("t.co/")) { // Link to unshorten
//      link = Browser.unshortenURL(link);
//    }
    if (link.endsWith("/")) {
      link = link.substring(0, link.length() - 1);
    }
    System.out.println("Link parsed: " + link);
    return link.trim();
  }
}
