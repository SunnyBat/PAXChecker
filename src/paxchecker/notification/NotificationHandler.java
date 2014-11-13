package paxchecker.notification;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.HttpURLConnection;

/**
 *
 * @author Sunny
 */
public class NotificationHandler {

  private static final String NOTIFICATIONS_LINK = "https://dl.dropboxusercontent.com/u/16152108/PAXCheckerNotifications.txt";
  private static URL notificationURL;

  public static void init() {
    try {
      notificationURL = new URL(NOTIFICATIONS_LINK);
    } catch (MalformedURLException mue) {
    }
  }

  public static void loadNotifications() {
    try {
      HttpURLConnection urlC = (HttpURLConnection) notificationURL.openConnection();
    } catch (IOException ioe) {
      ioe.printStackTrace();
    }
  }

  public static boolean isNewNotification() {
    return false;
  }

}
