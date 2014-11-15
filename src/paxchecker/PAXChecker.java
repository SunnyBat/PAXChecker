package paxchecker;

import paxchecker.check.*;
import paxchecker.tickets.Checker;
import paxchecker.update.UpdateHandler;
import paxchecker.gui.Setup;
import paxchecker.notification.NotificationHandler;

/**
 *
 * @author SunnyBat
 */
public class PAXChecker {

  public static final String VERSION = "1.7.6.3";
  public static Setup setup;

  /**
   * @param args the command line arguments
   */
  public static void main(String[] args) {
    System.out.println("Initializing...");
    Checker.hackedAroundStatus();
    javax.swing.ToolTipManager.sharedInstance().setDismissDelay(600000); // Make Tooltips stay forever
    Email.init();
    startProgram(args);
  }

  public static void startProgram(String[] args) {
    boolean doUpdate = true;
    boolean checkPax = true;
    boolean checkShowclix = true;
    boolean checkTwitter = true;
    boolean autoStart = false;
    boolean commandLine = false;
    boolean savePrefs = false;
    if (args.length > 0) {
      System.out.println("Args!");
      argsCycle:
      for (int a = 0; a < args.length; a++) {
        System.out.println("args[" + a + "] = " + args[a]);
        switch (args[a].toLowerCase()) {
          case "-noupdate":
            // Used by the program when starting the new version just downloaded. Can also be used if you don't want updates
            doUpdate = false;
            break;
          case "-notificationid":
            NotificationHandler.setLastNotificationID(args[a + 1]);
          case "-nonotifications":
            NotificationHandler.setLastNotificationID("DISABLE");
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
            commandLine = true;
            break;
          case "-property":
            try {
              String key = args[a + 1];
              String value = args[a + 2];
              Email.setProperty(key, value);
            } catch (Exception e) {
              ErrorHandler.showErrorWindow("ERROR setting custom property!", "Unable to set custom properties. See error details for more information.", e);
            }
            break;
          case "-savesettings":
            savePrefs = true;
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
    if (autoStart) {
      if (checkPax) {
        TicketChecker.addChecker(new CheckPaxsite(Browser.getExpo()));
      }
      if (checkShowclix) {
        TicketChecker.addChecker(new CheckShowclix(Browser.getExpo()));
      }
      if (checkTwitter) {
        TicketChecker.addChecker(new CheckTwitter(Browser.getExpo()));
      }
    }
    if (commandLine) {
      ErrorHandler.setCommandLine(true);
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
    KeyboardHandler.init();
    if (doUpdate) {
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
      setup = new Setup();
      NotificationHandler.loadNotifications();
      NotificationHandler.showNewNotifications();
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

  /**
   * Sends a test email on a daemon Thread. Note that this also updates the Status window if possible.
   */
  public static void sendBackgroundTestEmail() {
    startBackgroundThread(new Runnable() {
      @Override
      public void run() {
        try {
          Checker.setStatusTextButtonState(false);
          Checker.setStatusTextButtonText("Sending...");
          if (!Email.testEmail()) {
            Checker.setStatusTextButtonText("Test Text");
            Checker.setStatusTextButtonState(true);
            return;
          }
          long timeStarted = System.currentTimeMillis();
          while (System.currentTimeMillis() - timeStarted < 60000) {
            Checker.setStatusTextButtonText((60 - (int) ((System.currentTimeMillis() - timeStarted) / 1000)) + "");
            Thread.sleep(200);
          }
          Checker.setStatusTextButtonText("Test Text");
          Checker.setStatusTextButtonState(true);
        } catch (Exception e) {
          System.out.println("ERROR sending background test email!");
          e.printStackTrace();
          Checker.setStatusTextButtonText("Test Text");
          Checker.setStatusTextButtonState(true);
        }
      }
    }, "Send Test Email");
  }
}
