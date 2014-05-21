package paxchecker;

import java.io.*;
import java.net.*;
import java.awt.Desktop;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 *
 * @author SunnyBat
 */
public class Browser {

  private static boolean checkPAXWebsite;
  private static boolean checkShowclix;
  private static int lastShowclixEventID = 3817350;

  public static boolean isPAXWebsiteUpdated() {
    if (!checkPAXWebsite) {
      return false;
    }
    String lineText = getCurrentButtonLinkLine();
    if (lineText == null) {
      PAXChecker.status.setWebsiteLink("ERROR connecting to the PAX Prime website!");
      return false;
    } else if (lineText.equals("IOException") || lineText.equals("NoConnection")) {
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
          return line;
        }
      }
    } catch (UnknownHostException | MalformedURLException uhe) {
      return "NoConnection";
    } catch (IOException ioe) {
      ioe.printStackTrace();
      return "IOException";
    } catch (Exception e) {
      ErrorManagement.showErrorWindow("ERROR", "An unknown error has occurred while attempting to read the PAX website.", e);
      System.out.println("ERROR");
      return null;
    } finally {
      try {
        if (is != null) {
          is.close();
        }
      } catch (IOException ioe) {
        // nothing to see here
      }
    }
    System.out.println("NULL");
    return null;
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

  public static boolean isShowclixUpdated() {
    if (!checkShowclix) {
      return false;
    }
    int currEvent = getShowclixInfo();
    if (currEvent == -1) {
      PAXChecker.status.setShowclixLink("Unable to to connect to the Showclix website");
      return false;
    }
    String eventUrl = "http://showclix.com/event/" + currEvent;
    PAXChecker.status.setShowclixLink(eventUrl);
    if (currEvent != lastShowclixEventID) {
      return true;
    }
    return false;
  }

  public static int getShowclixInfo() {
    try {
      URL url = new URL("http://api.showclix.com/Seller/16886/events");
      HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
      httpCon.addRequestProperty("User-Agent", "Mozilla/4.0");
      BufferedReader reader = new BufferedReader(new InputStreamReader(httpCon.getInputStream()));
      String jsonText = "";
      while (reader.ready()) {
        jsonText += reader.readLine();
      }
      reader.close();
      JSONParser mP = new JSONParser();
      JSONObject obj = (JSONObject) mP.parse(jsonText);

      int maxId = 0;
      for (String s : (Iterable<String>) obj.keySet()) {
        maxId = Math.max(maxId, Integer.parseInt((String) s));
      }
      return maxId;
    } catch (Exception e) {
//      ErrorManagement.showErrorWindow("ERORR checking the Showclix website for updates!", e);
      e.printStackTrace();
      return -1;
    }
  }

  public static String getShowclixLink() {
    try {
      return "http://showclix.com/event/" + getShowclixInfo();
    } catch (Exception e) {
      ErrorManagement.showErrorWindow("ERORR checking the Showclix website for updates!", e);
      return null;
    }
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

  public static void enablePaxWebsiteChecking() {
    checkPAXWebsite = true;
  }

  public static void enableShowclixWebsiteChecking() {
    checkShowclix = true;
  }

  public static boolean isCheckingPaxWebsite() {
    return checkPAXWebsite;
  }

  public static boolean isCheckingShowclix() {
    return checkShowclix;
  }
}
