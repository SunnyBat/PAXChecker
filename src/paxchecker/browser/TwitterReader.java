package paxchecker.browser;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.util.List;
import paxchecker.Encryption;
import paxchecker.error.ErrorDisplay;
import twitter4j.*;
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
  private static final String[] KEYWORDS = {"passes", "tickets", "sale", "showclix", "available"};
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
   * Initialize the class Sets the lastStatusID to the latest tweet. The assumption is that tickets aren't already on sale. In case of false alarms
   * (instances where the twitter uses one of the keywords, but tickets aren't on sale yet), after restarting the app it will not trigger on that
   * tweet.
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
    System.out.println("Initialized: " + (twitter != null));
    return twitter != null;
  }

  public long getLatestTweetID() {
    try {
      Paging p = new Paging(lastIDFound);
      List<Status> statuses = twitter.getUserTimeline(TWITTER_HANDLE, p);
      System.out.println("Size: " + statuses.size());
      if (statuses.isEmpty()) {
        return lastIDFound;
      }
      lastIDFound = statuses.get(0).getId();
      return lastIDFound;
    } catch (TwitterException twitterException) {
    }
    return -1;
  }

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

  public String getLinkFromTweet(long tweetID) {
    return parseLink(getTweet(tweetID));
  }

  public static void setKeys(String CK, String CS, String AT, String AS) {
    if (CK == null || CS == null || AT == null || AS == null) {
      return;
    }
    try {
      consumerKey = Encryption.encrypt(CK);
      consumerSecret = Encryption.encrypt(CS);
      accessToken = Encryption.encrypt(AT);
      accessSecret = Encryption.encrypt(AS);
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
      if (tweet.toLowerCase().contains(keyString.toLowerCase())) {
        return true;
      }
    }
    //If nothing else, return false.
    return false;
  }

  public static String parseLink(String link) {
    if (link == null) {
      return "";
    }
    String linkFound = "";
    if (link.contains("http://")) {
      linkFound = link.substring(link.indexOf("http://"));
    } else if (link.contains("https://")) {
      linkFound = link.substring(link.indexOf("https://"));
    } else if (link.contains("t.co/")) {
      linkFound = link.substring(link.indexOf("t.co/"));
    }
    if (link.contains("t.co/")) {
      linkFound = Browser.unshortenURL(linkFound);
    }
    if (linkFound.contains(" ")) {
      linkFound = linkFound.substring(0, linkFound.indexOf(" "));
    }
    return linkFound.trim();
  }

}
