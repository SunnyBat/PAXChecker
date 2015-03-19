package com.github.sunnybat.paxchecker.browser;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.util.List;
import com.github.sunnybat.paxchecker.check.TwitterStreamer;
import com.github.sunnybat.paxchecker.preferences.Preference;
import com.github.sunnybat.paxchecker.preferences.PreferenceHandler;
import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;
import com.github.sunnybat.commoncode.error.ErrorDisplay;
import com.github.sunnybat.commoncode.encryption.Encryption;

/**
 * Using the Twitter4J library (3rd party TwitterReader Library for Java).
 *
 * @author Andrew
 */
public class TwitterReader {

  private static Twitter twitter;
  private static String consumerKey;
  private static String consumerSecret;
  private static String accessToken;
  private static String accessSecret;
  private static final String[] KEYWORDS = {"pax", "passes", "ticket", "sale", "badge", "showclix", "byoc"}; // Must be all lowercase
  private long lastIDFound;
  private final String TWITTER_HANDLE;

  public TwitterReader(String handle) {
    TWITTER_HANDLE = handle;
    try {
      List<Status> statuses = twitter.getUserTimeline(TWITTER_HANDLE);
      lastIDFound = statuses.get(0).getId();
    } catch (Exception ex) {
      System.out.println("Problem initializing Twitter API!");
    }
  }

  /**
   * Initialize the class. Sets the lastStatusID to the latest tweet. The assumption is that tickets aren't already on sale.
   */
  public static void init() {
    if (consumerKey == null || consumerSecret == null || accessToken == null || accessSecret == null) {
      System.out.println("ERROR: Twitter API not configured correctly!");
      return;
    }
    ConfigurationBuilder cb = new ConfigurationBuilder();
    try {
      cb.setDebugEnabled(true)
          .setOAuthConsumerKey(Encryption.decrypt(consumerKey))
          .setOAuthConsumerSecret(Encryption.decrypt(consumerSecret))
          .setOAuthAccessToken(Encryption.decrypt(accessToken))
          .setOAuthAccessTokenSecret(Encryption.decrypt(accessSecret));
    } catch (GeneralSecurityException | IOException generalSecurityException) {
      ErrorDisplay.showErrorWindow("ERROR setting Twitter API keys -- API not initialized.");
      return;
    }
    TwitterFactory tf = new TwitterFactory(cb.build());
    twitter = tf.getInstance();
    System.out.println("Twitter initialized!");
  }

  public static boolean isInitialized() {
    return twitter != null;
  }

  /**
   * Gets the ID of the latest tweet.
   *
   * @return The ID of the latest tweet
   */
  public long getLatestTweetID() {
    try {
      Paging p = new Paging(lastIDFound);
      List<Status> statuses = twitter.getUserTimeline(TWITTER_HANDLE, p);
      if (statuses.isEmpty()) {
        return lastIDFound;
      }
      lastIDFound = statuses.get(0).getId();
      return lastIDFound;
    } catch (TwitterException twitterException) {
    }
    return -1;
  }

  /**
   * Gets the text of the given Tweet
   *
   * @param tweetID The Tweet ID
   * @return The tweet
   */
  public String getTweet(long tweetID) {
    try {
      List<Status> statuses = twitter.getUserTimeline(TWITTER_HANDLE);
      for (Status stat : statuses) {
        if (stat.getId() == tweetID) {
          return stat.getText();
        }
      }
      System.out.println("ERROR: Unable to find Tweet ID " + tweetID);
      return statuses.get(0).getText();
    } catch (Exception ex) {
      System.out.println("Something went wrong with checking twitter..." + ex);
      return "[ERROR]";
    }
  }

  /**
   * Gets the link from the given Tweet
   *
   * @param tweetID The tweet ID
   * @return The link from the tweet
   */
  public String getLinkFromTweet(long tweetID) {
    return Browser.parseLink(getTweet(tweetID));
  }

  public static void setKeys(String CK, String CS, String AT, String AS) {
    if (CK == null || CS == null || AT == null || AS == null) {
      System.out.println("Twitter API keys not properly set!");
      return;
    }
    try {
      consumerKey = Encryption.encrypt(CK);
      consumerSecret = Encryption.encrypt(CS);
      accessToken = Encryption.encrypt(AT);
      accessSecret = Encryption.encrypt(AS);
      PreferenceHandler.getPreferenceObject(Preference.TYPES.TWITTER_CONSUMER_KEY).setValue(consumerKey);
      PreferenceHandler.getPreferenceObject(Preference.TYPES.TWITTER_CONSUMER_SECRET).setValue(consumerSecret);
      PreferenceHandler.getPreferenceObject(Preference.TYPES.TWITTER_APP_KEY).setValue(accessToken);
      PreferenceHandler.getPreferenceObject(Preference.TYPES.TWITTER_APP_SECRET).setValue(accessSecret);
    } catch (GeneralSecurityException | UnsupportedEncodingException generalSecurityException) {
    }
  }

  /**
   * Iterates through the list of statuses, seeing if any match against the trigger keywords.
   *
   * @param tweet The Tweet to search through
   * @return True if the tweet includes any of the trigger keywords, false otherwise.
   */
  public static boolean hasKeyword(String tweet) {
    for (String keyString : KEYWORDS) {
      if (tweet.toLowerCase().contains(keyString)) {
        return true;
      }
    }
    //If nothing else, return false.
    return false;
  }

  /**
   * Runs a new Twitter stream with the given handles. This should only be run as long as a Twitter stream is not currently running.
   *
   * @param handles
   */
  public static void runTwitterStream(String[] handles) {
    if (!isInitialized()) {
      System.out.println("Unable to start Twitter stream -- Twitter not properly configured!");
      return;
    }
    TwitterStreamer.runTwitterStream(twitter, handles);
  }
}
