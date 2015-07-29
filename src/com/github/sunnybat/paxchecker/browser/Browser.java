package com.github.sunnybat.paxchecker.browser;

import com.github.sunnybat.commoncode.error.ErrorBuilder;
import java.awt.Desktop;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;

/**
 *
 * @author SunnyBat
 */
public class Browser {

  private String expo; // CHECK: Should I just remove this entirely...?

  /**
   * Sets the current expo. This should adhere to the format of "PAX [expo]" or just "[expo]". Using a different format may result in Browser or
   * program inoperability. The expo set is used multiple times throughout the program for user feedback, so it's recommended to capitalize it
   * correctly.
   *
   * @param e The String to set as the expo
   */
  public void setExpo(String e) {
    if (e != null) {
      if (!e.toLowerCase().startsWith("pax ")) {
        e = "PAX " + e;
      }
      expo = e;
    }
  }

  /**
   * Returns the expo currently set. This should adhere to the format of "PAX [expo]" or just "[expo]".
   *
   * @return The expo currently set
   * @see #setExpo(java.lang.String)
   */
  public String getExpo() {
    if (expo == null) {
      return "PAX Prime";
    }
    return expo;
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
      new ErrorBuilder()
          .setErrorMessage("Unable to open link in default browser -- link is null!")
          .buildWindow();
      return;
    }
    Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
    if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
      try {
        desktop.browse(url.toURI());
      } catch (URISyntaxException | IOException e) {
        new ErrorBuilder()
            .setError(e)
            .setErrorTitle("ERROR opening browser window")
            .setErrorMessage("Unable to open link in browser window!")
            .buildWindow();
      }
    } else {
      try {
        if (!browse(url.toURI())) {
          new ErrorBuilder()
              .setErrorMessage("Unable to open link in default browser -- desktop is not supported")
              .buildWindow();
        }
      } catch (URISyntaxException use) {
        new ErrorBuilder()
            .setError(use)
            .setErrorMessage("Unable to open link in default browser -- URISyntaxException?")
            .buildWindow();
      }
    }
  }

  public static HttpURLConnection setUpConnection(URL url) { // NOTE: getURL() method on this will only change to redicted URL once data has been read
    try {
      HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
      httpCon.addRequestProperty("User-Agent", "Mozilla/4.0");
      httpCon.setConnectTimeout(5000);
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
    URLConnection myConn = null;
    try {
      URL myURL = new URL(toShorten);
      myConn = myURL.openConnection(); // As long as Java follows redirects (called from PAXChecker.main()) we're good!
      myConn.getInputStream();
    } catch (Exception e) {
    }
    if (myConn != null) {
      return myConn.getURL().toString();
    } else {
      return toShorten;
    }
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
    if (link.contains("\"")) {
      link = link.substring(0, link.indexOf("\""));
    }
    link = link.trim();
    if (link.endsWith("/")) {
      link = link.substring(0, link.length() - 1);
    }
    System.out.println("Link parsed: " + link);
    return link.trim();
  }

  // Credit to MightyPork on StackOverflow for the following methods.
  // http://stackoverflow.com/questions/18004150/desktop-api-is-not-supported-on-the-current-platform/18004334#18004334
  public static boolean browse(URI uri) {
    return openSystemSpecific(uri.toString());
  }

  private static boolean openSystemSpecific(String what) {
    EnumOS os = getOs();
    if (os.isLinux()) {
      if (runCommand("kde-open", "%s", what)) {
        return true;
      } else if (runCommand("gnome-open", "%s", what)) {
        return true;
      } else if (runCommand("xdg-open", "%s", what)) {
        return true;
      }
    }
    if (os.isMac()) {
      if (runCommand("open", "%s", what)) {
        return true;
      }
    }
    if (os.isWindows()) {
      if (runCommand("explorer", "%s", what)) {
        return true;
      }
    }
    return false;
  }

  private static boolean runCommand(String command, String args, String file) {
    System.out.println("Trying to exec:\n   cmd = " + command + "\n   args = " + args + "\n   %s = " + file);
    String[] parts = prepareCommand(command, args, file);
    try {
      Process p = Runtime.getRuntime().exec(parts);
      if (p == null) {
        return false;
      }
      try {
        int retval = p.exitValue();
        if (retval == 0) {
          System.err.println("Process ended immediately.");
          return false;
        } else {
          System.err.println("Process crashed.");
          return false;
        }
      } catch (IllegalThreadStateException itse) {
        System.err.println("Process is running.");
        return true;
      }
    } catch (IOException e) {
      logErr("Error running command.", e);
      return false;
    }
  }

  private static String[] prepareCommand(String command, String args, String file) {
    java.util.List<String> parts = new java.util.ArrayList<>();
    parts.add(command);
    if (args != null) {
      for (String s : args.split(" ")) {
        s = String.format(s, file); // put in the filename thing
        parts.add(s.trim());
      }
    }
    return parts.toArray(new String[parts.size()]);
  }

  private static void logErr(String msg, Throwable t) {
    System.err.println(msg);
    t.printStackTrace();
  }

  private static enum EnumOS {

    linux, macos, solaris, unknown, windows;

    public boolean isLinux() {

      return this == linux || this == solaris;
    }

    public boolean isMac() {

      return this == macos;
    }

    public boolean isWindows() {
      return this == windows;
    }
  }

  private static EnumOS getOs() {
    String s = System.getProperty("os.name").toLowerCase();
    if (s.contains("win")) {
      return EnumOS.windows;
    } else if (s.contains("mac")) {
      return EnumOS.macos;
    } else if (s.contains("solaris")) {
      return EnumOS.solaris;
    } else if (s.contains("sunos")) {
      return EnumOS.solaris;
    } else if (s.contains("linux")) {
      return EnumOS.linux;
    } else if (s.contains("unix")) {
      return EnumOS.linux;
    } else {
      return EnumOS.unknown;
    }
  }
}
