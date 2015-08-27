package com.github.sunnybat.paxchecker.resources;

/**
 * Constants for Resources.
 *
 * @author SunnyBat
 */
class ResourceConstants {

  public static final String RESOURCE_LOCATION = getResourceLocation();
  public static final String[] DEFAULT_FILE_NAMES = {"Alarm.wav", "Alert.png", "PAXPrime.png", "PAXEast.png", "PAXSouth.png", "PAXAus.png"};

  private static String getResourceLocation() {
    String os = System.getProperty("os.name").toLowerCase();
    if (os.contains("windows")) {
      return System.getenv("APPDATA") + "/PAXChecker/"; // Has / and \ in path, but Java apparently doesn't care
    } else if (os.contains("mac")) {
      System.out.println("RD PATH: " + System.getProperty("user.home") + "/Library/Application Support/PAXChecker/");
      return System.getProperty("user.home") + "/Library/Application Support/PAXChecker/"; // TODO: Mac location
    } else if (os.contains("linux") || os.contains("ubuntu")) {
      return System.getProperty("user.home") + "/.config/PAXChecker/";
    } else { // Store in current folder, we have no clue what OS this is
      return "PAXChecker/";
    }
  }

}
