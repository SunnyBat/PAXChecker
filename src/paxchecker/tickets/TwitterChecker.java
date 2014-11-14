package paxchecker.tickets;

import java.util.List;
import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;

/**
 * Using the Twitter4J library (3rd party TwitterChecker Library for Java).
 *
 * @author Andrew
 */
public class TwitterChecker {

  private static boolean checkPAXTwitter;
  private static long lastStatusID;
  private static Twitter twitter;
  private static long officialPAXTwitterID;
  private static String ConsumerKey;
  private static String ConsumerSecret;
  private static String AccessToken;
  private static String AccessSecret;
  private static final String officialPAXTwitter = "@Official_PAX";
  private static final String[] keyWordArray = {"passes", "tickets", "sale", "showclix", "available"};

  /**
   * Initialize the class Sets the lastStatusID to the latest tweet. The assumption is that tickets aren't already on sale. In case of false alarms
   * (instances where the twitter uses one of the keywords, but tickets aren't on sale yet), after restarting the app it will not trigger on that
   * tweet.
   */
  public static void init() {
    ConfigurationBuilder cb = new ConfigurationBuilder();
    cb.setDebugEnabled(true)
        .setOAuthConsumerKey(ConsumerKey)
        .setOAuthConsumerSecret(ConsumerSecret)
        .setOAuthAccessToken(AccessToken)
        .setOAuthAccessTokenSecret(AccessSecret);

    TwitterFactory tf = new TwitterFactory(cb.build());
    twitter = tf.getInstance();

    try {
      List<Status> statuses = twitter.getUserTimeline(officialPAXTwitter);
      lastStatusID = statuses.get(0).getId();
      officialPAXTwitterID = statuses.get(0).getUser().getId();
    } catch (Exception ex) {
      System.out.println("Problem initializing twitter api");
    }
  }

  public static void setKeys(String CK, String CS, String AT, String AS) {
    ConsumerKey = CK;
    ConsumerSecret = CS;
    AccessToken = AT;
    AccessSecret = AS;

  }

  /**
   * Enables the checking of the official twitter account for the announcement of ticket sales.
   */
  public static void enablePaxTwitterChecking() {
    checkPAXTwitter = true;
  }

  /**
   * Checks whether or not the program should check the PAX official twitter.
   *
   * @return True if should check, false if not.
   */
  public static boolean isCheckingPaxTwitter() {
    return checkPAXTwitter;
  }

  /**
   * Checks whether or not the official PAX twitter has been updated. If it has, check to see if any of the keywords in @keyWordArray are contained
   * within the tweet.
   *
   * @return True if the latest tweet contains any of the trigger keywords, False if not.
   */
  public static boolean isPaxTwitterUpdated() {
    /**
     * Here there be dragons.
     */
    if (!isCheckingPaxTwitter()) {
      return false;
    }
    Paging paging = new Paging(lastStatusID);
    try {
      //User paxUser = twitter.showUser(officialPAXTwitter);
      //long userID = paxUser.getId();
      List<Status> statuses = twitter.getUserTimeline(officialPAXTwitterID, paging);
      //List<Status> statuses = twitter.getUserTimeline(officialPAXTwitter);
      if (checkStatusesForKeywords(statuses)) {
        return true;
      }
    } catch (Exception ex) {
      System.out.println("Something went wrong with checking twitter..." + ex);
      return false;
    }
    //return false if nothing else, oh no, no, no, no false alarms here!
    return false;
  }

  /**
   * Iterates through the list of statuses, seeing if any match against the trigger keywords.
   *
   * @param statuses A Java List containing twitter Status objects.
   * @return True if any of the included statuses include any of the trigger keywords, false otherwise.
   */
  public static boolean checkStatusesForKeywords(List<Status> statuses) {
    for (Status status : statuses) {
      for (String keyString : keyWordArray) {
        if (status.getText().toLowerCase().contains(keyString.toLowerCase())) {
          return true;
        }
      }
    }
    //If nothing else, return false.
    return false;
  }

}
