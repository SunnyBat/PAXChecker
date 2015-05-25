package com.github.sunnybat.paxchecker.browser;

import com.github.sunnybat.commoncode.encryption.Encryption;
import com.github.sunnybat.commoncode.error.ErrorBuilder;
import com.github.sunnybat.paxchecker.check.TwitterStreamer;
import com.github.sunnybat.paxchecker.preferences.Preference;
import com.github.sunnybat.paxchecker.preferences.PreferenceHandler;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import twitter4j.Paging;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

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
  private static final String[] KEYWORDS = {"pax", "passes", "ticket", "tix", "sale", "badge", "showclix", "byoc", "hotel"}; // Must be all lowercase
  private long lastIDFound;
  private final String TWITTER_HANDLE;
  private static TwitterStreamer myStream;
  private static final List<String> handleList = new ArrayList<>();

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
    } catch (GeneralSecurityException | IOException gse) {
      new ErrorBuilder()
          .setError(gse)
          .setErrorTitle("Twitter Error")
          .setErrorMessage("ERROR setting Twitter API keys -- API not initialized.")
          .buildWindow();
      return;
    }
    TwitterFactory tf = new TwitterFactory(cb.build());
    twitter = tf.getInstance();
    System.out.println("Twitter initialized!");
    myStream = new TwitterStreamer(twitter);
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
   * Runs a new Twitter stream instance. If the Twitter stream is currently running (either connected or attempting to reconnect), this method will do
   * nothing.
   */
  public static void runTwitterStream() {
    if (!isInitialized()) {
      System.out.println("Unable to start Twitter stream -- Twitter not properly configured!");
      return;
    }
    String[] list = new String[handleList.size()];
    handleList.toArray(list);
    myStream.startStreamingTwitter(list);
  }

  /**
   * Checks whether or not the Twitter stream is currently on. This means that it is either connected, attempting to reconnect, or waiting to attempt
   * to reconnect. If false, runTwitterStream() may be called to start it.
   *
   * @return True if it is, false if not
   */
  public static boolean isStreamingTwitter() {
    if (!isInitialized()) {
      return false;
    }
    return myStream.isStreamingTwitter();
  }

  public static boolean isConnected() {
    return myStream.isConnected();
  }

  /**
   * Enables filtering tweets received by keyword. This cannot be undone.
   */
  public static void enableKeywordFiltering() {
    if (isInitialized()) {
      myStream.enableKeywordFiltering();
    }
  }

  /**
   * Adds a Twitter handle to the list of handles to check.
   *
   * @param s The handle to check
   */
  public static void addHandle(String s) {
    handleList.add(s);
  }
}
