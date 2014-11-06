/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package paxchecker;

import java.io.*;
import java.net.*;

/**
 *
 * @author Sunny
 */
public class Paxsite {

  private static boolean checkPAXWebsite;
  private static String websiteLink;

  /**
   * Sets whether or not to check the PAX website for ticket sales.
   * @param check True to check, false to not
   */
  public static void setCheckPax(boolean check) {
    checkPAXWebsite = check;
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
      Checker.setStatusWebsiteLink("ERROR connecting to the PAX website!");
      System.out.println("ERROR connecting to the PAX website!");
      return false;
    } else if (lineText.equals("IOException") || lineText.equals("NoConnection")) {
      Checker.setStatusWebsiteLink("Unable to connect: " + lineText);
      System.out.println("Unable to connect: " + lineText);
      return false;
    } else if (lineText.equals("NoFind")) {
      Checker.setStatusWebsiteLink("Unable to find the Register Online button!");
      System.out.println("Unable to find the Register Online button!");
      return false;
    } else if (!lineText.contains("\"" + websiteLink + "\"")) {
      System.out.println("OMG IT'S UPDATED: " + lineText);
      return true;
    } else {
      Checker.setStatusWebsiteLink(parseHRef(lineText));
      return false;
    }
  }

  public static String getCurrentButtonLink() {
    return parseHRef(getCurrentButtonLinkLine());
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
      httpCon1.setConnectTimeout(Math.min(Checker.getRefreshTime()*1000, 10000));
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

}
