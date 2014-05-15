package paxchecker;

import java.io.*;
import java.net.*;
import java.awt.Desktop;

/**
 *
 * @author SunnyBat
 */
public class Browser {

  public static boolean isUpdated() {
    String lineText = getCurrentButtonLinkLine();
    if (lineText == null) {
      PAXChecker.status.setWebsiteLink("ERROR connecting to the PAX Prime website!");
      return false;
    } else if (lineText.equals("IOException")) {
      PAXChecker.status.setWebsiteLink("Unable to connect to the PAX Prime website!");
      return false;
    } else if (!lineText.contains("\"http://prime.paxsite.com\"")) {
      System.out.println("OMG IT'S UPDATED: " + lineText);
      return true;
    } else {
      PAXChecker.status.setWebsiteLink(parseHRef(lineText));
      return false;
    }
  }

  public static String parseHRef(String parse) {
    try {
      parse = parse.trim(); // Remove white space
      parse = parse.substring(parse.indexOf("href=") + 6); // Get index of link
      parse = parse.substring(0, parse.indexOf("\"")); // Remove everything after the link (hopefully this works for the Showclix link)
      if (parse.startsWith("\"") && parse.endsWith("\"")) {
        parse = parse.substring(1, parse.length() - 1);
      } else if (parse == null || parse.length() < 10) {
        System.out.println("Unable to correctly parse link from button HTML.");
        return "http://prime.paxsite.com";
      }
      return parse;
    } catch (Exception e) {
      System.out.println("ERROR: Unable to parse link from button");
      e.printStackTrace();
      return "http://prime.paxsite.com";
    }
  }

  public static String getCurrentButtonLinkLine() {
    URL url;
    InputStream is = null;
    BufferedReader br;
    String line;
    try {
      url = new URL("http://prime.paxsite.com/registration");
      is = url.openStream();
      br = new BufferedReader(new InputStreamReader(is));
      while ((line = br.readLine()) != null) {
        line = line.trim();
        if (line.contains("class=\"btn red\"") && line.contains("title=\"Register Online\"")) {
          return line.trim();
        }
      }
    } catch (MalformedURLException mue) {
      mue.printStackTrace();
    } catch (IOException ioe) {
      ioe.printStackTrace();
      return "IOException";
    } catch (Exception e) {
      ErrorManagement.showErrorWindow("ERROR", "An unknown error has occurred while attempting to read the PAX website.", e);
    } finally {
      try {
        if (is != null) {
          is.close();
        }
      } catch (IOException ioe) {
        // nothing to see here
      }
    }
    return null;
  }

  public static void openLinkInBrowser(String link) {
    Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
    if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
      try {
        desktop.browse(new URI(link));
      } catch (Exception e) {
        e.printStackTrace();
      }
    } else {
      System.out.println("Unable to open " + link + " in default browser.");
      ErrorManagement.showErrorWindow("ERROR", "Unable to open the requested link: " + link, null);
    }
  }
}
