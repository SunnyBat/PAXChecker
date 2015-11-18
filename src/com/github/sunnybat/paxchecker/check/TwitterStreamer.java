package com.github.sunnybat.paxchecker.check;

import com.github.sunnybat.commoncode.error.ErrorBuilder;
import com.github.sunnybat.paxchecker.browser.Browser;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import twitter4j.ConnectionLifeCycleListener;
import twitter4j.DirectMessage;
import twitter4j.FilterQuery;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.User;
import twitter4j.UserList;
import twitter4j.UserStreamListener;
import twitter4j.conf.ConfigurationBuilder;

/**
 *
 * @author Sunny
 */
public abstract class TwitterStreamer {

  private Twitter myTwitter;
  private List<String> usersToCheck = new ArrayList<>(Arrays.asList("Official_PAX","rkhoo")); // No @ in the raw list!
  private List<String> keywords = new ArrayList<>(Arrays.asList("pax", "passes", "ticket", "tix", "sale", "badge", "showclix", "byoc", "hotel")); // All lowercase
  private TwitterStream myStream;
  private int successiveErrorCount;
  private boolean filterKeywords;

  /**
   * Creates a new TwitterStream with the given Twitter API keys. Note that
   *
   * @param keys
   */
  public TwitterStreamer(String[] keys) {
    if (keys == null || keys.length != 4) {
      throw new IllegalArgumentException("keys must contain exactly four valid Twitter API keys");
    }
    ConfigurationBuilder cb = new ConfigurationBuilder();
    cb.setDebugEnabled(true)
        .setOAuthConsumerKey(keys[0])
        .setOAuthConsumerSecret(keys[1])
        .setOAuthAccessToken(keys[2])
        .setOAuthAccessTokenSecret(keys[3]);
    TwitterFactory tf = new TwitterFactory(cb.build());
    myTwitter = tf.getInstance();
    System.out.println("Twitter initialized! Following: " + usersToCheck.toString());

  }

  private final UserStreamListener listener = new UserStreamListener() {
    @Override
    public void onStatus(Status status) { // Only called when a user the program is watching tweets
      System.out.println("onStatus @" + status.getUser().getScreenName() + " - " + status.getText());
      String statusText = status.getText();
      Boolean isRetweet = status.isRetweet();

      String statusTextForNotification = statusText;
      if (filterKeywords && !hasKeyword(statusText)) {
        System.out.println("Does not contain keyword -- ignoring");
        return;
      }
      while (statusText.contains("t.co/") && !isRetweet) { // ALL links are shortened
        String link = Browser.parseLink(statusText);
        System.out.println("Expanded URL: " + status.getURLEntities()[0].getExpandedURL());
        String expandedURL = status.getURLEntities()[0].getExpandedURL();
        statusText = statusText.substring(statusText.indexOf(link) + link.length()); // Remove link from statusText
        System.out.println("truncated tweet: " + statusTextForNotification.replaceAll(link, ""));
        statusTextForNotification = statusTextForNotification.replaceAll(link, "");
        link = Browser.unshortenURL(link);
        linkFound(expandedURL, statusTextForNotification);
      }
    }

    @Override
    public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
    }

    @Override
    public void onDeletionNotice(long directMessageId, long userId) {
    }

    @Override
    public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
    }

    @Override
    public void onScrubGeo(long userId, long upToStatusId) {
    }

    @Override
    public void onStallWarning(StallWarning warning) {
      Throwable t = new Throwable(warning.getCode() + "\n\n" + warning.getMessage() + "\nn" + warning.getPercentFull());
      new ErrorBuilder()
          .setError(t)
          .setErrorTitle("Stall Warning")
          .setErrorMessage("A stall warning has been thrown by the Twiiter4j library. This may have killed the Twitter stream, regardless of whether"
              + " or not the Status Window says it's connected. It's recommended that you restart the PAXChecker.")
          .buildWindow();
      System.out.println("Stall warning?");
    }

    @Override
    public void onFriendList(long[] friendIds) {
    }

    @Override
    public void onFavorite(User source, User target, Status favoritedStatus) {
    }

    @Override
    public void onUnfavorite(User source, User target, Status unfavoritedStatus) {
    }

    @Override
    public void onFollow(User source, User followedUser) {
    }

    @Override
    public void onUnfollow(User source, User followedUser) {
    }

    @Override
    public void onDirectMessage(DirectMessage directMessage) {
    }

    @Override
    public void onUserListMemberAddition(User addedMember, User listOwner, UserList list) {
    }

    @Override
    public void onUserListMemberDeletion(User deletedMember, User listOwner, UserList list) {
    }

    @Override
    public void onUserListSubscription(User subscriber, User listOwner, UserList list) {
    }

    @Override
    public void onUserListUnsubscription(User subscriber, User listOwner, UserList list) {
    }

    @Override
    public void onUserListCreation(User listOwner, UserList list) {
    }

    @Override
    public void onUserListUpdate(User listOwner, UserList list) {
    }

    @Override
    public void onUserListDeletion(User listOwner, UserList list) {
    }

    @Override
    public void onUserProfileUpdate(User updatedUser) {
    }

    @Override
    public void onBlock(User source, User blockedUser) {
    }

    @Override
    public void onUnblock(User source, User unblockedUser) {
    }

    @Override
    public void onQuotedTweet(User source, User quoter, Status stat) {
    }

    @Override
    public void onFavoritedRetweet(User source, User favoriter, Status stat) {
    }

    @Override
    public void onRetweetedRetweet(User source, User retweeter, Status stat) {
    }

    @Override
    public void onUserDeletion(long id) {
    }

    @Override
    public void onUserSuspension(long id) {
    }

    @Override
    public void onException(Exception ex) {
      ex.printStackTrace();
      if (++successiveErrorCount == 1) {
        if (ex.getMessage().contains("Authentication credentials (https://dev.twitter.com/pages/auth) were missing or incorrect")) {
          new ErrorBuilder()
              .setError(ex)
              .setErrorTitle("WARNING: Unable to authenticate Twitter stream")
              .setErrorMessage("\nMake sure that you have specified the correct credentials and that your computer's system time is correct!"
                  + "\nThe program will attempt to authenticate two more times.")
              .buildWindow();
        }
      } else if (successiveErrorCount == 3) {
        if (ex.getMessage().contains("Authentication credentials (https://dev.twitter.com/pages/auth) were missing or incorrect")) {
          new ErrorBuilder()
              .setError(ex)
              .setErrorTitle("ERROR: The program was unable to authenticate your Twitter credentials")
              .setErrorMessage("\nMake sure that you have specified the correct credentials and that your computer's system time is correct!"
                  + "\nThe Twitter feed has been shut down. To reconnect, you must do so manually.")
              .buildWindow();
        } else if (ex.getMessage().contains("420:Returned by the Search and Trends API when you are being rate limited")) {
          new ErrorBuilder()
              .setError(ex)
              .setErrorTitle("ERROR: Too Many Logins")
              .setErrorMessage("You currently have too many Twitter streams open at once. Either create multiple Twitter accounts or wait"
                  + " a few minutes (to let any previously open streams close by themselves) and try again. ")
              .buildWindow();
        } else {
          new ErrorBuilder()
              .setError(ex)
              .setErrorTitle("ERROR: Disconnected from Twitter Streaming service")
              .setErrorMessage("\nClick the Reconnect button or restart the PAXChecker to reconnect. If this persists, let /u/SunnyBat know!"
                  + "\n\nThe Twitter feed has been shut down. To reconnect, you must do so manually.")
              .buildWindow();
        }
        myStream.shutdown();
      }
    }
  };

  private final ConnectionLifeCycleListener cLCListener = new ConnectionLifeCycleListener() {
    @Override
    public void onCleanUp() {
      System.out.println("Twitter Streaming cleanup");
      myStream = null; // Make program know that the Twitter stream is dead
      successiveErrorCount = 0;
      twitterKilled();
    }

    @Override
    public void onConnect() {
      System.out.println("Connected to Twitter Streaming service.");
      twitterConnected();
      successiveErrorCount = 0;
    }

    @Override
    public void onDisconnect() {
      System.out.println("Disconnected from Twitter Streaming service");
      twitterDisconnected();
    }
  };

  /**
   * Adds a new keyword to filter for. If the Twitter stream is running, this has no effect until restarted.
   *
   * @param keyword The keyword to add to the filter
   */
  public void addKeyword(String keyword) {
    if (keyword == null) {
      throw new IllegalArgumentException("Keyword cannot be null");
    }
    keywords.add(keyword.toLowerCase());
  }

  /**
   * Adds a new user to watch. If the Twitter stream is running, this has no effect until restarted.
   *
   * @param user The user to watch
   */
  public void addUser(String user) {
    if (user == null) {
      throw new IllegalArgumentException("User cannot be null");
    }
    if (user.startsWith("@")) {
      user = user.substring(1);
    }
    usersToCheck.add(user);
  }

  private boolean hasKeyword(String text) {
    if (keywords.isEmpty()) {
      System.out.println("NOTE: Keyword list is empty!");
      return true;
    }
    for (String keyword : keywords) {
      if (text.toLowerCase().contains(keyword)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Starts looking for Twitter updates. If the stream is already started -- connected or disconnected -- this method does nothing.
   */
  public void startStreamingTwitter() {
    if (isStreamingTwitter()) { // If already running, return
      System.out.println("Twitter stream is already running!");
      return;
    }
    try {
      myStream = new TwitterStreamFactory().getInstance(myTwitter.getAuthorization());
      myStream.addListener(listener);
      myStream.addConnectionLifeCycleListener(cLCListener);
      long[] ids = new long[usersToCheck.size()];
      for (int i = 0; i < usersToCheck.size(); i++) {
        try {
          User toAdd = myTwitter.showUser(usersToCheck.get(i));
          ids[i] = toAdd.getId();
        } catch (TwitterException tE) {
          System.out.println("TwitterException: Unable to add user " + usersToCheck.get(i) + " to list!");
        }
      }
      FilterQuery filter = new FilterQuery(0, ids);
      //myStream.user();
      myStream.filter(filter);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Checks whether or not the Twitter stream is currently on. This means that it is either connected, attempting to reconnect, or waiting to attempt
   * to reconnect. If false, startStreamingTwitter() may be called to start it.
   *
   * @return True if streaming is on, false if not
   */
  public boolean isStreamingTwitter() {
    return myStream != null;
  }

  /**
   * Checks whether or not the Twitter stream is currently connected. Note that if the stream is disconnected but is still streaming, it will be
   * automatically attempting to reconnect.
   *
   * @return True if it's connected, false if disconnected
   * @see #isStreamingTwitter()
   */
  public boolean isConnected() {
    return successiveErrorCount == 0 && isStreamingTwitter();
  }

  /**
   * Enables keyword filtering. This is in addition to filtering by user tweets. Recommended only if you really don't want any false positives.
   */
  public void enableKeywordFiltering() {
    filterKeywords = true;
  }

  /**
   * Called when a new link has been found
   *
   * @param link The link found
   */
  public abstract void linkFound(String link, String statusText);

  /**
   * Called when a connection to the Twitter API has been made, including reconnects
   */
  public abstract void twitterConnected();

  /**
   * Called when disconnected from the Twitter API and will automatically retry
   */
  public abstract void twitterDisconnected();

  /**
   * Called when disconnected from the Twitter API and will not retry automatically
   */
  public abstract void twitterKilled();

}
