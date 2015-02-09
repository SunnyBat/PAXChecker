package paxchecker.notification;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import paxchecker.DataTracker;
import paxchecker.preferences.Preference;
import paxchecker.preferences.PreferenceHandler;
import com.github.sunnybat.commoncode.error.*;

/**
 *
 * @author Sunny
 */
public class NotificationHandler {

  private static final String NOTIFICATIONS_LINK = "https://dl.dropboxusercontent.com/u/16152108/PAXCheckerNotifications.txt";
  private static final ArrayList<Notification> notificationList = new ArrayList<>();
  private static String lastNotificationID = "";

  /**
   * Init.
   */
  public static void init() {
    setLastNotificationID(PreferenceHandler.getStringPreference(Preference.TYPES.LAST_NOTIFICATION_ID));
  }

  /**
   * Sets the last Notification ID loaded. Set to DISABLE to disable notifications.
   *
   * @param lNID The last Notification ID loaded
   */
  public static void setLastNotificationID(String lNID) {
    if (lNID == null) {
      return;
    }
    lastNotificationID = lNID;
  }

  /**
   * This loads all new notifications. This method will block until complete.
   */
  public static void loadNotifications() {
    if (lastNotificationID != null && lastNotificationID.equals("DISABLE")) {
      return;
    }
    URLConnection inputConnection;
    InputStream textInputStream;
    BufferedReader myReader = null;
    try {
      URL notificationURL = new URL(NOTIFICATIONS_LINK);
      inputConnection = notificationURL.openConnection();
      textInputStream = inputConnection.getInputStream();
      myReader = new BufferedReader(new InputStreamReader(textInputStream));
      String line;
      Notification currNotification = null;
      while ((line = myReader.readLine()) != null) {
        DataTracker.addDataUsed(line.length());
        line = line.trim();
        if (line.startsWith("~~~")) {
          currNotification = new Notification(line.substring(3, line.lastIndexOf("~~~")));
          notificationList.add(currNotification);
          System.out.println("Notification " + currNotification.getID());
        } else if (line.startsWith("TOKEN:")) {
          try {
            String d = line.substring(6);
            if (d.startsWith("TITLE:")) {
              currNotification.setTitle(d.substring(6));
            } else if (d.startsWith("INFO:")) {
              currNotification.setInfo(d.substring(5));
            } else if (d.startsWith("BUTTON:")) {
              currNotification.setButtonText(d.substring(7));
            } else if (d.startsWith("MOREINFO:")) {
              currNotification.setButtonLink(d.substring(9));
            } else {
              System.out.println("Unknown token: " + d);
            }
          } catch (NumberFormatException numberFormatException) {
            System.out.println("Unable to set token: " + line);
          }
        } else {
          if (currNotification != null) {
            currNotification.addInfo(line);
          }
        }
      }
      System.out.println("Finished loading notifications.");
    } catch (Exception e) {
      ErrorDisplay.showErrorWindow("Error Loading Notifications", "Unable to load notifications -- an unknown error has occurred.", e);
    } finally {
      try {
        if (myReader != null) {
          myReader.close();
        }
      } catch (IOException e) {
        // nothing to see here
      }
    }
  }

  /**
   * Gets the currently loaded list of new notifications.
   *
   * @return An ArrayList with all new notifications
   */
  private static ArrayList<Notification> newNotifications() {
    ArrayList<Notification> list = new ArrayList<>();
    for (Notification n : notificationList) {
      if (n.getID().equals(lastNotificationID)) {
        break;
      }
      list.add(n);
    }
    return list;
  }

  /**
   * Checks whether or not there are new notifications available.
   *
   * @return True for new notifications, false if none
   */
  public static boolean isNewNotification() {
    if (lastNotificationID.equals("DISABLE")) {
      return false;
    }
    return !newNotifications().isEmpty();
  }

  /**
   * Shows all new notifications. This blocks the current Thread until all notifications have been closed.
   */
  public static void showNewNotifications() {
    if (!isNewNotification()) {
      return;
    }
    boolean first = false;
    ArrayList<Notification> newNotifications = newNotifications();
    CountDownLatch cDL = new CountDownLatch(newNotifications.size());
    for (Notification n : newNotifications) {
      if (!first) {
        PreferenceHandler.getPreferenceObject(Preference.TYPES.LAST_NOTIFICATION_ID).setValue(n.getID());
        first = true;
      }
      if (paxchecker.PAXChecker.isCommandLine()) {
        System.out.println("NOTIFICATION " + n.getID());
        System.out.println(n.getInfo());
        cDL.countDown();
      } else {
        NotificationWindow nW = new NotificationWindow(n, cDL);
        nW.setMoreInfoButtonLink(n.getButtonLink());
        if (n.getButtonText() != null) {
          nW.setCloseButtonText(n.getButtonText());
        }
      }
    }
    newNotifications.clear();
    try {
      cDL.await();
    } catch (InterruptedException interruptedException) {
    }
  }

}
