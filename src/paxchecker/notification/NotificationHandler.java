package paxchecker.notification;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import paxchecker.ErrorHandler;
import paxchecker.DataTracker;

/**
 *
 * @author Sunny
 */
public class NotificationHandler {

  private static final String NOTIFICATIONS_LINK = "https://dl.dropboxusercontent.com/u/16152108/PAXCheckerNotifications.txt";
  private static final ArrayList<Notification> notificationList = new ArrayList<>();
  private static String lastNotificationID;

  public static void init() {
    setLastNotificationID("1"); // Change to SettingsHandler later
  }

  public static void setLastNotificationID(String lNID) {
    lastNotificationID = lNID;
  }

  public static void loadNotifications() {
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
      ErrorHandler.showErrorWindow("Error Loading Version Notes", "Unable to load version notes -- an unknown error has occurred.", e);
      e.printStackTrace();
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

  public static ArrayList<Notification> newNotifications() {
    ArrayList<Notification> list = new ArrayList<>();
    for (Notification n : notificationList) {
      if (n.getID().equals(lastNotificationID)) {
        break;
      }
      list.add(n);
    }
    return list;
  }

  public static boolean isNewNotification() {
    return !newNotifications().isEmpty();
  }

  public static void showNewNotifications() {
    if (!isNewNotification()) {
      return;
    }
    ArrayList<Notification> newNotifications = newNotifications();
    for (Notification n : newNotifications) {
      NotificationWindow nW = new NotificationWindow(n);
    }
  }

}
