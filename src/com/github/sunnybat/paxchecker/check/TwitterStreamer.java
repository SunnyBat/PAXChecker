package com.github.sunnybat.paxchecker.check;

import com.github.sunnybat.commoncode.error.ErrorBuilder;
import com.github.sunnybat.paxchecker.browser.Browser;
import com.github.sunnybat.paxchecker.browser.TwitterReader;
import java.util.Arrays;
import twitter4j.*;

/**
 *
 * @author Sunny
 */
public class TwitterStreamer {

  public TwitterStreamer(Twitter t) {
    myTwitter = t;
  }

  private Twitter myTwitter;
  private String[] usersToCheck;
  private TwitterStream myStream;
  private int successiveErrorCount;
  private boolean filterKeywords;
  public final UserStreamListener listener = new UserStreamListener() {
    @Override
    public void onStatus(Status status) { // Feels SO hacked together right now
      System.out.println("onStatus @" + status.getUser().getScreenName() + " - " + status.getText());
      if (filterKeywords) {
        if (!TwitterReader.hasKeyword(status.getText())) {
          System.out.println("Tweet does not have keywords -- ignoring.");
          return;
        }
      }
      for (String s : usersToCheck) {
        if (s.equals(status.getUser().getScreenName().toLowerCase())) {
          String statusText = status.getText();
          while (statusText.contains("t.co/")) { // ALL links are shortened
            String link = Browser.parseLink(statusText);
            statusText = statusText.substring(statusText.indexOf(link) + link.length()); // Remove link from statusText
            link = Browser.unshortenURL(link);
            if (!TicketChecker.hasOpenedLink(link)) {
              CheckSetup.linkFound(link);
              TicketChecker.addLinkFound(link);
            } else {
              System.out.println("Link already found -- ignoring");
            }
          }
          return;
        }
      }
      System.out.println("Tweet is not in list of names to check -- ignoring");
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
          .setErrorMessage("A stall warning has been thrown by the Twiiter4j library.")
          .buildWindow();
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
      CheckSetup.twitterStreamKilled();
    }

    @Override
    public void onConnect() {
      System.out.println("Connected to Twitter Streaming service.");
      CheckSetup.twitterConnection(true);
      successiveErrorCount = 0;
    }

    @Override
    public void onDisconnect() {
      System.out.println("Disconnected from Twitter Streaming service");
//      CheckSetup.twitterConnection(false);
      CheckSetup.twitterConnection(successiveErrorCount * 10); // I don't think *10 is right... Is it 10, 15, 30?
    }
  };

  public void startStreamingTwitter(String[] handles) {
    usersToCheck = handles.clone(); // Set users to check
    for (int i = 0; i < usersToCheck.length; i++) { // Strip @ from Twitter handle
      if (usersToCheck[i].startsWith("@")) {
        usersToCheck[i] = usersToCheck[i].replaceFirst("@", "");
      }
      usersToCheck[i] = usersToCheck[i].toLowerCase();
    }
    if (isStreamingTwitter()) { // If already running, return
      return;
    }
    System.out.println(Arrays.toString(handles));
    try {
      myStream = new TwitterStreamFactory().getInstance(myTwitter.getAuthorization());
      myStream.addListener(listener);
      myStream.addConnectionLifeCycleListener(cLCListener);
      myStream.user(handles);
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

  public void enableKeywordFiltering() {
    filterKeywords = true;
  }

}
