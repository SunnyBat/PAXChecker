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
    public void onStatus(Status status) {
      System.out.println("onStatus @" + status.getUser().getScreenName() + " - " + status.getText());
      if (!TwitterReader.hasKeyword(status.getText())) { // TODO: Check if screenName is in the list of users to check for
        System.out.println("Tweet does not have keywords -- ignoring.");
      } else {
        for (String s : usersToCheck) {
          if (s.startsWith("@")) {
            s = s.replaceFirst("@", "");
          }
          if (s.toLowerCase().equals(status.getUser().getScreenName().toLowerCase())) {
            String link = Browser.parseLink(status.getText());
            if (!TicketChecker.hasOpenedLink(link)) {
              CheckSetup.linkFound(link);
              TicketChecker.addLinkFound(link);
            }
            return;
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
      ErrorDisplay.showErrorWindow("Error Enabling Twitter", "A connection to the Twitter Streaming API was unable to be established. "
          + "This is probably due to an expired or invalid Twitter API key. Please double-check your API keys.", ex);
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
