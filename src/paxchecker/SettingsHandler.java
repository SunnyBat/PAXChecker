package paxchecker;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 *
 * @author SunnyBat
 */
public class SettingsHandler {

  private static Preferences myPrefs = Preferences.userRoot();
  private static boolean saveRefreshTime;
  private static boolean saveCheckShowclix;
  private static boolean saveCheckPax;
  private static boolean savePlayAlarm;
  private static boolean saveEvent;
  private static boolean saveEmail;
  private static boolean saveCellnum;
  private static boolean saveProvider;

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

  public static void setSaveRefreshTime(boolean save) {
    saveRefreshTime = save;
  }

  public static void setSaveShowclix(boolean save) {
    saveCheckShowclix = save;
  }

  public static void setSavePax(boolean save) {
    saveCheckPax = save;
  }

  public static void setSaveAlarm(boolean save) {
    savePlayAlarm = save;
  }

  public static void setSaveEvent(boolean save) {
    saveEvent = save;
  }

  public static void setSaveEmail(boolean save) {
    saveEmail = save;
  }

  public static void setSaveCellnum(boolean save) {
    saveCellnum = save;
  }

  public static void setSaveProvider(boolean save) {
    saveProvider = save;
  }

  /**
   * Saves the Preferences given into a Preferences file. I'm not really sure where the file is, nor
   * am I concerned about it. Note that this also saves the cell number given. If you want to save a
   * different cell number, see {@link #saveCellNum(String)}.
   *
   * @param refreshTime   The time (in seconds) between refreshes
   * @param checkPax      True to check the PAX website, false to not
   * @param checkShowclix True to check the Showclix website, false to not
   * @param playAlarm     True to play the alarm, false to not
   * @param expo          The Expo being checked for. Note it should be in "PAX XXXX" format.
   * @param email         The email address being used.
   * @param provider      The provider being used.
   */
  public static void saveAllPrefs(int refreshTime, boolean checkPax, boolean checkShowclix, boolean playAlarm, String expo, String provider) {
    try {
      myPrefs.sync();
    } catch (BackingStoreException backingStoreException) {
      ErrorManagement.showErrorWindow("Unable to sync Preferences! Preferences will not be saved.");
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

  private static void saveRefreshTime(int time) {
    if (saveRefreshTime) {
      myPrefs.putInt(PREFTYPES.REFRESHTIME.name(), time);
    } else {
      myPrefs.remove(PREFTYPES.REFRESHTIME.name());
    }
  }

  private static void saveCheckShowclix(boolean check) {
    if (saveCheckShowclix) {
      myPrefs.putBoolean(PREFTYPES.CHECK_SHOWCLIX.name(), check);
    } else {
      myPrefs.remove(PREFTYPES.CHECK_SHOWCLIX.name());
    }
  }

  private static void saveCheckPax(boolean check) {
    if (saveCheckPax) {
      myPrefs.putBoolean(PREFTYPES.CHECK_PAX.name(), check);
    } else {
      myPrefs.remove(PREFTYPES.CHECK_PAX.name());
    }
  }

  private static void savePlayAlarm(boolean alarm) {
    if (savePlayAlarm) {
      myPrefs.putBoolean(PREFTYPES.PLAY_ALARM.name(), alarm);
    } else {
      myPrefs.remove(PREFTYPES.PLAY_ALARM.name());
    }
  }

  private static void saveEvent(String expo) {
    if (saveEvent) {
      myPrefs.put(PREFTYPES.EVENT.name(), expo);
    } else {
      myPrefs.remove(PREFTYPES.EVENT.name());
    }
  }

  private static void saveProvider(String provider) {
    if (saveProvider) {
      myPrefs.put(PREFTYPES.PROVIDER.name(), provider);
    } else {
      myPrefs.remove(PREFTYPES.PROVIDER.name());
    }
  }

  private static void saveEmail() {
    if (saveEmail) {
      String email = Email.getUsername();
      if (email == null) {
        myPrefs.put(PREFTYPES.EMAIL.name(), "");
      } else {
        if (email.equals("@yahoo.com")) {
          myPrefs.put(PREFTYPES.EMAIL.name(), "");
        } else {
          myPrefs.put(PREFTYPES.EMAIL.name(), "");
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

  private static void saveCellNum() {
    if (saveCellnum) {
      String textEmail = Email.getTextEmail();
      java.util.List textList = Email.getEmailList();
      if (textEmail != null) {
        myPrefs.put(PREFTYPES.CELLNUM.name(), textEmail.equals("@yahoo.com") ? "" : textEmail);
      } else if (textList != null) {
        if (textList.isEmpty()) {
          myPrefs.put(PREFTYPES.CELLNUM.name(), "");
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
        myPrefs.put(PREFTYPES.CELLNUM.name(), longEmail == null ? "" : longEmail);
      } else {
        myPrefs.put(PREFTYPES.CELLNUM.name(), "");
      }
    } else {
      myPrefs.remove(PREFTYPES.CELLNUM.name());
    }
  }

  public static boolean getSaveRefreshTime() {
    return myPrefs.getInt(PREFTYPES.REFRESHTIME.name(), 61) != 61;
  }

  public static boolean getSaveShowclix() {
    return !(!myPrefs.getBoolean(PREFTYPES.CHECK_SHOWCLIX.name(), false) && myPrefs.getBoolean(PREFTYPES.CHECK_SHOWCLIX.name(), true));
  }

  public static boolean getSavePax() {
    return !(!myPrefs.getBoolean(PREFTYPES.CHECK_PAX.name(), false) && myPrefs.getBoolean(PREFTYPES.CHECK_PAX.name(), true));
  }

  public static boolean getSaveAlarm() {
    return !(!myPrefs.getBoolean(PREFTYPES.PLAY_ALARM.name(), false) && myPrefs.getBoolean(PREFTYPES.PLAY_ALARM.name(), true));
  }

  public static boolean getSaveEvent() {
    return !myPrefs.get(PREFTYPES.EVENT.name(), "NOPE").toString().equals("NOPE");
  }

  public static boolean getSaveEmail() {
    return !myPrefs.get(PREFTYPES.EMAIL.name(), "NOPE").toString().equals("NOPE");
  }

  public static boolean getSaveCellnum() {
    return !myPrefs.get(PREFTYPES.CELLNUM.name(), "NOPE").toString().equals("NOPE");
  }

  public static boolean getSaveProvider() {
    return !myPrefs.get(PREFTYPES.PROVIDER.name(), "NOPE").toString().equals("NOPE");
  }

  public static boolean getSavePrefs() {
    return myPrefs.getBoolean(PREFTYPES.SAVE_PREFS.name(), true);
  }

  public static int getDelayTime() {
    return myPrefs.getInt(PREFTYPES.REFRESHTIME.name(), 15);
  }

  public static boolean getCheckPaxWebsite() {
    return myPrefs.getBoolean(PREFTYPES.CHECK_PAX.name(), true);
  }

  public static boolean getCheckShowclix() {
    return myPrefs.getBoolean(PREFTYPES.CHECK_SHOWCLIX.name(), true);
  }

  public static boolean getPlayAlarm() {
    return myPrefs.getBoolean(PREFTYPES.PLAY_ALARM.name(), false);
  }

  public static String getExpo() {
    return myPrefs.get(PREFTYPES.EVENT.name(), "Prime");
  }

  public static String getEmail() {
    return myPrefs.get(PREFTYPES.EMAIL.name(), "");
  }

  public static String getCellNumber() {
    String cellNum = myPrefs.get(PREFTYPES.CELLNUM.name(), "");
    if (cellNum.contains("@") && !cellNum.contains(";")) {
      if (Email.getCarrierExtension(getProvider()).equals(cellNum.substring(cellNum.indexOf("@")))) {
        cellNum = cellNum.substring(0, cellNum.indexOf("@"));
      }
    }
    return cellNum;
  }

  public static String getProvider() {
    return myPrefs.get(PREFTYPES.PROVIDER.name(), "AT&T");
  }

  private static enum PREFTYPES {

    REFRESHTIME, CHECK_SHOWCLIX, CHECK_PAX, PLAY_ALARM, EVENT, EMAIL, CELLNUM, PROVIDER, SAVE_PREFS;
  }

  public static void setSavePrefs(boolean save) {
    myPrefs.putBoolean(PREFTYPES.SAVE_PREFS.name(), save);
    if (!save) {
      setSaveAll(false, false, false, false, false, false, false, false);
    }
  }

  public static String getPrefsPath() {
    return myPrefs.absolutePath();
  }
}