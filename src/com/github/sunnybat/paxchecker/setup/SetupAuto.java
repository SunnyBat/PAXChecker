package com.github.sunnybat.paxchecker.setup;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author SunnyBat
 */
public class SetupAuto implements Setup {

  private String[] args;

  public SetupAuto(String[] args) {
    this.args = new String[args.length];
    System.arraycopy(args, 0, this.args, 0, args.length);
  }

  @Override
  public void promptForSettings() {
    // Do not need to do anything
  }

  private boolean hasArg(String arg) {
    for (String s : args) {
      if (s.equals(arg)) {
        return true;
      }
    }
    return false;
  }

  private String getArg(String arg) {
      return getArg(arg, 1);
  }

  private String getArg(String arg, int indexesOut) {
    for (int i = 0; i < args.length; i++) {
      if (args[i].equals(arg) && i < args.length - indexesOut) {
        return args[i+indexesOut];
      }
    }
    return ""; // Probably shouldn't return null?
  }

  @Override
  public String getEmailUsername() {
    return getArg("-email");
  }

  @Override
  public String getEmailPassword() {
    return getArg("-password");
  }

  @Override
  public List<String> getEmailAddresses() {
    List<String> emails = new ArrayList<>();
    String arg = getArg("-cellnum");
    if (arg != null) {
      String[] split = arg.split(";");
      for (String s : split) {
        emails.add(s.trim());
      }
    }
    return emails;
  }

  @Override
  public boolean shouldCheckPAXWebsite() {
    return !hasArg("-nopax");
  }

  @Override
  public boolean shouldCheckShowclix() {
    return !hasArg("-noshowclix");
  }

  @Override
  public boolean shouldCheckKnownEvents() {
    return !hasArg("-noknownevents");
  }

  @Override
  public boolean shouldCheckTwitter() {
    return !hasArg("-notwitter");
  }

  @Override
  public boolean shouldFilterShowclix() {
    return hasArg("-filtershowclix");
  }

  @Override
  public boolean shouldFilterTwitter() {
    return hasArg("-filtertwitter");
  }

  @Override
  public boolean shouldTextTweets() {
    return hasArg("-texttweets");
  }

  @Override
  public boolean shouldPlayAlarm() {
    return hasArg("-alarm");
  }

  @Override
  public int timeBetweenChecks() {
    try {
      return Integer.parseInt(getArg("-delay"));
    } catch (NumberFormatException nfe) {
      return 10;
    }
  }

  @Override
  public String getExpoToCheck() {
    return getArg("-expo");
  }

  @Override
  public String getTwitterConsumerKey() {
    return getArg("-twitterkeys", 1);
  }

  @Override
  public String getTwitterConsumerSecret() {
    return getArg("-twitterkeys", 2);
  }

  @Override
  public String getTwitterApplicationKey() {
    return getArg("-twitterkeys", 3);
  }

  @Override
  public String getTwitterApplicationSecret() {
    return getArg("-twitterkeys", 4);
  }

  @Override
  public boolean shouldCheckForUpdatesDaily() {
    return hasArg("-dailyupdates");
  }

}
