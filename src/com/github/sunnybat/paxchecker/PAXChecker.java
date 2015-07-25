package com.github.sunnybat.paxchecker;

import com.github.sunnybat.commoncode.email.EmailAccount;
import com.github.sunnybat.commoncode.update.*;
import com.github.sunnybat.paxchecker.browser.Browser;
import com.github.sunnybat.paxchecker.check.*;
import com.github.sunnybat.paxchecker.gui.LoadingWindow;
import com.github.sunnybat.paxchecker.gui.Status;
import com.github.sunnybat.paxchecker.setup.*;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

/**
 *
 * @author SunnyBat
 */
public final class PAXChecker {

  public static final String VERSION = "3.0.0 R1";
  private static final Object CLINE_LOCK = new Object();
  private static boolean commandLine;
  private static final String PATCH_NOTES_LINK = "https://dl.orangedox.com/r29siEtUhPNW4FKg7T/PAXCheckerUpdates.txt?dl=1";
  private static final String UPDATE_LINK = "https://dl.orangedox.com/TXu5eUDa2Ds3RSKVUI/PAXChecker.jar?dl=1";
  private static final String BETA_UPDATE_LINK = "https://dl.orangedox.com/BqkMXYrpYjlBEbfVmd/PAXCheckerBETA.jar?dl=1";

  /**
   * @param args the command line arguments
   */
  public static void main(String[] args) {
//    new com.github.sunnybat.paxchecker.setup.SetupCLI().promptForSettings();
//    com.github.sunnybat.paxchecker.setup.SetupGUI myGUITwo = new com.github.sunnybat.paxchecker.setup.SetupGUI();
//    myGUITwo.showWindow();
    removeOldPreferences();
    System.out.println("Initializing...");
    java.net.HttpURLConnection.setFollowRedirects(true); // Follow all redirects automatically, so when checking Showclix event pages, it works!

    LoadingWindow window = new LoadingWindow();
    window.showWindow();

    // CHECK UPDATES, NOTIFICATIONS
    try {
      window.setStatus("Checking for updates...");
      PatchNotesDownloader notesDownloader = new PatchNotesDownloader(PATCH_NOTES_LINK);
      notesDownloader.downloadVersionNotes(VERSION);
      if (notesDownloader.updateAvailable()) {
        // TODO: Add support for anonymous downloads
        UpdateDownloader myDownloader = new UpdateDownloader(UPDATE_LINK, BETA_UPDATE_LINK);
        UpdatePrompt myPrompt = new UpdatePrompt("PAXChecker", myDownloader.getUpdateSize(), notesDownloader.getUpdateLevel(),
            "VERSION", notesDownloader.getVersionNotes(VERSION));
        window.setVisible(false); // TODO: Make better method
        myPrompt.showWindow();
        try {
          myPrompt.waitForClose();
        } catch (InterruptedException e) {
        }
        if (myPrompt.shouldUpdateProgram()) {
          myDownloader.updateProgram(myPrompt, new File(PAXChecker.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()));
          return;
        } else {
          window.showWindow();
        }
      }
    } catch (IOException iOException) {
    } catch (URISyntaxException uRISyntaxException) {
    }
    window.dispose();

    // SETUP
    boolean isHeadless = GraphicsEnvironment.isHeadless();
    boolean isAutostart = false;
    for (String s : args) {
      switch (s.toLowerCase()) {
        case "-cli":
          isHeadless = true;
          break;
        case "-autostart":
          isAutostart = true;
          break;
//          case "-noupdate":
//            doUpdate = false;
//            break;
//          case "-notificationid":
//            NotificationHandler.setLastNotificationID(args[a + 1]);
//            break;
//          case "-nonotifications":
//            NotificationHandler.setLastNotificationID("DISABLE");
//            break;
//          case "-follow":
//          case "-checktwitter":
//            String twitterHandle = args[a + 1];
//            TwitterReader.addHandle(twitterHandle);
//          case "-autostart":
//            autoStart = true;
//            break;
//          case "-startminimized":
//            CheckSetup.startMinimized();
//            break;
//          case "-property":
//            try {
//              String key = args[a + 1];
//              String value = args[a + 2];
//              Email.setProperty(key, value);
//            } catch (Exception e) {
//              new ErrorBuilder()
//                  .setError(e)
//                  .setErrorTitle("ERROR setting custom property!")
//                  .setErrorMessage("Unable to set custom properties. See error details for more information.")
//                  .buildWindow();
//            }
//            break;
//          case "-savesettings":
//            savePrefs = true;
//            break;
//          case "-consumerkey":
//            twitterTokens[0] = args[a + 1];
//            break;
//          case "-consumersecret":
//            twitterTokens[1] = args[a + 1];
//            break;
//          case "-applicationkey":
//            twitterTokens[2] = args[a + 1];
//            break;
//          case "-applicationsecret":
//            twitterTokens[3] = args[a + 1];
//            break;
//          case "-savelog":
//            try {
//              if (args.length > a + 1 && !args[a + 1].startsWith("-")) {
//                System.setOut(new SavePrintStream(System.out, args[a + 1]));
//              } else {
//                System.setOut(new SavePrintStream(System.out));
//              }
//            } catch (FileNotFoundException fnfe) {
//              System.out.println("Unable to set output stream saver -- File does not exist?");
//              fnfe.printStackTrace();
//            }
//            break;
//          case "-alarmfile":
//            Audio.setAlarmFile(args[a + 1]);
//            break;
//          default:
//            if (args[a].startsWith("-")) {
//              System.out.println("Unknown argument: " + args[a]);
//            }
//            break;
      }
    }
    Setup mySetup;
    if (isAutostart) {
      mySetup = new SetupAuto(args);
    } else if (isHeadless) {
      mySetup = new SetupCLI();
    } else {
      mySetup = new SetupGUI();
    }
    mySetup.promptForSettings();

    Status myStatus = new Status();

    EmailAccount emailAccount = null;
    try {
      emailAccount = new EmailAccount(mySetup.getEmailUsername(), mySetup.getEmailPassword());
      for (String s : mySetup.getEmailAddresses()) {
        emailAccount.addEmailAddress(s);
      }
    } catch (IllegalArgumentException e) {
    }
    TicketChecker myChecker = initChecker(mySetup, myStatus);

    myStatus.showWindow();

    while (true) {
      long startTime = System.currentTimeMillis();
      if (myChecker.isUpdated()) {
        Browser.openLinkInBrowser(myChecker.getLinkFound());
        if (emailAccount != null) {
          emailAccount.sendMessage("PAXChecker", "A new link has been found: " + myChecker.getLinkFound());
        }
      }
      while (System.currentTimeMillis() - startTime < mySetup.timeBetweenChecks() * 1000) {
        try {
          Thread.sleep(250);
        } catch (InterruptedException iE) {
        }
      }
    }
  }

  private static void removeOldPreferences() {
    try {
      System.out.println("Checking for old preferences root...");
      java.util.prefs.Preferences p = java.util.prefs.Preferences.userRoot().node("paxchecker");
      p.removeNode();
      p.flush();
      System.out.println("Removed node.");
    } catch (Exception e) {
      System.out.println("Unable to remove old preferences node -- already removed?");
    }
  }

  private static TicketChecker initChecker(Setup mySetup, Status myStatus) {
    TicketChecker myChecker = new TicketChecker(myStatus);
    if (mySetup.shouldCheckPAXWebsite()) {
      myChecker.addChecker(new CheckPaxsite());
    }
    if (mySetup.shouldCheckShowclix()) {
      myChecker.addChecker(new CheckShowclix());
    }
    if (mySetup.shouldCheckKnownEvents()) {
      myChecker.addChecker(new CheckShowclixEventPage());
    }
    if (mySetup.shouldCheckTwitter()) {
      // TODO: Add Twitter checker
    }
    myChecker.initCheckers();
    return myChecker;
  }

  public static void enableCommandLine() {
    synchronized (CLINE_LOCK) {
      commandLine = true;
    }
  }

  public static boolean isCommandLine() {
    synchronized (CLINE_LOCK) {
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
