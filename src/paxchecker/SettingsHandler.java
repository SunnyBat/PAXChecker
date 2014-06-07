package paxchecker;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 *
 * @author SunnyBat
 */
public class SettingsHandler {

  private static Preferences myPrefs = Preferences.userRoot();

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
  public static void savePrefs(int refreshTime, boolean checkPax, boolean checkShowclix, boolean playAlarm, String expo, String provider) {
    try {
//      if (myPrefs.nodeExists(getPrefsPath())) {
//        myPrefs.removeNode();
//      }
      myPrefs.sync();
    } catch (BackingStoreException backingStoreException) {
      System.out.println("Error syncing settings! (They might not load properly...)");
    }
    try {
      myPrefs.putInt(PREFTYPES.REFRESHTIME.name(), refreshTime);
      myPrefs.putBoolean(PREFTYPES.CHECK_PAX.name(), checkPax);
      myPrefs.putBoolean(PREFTYPES.CHECK_SHOWCLIX.name(), checkShowclix);
      myPrefs.putBoolean(PREFTYPES.PLAY_ALARM.name(), playAlarm);
      myPrefs.put(PREFTYPES.EVENT.name(), (expo == null ? "" : expo));
      myPrefs.put(PREFTYPES.PROVIDER.name(), (provider == null ? "" : provider));
      saveCellNum();
      saveEmail();
      System.out.println("Pax = " + checkPax + ", Showclix = " + checkShowclix + ", playAlarm = " + playAlarm + ", Expo = " + expo + ", Provider = " + provider);
      myPrefs.flush();
    } catch (BackingStoreException backingStoreException) {
      System.out.println("Unable to save settings!");
    }
  }

  private static void saveEmail() {
    String email = Email.getUsername();
    if (email == null) {
      myPrefs.remove(PREFTYPES.EMAIL.name());
    } else {
      if (email.equals("@yahoo.com")) {
        myPrefs.remove(PREFTYPES.EMAIL.name());
      } else {
        myPrefs.put(PREFTYPES.EMAIL.name(), email);
      }
    }
  }

  private static void saveCellNum() {
    String textEmail = Email.getTextEmail();
    java.util.List textList = Email.getEmailList();
    if (textEmail != null) {
      myPrefs.put(PREFTYPES.CELLNUM.name(), textEmail.equals("@yahoo.com") ? "" : textEmail);
    } else if (textList != null) {
      if (textList.isEmpty()) {
        myPrefs.remove(PREFTYPES.CELLNUM.name());
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
      myPrefs.remove(PREFTYPES.CELLNUM.name());
    }
    System.out.println("Cellnum = " + myPrefs.get(PREFTYPES.CELLNUM.name(), "NONGET"));
  }

  /**
   * Saves the specified email address to the Preferences file.
   *
   * @param email The email address to save to the Preferences file
   */
  public static void saveEmail(String email) {
    if (email != null) {
      myPrefs.put(PREFTYPES.EMAIL.name(), email);
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
    if (cellNum != null) {
      myPrefs.put(PREFTYPES.CELLNUM.name(), cellNum);
    } else {
      myPrefs.remove(PREFTYPES.CELLNUM.name());
    }
  }

  public static int getDelayTime() {
    return myPrefs.getInt(PREFTYPES.REFRESHTIME.name(), 10);
  }

  public static boolean getCheckPaxWebsite() {
    return myPrefs.getBoolean(PREFTYPES.CHECK_PAX.name(), true);
  }

  public static boolean getCheckShowclix() {
    return myPrefs.getBoolean(PREFTYPES.CHECK_SHOWCLIX.name(), true);
  }

  public static boolean getPlayAlarm() {
    return myPrefs.getBoolean(PREFTYPES.PLAY_ALARM.name(), true);
  }

  public static String getExpo() {
    return myPrefs.get(PREFTYPES.EVENT.name(), "Prime");
  }

  public static String getEmail() {
    return myPrefs.get(PREFTYPES.EMAIL.name(), "");
  }

  public static String getCellNumber() {
    return myPrefs.get(PREFTYPES.CELLNUM.name(), "");
  }

  public static String getProvider() {
    return myPrefs.get(PREFTYPES.PROVIDER.name(), "AT&T");
  }

  private static enum PREFTYPES {

    REFRESHTIME, CHECK_SHOWCLIX, CHECK_PAX, PLAY_ALARM, EVENT, EMAIL, CELLNUM, PROVIDER;
  }

  public static String getPrefsPath() {
    return myPrefs.absolutePath();
  }
}
