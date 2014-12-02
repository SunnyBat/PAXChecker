package paxchecker;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import paxchecker.error.ErrorDisplay;
import paxchecker.browser.Browser;
import paxchecker.tickets.*;
import paxchecker.update.UpdateHandler;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import paxchecker.check.TicketChecker;

/**
 *
 * @author SunnyBat
 */
public class SettingsHandler {

  private static final Preferences myPrefs = Preferences.userRoot().node("paxchecker");
  private static boolean saveRefreshTime;
  private static boolean saveCheckShowclix;
  private static boolean saveCheckPax;
  private static boolean saveCheckTwitter;
  private static boolean savePlayAlarm;
  private static boolean saveEvent;
  private static boolean saveEmail;
  private static boolean saveCellnum;
  private static boolean saveTwitterAPIKeys;

  /**
   * Sets whether or not to save each preference.
   *
   * @param refreshTime True or false
   * @param showclix True or false
   * @param pax True or false
   * @param twitter True or false
   * @param alarm True or false
   * @param event True or false
   * @param email True or false
   * @param cellnum True or false
   */
  public static void setSaveAll(boolean refreshTime, boolean showclix, boolean pax, boolean twitter, boolean alarm, boolean event, boolean email, boolean cellnum) {
    setSaveRefreshTime(refreshTime);
    setSaveShowclix(showclix);
    setSavePax(pax);
    setSaveTwitter(twitter);
    setSaveAlarm(alarm);
    setSaveEvent(event);
    setSaveEmail(email);
    setSaveCellnum(cellnum);
  }

  /**
   * Saves all the preferences. Note that this uses the values currently assigned to the program, so if Setup hasn't been completed yet, some
   * Preferences will likely be incorrect.
   */
  public static void saveAllPrefs() {
    saveAllPrefs(Email.getUsername(), Email.convertToString(Email.getAddressList()), Checker.getRefreshTime(), TicketChecker.isCheckingPaxsite(), TicketChecker.isCheckingShowclix(), TicketChecker.isCheckingTwitter(), Audio.soundEnabled(), Browser.getExpo(), UpdateHandler.getUseBeta(), true, true);
  }

  /**
   * Saves the Preferences given into a Preferences file. The Preferences are located in the user's registry (for Windows). Note that this also saves
   * the cell number given. If you want to save a different cell number, see {@link #saveCellNum(String)}.
   *
   * @param refreshTime The time (in seconds) between refreshes
   * @param checkPax True to check the PAX website, false to not
   * @param checkShowclix True to check the Showclix website, false to not
   * @param playAlarm True to play the alarm, false to not
   * @param expo The Expo being checked for. Note it should be in "PAX XXXX" format.
   * @param useBeta True to use BETA versions, false to not
   */
  public static void saveAllPrefs(String email, String cNum, int refreshTime, boolean checkPax, boolean checkShowclix, boolean checkTwitter, boolean playAlarm, String expo, boolean useBeta, boolean lNotifs, boolean lUpdts) {
    try {
      myPrefs.sync();
    } catch (BackingStoreException bSE) {
      ErrorDisplay.showErrorWindow("Unable to sync Preferences! Preferences will not be saved.");
      return;
    }
    try {
      saveRefreshTime(refreshTime);
      saveCheckPax(checkPax);
      saveCheckShowclix(checkShowclix);
      saveCheckTwitter(checkTwitter);
      savePlayAlarm(playAlarm);
      saveEvent(expo == null ? "" : expo);
      saveCellNum(cNum);
      saveEmail(email);
      saveUseBeta(useBeta);
      saveLoadNotifications(lNotifs);
      saveLoadUpdates(lUpdts);
      myPrefs.flush();
    } catch (BackingStoreException bSE) {
      ErrorDisplay.showErrorWindow("Unable to save Preferences! Preferences might not be saved.");
    }
  }

  /**
   * Sets whether or not to save the refresh time of the program. Note that you still must commit the changes using
   * {@link #saveAllPrefs(int, boolean, boolean, boolean, java.lang.String, java.lang.String) saveAllPrefs()}.
   *
   * @param save True to save refresh time, false to not
   */
  public static void setSaveRefreshTime(boolean save) {
    saveRefreshTime = save;
  }

  /**
   * Sets whether or not to save the Check Showclix option. Note that you still must commit the changes using
   * {@link #saveAllPrefs(int, boolean, boolean, boolean, java.lang.String, java.lang.String) saveAllPrefs()}.
   *
   * @param save True to save refresh time, false to not
   */
  public static void setSaveShowclix(boolean save) {
    saveCheckShowclix = save;
  }

  /**
   * Sets whether or not to save the Check PAX Website option. Note that you still must commit the changes using
   * {@link #saveAllPrefs(int, boolean, boolean, boolean, java.lang.String, java.lang.String) saveAllPrefs()}.
   *
   * @param save True to save check PAX website preference, false to not
   */
  public static void setSavePax(boolean save) {
    saveCheckPax = save;
  }

  /**
   * Sets whether or not to save the Check Twitter option. Note that you still must commit the changes using
   * {@link #saveAllPrefs(int, boolean, boolean, boolean, java.lang.String, java.lang.String) saveAllPrefs()}.
   *
   * @param save True to save check Twitter preference, false to not
   */
  public static void setSaveTwitter(boolean save) {
    saveCheckTwitter = save;
  }

  /**
   * Sets whether or not to save the Play Alarm option. Note that you still must commit the changes using
   * {@link #saveAllPrefs(int, boolean, boolean, boolean, java.lang.String, java.lang.String) saveAllPrefs()}.
   *
   * @param save True to save refresh time, false to not
   */
  public static void setSaveAlarm(boolean save) {
    savePlayAlarm = save;
  }

  /**
   * Sets whether or not to save the event selected. Note that you still must commit the changes using
   * {@link #saveAllPrefs(int, boolean, boolean, boolean, java.lang.String, java.lang.String) saveAllPrefs()}.
   *
   * @param save True to save refresh time, false to not
   */
  public static void setSaveEvent(boolean save) {
    saveEvent = save;
  }

  /**
   * Sets whether or not to save the email address used. Note that you still must commit the changes using
   * {@link #saveAllPrefs(int, boolean, boolean, boolean, java.lang.String, java.lang.String) saveAllPrefs()}.
   *
   * @param save True to save refresh time, false to not
   */
  public static void setSaveEmail(boolean save) {
    saveEmail = save;
  }

  /**
   * Sets whether or not to save the cell number used. Note that you still must commit the changes using
   * {@link #saveAllPrefs(int, boolean, boolean, boolean, java.lang.String, java.lang.String) saveAllPrefs()}.
   *
   * @param save True to save refresh time, false to not
   */
  public static void setSaveCellnum(boolean save) {
    saveCellnum = save;
  }

  /**
   * Sets whether or not to save the Twitter API keys.
   *
   * @param save True to save the API keys, false to not
   */
  public static void setSaveTwitterAPIKeys(boolean save) {
    saveTwitterAPIKeys = save;
  }

  /**
   * Saves the refresh time to the Preferences.
   *
   * @param time The time (in seconds) between refreshes
   */
  private static void saveRefreshTime(int time) {
    if (saveRefreshTime) {
      myPrefs.putInt(PREFTYPES.REFRESHTIME.name(), time);
    } else {
      myPrefs.remove(PREFTYPES.REFRESHTIME.name());
    }
  }

  /**
   * Saves the Check Showclix option to the Preferences.
   *
   * @param check True to check the Showclix website, false to not
   */
  private static void saveCheckShowclix(boolean check) {
    if (saveCheckShowclix) {
      myPrefs.putBoolean(PREFTYPES.CHECK_SHOWCLIX.name(), check);
    } else {
      myPrefs.remove(PREFTYPES.CHECK_SHOWCLIX.name());
    }
  }

  /**
   * Saves the Check PAX Website option to the Preferences.
   *
   * @param check True to check the PAX website, false to not
   */
  private static void saveCheckPax(boolean check) {
    if (saveCheckPax) {
      myPrefs.putBoolean(PREFTYPES.CHECK_PAX.name(), check);
    } else {
      myPrefs.remove(PREFTYPES.CHECK_PAX.name());
    }
  }

  /**
   * Saves the Check Twitter option to the Preferences.
   *
   * @param check True to check Twitter, false to not
   */
  private static void saveCheckTwitter(boolean check) {
    if (saveCheckTwitter) {
      myPrefs.putBoolean(PREFTYPES.CHECK_TWITTER.name(), check);
    } else {
      myPrefs.remove(PREFTYPES.CHECK_TWITTER.name());
    }
  }

  /**
   * Saves the Play Alarm option to the Preferences.
   *
   * @param alarm True to play the alarm, false to not
   */
  private static void savePlayAlarm(boolean alarm) {
    if (savePlayAlarm) {
      myPrefs.putBoolean(PREFTYPES.PLAY_ALARM.name(), alarm);
    } else {
      myPrefs.remove(PREFTYPES.PLAY_ALARM.name());
    }
  }

  /**
   * Saves the Event option to the Preferences.
   *
   * @param expo The Event last chosen
   */
  private static void saveEvent(String expo) {
    if (saveEvent) {
      myPrefs.put(PREFTYPES.EVENT.name(), expo);
    } else {
      myPrefs.remove(PREFTYPES.EVENT.name());
    }
  }

  /**
   * Saves the program's email address used to the Preferences. This uses the email address currently set in Email.java.
   *
   * @see paxchecker.Email#getTextEmail()
   * @see paxchecker.Email#getEmailList()
   */
  private static void saveEmail() {
    if (saveEmail) {
      String email = Email.getUsername();
      if (email == null) {
        myPrefs.put(PREFTYPES.EMAIL.name(), "");
      } else {
        if (email.equals("@yahoo.com")) {
          myPrefs.put(PREFTYPES.EMAIL.name(), "");
        } else {
          myPrefs.put(PREFTYPES.EMAIL.name(), Email.getUsername());
        }
      }
    } else {
      myPrefs.remove(PREFTYPES.EMAIL.name());
    }
  }

  /**
   * Saves the specified email address to the Preferences file.
   *
   * @param email The email address to save to the Preferences file
   */
  public static void saveEmail(String email) {
    if (saveEmail) {
      if (email != null) {
        myPrefs.put(PREFTYPES.EMAIL.name(), email);
      } else {
        myPrefs.put(PREFTYPES.EMAIL.name(), "");
      }
    } else {
      myPrefs.remove(PREFTYPES.EMAIL.name());
    }
  }

  /**
   * Saves the specified cell number (address) to the Preferences file.
   *
   * @param cellNum The cell number to save to the Preferences file
   */
  public static void saveCellNum(String cellNum) {
    if (saveCellnum) {
      if (cellNum != null) {
        myPrefs.put(PREFTYPES.CELLNUM.name(), cellNum);
      } else {
        myPrefs.put(PREFTYPES.CELLNUM.name(), "");
      }
    } else {
      myPrefs.remove(PREFTYPES.CELLNUM.name());
    }
  }

  /**
   * Saves the given expo's last event link.
   *
   * @param expo The expo for which this link applies
   * @param link The link to save
   */
  public static void saveLastEvent(String expo, String link) {
    myPrefs.put(PREFTYPES.LAST_EVENT.name() + "_" + expo.toUpperCase(), link);
    try {
      myPrefs.sync();
    } catch (BackingStoreException bSE) {
      bSE.printStackTrace();
    }
  }

  /**
   * Saves the given link for the currently set expo.
   *
   * @param link The link to save
   */
  public static void saveLastEvent(String link) {
    saveLastEvent(Browser.getExpo(), link);
  }

  /**
   * Saves whether or not to use BETA versions.
   *
   * @param use BETA version preference
   */
  public static void saveUseBeta(boolean use) {
    myPrefs.putBoolean(PREFTYPES.USE_BETA.name(), use);
  }

  /**
   * Saves the last Notification ID found
   *
   * @param ID The last Notification ID found
   */
  public static void saveLastNotificationID(String ID) {
    myPrefs.put(PREFTYPES.LAST_NOTIFICATION_ID.name(), ID);
  }

  /**
   * Saves whether or not to load notifications.
   *
   * @param use Load notifications preference
   */
  public static void saveLoadNotifications(boolean use) {
    myPrefs.putBoolean(PREFTYPES.LOAD_NOTIFICATIONS.name(), use);
  }

  /**
   * Saves whether or not to load program updates.
   *
   * @param use Load updates preference
   */
  public static void saveLoadUpdates(boolean use) {
    myPrefs.putBoolean(PREFTYPES.LOAD_UPDATES.name(), use);
  }

  /**
   * Saves the Twitter API keys. Note that this requires a String[] of length 4.
   *
   * @param keys The Twitter API keys to save
   */
  public static void saveTwitterKeys(String[] keys) {
    if (!saveTwitterAPIKeys) {
      System.out.println("NOT saving API keys.");
      myPrefs.remove(PREFTYPES.TWITTER_CONSUMER_KEY.name());
      myPrefs.remove(PREFTYPES.TWITTER_CONSUMER_SECRET.name());
      myPrefs.remove(PREFTYPES.TWITTER_APP_KEY.name());
      myPrefs.remove(PREFTYPES.TWITTER_APP_SECRET.name());
      return;
    }
    if (keys.length != 4) {
      System.out.println("ERROR saving Twitter API keys: Length is " + keys.length + ", not 4!");
      return;
    }
    try {
      System.out.println("Save: " + Arrays.toString(keys));
      myPrefs.put(Encryption.encrypt(PREFTYPES.TWITTER_CONSUMER_KEY.name()), keys[0]);
      myPrefs.put(Encryption.encrypt(PREFTYPES.TWITTER_CONSUMER_SECRET.name()), keys[1]);
      myPrefs.put(Encryption.encrypt(PREFTYPES.TWITTER_APP_KEY.name()), keys[2]);
      myPrefs.put(Encryption.encrypt(PREFTYPES.TWITTER_APP_SECRET.name()), keys[3]);
    } catch (NullPointerException npe) {
      System.out.println("ERROR saving Twitter API keys: A key is null!");
    } catch (GeneralSecurityException | UnsupportedEncodingException ex) {
      System.out.println("ERROR encrypting Twitter API keys!");
    }
  }

  /**
   * Checks whether or not the program saved the Refresh Time preference
   *
   * @return True if preference was saved, false if not
   */
  public static boolean getSaveRefreshTime() {
    return myPrefs.getInt(PREFTYPES.REFRESHTIME.name(), 61) != 61;
  }

  /**
   * Checks whether or not the program saved the Check Showclix Website preference
   *
   * @return True if preference was saved, false if not
   */
  public static boolean getSaveShowclix() {
    return !(!myPrefs.getBoolean(PREFTYPES.CHECK_SHOWCLIX.name(), false) && myPrefs.getBoolean(PREFTYPES.CHECK_SHOWCLIX.name(), true));
  }

  /**
   * Checks whether or not the program saved the Check PAX Website preference
   *
   * @return True if preference was saved, false if not
   */
  public static boolean getSavePax() {
    return !(!myPrefs.getBoolean(PREFTYPES.CHECK_PAX.name(), false) && myPrefs.getBoolean(PREFTYPES.CHECK_PAX.name(), true));
  }

  /**
   * Checks whether or not the program saved the Twitter preference
   *
   * @return True if preference was saved, false if not
   */
  public static boolean getSaveTwitter() {
    return !(!myPrefs.getBoolean(PREFTYPES.CHECK_TWITTER.name(), false) && myPrefs.getBoolean(PREFTYPES.CHECK_TWITTER.name(), true));
  }

  /**
   * Checks whether or not the program saved the Play Alarm preference
   *
   * @return True if preference was saved, false if not
   */
  public static boolean getSaveAlarm() {
    return !(!myPrefs.getBoolean(PREFTYPES.PLAY_ALARM.name(), false) && myPrefs.getBoolean(PREFTYPES.PLAY_ALARM.name(), true));
  }

  /**
   * Checks whether or not the program saved the Event preference
   *
   * @return True if preference was saved, false if not
   */
  public static boolean getSaveEvent() {
    return !myPrefs.get(PREFTYPES.EVENT.name(), "NOPE").equals("NOPE");
  }

  /**
   * Checks whether or not the program saved the Email preference
   *
   * @return True if preference was saved, false if not
   */
  public static boolean getSaveEmail() {
    return !myPrefs.get(PREFTYPES.EMAIL.name(), "NOPE").equals("NOPE");
  }

  /**
   * Checks whether or not the program saved the Cell Number preference
   *
   * @return True if preference was saved, false if not
   */
  public static boolean getSaveCellnum() {
    return !myPrefs.get(PREFTYPES.CELLNUM.name(), "NOPE").equals("NOPE");
  }

  /**
   * Checks whether or not the program saved the preferences
   *
   * @return True if preference was saved, false if not
   */
  public static boolean getSavePrefs() {
    return myPrefs.getBoolean(PREFTYPES.SAVE_PREFS.name(), true);
  }

  /**
   * Checks whether or not to save the Twitter API keys.
   *
   * @return True to save them, false to not
   */
  public static boolean getSaveTwitterKeys() {
    return !myPrefs.get(PREFTYPES.TWITTER_CONSUMER_KEY.name(), "NOPE").equals("NOPE");
  }

  /**
   * Gets the currently saved refresh time of the program, or 15 if preference was not saved.
   *
   * @return The delay time saved, or 15 if not saved
   */
  public static int getDelayTime() {
    return myPrefs.getInt(PREFTYPES.REFRESHTIME.name(), 15);
  }

  /**
   * Gets whether the program should check the PAX Website, or true if preference was not saved.
   *
   * @return True if the program should check the PAX website, true if not
   */
  public static boolean getCheckPaxWebsite() {
    return myPrefs.getBoolean(PREFTYPES.CHECK_PAX.name(), true);
  }

  /**
   * Gets whether the program should check Twitter, or false if preference was not saved.
   *
   * @return True if the program should check Twitter, false if not
   */
  public static boolean getCheckTwitter() {
    return myPrefs.getBoolean(PREFTYPES.CHECK_TWITTER.name(), false);
  }

  /**
   * Gets whether the program should check the Showclix website, or true if preference was not saved.
   *
   * @return True if the program should check the Showclix website, false if not
   */
  public static boolean getCheckShowclix() {
    return myPrefs.getBoolean(PREFTYPES.CHECK_SHOWCLIX.name(), true);
  }

  /**
   * Gets whether the program should play the audio alarm, or false if preference was not saved.
   *
   * @return True if the program should play the alarm, false if not
   */
  public static boolean getPlayAlarm() {
    return myPrefs.getBoolean(PREFTYPES.PLAY_ALARM.name(), false);
  }

  /**
   * Gets the expo selected last, or Prime if preference was not saved.
   *
   * @return The expo last selected
   */
  public static String getExpo() {
    return myPrefs.get(PREFTYPES.EVENT.name(), "Prime");
  }

  /**
   * Gets the email address last used, or a blank String if preference was not saved.
   *
   * @return The email address last used
   */
  public static String getEmail() {
    return myPrefs.get(PREFTYPES.EMAIL.name(), "");
  }

  /**
   * Gets the cell number last used, or a blank String if preference was not saved. This does NOT return the carrier extension if the provider saved
   * is the same as the extension.
   *
   * @return The cell number last used
   */
  public static String getCellNumber() {
    return myPrefs.get(PREFTYPES.CELLNUM.name(), "");
  }

  /**
   * Gets the last Showclix link found for a specific expo. Note that this returns in the following format: https://www.showclix.com/Event/EVENTID
   *
   * @param expo The Expo to check for
   * @return The last Showclix link for the specific expo
   */
  public static String getLastEvent(String expo) {
    return myPrefs.get(PREFTYPES.LAST_EVENT.name() + "_" + expo.toUpperCase(), TicketChecker.getLinkFound()); // UPDATE
  }

  /**
   * Gets whether or not to use BETA versions.
   *
   * @return True to use BETA versions, false to not
   */
  public static boolean getUseBetaVersion() {
    return myPrefs.getBoolean(PREFTYPES.USE_BETA.name(), false);
  }

  /**
   * Gets the last Notification ID found by the program.
   *
   * @return The last Notification ID found
   */
  public static String getLastNotificationID() {
    return myPrefs.get(PREFTYPES.LAST_NOTIFICATION_ID.name(), "");
  }

  /**
   * Gets whether or not to load notifications
   *
   * @return True to load notifications, false to not
   */
  public static boolean getLoadNotifications() {
    return myPrefs.getBoolean(PREFTYPES.LOAD_NOTIFICATIONS.name(), true);
  }

  /**
   * Gets whether or not to load program updates
   *
   * @return True to load updates, false to not
   */
  public static boolean getLoadUpdates() {
    return myPrefs.getBoolean(PREFTYPES.LOAD_UPDATES.name(), true);
  }

  /**
   * Gets the saved Twitter API keys. The String[] returned is guaranteed to be size 4.
   *
   * @return The Twitter API keys, or null for each if not saved.
   */
  public static String[] getTwitterKeys() {
    String[] keys = new String[4];
    try {
      keys[0] = Encryption.decrypt(myPrefs.get(PREFTYPES.TWITTER_CONSUMER_KEY.name(), null));
      keys[1] = Encryption.decrypt(myPrefs.get(PREFTYPES.TWITTER_CONSUMER_SECRET.name(), null));
      keys[2] = Encryption.decrypt(myPrefs.get(PREFTYPES.TWITTER_APP_KEY.name(), null));
      keys[3] = Encryption.decrypt(myPrefs.get(PREFTYPES.TWITTER_APP_SECRET.name(), null));
    } catch (GeneralSecurityException | IOException gse) {
      System.out.println("ERROR decrypting Twitter API keys!");
    }
    System.out.println("Load: " + Arrays.toString(keys));
    return keys;
  }

  private static enum PREFTYPES {

    REFRESHTIME, CHECK_SHOWCLIX, CHECK_PAX, CHECK_TWITTER, PLAY_ALARM, EVENT, EMAIL, CELLNUM,
    SAVE_PREFS, LAST_EVENT, USE_BETA, LAST_NOTIFICATION_ID, LOAD_NOTIFICATIONS, LOAD_UPDATES,
    TWITTER_CONSUMER_KEY, TWITTER_CONSUMER_SECRET, TWITTER_APP_KEY, TWITTER_APP_SECRET;
  }

  /**
   * Sets whether or not the program should save the preferences. Note that if this is set to false, ALL of the preferences will simply be set to
   * false. Further changes to preferences could result in preferences being saved.
   *
   * @param save True to save preferences, false to not
   */
  public static void setSavePrefs(boolean save) {
    myPrefs.putBoolean(PREFTYPES.SAVE_PREFS.name(), save);
    if (!save) {
      setSaveAll(false, false, false, false, false, false, false, false);
    }
  }
}
