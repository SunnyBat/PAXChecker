package paxchecker;

import java.awt.Desktop;
import java.net.*;

/**
 *
 * @author SunnyBat
 */
public class Browser {

  private static String Expo;

  /**
   * Sets the current expo. This should adhere to the format of "PAX [expo]" or just "[expo]". Using a different format may result in Browser or
   * program inoperability. The expo set is used multiple times throughout the program for user feedback, so it's recommended to capitalize it
   * correctly.
   *
   * @param e The String to set as the expo
   */
  public static void setExpo(String e) {
    Expo = e;
    Showclix.setShowclixID(Showclix.getLatestShowclixID(e));
    Paxsite.setWebsiteLink(Paxsite.getWebsiteLink(getExpo()));
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
    Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
    if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
      if (link.startsWith("/") || link.startsWith("\\")) {
        link = Paxsite.getWebsiteLink(getExpo()) + link;
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
