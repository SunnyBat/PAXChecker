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
  private static long updateSize;
  private static long dataUsed;
  private static String Expo;
  private static String websiteLink;
  private static URL updateURL;
  private static URL patchNotesURL;
  private static volatile String versionNotes;
  private static final String UDATE_LINK = "https://dl.dropboxusercontent.com/u/16152108/PAXChecker.jar";
  private static final String PATCH_NOTES_LINK = "https://dl.dropboxusercontent.com/u/16152108/PAXCheckerUpdates.txt";
  private static final String SHOWCLIX_API_LINK_PRIME = "http://api.showclix.com/Seller/16886/events";
  private static final String SHOWCLIX_API_LINK_EAST = "http://api.showclix.com/Seller/17792/events";
  private static final String SHOWCLIX_API_LINK_SOUTH = "http://api.showclix.com/Seller/19042/events";
  private static final String SHOWCLIX_API_LINK_AUS = "http://api.showclix.com/Seller/15374/events";

  public static void init() {
    try {
      updateURL = new URL(UDATE_LINK);
      patchNotesURL = new URL(PATCH_NOTES_LINK);
    } catch (Exception e) {
      System.out.println("Unable to make a new URL?");
    }
  }

  /**
   * Adds an amount of data (in bytes) used by the program. This should be called whenever a network connection is made.
   *
   * @param data The amount of data (in bytes) to add to the total data used
   */
  public static void addDataUsed(long data) {
    dataUsed += data;
  }

  /**
   * Gets the amount of data (in bytes) used by the program.
   *
   * @return The amount of data (in bytes) used by the program
   */
  public static long getDataUsed() {
    return dataUsed;
  }

  /**
   * Gets the amount of data in megabytes used by the program. Note that the double only extends out two decimal places.
   *
   * @return The amount of data in megabytes used by the program
   */
  public static double getDataUsedMB() {
    return (double) ((int) ((double) getDataUsed() / 1024 / 1024 * 100)) / 100; // *100 to make the double have two extra numbers, round with typecasting to integer, then divide that by 100 and typecast to double to get a double with two decimal places
  }

  public static void setExpo(String e) {
    Expo = e;
    setShowclixID(getLatestShowclixID(e));
  }

  public static String getExpo() {
    return Expo;
  }

  public static void enablePaxWebsiteChecking() {
    checkPAXWebsite = true;
  }

  public static boolean isCheckingPaxWebsite() {
    return checkPAXWebsite;
  }

  public static boolean isPAXWebsiteUpdated() {
    if (!isCheckingPaxWebsite()) {
      return false;
    }
    String lineText = getCurrentButtonLinkLine();
    if (lineText == null) {
      if (PAXChecker.status != null) {
        PAXChecker.status.setWebsiteLink("ERROR connecting to the PAX website!");
      }
      return false;
    } else if (lineText.equals("IOException") || lineText.equals("NoConnection")) {
      if (PAXChecker.status != null) {
        PAXChecker.status.setWebsiteLink("Unable to connect to the PAX website!");
      }
      return false;
    } else if (lineText.equals("NoFind")) {
      if (PAXChecker.status != null) {
        PAXChecker.status.setWebsiteLink("Unable to find the Register Online button!");
      }
      return false;
    } else if (!lineText.contains("\"" + websiteLink + "\"")) {
      System.out.println("OMG IT'S UPDATED: " + lineText);
      return true;
    } else {
      if (PAXChecker.status != null) {
        PAXChecker.status.setWebsiteLink(parseHRef(lineText));
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
      is = url.openStream();
      br = new BufferedReader(new InputStreamReader(is));
      while ((line = br.readLine()) != null) {
        addDataUsed(line.length());
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
        System.out.println("Note: Unable to close InputStream for getCurrentButtonLinkLine()");
        ioe.printStackTrace();
      }
    }
    System.out.println("Button not found!");
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
    try {
      parse = parse.trim(); // Remove white space
      parse = parse.substring(parse.indexOf("href=") + 6); // Get index of link
      parse = parse.substring(0, parse.indexOf("\"")); // Remove everything after the link
      if (parse.startsWith("\"") && parse.endsWith("\"")) {
        parse = parse.substring(1, parse.length() - 1);
      } else if (parse == null || parse.length() < 10) {
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
        addDataUsed(line.length());
        jsonText += line;
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
      e.printStackTrace();
      return -1;
    }
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
      ErrorManagement.showErrorWindow("ERORR checking the Showclix website for updates!", e);
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
        ErrorManagement.showErrorWindow("ERROR opening browser window", "Unable to open link in browser window!", e);
      }
    } else {
      System.out.println("Unable to open link in default browser.");
      ErrorManagement.showErrorWindow("ERROR", "Unable to open link in default browser.", null);
    }
  }

  /**
   * Opens the URL given in the computer's default browser. Note that this will NOT work if the desktop environment isn't supported (generally a
   * non-issue).
   * Also note that this will simply open the URL -- it will not parse through it to make sure it is valid!
   *
   * @param url The URL to open in the computer's default browser
   */
  public static void openLinkInBrowser(URL url) {
    Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
    if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
      try {
        desktop.browse(url.toURI());
      } catch (Exception e) {
        ErrorManagement.showErrorWindow("ERROR opening browser window", "Unable to open link in browser window!", e);
      }
    } else {
      System.out.println("Unable to open link in default browser.");
      ErrorManagement.showErrorWindow("ERROR", "Unable to open link in default browser.", null);
    }
  }

  public static String getVersionNotes(String version) {
    String versNotes = getVersionNotes();
    if (versNotes == null) {
      return null;
    }
    try {
      versNotes = versNotes.substring(0, versNotes.indexOf("~~~" + version)).trim();
    } catch (Exception e) {
    }
    return versNotes;
  }

  public static String getVersionNotes() {
    return versionNotes;
  }

  public static void loadVersionNotes() {
    URLConnection inputConnection;
    InputStream textInputStream;
    BufferedReader myReader = null;
    try {
      inputConnection = patchNotesURL.openConnection();
      textInputStream = inputConnection.getInputStream();
      myReader = new BufferedReader(new InputStreamReader(textInputStream));
      String line;
      String lineSeparator = System.getProperty("line.separator", "\n");
      String allText = "Patch Notes:" + lineSeparator;
      while ((line = myReader.readLine()) != null) {
        addDataUsed(line.length());
        line = line.trim();
        if (line.startsWith("TOKEN:")) {
          try {
            String d = line.substring(6);
            if (d.startsWith("SETSHOWCLIXID:")) {
              String load = d.substring(14);
              System.out.println("Load = " + load);
              setShowclixID(Integer.parseInt(load));
            } //else if (d.startsWith("")) {
//              String load = d.substring(0);
//              System.out.println("Load = " + load);
//              setShowclixID(Integer.parseInt(load));
//            }
          } catch (NumberFormatException numberFormatException) {
            System.out.println("Unable to set token: " + line);
          }
        } else {
          allText += line + lineSeparator;
        }
      }
      versionNotes = allText.trim();
    } catch (Exception e) {
      System.out.println("Unable to load version notes!");
    } finally {
      try {
        if (myReader != null) {
          myReader.close();
        }
      } catch (IOException e) {
        // nothing to see here
      }
    }
  }

  public static long getUpdateSize() {
    return updateSize;
  }

  /**
   * Checks whether or not an update to the program is available. Note that this compares the file sizes between the current file and the file on the
   * Dropbox server. This means that if ANY modification is made to the JAR file, it's likely to trigger an update. This THEORETICALLY works well.
   * We'll find out whether or not it will actually work in practice.
   *
   * @return True if an update is available, false if not.
   */
  public static boolean updateAvailable() {
    try {
      File mF = new File(PAXChecker.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
      long fileSize = mF.length();
      if (fileSize == 4096) { // No, I do NOT want to update when I'm running in Netbeans
        return false;
      }
      URLConnection conn = updateURL.openConnection();
      updateSize = conn.getContentLengthLong();
      System.out.println("Updatesize = " + updateSize + " -- Filesize = " + fileSize);
      if (updateSize == -1) {
        ErrorManagement.showErrorWindow("ERROR checking for updates!", "PAX Checker was unable to check for updates.", null);
        return false;
      } else if (updateSize != fileSize) {
        System.out.println("Update available!");
        return true;
      }
    } catch (Exception e) {
      System.out.println("ERROR updating program!");
      ErrorManagement.showErrorWindow("ERROR updating program!", "The program was unable to check for new updates.", e);
    }
    return false;
  }

  /**
   * Downloads the latest JAR file from the Dropbox server. Note that this automatically closes the program once finished. Also note that once this is
   * run, the program WILL eventually close, either through finishing the update or failing to properly update.
   */
  public static void updateProgram() {
    try {
      URLConnection conn = updateURL.openConnection();
      InputStream inputStream = conn.getInputStream();
      long remoteFileSize = conn.getContentLength();
      System.out.println("Downloding file...\nUpdate Size(compressed): " + remoteFileSize + " Bytes");
      String path = PAXChecker.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
      BufferedOutputStream buffOutputStream = new BufferedOutputStream(new FileOutputStream(new File(path.substring(0, path.lastIndexOf(".jar")) + ".temp.jar")));
      byte[] buffer = new byte[32 * 1024];
      int bytesRead = 0;
      int in = 0;
      int prevPercent = 0;
      while ((bytesRead = inputStream.read(buffer)) != -1) {
        in += bytesRead;
        buffOutputStream.write(buffer, 0, bytesRead);
        if (PAXChecker.update != null) {
          if ((int) (((in * 100) / remoteFileSize)) != prevPercent) {
            prevPercent = (int) (((in * 100) / remoteFileSize));
            PAXChecker.update.updateProgress(prevPercent);
          }
        }
      }
      buffOutputStream.flush();
      buffOutputStream.close();
      inputStream.close();
      if (PAXChecker.update != null) {
        PAXChecker.update.setStatusLabelText("Finishing up...");
      }
      try { // Code to make a copy of the current JAR file
        File inputFile = new File(path.substring(0, path.lastIndexOf(".jar")) + ".temp.jar");
        InputStream fIn = new BufferedInputStream(new FileInputStream(inputFile));
        File outputFile = new File(path);
        buffOutputStream = new BufferedOutputStream(new FileOutputStream(outputFile));
        buffer = new byte[32 * 1024];
        bytesRead = 0;
        in = 0;
        while ((bytesRead = fIn.read(buffer)) != -1) {
          in += bytesRead;
          buffOutputStream.write(buffer, 0, bytesRead);
        }
        buffOutputStream.flush();
        buffOutputStream.close();
        fIn.close();
        inputFile.delete();
      } catch (Exception e) {
        ErrorManagement.showErrorWindow("ERROR updating", "Unable to complete update -- unable to copy temp JAR file to current JAR file.", e);
        ErrorManagement.fatalError();
      }
      System.out.println("Download Complete!");
      PAXChecker.startNewProgramInstance();
    } catch (Exception e) {
      e.printStackTrace();
      System.out.println("ERROR updating program!");
      ErrorManagement.showErrorWindow("ERROR updating the program", "The program was unable to successfully download the update. If the problem continues, please manually download the latest version at " + updateURL.getPath(), e);
      ErrorManagement.fatalError();
    }
  }
}
