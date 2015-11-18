package com.github.sunnybat.paxchecker;

/**
 * Identifies the different expos that the program can search for
 * @author SunnyBat
 */
public enum Expo {

  PRIME, EAST, SOUTH, AUS;

  @Override
  public String toString() {
    String name = super.toString();
    return "PAX " + name.charAt(0) + name.toLowerCase().substring(1);
  }

  public static Expo parseExpo(String expo) {
    expo = expo.toLowerCase();
    if (expo.contains("prime")) {
      return PRIME;
    } else if (expo.contains("east")) {
      return EAST;
    } else if (expo.contains("south")) {
      return SOUTH;
    } else if (expo.contains("aus")) {
      return AUS;
    } else {
      throw new IllegalArgumentException("No expo found");
    }
  }
}
