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

  private static String[] usersToCheck;
  private static TwitterStream myStream;
  public static final UserStreamListener listener = new UserStreamListener() {
    @Override
    public void onStatus(Status status) { // Feels SO hacked together right now
      System.out.println("onStatus @" + status.getUser().getScreenName() + " - " + status.getText());
      if (!TwitterReader.hasKeyword(status.getText())) { // TODO: Check if screenName is in the list of users to check for
        System.out.println("Tweet does not have keywords -- ignoring.");
      } else {
        for (String s : usersToCheck) {
          if (s.startsWith("@")) {
            s = s.replaceFirst("@", "");
          }
          if (s.toLowerCase().equals(status.getUser().getScreenName().toLowerCase())) {
            if (status.getText().contains("t.co/")) {
              String tStatus = status.getText();
              String link = Browser.parseLink(tStatus);
              while (link != null) { // Continuously parse through tweet
                String toOpen = Browser.unshortenURL(link);
                if (!toOpen.contains("showclix") && !toOpen.contains("t.co")) {
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
      System.out.println("onException:" + ex.getMessage());
      ErrorDisplay.showErrorWindow("Error with Twitter", "An error has occurred with Twitter checking. If this error occurred right as you started "
          + "the program, it's probably an issue with your Twitter API keys. Otherwise, an internal program error has occurred.", ex);
    }
  };

  public static void runTwitterStream(Twitter twitter, String[] handles) {
    if (isStreamingTwitter()) {
      return;
    }
    System.out.println(Arrays.toString(handles));
    myStream = new TwitterStreamFactory().getInstance(twitter.getAuthorization());
    myStream.addListener(listener);
    myStream.user(handles);
    usersToCheck = handles.clone();
  }

  public static boolean isStreamingTwitter() {
    return myStream != null;
  }

}
