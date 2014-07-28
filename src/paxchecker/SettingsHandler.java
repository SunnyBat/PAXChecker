package paxchecker;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 *
 * @author SunnyBat
 */
public class SettingsHandler {

  private static final Preferences myPrefs = Preferences.userRoot();
  private static boolean saveRefreshTime;
  private static boolean saveCheckShowclix;
  private static boolean saveCheckPax;
  private static boolean savePlayAlarm;
  private static boolean saveEvent;
  private static boolean saveEmail;
  private static boolean saveCellnum;
  private static boolean saveProvider;
  private static String lastShowclixLink;

  public static void setSaveAll(boolean refreshTime, boolean showclix, boolean pax, boolean alarm, boolean event, boolean email, boolean cellnum, boolean provider) {
    setSaveRefreshTime(refreshTime);
    setSaveShowclix(showclix);
    setSavePax(pax);
    setSaveAlarm(alarm);
    setSaveEvent(event);
    setSaveEmail(email);
    setSaveCellnum(cellnum);
    setSaveProvider(provider);
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
   * @param save True to save refresh time, false to not
   */
  public static void setSavePax(boolean save) {
    saveCheckPax = save;
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
   * Sets whether or not to save the cell provider used. Note that you still must commit the changes using
   * {@link #saveAllPrefs(int, boolean, boolean, boolean, java.lang.String, java.lang.String) saveAllPrefs()}.
   *
   * @param save True to save refresh time, false to not
   */
  public static void setSaveProvider(boolean save) {
    saveProvider = save;
  }

  /**
   * Saves all the preferences. Note that this uses the values currently assigned to the program, so if Setup hasn't been completed yet, some
   * Preferences will likely be wrong.
   */
  public static void saveAllPrefs() {
    try {
      myPrefs.sync();
    } catch (BackingStoreException backingStoreException) {
      ErrorHandler.showErrorWindow("Unable to sync Preferences! Preferences will not be saved.");
      return;
    }
    try {
      //saveRefreshTime(PAXChecker.);
      saveCheckPax(Browser.isCheckingPaxWebsite());
      saveCheckShowclix(Browser.isCheckingShowclix());
      savePlayAlarm(Audio.playAlarm());
      saveEvent(Browser.getExpo() == null ? "" : Browser.getExpo());
      saveProvider(Email.getProvider() == null ? "" : Email.getProvider());
      System.out.println("Save provider = " + Email.getProvider());
      saveCellNum();
      saveEmail();
      myPrefs.flush();
    } catch (BackingStoreException backingStoreException) {
      System.out.println("Unable to save settings!");
    }
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
   * @param provider The provider being used.
   */
  public static void saveAllPrefs(int refreshTime, boolean checkPax, boolean checkShowclix, boolean playAlarm, String expo, String provider) {
    try {
      myPrefs.sync();
    } catch (BackingStoreException backingStoreException) {
      ErrorHandler.showErrorWindow("Unable to sync Preferences! Preferences will not be saved.");
      return;
    }
    try {
      saveRefreshTime(refreshTime);
      saveCheckPax(checkPax);
      saveCheckShowclix(checkShowclix);
      savePlayAlarm(playAlarm);
      saveEvent(expo == null ? "" : expo);
      saveProvider(provider == null ? "" : provider);
      System.out.println("Save provider = " + provider);
      saveCellNum();
      saveEmail();
      System.out.println("Pax = " + checkPax + ", Showclix = " + checkShowclix + ", playAlarm = " + playAlarm + ", Expo = " + expo + ", Provider = " + provider);
      myPrefs.flush();
    } catch (BackingStoreException backingStoreException) {
      System.out.println("Unable to save settings!");
    }
  }

  /**
   * Saves the refresh time to the Preferences.
   *
   * @param time The time (in seconds) between refreshes
   */
  private static void saveRefreshTime(int time) {
    if (saveRefreshTime) {
      myPrefs.putInt(PREFTYPES.PAXCHECK_REFRESHTIME.name(), time);
    } else {
      myPrefs.remove(PREFTYPES.PAXCHECK_REFRESHTIME.name());
    }
  }

  private static void saveCheckShowclix(boolean check) {
    if (saveCheckShowclix) {
      myPrefs.putBoolean(PREFTYPES.PAXCHECK_CHECK_SHOWCLIX.name(), check);
    } else {
      myPrefs.remove(PREFTYPES.PAXCHECK_CHECK_SHOWCLIX.name());
    }
  }

  private static void saveCheckPax(boolean check) {
    if (saveCheckPax) {
      myPrefs.putBoolean(PREFTYPES.PAXCHECK_CHECK_PAX.name(), check);
    } else {
      myPrefs.remove(PREFTYPES.PAXCHECK_CHECK_PAX.name());
    }
  }

  private static void savePlayAlarm(boolean alarm) {
    if (savePlayAlarm) {
      myPrefs.putBoolean(PREFTYPES.PAXCHECK_PLAY_ALARM.name(), alarm);
    } else {
      myPrefs.remove(PREFTYPES.PAXCHECK_PLAY_ALARM.name());
    }
  }

  private static void saveEvent(String expo) {
    if (saveEvent) {
      myPrefs.put(PREFTYPES.PAXCHECK_EVENT.name(), expo);
    } else {
      myPrefs.remove(PREFTYPES.PAXCHECK_EVENT.name());
    }
  }

  private static void saveProvider(String provider) {
    if (saveProvider) {
      myPrefs.put(PREFTYPES.PAXCHECK_PROVIDER.name(), provider);
    } else {
      myPrefs.remove(PREFTYPES.PAXCHECK_PROVIDER.name());
    }
  }

  private static void saveEmail() {
    if (saveEmail) {
      String email = Email.getUsername();
      if (email == null) {
        myPrefs.put(PREFTYPES.PAXCHECK_EMAIL.name(), "");
      } else {
        if (email.equals("@yahoo.com")) {
          myPrefs.put(PREFTYPES.PAXCHECK_EMAIL.name(), "");
        } else {
          myPrefs.put(PREFTYPES.PAXCHECK_EMAIL.name(), Email.getUsername());
        }
      }
    } else {
      myPrefs.remove(PREFTYPES.PAXCHECK_EMAIL.name());
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
        myPrefs.put(PREFTYPES.PAXCHECK_EMAIL.name(), email);
      } else {
        myPrefs.put(PREFTYPES.PAXCHECK_EMAIL.name(), "");
      }
    } else {
      myPrefs.remove(PREFTYPES.PAXCHECK_EMAIL.name());
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
        myPrefs.put(PREFTYPES.PAXCHECK_CELLNUM.name(), cellNum);
      } else {
        myPrefs.put(PREFTYPES.PAXCHECK_CELLNUM.name(), "");
      }
    } else {
      myPrefs.remove(PREFTYPES.PAXCHECK_CELLNUM.name());
    }
  }

  /**
   * Saves the current cell number (address) to the Preferences file. Note that this gets the email from {@link paxchecker.Email#getTextEmail()} or
   * {@link paxchecker.Email#getEmailList()}.
   */
  private static void saveCellNum() {
    if (saveCellnum) {
      String textEmail = Email.getTextEmail();
      java.util.List textList = Email.getEmailList();
      if (textEmail != null) {
        myPrefs.put(PREFTYPES.PAXCHECK_CELLNUM.name(), textEmail.equals("@yahoo.com") ? "" : textEmail);
      } else if (textList != null) {
        if (textList.isEmpty()) {
          myPrefs.put(PREFTYPES.PAXCHECK_CELLNUM.name(), "");
          return;
        }
        String longEmail = null;
        java.util.Iterator<String> mI = textList.iterator();
        while (mI.hasNext()) {
          String s = mI.next();
          if (s == null) {
            continue;
          }
          if (longEmail == null) {
            longEmail = s;
          } else {
            longEmail += "; " + s;
          }
        }
        myPrefs.put(PREFTYPES.PAXCHECK_CELLNUM.name(), longEmail == null ? "" : longEmail);
      } else {
        myPrefs.put(PREFTYPES.PAXCHECK_CELLNUM.name(), "");
      }
    } else {
      myPrefs.remove(PREFTYPES.PAXCHECK_CELLNUM.name());
    }
  }

  /**
   * Checks whether or not the program saved the Refresh Time preference
   *
   * @return True if preference was saved, false if not
   */
  public static boolean getSaveRefreshTime() {
    return myPrefs.getInt(PREFTYPES.PAXCHECK_REFRESHTIME.name(), 61) != 61;
  }

  /**
   * Checks whether or not the program saved the Check Showclix Website preference
   *
   * @return True if preference was saved, false if not
   */
  public static boolean getSaveShowclix() {
    return !(!myPrefs.getBoolean(PREFTYPES.PAXCHECK_CHECK_SHOWCLIX.name(), false) && myPrefs.getBoolean(PREFTYPES.PAXCHECK_CHECK_SHOWCLIX.name(), true));
  }

  /**
   * Checks whether or not the program saved the Check PAX Website preference
   *
   * @return True if preference was saved, false if not
   */
  public static boolean getSavePax() {
    return !(!myPrefs.getBoolean(PREFTYPES.PAXCHECK_CHECK_PAX.name(), false) && myPrefs.getBoolean(PREFTYPES.PAXCHECK_CHECK_PAX.name(), true));
  }

  /**
   * Checks whether or not the program saved the Play Alarm preference
   *
   * @return True if preference was saved, false if not
   */
  public static boolean getSaveAlarm() {
    return !(!myPrefs.getBoolean(PREFTYPES.PAXCHECK_PLAY_ALARM.name(), false) && myPrefs.getBoolean(PREFTYPES.PAXCHECK_PLAY_ALARM.name(), true));
  }

  /**
   * Checks whether or not the program saved the Event preference
   *
   * @return True if preference was saved, false if not
   */
  public static boolean getSaveEvent() {
    return !myPrefs.get(PREFTYPES.PAXCHECK_EVENT.name(), "NOPE").equals("NOPE");
  }

  /**
   * Checks whether or not the program saved the Email preference
   *
   * @return True if preference was saved, false if not
   */
  public static boolean getSaveEmail() {
    return !myPrefs.get(PREFTYPES.PAXCHECK_EMAIL.name(), "NOPE").equals("NOPE");
  }

  /**
   * Checks whether or not the program saved the Cell Number preference
   *
   * @return True if preference was saved, false if not
   */
  public static boolean getSaveCellnum() {
    return !myPrefs.get(PREFTYPES.PAXCHECK_CELLNUM.name(), "NOPE").equals("NOPE");
  }

  /**
   * Checks whether or not the program saved the Cell Provider preference
   *
   * @return True if preference was saved, false if not
   */
  public static boolean getSaveProvider() {
    return !myPrefs.get(PREFTYPES.PAXCHECK_PROVIDER.name(), "NOPE").equals("NOPE");
  }

  /**
   * Checks whether or not the program saved the preferences
   *
   * @return True if preference was saved, false if not
   */
  public static boolean getSavePrefs() {
    return myPrefs.getBoolean(PREFTYPES.PAXCHECK_SAVE_PREFS.name(), true);
  }

  /**
   * Gets the currently saved refresh time of the program, or 15 if preference was not saved.
   *
   * @return The delay time saved, or 15 if not saved
   */
  public static int getDelayTime() {
    return myPrefs.getInt(PREFTYPES.PAXCHECK_REFRESHTIME.name(), 15);
  }

  /**
   * Gets whether the program should check the PAX Website, or true if preference was not saved.
   *
   * @return True if the program should check the PAX website, false if not
   */
  public static boolean getCheckPaxWebsite() {
    return myPrefs.getBoolean(PREFTYPES.PAXCHECK_CHECK_PAX.name(), true);
  }

  /**
   * Gets whether the program should check the Showclix website, or true if prefrence was not saved.
   *
   * @return True if the program should check the Showclix website, false if not
   */
  public static boolean getCheckShowclix() {
    return myPrefs.getBoolean(PREFTYPES.PAXCHECK_CHECK_SHOWCLIX.name(), true);
  }

  public static boolean getPlayAlarm() {
    return myPrefs.getBoolean(PREFTYPES.PAXCHECK_PLAY_ALARM.name(), false);
  }

  public static String getExpo() {
    return myPrefs.get(PREFTYPES.PAXCHECK_EVENT.name(), "Prime");
  }

  public static String getEmail() {
    return myPrefs.get(PREFTYPES.PAXCHECK_EMAIL.name(), "");
  }

  public static String getCellNumber() {
    String cellNum = myPrefs.get(PREFTYPES.PAXCHECK_CELLNUM.name(), "");
    if (cellNum.contains("@") && !cellNum.contains(";")) {
      if (Email.getCarrierExtension(getProvider()).equals(cellNum.substring(cellNum.indexOf("@")))) {
        cellNum = cellNum.substring(0, cellNum.indexOf("@"));
      }
    }
    return cellNum;
  }

  public static String getProvider() {
    return myPrefs.get(PREFTYPES.PAXCHECK_PROVIDER.name(), "AT&T");
  }

  public static boolean prefsExist() {
    if (myPrefs.get(PREFTYPES.PAXCHECK_EMAIL.name(), "NONE!!!").equals("NONE!!!")) {
    }
    return true;
  }

  private static enum PREFTYPES {

    PAXCHECK_REFRESHTIME, PAXCHECK_CHECK_SHOWCLIX, PAXCHECK_CHECK_PAX, PAXCHECK_PLAY_ALARM, PAXCHECK_EVENT, PAXCHECK_EMAIL, PAXCHECK_CELLNUM, PAXCHECK_PROVIDER, PAXCHECK_SAVE_PREFS;
  }

  public static void setSavePrefs(boolean save) {
    myPrefs.putBoolean(PREFTYPES.PAXCHECK_SAVE_PREFS.name(), save);
    if (!save) {
      setSaveAll(false, false, false, false, false, false, false, false);
    }
  }

  public static String getPrefsPath() {
    return myPrefs.absolutePath();
  }
}
