package paxchecker;

import paxchecker.error.ErrorDisplay;
import paxchecker.browser.Browser;
import paxchecker.browser.TwitterReader;
import paxchecker.check.*;
import paxchecker.tickets.Checker;
import paxchecker.update.UpdateHandler;
import paxchecker.gui.Setup;
import paxchecker.gui.Startup;
import paxchecker.notification.NotificationHandler;

/**
 *
 * @author SunnyBat
 */
public final class PAXChecker {

  public static final String VERSION = "2.0.0 R4";
  public static Setup setup;
  private static Startup start;

  /**
   * @param args the command line arguments
   */
  public static void main(String[] args) {
    System.out.println("Initializing...");
    initClasses();
    javax.swing.ToolTipManager.sharedInstance().setDismissDelay(600000); // Make Tooltips stay forever
    startProgram(args);
  }

  private static void initClasses() {
    Checker.init();
    Email.init();
    KeyboardHandler.init();
    NotificationHandler.init();
  }

  public static void startProgram(String[] args) {
    boolean doUpdate = true;
    boolean checkPax = true;
    boolean checkShowclix = true;
    boolean checkTwitter = true;
    boolean autoStart = false;
    boolean cLine = false;
    boolean savePrefs = false;
    String[] tokens = new String[4];
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
          case "-cli":
            cLine = true;
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
            tokens[0] = args[a + 1];
            break;
          case "-consumersecret":
            tokens[1] = args[a + 1];
            break;
          case "-applicationkey":
            tokens[2] = args[a + 1];
            break;
          case "-applicationsecret":
            tokens[3] = args[a + 1];
            break;
          default:
            if (args[a].startsWith("-")) {
              System.out.println("Unknown argument: " + args[a]);
            }
            break;
        }
      }
      if (autoStart && !checkPax && !checkShowclix) {
        System.out.println("ERROR: Program is not checking PAX or Showclix website. Program will now exit.");
        System.exit(0);
      }
    }
    System.out.println("Loading patch notes...");
    TwitterReader.setKeys(tokens[0], tokens[1], tokens[2], tokens[3]);
    TwitterReader.init();
    if (autoStart) {
      if (checkPax) {
        TicketChecker.addChecker(new CheckPaxsite());
      }
      if (checkShowclix) {
        TicketChecker.addChecker(new CheckShowclix());
      }
      if (checkTwitter && TwitterReader.isInitialized()) {
        TicketChecker.addChecker(new CheckTwitter());
      }
    }
    if (cLine) {
      ErrorDisplay.setCommandLine(true);
      if (doUpdate) {
        UpdateHandler.loadVersionNotes();
        UpdateHandler.autoUpdate(args);
      } else {
        startBackgroundThread(new Runnable() {
          @Override
          public void run() {
            UpdateHandler.loadVersionNotes();
          }
        }, "Patch Notes");
      }
      if (!autoStart) {
        Checker.commandLineSettingsInput();
      }
      if (savePrefs) {
        SettingsHandler.setSaveAll(true, true, true, true, true, true, true, true);
        SettingsHandler.saveAllPrefs();
      }
      Checker.startCommandLineWebsiteChecking();
      return;
    }
    start = new paxchecker.gui.Startup();
    if (doUpdate) {
      start.setStatus("Loading Version Notes...");
      UpdateHandler.loadVersionNotes();
      UpdateHandler.checkUpdate(args);
    } else {
      startBackgroundThread(new Runnable() {
        @Override
        public void run() {
          UpdateHandler.loadVersionNotes();
        }
      }, "Patch Notes");
    }
    if (autoStart) {
      Checker.startCheckingWebsites();
    } else {
      start.setStatus("Loading Notifications...");
      NotificationHandler.loadNotifications();
      start.dispose();
      NotificationHandler.showNewNotifications();
      setup = new Setup();
    }
    Checker.loadAlertIcon();
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

  /**
   * Sends a test email. Uses the same Thread, blocks until completed.
   */
  public static void sendTestEmail() {
    Email.testEmail();
  }
}
