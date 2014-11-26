/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package paxchecker.browser;

import java.io.*;
import java.net.*;
import paxchecker.DataTracker;
import paxchecker.error.ErrorDisplay;

/**
 *
 * @author Sunny
 */
public class PaxsiteReader {

  public static String getCurrentButtonLink(String expo) {
    return parseHRef(getCurrentButtonLinkLine(expo));
  }

  /**
   * Finds the link of the Register Now button on the PAX website. This scans EVENT.paxsite.com/registration for the Register Now button link, and
   * returns the ENTIRE line, HTML and all.
   *
   * @param expo The expo to check
   * @return The line (HTML included) that the Register Now button link is on
   * @see #parseHRef(java.lang.String)
   */
  private static String getCurrentButtonLinkLine(String expo) {
    URL url;
    InputStream is = null;
    BufferedReader br;
    String line;
    try {
      url = new URL(getWebsiteLink(expo) + "/registration");
      //is = url.openStream();
      HttpURLConnection httpCon = Browser.setUpConnection(url);
      is = httpCon.getInputStream();
      br = new BufferedReader(new InputStreamReader(is));
      while ((line = br.readLine()) != null) {
        DataTracker.addDataUsed(line.length());
        line = line.trim();
        if (line.contains("class=\"btn red\"") && line.contains("title=\"Register Online\"")) {
          return line;
        }
      }
    } catch (UnknownHostException | MalformedURLException | SocketTimeoutException e) {
      return "[NoConnection]";
    } catch (IOException ioe) {
      return "[IOException]";
    } catch (Exception e) {
      ErrorDisplay.showErrorWindow("ERROR", "An unknown error has occurred while attempting to read the PAX website.", e);
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
    return "[NoFind]";
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
  private static String parseHRef(String parse) {
    if (parse == null) {
      System.out.println("ERROR: parseHRef arg parse is null!");
      return "[null?]";
    }
    try {
      parse = parse.trim(); // Remove white space
      parse = parse.substring(parse.indexOf("href=") + 6); // Get index of link
      parse = parse.substring(0, parse.indexOf("\"")); // Remove everything after the link
      if (parse.startsWith("\"") && parse.endsWith("\"")) {
        parse = parse.substring(1, parse.length() - 1);
      } else if (parse.length() < 10) {
        System.out.println("Unable to correctly parse link from button HTML.");
        return null;
      }
      //System.out.println("Link parsed from Register Online button: " + parse);
      return parse.trim(); // PAX Aus currently has a space at the end of the registration button link... It doesn't sit well with Browser.java
    } catch (Exception e) {
      System.out.println("ERROR: Unable to parse link from button");
      e.printStackTrace();
      return "[Button Parsing Error]";
    }
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
      case "dev":
      case "pax dev":
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
      default:
        System.out.println("Expo not found: " + expo);
        return "http://prime.paxsite.com";
    }
  }

}
