package com.github.sunnybat.paxchecker.notification;

import com.github.sunnybat.commoncode.error.ErrorBuilder;
import com.github.sunnybat.paxchecker.DataTracker;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

/**
 *
 * @author Sunny
 */
public class NotificationHandler {

	private final String NOTIFICATIONS_LINK = "https://dl.orangedox.com/mNPQJr3JDBfyk3ytaQ/PAXCheckerNotifications.txt?dl=1";
	private final String NOTIFICATIONS_LINK_ANONYMOUS = "https://www.dropbox.com/s/gsy3dfraghhse80/PAXCheckerNotifications.txt?dl=1";
	private final ArrayList<Notification> notificationList = new ArrayList<>();
	private String lastNotificationID = "";
	private boolean anonymousStatistics;
	private boolean isHeadless;

	/**
	 * Creates a new NotificationHandler.
	 *
	 * @param anonymousStatistics True for anonymous statistics, false for usage tracking
	 * @param lastNotificationID The ID of the last notification loaded, or null or "DISABLE" to
	 * disable notifications
	 */
	public NotificationHandler(String lastNotificationID) {
		if (lastNotificationID == null) {
			this.lastNotificationID = "DISABLE";
		} else {
			this.lastNotificationID = lastNotificationID;
		}
	}

	public void setAnonymous() {
		anonymousStatistics = true;
	}

	public void setHeadless() {
		isHeadless = true;
	}

	/**
	 * This loads all new notifications. This method will block until complete.
	 */
	public void loadNotifications() {
		if (lastNotificationID.equals("DISABLE")) {
			return;
		}
		URLConnection inputConnection;
		InputStream textInputStream;
		BufferedReader myReader = null;
		try {
			URL notificationURL;
			if (anonymousStatistics) {
				notificationURL = new URL(NOTIFICATIONS_LINK_ANONYMOUS);
			} else {
				notificationURL = new URL(NOTIFICATIONS_LINK);
			}
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
				} else if (currNotification != null) {
					currNotification.addInfo(line);
				}
			}
			System.out.println("Finished loading notifications.");
		} catch (Exception e) {
			new ErrorBuilder()
					.setError(e)
					.setErrorTitle("Error Loading Notifications")
					.setErrorMessage("Unable to load notifications -- an unknown error has occurred.")
					.buildWindow();
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
	private ArrayList<Notification> newNotifications() {
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
	public boolean isNewNotification() {
		if (lastNotificationID.equals("DISABLE")) {
			return false;
		}
		return !newNotifications().isEmpty();
	}

	/**
	 * Shows all new notifications. This blocks the current Thread until all notifications have been
	 * closed.
	 *
	 * @return The ID of the latest notification shown, or null if there are no new notifications
	 */
	public String showNewNotifications() {
		if (!isNewNotification()) {
			return null;
		}
		boolean first = false;
		ArrayList<Notification> newNotifications = newNotifications();
		CountDownLatch cDL = new CountDownLatch(newNotifications.size());
		for (Notification n : newNotifications) {
			if (!first) {
				lastNotificationID = n.getID();
				first = true;
			}
			if (isHeadless) {
				System.out.println("NOTIFICATION " + n.getID());
				System.out.println(n.getInfo());
				cDL.countDown();
			} else {
				NotificationWindow nW = new NotificationWindow(n, cDL);
				if (n.getButtonLink() != null) {
					nW.setMoreInfoButtonLink(n.getButtonLink());
				}
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
		return lastNotificationID;
	}

}
