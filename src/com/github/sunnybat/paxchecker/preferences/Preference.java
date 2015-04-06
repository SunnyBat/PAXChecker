package com.github.sunnybat.paxchecker.preferences;

/**
 *
 * @author Sunny
 */
public class Preference {

  public static enum TYPES {

    REFRESHTIME, CHECK_SHOWCLIX, CHECK_PAX, CHECK_TWITTER, PLAY_ALARM, EVENT, EMAIL, CELLNUM,
    SAVE_PREFS, LAST_EVENT, USE_BETA, LAST_NOTIFICATION_ID, LOAD_NOTIFICATIONS, LOAD_UPDATES,
    TWITTER_CONSUMER_KEY, TWITTER_CONSUMER_SECRET, TWITTER_APP_KEY, TWITTER_APP_SECRET,
    SHOWCLIX_DEEP_CHECK, ANONYMOUS_STATISTICS, DAILY_UPDATES, FILTER_TWITTER, CHECK_KNOWN_EVENTS;
  }

  protected Preference(TYPES pref) {
    type = pref;
    value = PreferenceHandler.loadPreferenceValue(pref);
    shouldSave = PreferenceHandler.isInPrefs(pref);
    System.out.println("Loaded Preference " + toString());
  }

  private final TYPES type;
  private Object value;
  private boolean shouldSave;
  private boolean forceSave;

  /**
   * Checks whether or not the given Preference is saved within the Preferences framework.
   *
   * @return True if it is, false if it's not
   */
  public boolean isSavedInPreferences() {
    return PreferenceHandler.isInPrefs(type);
  }

  public TYPES getPrefType() {
    return type;
  }

  /**
   * Checks whether or not the given Preference should be saved.
   *
   * @return True to save, false to not
   */
  public boolean shouldSave() {
    return shouldSave;
  }

  public void setShouldSave(boolean save) {
    shouldSave = save;
  }

  /**
   * Returns the expected Object from the preference. This currently returns a boolean, integer, or String.
   *
   * @return The Object associated with the Preference, or null if none has been set
   */
  public Object getValue() {
    return value;
  }

  public void setValue(Object val) {
    if (val == null) {
      System.out.println("NOTE: Object val set to null!");
    }
    value = val;
    System.out.println(type.name() + " -- Value = " + value);
  }

  public boolean forceSave() {
    return forceSave;
  }

  public void setForceSave() {
    forceSave = true;
  }

  @Override
  public String toString() {
    return type.name() + " :: value = " + value + " -- shouldSave = " + shouldSave;
  }

}
