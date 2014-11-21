package paxchecker.browser;

import paxchecker.tickets.Checker;
import java.awt.Desktop;
import java.io.*;
import java.net.*;
import java.util.Scanner;
import paxchecker.error.ErrorDisplay;

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
    Expo = e;
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
    try {
      openLinkInBrowser(new URL(link));
    } catch (MalformedURLException mue) {
      mue.printStackTrace();
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
      httpCon.setConnectTimeout(Math.min(Checker.getRefreshTime() * 1000, 15000));
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
    }
    return toShorten;
  }
}