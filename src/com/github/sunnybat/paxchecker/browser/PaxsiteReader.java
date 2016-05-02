package com.github.sunnybat.paxchecker.browser;

import com.github.sunnybat.commoncode.error.ErrorBuilder;
import com.github.sunnybat.paxchecker.DataTracker;
import com.github.sunnybat.paxchecker.Expo;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;

/**
 *
 * @author Sunny
 */
public class PaxsiteReader {

  private Expo expoToCheck;

  public PaxsiteReader(Expo toCheck) {
    expoToCheck = toCheck;
  }

  public String getCurrentButtonLink() {
    try {
      URL urlToConnectTo = new URL(getWebsiteLink(expoToCheck) + "/registration");
      String link = findShowclixLink(urlToConnectTo);
      return link;
    } catch (MalformedURLException mue) {
    }
    return "[NoConnection]";
  }

  private String findShowclixLink(URL urlToConnectTo) {
    BufferedReader lineReader = null;
    try {
      String line;
      lineReader = setUpConnection(urlToConnectTo);
      while ((line = lineReader.readLine()) != null) {
        DataTracker.addDataUsed(line.length());
        line = line.trim();
        if (line.contains("www.showclix.com")) {
          String parseRef = line.toLowerCase();
          String ret;
          if (parseRef.contains("http://")) {
            ret = line.substring(parseRef.indexOf("www.showclix.com") - 7);
          } else if (parseRef.contains("https://")) {
            ret = line.substring(parseRef.indexOf("www.showclix.com") - 8);
          } else {
            ret = line.substring(parseRef.indexOf("www.showclix.com"));
          }
          parseRef = ret.toLowerCase();
          ret = ret.substring(0, parseRef.indexOf("\""));
          return ret;
        }
      }
    } catch (UnknownHostException | MalformedURLException | SocketTimeoutException e) {
      return "[NoConnection]";
    } catch (IOException ioe) {
      return "[IOException]";
    } catch (Exception e) {
      new ErrorBuilder()
          .setError(e)
          .setErrorMessage("An unknown error has occurred while attempting to read the PAX website.")
          .buildWindow();
      System.out.println("ERROR");
      return null;
    } finally {
      try {
        if (lineReader != null) {
          lineReader.close();
        }
      } catch (IOException ioe) {
        // nothing to see here
        System.out.println("Note: Unable to close InputStream for getCurrentButtonLinkLine()");
        ioe.printStackTrace();
      }
    }
    return "[NoFind]";
  }

  private BufferedReader setUpConnection(URL urlToConnectTo) throws UnknownHostException, MalformedURLException, SocketTimeoutException, IOException {
    InputStream rawInputStream = null;
    BufferedReader lineReader;
    HttpURLConnection httpCon = Browser.setUpConnection(urlToConnectTo);
    rawInputStream = httpCon.getInputStream();
    lineReader = new BufferedReader(new InputStreamReader(rawInputStream));
    return lineReader;
  }

  /**
   * Returns the HTTP address of the given PAX Expo. Be sure to only use the name of the expo (ex: prime) OR the full name (ex: pax prime) as the
   * argument.
   *
   * @param expo The PAX expo to get the website link for
   * @return The website link of the specified expo, or the PAX Prime link if invalid.
   */
  public String getWebsiteLink(Expo expo) {
    if (expo == null) {
      return "http://west.paxsite.com";
    }
    switch (expo) {
      case PRIME:
        return "http://west.paxsite.com";
      case EAST:
        return "http://east.paxsite.com";
      case SOUTH:
        return "http://south.paxsite.com";
      case AUS:
        return "http://aus.paxsite.com";
      default:
        System.out.println("Expo not found: " + expo);
        return "http://west.paxsite.com";
    }
  }

}
