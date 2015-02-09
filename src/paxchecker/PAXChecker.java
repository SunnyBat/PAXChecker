package paxchecker;

import java.io.IOException;
import paxchecker.browser.Browser;
import paxchecker.browser.TwitterReader;
import paxchecker.check.*;
import paxchecker.tickets.Checker;
import paxchecker.update.UpdateHandler;
import paxchecker.gui.Setup;
import paxchecker.gui.LoadingWindow;
import paxchecker.notification.NotificationHandler;
import paxchecker.preferences.Preference;
import paxchecker.preferences.PreferenceHandler;
import com.github.sunnybat.commoncode.error.ErrorDisplay;

/**
 *
 * @author SunnyBat
 */
public final class PAXChecker {

  public static final String VERSION = "2.0.0 R6";
  private static Setup setup;
  private static final Object LOCK = new Object();
  private static boolean commandLine;
  private static LoadingWindow start;

  /**
   * @param args the command line arguments
   */
  public static void main(String[] args) {
    System.out.println("Initializing...");
    if (isCLine(args)) {
      enableCommandLine();
    } else {
      try {
        start = new LoadingWindow();
        start.setStatus("Initializing program...");
        start.showWindow();
        javax.swing.ToolTipManager.sharedInstance().setDismissDelay(600000); // Make Tooltips stay forever
        setup = new Setup();
      } catch (java.awt.HeadlessException e) {
        System.out.println("Headless environment detected: Switching to CLI mode.");
        enableCommandLine();
      }
    }
    initClasses();
    startProgram(args);
  }

  private static boolean isCLine(String[] args) {
    for (String s : args) {
      if (s.equals("-cli")) {
        return true;
      }
    }
    return false;
  }

  private static void initClasses() {
    PreferenceHandler.init();
    Checker.init();
    Email.init();
    UpdateHandler.init();
    KeyboardHandler.init();
    NotificationHandler.init();
  }

  public static void startProgram(String[] args) {
    boolean doUpdate = PreferenceHandler.getBooleanPreference(Preference.TYPES.LOAD_UPDATES);
    boolean checkPax = true;
    boolean checkShowclix = true;
    boolean checkTwitter = true;
    boolean autoStart = false;
    boolean savePrefs = false;
    String[] twitterTokens = new String[4];
    Checker.addHandle("@Official_PAX");
    if (args.length > 0) {
      System.out.println("Args!");
      argsCycle:
      for (int a = 0; a < args.length; a++) {
        System.out.println("args[" + a + "] = " + args[a]);
        switch (args[a].toLowerCase()) {
          case "-noupdate":
            doUpdate = false;
            break;
          case "-notificationid":
            NotificationHandler.setLastNotificationID(args[a + 1]);
            break;
          case "-nonotifications":
            NotificationHandler.setLastNotificationID("DISABLE");
            break;
          case "-typelink":
            KeyboardHandler.setTypeLink(true);
            break;
          case "-email":
            Email.setUsername(args[a + 1]);
            System.out.println("Username set to " + Email.getUsername());
            break;
          case "-password":
            Email.setPassword(args[a + 1]);
            System.out.println("Password set");
            break;
          case "-cellnum":
            for (int b = a + 1; b < args.length; b++) {
              if (args[b].startsWith("-")) {
                a = b - 1;
                continue argsCycle;
              }
              System.out.println("Adding email address " + args[b]);
              Email.addEmailAddress(args[b]);
            }
            break;
          case "-expo":
            Browser.setExpo(args[a + 1]);
            System.out.println("Expo set to " + Browser.getExpo());
            break;
          case "-nopax":
            if (!checkShowclix && !checkTwitter) {
              System.out.println("Already not checking Showclix or Twitter -- cannot set check PAX website to false");
              break;
            }
            System.out.println("Setting check PAX website to false");
            checkPax = false;
            break;
          case "-noshowclix":
            if (!checkPax && !checkTwitter) {
              System.out.println("Already not checking PAX website or Twitter -- cannot set check Showclix website to false");
              break;
            }
            System.out.println("Setting check Showclix website to false");
            checkShowclix = false;
            break;
          case "-notwitter":
            if (!checkPax && !checkShowclix) {
              System.out.println("Already not checking PAX website or Showclix -- cannot set check Twitter to false");
              break;
            }
            System.out.println("Setting check Twitter to false");
            checkTwitter = false;
            break;
          case "-checktwitter":
            String twitterHandle = args[a + 1];
            Checker.addHandle(twitterHandle);
          case "-alarm":
            System.out.println("Alarm activated");
            Audio.setPlayAlarm(true);
            break;
          case "-delay":
            Checker.setRefreshTime(Integer.getInteger(args[a + 1], 15));
            System.out.println("Set refresh time to " + Checker.getRefreshTime());
            break;
          case "-autostart":
            autoStart = true;
            break;
          case "-cli": // Redundant
            enableCommandLine();
            break;
          case "-property":
            try {
              String key = args[a + 1];
              String value = args[a + 2];
              Email.setProperty(key, value);
            } catch (Exception e) {
              ErrorDisplay.showErrorWindow("ERROR setting custom property!", "Unable to set custom properties. See error details for more information.", e);
            }
            break;
          case "-savesettings":
            savePrefs = true;
            break;
          case "-consumerkey":
            twitterTokens[0] = args[a + 1];
            break;
          case "-consumersecret":
            twitterTokens[1] = args[a + 1];
            break;
          case "-applicationkey":
            twitterTokens[2] = args[a + 1];
            break;
          case "-applicationsecret":
            twitterTokens[3] = args[a + 1];
            break;
          default:
            if (args[a].startsWith("-")) {
              System.out.println("Unknown argument: " + args[a]);
            }
            break;
        }
      }
      if (autoStart && !checkPax && !checkShowclix && !checkTwitter) {
        System.out.println("ERROR: Program is not checking PAX website, Showclix website, or Twitter. Program will now exit.");
        System.exit(0);
      }
    }
    if (!PreferenceHandler.getBooleanPreference(Preference.TYPES.LOAD_NOTIFICATIONS)) {
      NotificationHandler.setLastNotificationID("DISABLE");
    }
    if (twitterTokens[0] != null) {
      TwitterReader.setKeys(twitterTokens[0], twitterTokens[1], twitterTokens[2], twitterTokens[3]);
      TwitterReader.init();
    }
    if (!TwitterReader.isInitialized()) {
      if (PreferenceHandler.getStringPreference(Preference.TYPES.TWITTER_CONSUMER_KEY) != null) {
        System.out.println("Loading Twitter keys from Prefrences");
        try {
          TwitterReader.setKeys(Encryption.decrypt(PreferenceHandler.getStringPreference(Preference.TYPES.TWITTER_CONSUMER_KEY)),
              Encryption.decrypt(PreferenceHandler.getStringPreference(Preference.TYPES.TWITTER_CONSUMER_SECRET)),
              Encryption.decrypt(PreferenceHandler.getStringPreference(Preference.TYPES.TWITTER_APP_KEY)),
              Encryption.decrypt(PreferenceHandler.getStringPreference(Preference.TYPES.TWITTER_APP_SECRET)));
        } catch (Exception exception) {
          System.out.println("ERROR: Unable to load Twitter keys from Preferences!");
        }
        TwitterReader.init();
      } else {
        System.out.println("No Twitter keys found!");
      }
    }
    System.out.println("Loading patch notes...");
    if (autoStart) {
      if (checkPax) {
        TicketChecker.addChecker(new CheckPaxsite());
      }
      if (checkShowclix) {
        TicketChecker.addChecker(new CheckShowclix());
      }
      if (checkTwitter && TwitterReader.isInitialized()) {
        Checker.startTwitterStreaming();
      }
    }
    if (isCommandLine()) {
      if (doUpdate) {
        UpdateHandler.loadVersionNotes();
        if (UpdateHandler.updateAvailable()) {
          UpdateHandler.autoUpdate(args);
        }
      } else {
        startBackgroundThread(new Runnable() {
          @Override
          public void run() {
            UpdateHandler.loadVersionNotes();
          }
        }, "Patch Notes");
      }
      System.out.println("Loading notifications...");
      NotificationHandler.loadNotifications();
      System.out.println("Finished loading notifications.");
      NotificationHandler.showNewNotifications();
      if (!autoStart) {
        Checker.commandLineSettingsInput();
      }
      if (savePrefs) {
        PreferenceHandler.savePreferences();
      }
      Checker.startCommandLineWebsiteChecking();
      return;
    }
    if (doUpdate) {
      start.setStatus("Loading Version Notes...");
      UpdateHandler.loadVersionNotes();
      if (UpdateHandler.updateAvailable()) {
        UpdateHandler.promptUpdate(args);
      }
      setup.setPatchNotesText(UpdateHandler.getVersionNotes());
    } else {
      startBackgroundThread(new Runnable() {
        @Override
        public void run() {
          UpdateHandler.loadVersionNotes();
          setup.setPatchNotesText(UpdateHandler.getVersionNotes());
        }
      }, "Patch Notes");
    }
    start.setStatus("Loading Notifications...");
    NotificationHandler.loadNotifications();
    start.setStatus("Program loaded!");
    start.dispose();
    NotificationHandler.showNewNotifications();
    if (autoStart) {
      start.dispose();
      Checker.startCheckingWebsites();
    } else {
      setup.loadProgramSettings();
      setup.showWindow();
    }
  }

  public static void enableCommandLine() {
    synchronized (LOCK) {
      commandLine = true;
    }
  }

  public static boolean isCommandLine() {
    synchronized (LOCK) {
      return commandLine;
    }
  }

  /**
   * Creates a new non-daemon Thread with the given Runnable object.
   *
   * @param run The Runnable object to use
   */
  public static void continueProgram(Runnable run) {
    Thread newThread = new Thread(run);
    newThread.setName("Program Loop");
    newThread.setDaemon(false); // Prevent the JVM from stopping due to zero non-daemon threads running
    newThread.setPriority(Thread.NORM_PRIORITY);
    newThread.start(); // Start the Thread
  }

  /**
   * Starts a new daemon Thread.
   *
   * @param run The Runnable object to use
   * @param name The name to give the Thread
   */
  public static void startBackgroundThread(Runnable run, String name) {
    Thread newThread = new Thread(run);
    newThread.setName(name);
    newThread.setDaemon(true); // Kill the JVM if only daemon threads are running
    newThread.setPriority(Thread.MIN_PRIORITY); // Let other Threads take priority, as this will probably not run for long
    newThread.start(); // Start the Thread
  }
}
