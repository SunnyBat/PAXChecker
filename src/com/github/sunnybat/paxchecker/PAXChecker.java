package com.github.sunnybat.paxchecker;

import com.github.sunnybat.commoncode.email.EmailAccount;
import com.github.sunnybat.commoncode.error.ErrorBuilder;
import com.github.sunnybat.commoncode.preferences.PreferenceHandler;
import com.github.sunnybat.commoncode.startup.LoadingCLI;
import com.github.sunnybat.commoncode.startup.LoadingWindow;
import com.github.sunnybat.commoncode.startup.Startup;
import com.github.sunnybat.paxchecker.check.CheckPaxsite;
import com.github.sunnybat.paxchecker.check.CheckShowclix;
import com.github.sunnybat.paxchecker.check.CheckShowclixEventPage;
import com.github.sunnybat.paxchecker.check.TicketChecker;
import com.github.sunnybat.paxchecker.check.TwitterStreamer;
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
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author SunnyBat
 */
public final class PAXChecker {

  public static final String VERSION = "3.0.0";
  private static TwitterStreamer myStreamer; // TODO: Factor elsewhere?
  private static LinkManager myLinkManager; // TODO: Factor elsewhere?

  /**
   * @param args the command line arguments
   */
  public static void main(String[] args) {
    removeOldPreferences();
    System.out.println("Initializing...");
    java.net.HttpURLConnection.setFollowRedirects(true); // Follow all redirects automatically, so when checking Showclix event pages, it works!

    // SETUP
    boolean isHeadless = GraphicsEnvironment.isHeadless() || hasArgument(args, "-cli");
    List<String> followList = new ArrayList<>();
    Map<String, String> properties = new HashMap<>();
    String[] twitterTokens = new String[4];
    for (int i = 0; i < args.length; i++) {
      switch (args[i].toLowerCase()) {
        case "-follow":
        case "-checktwitter":
          followList.add(args[i + 1]);
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
          if (i < args.length - 1 && !args[i + 1].startsWith("-")) {
            Audio.setAlarmFile(args[i + 1]);
          } else {
            System.out.println("Invalid alarmfile specified!");
          }
          break;
      }
    }

    // STARTUP OPERATIONS
    PreferenceHandler prefs = new PreferenceHandler("paxchecker");
    final Startup loadingOutput;
    if (isHeadless) {
      ErrorBuilder.forceCommandLine();
      loadingOutput = new LoadingCLI(System.out);
    } else {
      loadingOutput = new LoadingWindow();
    }
    loadingOutput.start();

    // RESOURCES
    loadResources(loadingOutput, hasArgument(args, "-redownloadresources"));

    // UPDATES
    Updater programUpdater = new Updater(VERSION);
    String patchNotes = null;
    if (!hasArgument(args, "-noupdate") && prefs.getBooleanPreference("LOAD_UPDATES", true)) {
      if (hasArgument(args, "-anonymous") || prefs.getBooleanPreference("ANONYMOUS_STATISTICS", false)) {
        programUpdater.enableAnonymousMode();
      }
      if (isHeadless) {
        programUpdater.enableHeadlessMode();
      }
      if (programUpdater.loadUpdates(loadingOutput)) {
        patchNotes = programUpdater.getPatchNotes();
      } else {
        System.out.println("Error loading patch notes");
      }
    }

    // NOTIFICATIONS
    if (!hasArgument(args, "-nonotifications") && prefs.getBooleanPreference("LOAD_NOTIFICATIONS", true)) {
      loadNotifications(loadingOutput, hasArgument(args, "-anonymous") || prefs.getBooleanPreference("ANONYMOUS_STATISTICS", false),
          isHeadless, prefs);
    }

    // SETUP
    loadingOutput.stop();
    Setup mySetup;
    if (hasArgument(args, "-autostart")) {
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

    // PERIODIC UPDATES
    if (mySetup.shouldCheckForUpdatesDaily()) {
      new Thread(new PeriodicUpdateChecker(programUpdater)).start();
    }

    // SETUP
    Expo myExpo = Expo.parseExpo(mySetup.getExpoToCheck());
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
    // STATUS SETUP
    if (!isHeadless) {
      StatusGUI statGUI;
      if (emailAccount != null) {
        statGUI = new StatusGUI(myExpo, emailAccount.getUsername(), emailAccount.getAddressList());
      } else {
        statGUI = new StatusGUI(myExpo);
      }
      myStatus = statGUI;
      if (hasArgument(args, "-startminimized")) {
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
    myLinkManager = new LinkManager(emailAccount);
    TicketChecker myChecker = initChecker(mySetup, isHeadless ? null : (StatusGUI) myStatus, myExpo);
    if (mySetup.shouldCheckTwitter()) {
      TwitterStreamer tcheck = setupTwitter(myStatus, twitterTokens, mySetup.shouldTextTweets());
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
    argument = argument.toLowerCase();
    for (String s : args) {
      if (s.toLowerCase().equals(argument)) {
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

  private static void loadNotifications(Startup window, boolean anonymous, boolean headless, PreferenceHandler prefs) {
    window.setStatus("Checking for notifications...");
    String lastID = prefs.getStringPreference("LAST_NOTIFICATION_ID");
    if (lastID == null) {
      lastID = "-1";
    }
    NotificationHandler notifications = new NotificationHandler(lastID);
    if (anonymous) {
      notifications.setAnonymous();
    }
    if (headless) {
      notifications.setHeadless();
    }
    notifications.loadNotifications();
    String newID = notifications.showNewNotifications();
    if (newID == null) {
      newID = lastID;
    }
    prefs.getPreferenceObject("LAST_NOTIFICATION_ID").setValue(newID);
    prefs.savePreferences();
  }

  private static void loadResources(final Startup window, boolean redownload) {
    ResourceDownloader download = new ResourceDownloader() {
      private String currentFileName;

      @Override
      public void startingFile(String fileName) {
        currentFileName = fileName;
        window.setStatus("Starting " + currentFileName);
      }

      @Override
      public void filePercentage(int percent) {
        super.filePercentage(percent);
        window.setStatus("Downloading " + currentFileName + " (" + percent + "%)");
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

  private static TicketChecker initChecker(Setup mySetup, StatusGUI myStatus, Expo expo) {
    TicketChecker myChecker = new TicketChecker(myStatus);
    if (mySetup.shouldCheckPAXWebsite()) {
      myChecker.addChecker(new CheckPaxsite(expo));
    }
    if (mySetup.shouldCheckShowclix()) {
      myChecker.addChecker(new CheckShowclix(expo, mySetup.shouldFilterShowclix()));
    }
    if (mySetup.shouldCheckKnownEvents()) {
      myChecker.addChecker(new CheckShowclixEventPage());
    }
    return myChecker;
  }

  private static TwitterStreamer setupTwitter(final Status myStatus, final String[] keys, final boolean textTweets) {
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
        myLinkManager.openLink(link, textTweets, "Link found on Twitter: " + link + " -- Tweet Text: " + statusText);
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
          myLinkManager.openLink(checker.getLinkFound(), true);
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
