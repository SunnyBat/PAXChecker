package com.github.sunnybat.paxchecker.check;

import com.github.sunnybat.commoncode.error.ErrorDisplay;
import java.util.Arrays;
import com.github.sunnybat.paxchecker.browser.Browser;
import com.github.sunnybat.paxchecker.browser.TwitterReader;
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
      if (!TwitterReader.hasKeyword(status.getText())) { // TODO: Check if screenName is in the list of users to check for
        System.out.println("Tweet does not have keywords -- ignoring.");
      } else {
        for (String s : usersToCheck) {
          if (filterKeywords) {
            if (s.equals(status.getUser().getScreenName().toLowerCase()) && status.getText().contains("t.co/")) {
              String tStatus = status.getText();
              String link = Browser.parseLink(tStatus);
              while (link != null) { // Continuously parse through tweet
                String toOpen = Browser.unshortenURL(link);
                if (!toOpen.contains("showclix") && !toOpen.contains("t.co") && !toOpen.contains("onpeak")) {
                  System.out.println("Link is not Showclix or unshortened -- ignoring.");
                } else if (!TicketChecker.hasOpenedLink(toOpen)) {
                  CheckSetup.linkFound(toOpen);
                  TicketChecker.addLinkFound(toOpen);
                } else {
                  System.out.println("Link already found -- ignoring.");
                }
                tStatus = tStatus.substring(tStatus.indexOf(link) + link.length(), tStatus.length()); // Trim status Tweet to link
                link = Browser.parseLink(tStatus); // Get next link, or null if none
              }
              return;
            } else {
              System.out.println("Tweet does not contain link -- ignoring.");
            }
          } else {
            if (s.equals(status.getUser().getScreenName().toLowerCase())) {
              String statusText = status.getText().toLowerCase();
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
            }
          }
        }
        System.out.println("Tweet is not in list of names to check -- ignoring");
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
      ErrorDisplay.showErrorWindow("Stall Warning", "A stall warning has been thrown by the Twiiter4j library.", t);
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
          ErrorDisplay.showErrorWindow("WARNING: Unable to authenticate Twitter stream",
              "\nMake sure that you have specified the correct credentials and that your computer's system time is correct!"
              + "\nThe program will attempt to authenticate two more times.", ex);
        }
      } else if (successiveErrorCount == 3) {
        if (ex.getMessage().contains("Authentication credentials (https://dev.twitter.com/pages/auth) were missing or incorrect")) {
          ErrorDisplay.showErrorWindow("ERROR: The program was unable to authenticate your Twitter credentials",
              "\nMake sure that you have specified the correct credentials and that your computer's system time is correct!"
              + "\nThe Twitter feed has been shut down. Please restart the program to enable Twitter checking.", ex);
        } else {
          ErrorDisplay.showErrorWindow("ERROR: Disconnected from Twitter Streaming service",
              "\nRestart the PAXChecker to reconnect. If this persists, let /u/SunnyBat know!"
              + "\nThe Twitter feed has been shut down. Please restart the program to enable Twitter checking.", ex);
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
      CheckSetup.twitterConnection(false);
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

  public boolean isStreamingTwitter() {
    return myStream != null;
  }

  public void enableKeywordFiltering() {
    filterKeywords = true;
  }

}
