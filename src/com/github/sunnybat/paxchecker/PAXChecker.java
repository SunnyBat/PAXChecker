package com.github.sunnybat.paxchecker;

import com.github.sunnybat.commoncode.email.EmailAccount;
import com.github.sunnybat.commoncode.error.ErrorBuilder;
import com.github.sunnybat.commoncode.startup.LoadingCLI;
import com.github.sunnybat.commoncode.startup.LoadingWindow;
import com.github.sunnybat.commoncode.startup.Startup;
import com.github.sunnybat.commoncode.update.PatchNotesDownloader;
import com.github.sunnybat.commoncode.update.UpdateDownloader;
import com.github.sunnybat.commoncode.update.UpdatePrompt;
import com.github.sunnybat.paxchecker.browser.Browser;
import com.github.sunnybat.paxchecker.check.CheckPaxsite;
import com.github.sunnybat.paxchecker.check.CheckShowclix;
import com.github.sunnybat.paxchecker.check.CheckShowclixEventPage;
import com.github.sunnybat.paxchecker.check.TicketChecker;
import com.github.sunnybat.paxchecker.check.TwitterStreamer;
import com.github.sunnybat.paxchecker.gui.Tickets;
import com.github.sunnybat.paxchecker.notification.NotificationHandler;
import com.github.sunnybat.paxchecker.resources.ResourceDownloader;
import com.github.sunnybat.paxchecker.setup.Setup;
import com.github.sunnybat.paxchecker.setup.SetupAuto;
import com.github.sunnybat.paxchecker.setup.SetupCLI;
import com.github.sunnybat.paxchecker.setup.SetupGUI;
import com.github.sunnybat.paxchecker.status.Status;
import com.github.sunnybat.paxchecker.status.StatusCLI;
import com.github.sunnybat.paxchecker.status.StatusGUI;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author SunnyBat
 */
public final class PAXChecker {

  public static final String VERSION = "3.0.0 R5";
  private static final String PATCH_NOTES_LINK = "https://dl.orangedox.com/r29siEtUhPNW4FKg7T/PAXCheckerUpdates.txt?dl=1";
  private static final String UPDATE_LINK = "https://dl.orangedox.com/TXu5eUDa2Ds3RSKVUI/PAXChecker.jar?dl=1";
  private static final String BETA_UPDATE_LINK = "https://dl.orangedox.com/BqkMXYrpYjlBEbfVmd/PAXCheckerBETA.jar?dl=1";
  private static TwitterStreamer myStreamer;

  /**
   * @param args the command line arguments
   */
  public static void main(String[] args) {
    removeOldPreferences();
    System.out.println("Initializing...");
    java.net.HttpURLConnection.setFollowRedirects(true); // Follow all redirects automatically, so when checking Showclix event pages, it works!

    // SETUP
    boolean isHeadless = GraphicsEnvironment.isHeadless();
    boolean isAutostart = false;
    boolean checkUpdates = true;
    boolean checkNotifications = true;
    boolean startMinimized = false;
    List<String> followList = new ArrayList<>();
    Map<String, String> properties = new HashMap<>();
    String[] twitterTokens = new String[4];
    for (int i = 0; i < args.length; i++) {
      switch (args[i].toLowerCase()) {
        case "-cli":
          isHeadless = true;
          break;
        case "-autostart":
          isAutostart = true;
          break;
        case "-noupdate":
          checkUpdates = false;
          break;
//          case "-notificationid":
//            NotificationHandler.setLastNotificationID(args[a + 1]);
//            break;
        case "-nonotifications":
          checkNotifications = false;
          break;
        case "-follow":
        case "-checktwitter":
          followList.add(args[i + 1]);
          break;
        case "-startminimized":
          startMinimized = true;
          break;
        case "-property":
          if (args.length > i + 2) {
            String key = args[i + 1];
            String value = args[i + 2];
            if (!key.startsWith("-") && !value.startsWith("-")) {
              properties.put(key, value);
            } else {
              System.out.println("Not enough arguments specified for -property!");
            }
          } else {
            System.out.println("Not enough arguments specified for -property!");
          }
          break;
        case "-twitterkeys":
          if (args.length > i + 4) {
            twitterTokens[0] = args[i + 1];
            twitterTokens[1] = args[i + 2];
            twitterTokens[2] = args[i + 3];
            twitterTokens[3] = args[i + 4];
            for (String s : twitterTokens) { // Note to myself: YES this is verified working, stop checking it
              if (s.startsWith("-")) {
                System.out.println("Invalid argument for -twitterkeys [" + s + "] ignoring -twitterkeys arguments");
                for (int j = 0; j < twitterTokens.length; j++) {
                  twitterTokens[j] = null;
                }
                break;
              }
            }
          } else {
            System.out.println("Not enough arguments specified for -twitterkeys!");
          }
          break;
        case "-savelog":
          try {
            SavePrintStream printer;
            if (args.length > i + 1 && !args[i + 1].startsWith("-")) {
              printer = new SavePrintStream(System.out, args[i + 1]);
            } else {
              printer = new SavePrintStream(System.out);
            }
            System.setOut(printer); // TODO: Separate these so they print to the same file but print to out/err properly
            System.setErr(printer);
          } catch (FileNotFoundException fnfe) {
            System.out.println("Unable to set output stream saver -- File does not exist?");
            fnfe.printStackTrace();
          }
          break;
        case "-alarmfile":
          Audio.setAlarmFile(args[i + 1]);
          break;
        case "-redownloadresources":
          // Used elsewhere
          break;
        default:
          if (args[i].startsWith("-")) {
            System.out.println("Unknown argument: " + args[i]);
          }
          break;
      }
    }
    final Startup loadingOutput;
    if (isHeadless) {
      ErrorBuilder.forceCommandLine();
      loadingOutput = new LoadingCLI(System.out);
    } else {
      loadingOutput = new LoadingWindow();
    }
    String patchNotes = null;
    loadingOutput.start();
    loadResources(loadingOutput, hasArgument(args, "-redownloadresources"));
    if (!isHeadless) {
      if (checkUpdates) {
        patchNotes = loadUpdates(loadingOutput);
      }
      if (checkNotifications) {
        loadNotifications(loadingOutput);
      }
    } else {
      System.out.println("Updating and notifications are currently disabled in CLI mode and will be enabled in a future update.");
    }
    loadingOutput.stop();
    Setup mySetup;
    if (isAutostart) {
      mySetup = new SetupAuto(args);
    } else if (isHeadless) {
      mySetup = new SetupCLI(twitterTokens);
    } else {
      SetupGUI myGUI = new SetupGUI(twitterTokens);
      if (patchNotes != null) {
        myGUI.setPatchNotesText(patchNotes);
      } else {
        myGUI.setPatchNotesText("[Updating Disabled]");
      }
      mySetup = myGUI;
    }
    mySetup.promptForSettings(); // Might take a while
    twitterTokens[0] = mySetup.getTwitterConsumerKey();
    twitterTokens[1] = mySetup.getTwitterConsumerSecret();
    twitterTokens[2] = mySetup.getTwitterApplicationKey();
    twitterTokens[3] = mySetup.getTwitterApplicationSecret();

    // SETUP
    Browser myBrowser = new Browser();
    myBrowser.setExpo(mySetup.getExpoToCheck());
    if (mySetup.shouldFilterShowclix()) {
      com.github.sunnybat.paxchecker.browser.ShowclixReader.strictFilter(); // Laziness will be the reason I keep refactoring everything...
    }
    Status myStatus;
    // SET UP EMAIL ACCOUNT
    EmailAccount emailAccount;
    try {
      emailAccount = new EmailAccount(mySetup.getEmailUsername(), mySetup.getEmailPassword());
      for (String s : mySetup.getEmailAddresses()) {
        emailAccount.addEmailAddress(s);
      }
      for (String key : properties.keySet()) {
        emailAccount.setProperty(key, properties.get(key));
      }
    } catch (IllegalArgumentException e) {
      emailAccount = null;
    }
    if (!isHeadless) {
      StatusGUI statGUI;
      if (emailAccount != null) {
        statGUI = new StatusGUI(myBrowser.getExpo(), emailAccount.getUsername(), emailAccount.getAddressList());
      } else {
        statGUI = new StatusGUI(myBrowser.getExpo());
      }
      myStatus = statGUI;
      if (startMinimized) {
        statGUI.minimizeToTray();
      } else {
        statGUI.showWindow();
      }
    } else {
      myStatus = new StatusCLI();
    }
    if (emailAccount != null) {
      myStatus.enableEmail();
    }
    if (mySetup.shouldPlayAlarm()) {
      Audio.enableAlarm();
      myStatus.enableAlarm();
    }
    // SET UP CHECKERS
    final TicketChecker myChecker = initChecker(mySetup, isHeadless ? null : (StatusGUI) myStatus, myBrowser.getExpo()); // TODO: Better casting than this
    if (mySetup.shouldCheckTwitter()) {
      TwitterStreamer tcheck = setupTwitter(myStatus, twitterTokens, emailAccount);
      for (String s : followList) {
        tcheck.addUser(s);
      }
      tcheck.startStreamingTwitter(); // CHECK: Move this down?
      myStreamer = tcheck;
      myStatus.enableTwitter();
    }

    // START CHECKING
    checkForTickets(myStatus, myChecker, emailAccount, mySetup.timeBetweenChecks());
  }

  private static boolean hasArgument(String[] args, String argument) {
    for (String s : args) {
      if (s.toLowerCase().equals(argument.toLowerCase())) {
        return true;
      }
    }
    return false;
  }

  /**
   * Removes the old Preferences root to prevent registry cluttering.
   */
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

  private static String loadUpdates(Startup window) {
    String notes = null;
    try {
      window.setStatus("Checking for updates...");
      PatchNotesDownloader notesDownloader = new PatchNotesDownloader(PATCH_NOTES_LINK);
      notesDownloader.downloadVersionNotes(VERSION);
      notes = notesDownloader.getVersionNotes();
      if (notesDownloader.updateAvailable()) {
        // TODO: Add support for anonymous downloads
        UpdateDownloader myDownloader = new UpdateDownloader(UPDATE_LINK, BETA_UPDATE_LINK);
        UpdatePrompt myPrompt = new UpdatePrompt("PAXChecker", myDownloader.getUpdateSize(), notesDownloader.getUpdateLevel(),
            "VERSION", notesDownloader.getVersionNotes(VERSION));
        window.stop();
        myPrompt.showWindow();
        try {
          myPrompt.waitForClose();
        } catch (InterruptedException e) {
        }
        if (myPrompt.shouldUpdateProgram()) {
          myDownloader.updateProgram(myPrompt, new File(PAXChecker.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()));
          System.exit(0); // TODO: Is this actually the right way to kill the program?
        } else {
          window.start();
        }
      }
    } catch (IOException | URISyntaxException e) {
      e.printStackTrace();
    }
    return notes;
  }

  private static void loadNotifications(Startup window) {
    window.setStatus("Checking for notifications...");
    NotificationHandler notifications = new NotificationHandler(false, "-1"); // TODO: Load notifications from Preferences
    notifications.loadNotifications();
    notifications.showNewNotifications();
  }

  private static void loadResources(final Startup window, boolean redownload) {
    ResourceDownloader download = new ResourceDownloader() {
      private String currentFile;

      @Override
      public void startingFile(String fileName) {
        currentFile = fileName;
        window.setStatus("Starting " + currentFile);
      }

      @Override
      public void filePercentage(int percent) {
        super.filePercentage(percent);
        window.setStatus("Downloading " + currentFile + " (" + percent + "%)");
      }

      @Override
      public void finishedFile(String fileName) {
        window.setStatus("Finished downloading " + fileName);
      }
    };
    if (redownload) {
      download.forceRedownload();
    }
    download.downloadResources();
  }

  private static TicketChecker initChecker(Setup mySetup, StatusGUI myStatus, String expo) {
    TicketChecker myChecker = new TicketChecker(myStatus);
    if (mySetup.shouldCheckPAXWebsite()) {
      myChecker.addChecker(new CheckPaxsite(expo));
    }
    if (mySetup.shouldCheckShowclix()) {
      myChecker.addChecker(new CheckShowclix(expo));
    }
    if (mySetup.shouldCheckKnownEvents()) {
      myChecker.addChecker(new CheckShowclixEventPage());
    }
    myChecker.initCheckers();
    return myChecker;
  }

  private static TwitterStreamer setupTwitter(final Status myStatus, final String[] keys, final EmailAccount email) {
    return new TwitterStreamer(keys) {
      @Override
      public void twitterConnected() {
        myStatus.setTwitterStatus(true);
      }

      @Override
      public void twitterDisconnected() {
        myStatus.setTwitterStatus(false);
      }

      @Override
      public void twitterKilled() {
        myStatus.twitterStreamKilled();
      }

      @Override
      public void linkFound(String link, String statusText) {
        Browser.openLinkInBrowser(link);
        email.sendMessage("PAXChecker","Link found on Twitter! Tweet Text: '" + statusText + "' Expanded Link: " + link);
      }
    };
  }

  /**
   * Checks for tickets. Blocks indefinitely. Note that if checker is not checking anything, this method does not block and only modifies the
   * StatusGUI GUI status text.
   *
   * @param status The StatusGUI window to update
   * @param checker The TicketChecker to use
   * @param email The EmailAccount to use, or null for none
   * @param checkTime The time between checks
   * @throws NullPointerException if any arguments besides email are null
   */
  private static void checkForTickets(Status status, TicketChecker checker, EmailAccount email, int checkTime) {
    if (checker.isCheckingAnything()) {
      while (true) {
        status.setLastCheckedText("Checking for Updates");
        long startTime = System.currentTimeMillis();
        if (checker.isUpdated()) {
          Browser.openLinkInBrowser(checker.getLinkFound());
          if (email != null) {
            try {
              email.sendMessage("PAXChecker", "A new link has been found: " + checker.getLinkFound());
            } catch (IllegalStateException e) { // In case we send too fast
              System.out.println("Unable to send email (" + e.getMessage() + ")");
            }
          }
          Tickets ticketWindow = new Tickets(checker.getLinkFound()); // CHECK: Should I only allow one Tickets at a time?
          ticketWindow.showWindow();
        }
        status.setDataUsageText(DataTracker.getDataUsedMB());
        while (System.currentTimeMillis() - startTime < checkTime * 1000) {
          synchronized (status) {
            int button = status.getButtonPressed();
            if (button > 0) {
              status.resetButtonPressed();
              if (button == 1) {
                break;
              } else if (button == 2) {
                if (email != null) {
                  status.setInformationText("Sending test text...");
                  try {
                    if (email.sendMessage("PAXChecker", "This is a test text.")) {
                      status.setInformationText("Test text sent successfully!");
                    } else {
                      status.setInformationText("Unable to send test text");
                    }
                  } catch (IllegalStateException e) { // In case we send too fast
                    status.setInformationText("Unable to send test text (sent too fast?)");
                  }
                }
              } else if (button == 3) {
                if (Audio.playAlarm()) {
                  status.setInformationText("Alarm started.");
                } else {
                  status.setInformationText("Unable to play alarm.");
                }
              } else if (button == 4) {
                if (myStreamer != null) {
                  myStreamer.startStreamingTwitter();
                }
              }
            }
          }
          status.setLastCheckedText(checkTime - (int) ((System.currentTimeMillis() - startTime) / 1000));
          try {
            Thread.sleep(200);
          } catch (InterruptedException iE) {
            iE.printStackTrace();
          }
        }
      }
    } else {
      status.setLastCheckedText("[Only Checking Twitter]");
    }
  }
}
