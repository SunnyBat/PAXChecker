package paxchecker;

import java.awt.Color;
import java.io.File;
import paxchecker.GUI.*;

/**
 *
 * @author SunnyBat
 */
public class PAXChecker {

  public static final String VERSION = "1.6.3";
  public static final String REDDIT_THREAD_LINK = "http://www.reddit.com/r/PAX/comments/25inam/pax_registration_website_checker_java/";
  private static volatile int secondsBetweenRefresh;
  private static volatile boolean forceRefresh;
  private static volatile boolean updateProgram;
  private static volatile java.awt.Image alertIcon;
  private static boolean shouldTypeLink;
  // GUIs
  protected static Setup setup;
  protected static Status status;
  protected static Tickets tickets;
  protected static Update update;

  /**
   * @param args the command line arguments
   */
  public static void main(String[] args) {
    System.out.println("Current Time = " + Tickets.currentTime());
    System.out.println("Initializing...");
    javax.swing.ToolTipManager.sharedInstance().setDismissDelay(600000); // Make Tooltips stay forever
    boolean doUpdate = true;
    if (args.length > 0) {
      System.out.println("Args!");
      for (int a = 0; a < args.length; a++) {
        System.out.println("args[" + a + "] = " + args[a]);
        if (args[a].equals("noupdate")) { // Used by the program when starting the new version just downloaded. Can also be used if you don't want updates
          doUpdate = false;
        } else if (args[a].equals("typelink")) {
          shouldTypeLink = true;
        }
      }
    }
    Browser.init();
    Email.init();
    KeyboardHandler.init();
    prefetchIconsInBackground();
    loadPatchNotesInBackground();
    if (doUpdate) {
      try {
        System.out.println("Checking for updates...");
        if (Browser.updateAvailable()) {
          update = new Update();
          while (update.isVisible() && !updateProgram) {
            Thread.sleep(100);
          }
          if (updateProgram) {
            update.setStatusLabelText("Downloading update...");
            Browser.updateProgram();
            update.dispose();
            return;
          }
          update.dispose();
        }
      } catch (Exception e) {
        ErrorHandler.showErrorWindow("ERROR", "An error has occurred while attempting to update the program. If the problem persists, please manually download the latest version.", e);
        ErrorHandler.fatalError();
        return;
      }
    }
    setup = new Setup();
  }

  public static void startCheckingWebsites() {
    continueProgram(new Runnable() {
      @Override
      public void run() {
        setup = null;
        update = null;
        savePrefsInBackground();
        if (!Browser.checkShowclixLink(SettingsHandler.getLastEvent())) {
          SettingsHandler.saveLastEvent(Browser.getShowclixLink());
        }
        //System.gc();
        status = new Status();
        setStatusIconInBackground(getIconName(Browser.getExpo()));
        long startMS;
        int seconds = getRefreshTime(); // Saves time from accessing volatile variable; can be moved to inside do while if secondsBetweenRefresh can be changed when do while is running
        do {
          //status.setLastCheckedText("Checking for updates...");
          startMS = System.currentTimeMillis();
          if (Browser.isPAXWebsiteUpdated()) {
            final String link = Browser.parseHRef(Browser.getCurrentButtonLinkLine());
            KeyboardHandler.typeLinkNotification(link);
            Browser.openLinkInBrowser(link);
            Email.sendEmailInBackground("PAX Tickets ON SALE!", "The PAX website has been updated! URL found (in case of false positives): " + link);
            showTicketsWindow(link);
            status.dispose();
            Audio.playAlarm();
            break;
          }
          if (Browser.isShowclixUpdated()) {
            final String link = Browser.getShowclixLink();
            KeyboardHandler.typeLinkNotification(link);
            Browser.openLinkInBrowser(link); // Separate Thread because Browser.getShowclixLink() takes a while to do
            Email.sendEmailInBackground("PAX Tickets ON SALE!", "The Showclix website has been updated! URL found (in case of false positives): " + link);
            showTicketsWindow(link);
            status.dispose();
            Audio.playAlarm();
            break;
          }
          status.setDataUsageText(Browser.getDataUsedMB());
          while (System.currentTimeMillis() - startMS < (seconds * 1000)) {
            if (forceRefresh) {
              forceRefresh = false;
              break;
            }
            try {
              Thread.sleep(100);
            } catch (InterruptedException interruptedException) {
            }
            status.setLastCheckedText(seconds - (int) ((System.currentTimeMillis() - startMS) / 1000));
          }
        } while (status.isDisplayable());
        System.out.println("Finished!");
      }
    });
  }

  /**
   * Gets the time (in seconds) between website checks. This method is thread-safe.
   *
   * @return The amount of time between website checks
   */
  public static int getRefreshTime() {
    return secondsBetweenRefresh;
  }

  public static boolean shouldTypeLink() {
    return shouldTypeLink;
  }

  public static void maximizeStatusWindow() {
    status.maximizeWindow();
  }

  public static void continueProgram(Runnable run) {
    Thread newThread = new Thread(run);
    newThread.setName("Program Loop");
    newThread.setDaemon(false); // Prevent the JVM from stopping due to zero non-daemon threads running
    newThread.setPriority(Thread.NORM_PRIORITY);
    newThread.start(); // Start the Thread
  }

  /**
   * Sets the time between checking the PAX Registration website for updates. This can be called at any time, however it is recommended to only call
   * it during Setup.
   *
   * @param seconds The amount of seconds between website updates.
   */
  public static void setRefreshTime(int seconds) {
    secondsBetweenRefresh = seconds;
  }

  /**
   * Forces the program to check the PAX website for updates. Note that this resets the time since last check to 0.
   */
  public static void forceRefresh() {
    forceRefresh = true;
    if (status != null) {
      status.setButtonStatusText("Forced website check!");
    }
  }

  /**
   * Set the updateProgram flag to true. This will start the program updating process. This should only be called by the Update GUI when the main()
   * method is waiting for the prompt.
   */
  public static void startUpdatingProgram() {
    updateProgram = true;
  }

  /**
   * Creates the Tickets window and makes it visible. This should really only be called once, as subsequent calls will rewrite {@link #tickets} and
   * lose the object reference to the previously opened tickets window.
   */
  public static void showTicketsWindow() {
    tickets = new Tickets();
    try {
      tickets.setIconImage(alertIcon);
      tickets.setBackground(Color.RED);
    } catch (Exception e) {
      System.out.println("Unable to set IconImage!");
      e.printStackTrace();
    }
  }

  /**
   * Creates the Tickets window and makes it visible. This should really only be called once, as subsequent calls will rewrite {@link #tickets} and
   * lose the object reference to the previously opened tickets window.
   *
   * @param link The URL that was found by the program
   */
  public static void showTicketsWindow(String link) {
    tickets = new Tickets(link);
    try {
      tickets.setIconImage(alertIcon);
      tickets.setBackground(Color.RED);
    } catch (Exception e) {
      System.out.println("Unable to set IconImage!");
      e.printStackTrace();
    }
  }

  public static void startNewProgramInstance() {
    try {
      String path = PAXChecker.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
      ProcessBuilder pb = new ProcessBuilder(System.getProperty("java.home") + "\\bin\\javaw.exe", "-jar", new File(path).getAbsolutePath()); // path can have leading / on it, getAbsolutePath() removes them
      Process p = pb.start();
    } catch (Exception e) {
      ErrorHandler.showErrorWindow("Small Error", "Unable to automatically run update.", null);
    }
  }

  /**
   * This makes a new daemon, low-priority Thread and runs it.
   *
   * @param run The Runnable to make into a Thread and run
   */
  public static void startBackgroundThread(Runnable run) {
    startBackgroundThread(run, "General Background Thread");
  }

  public static void startBackgroundThread(Runnable run, String name) {
    Thread newThread = new Thread(run);
    newThread.setName(name);
    newThread.setDaemon(true); // Kill the JVM if only daemon threads are running
    newThread.setPriority(Thread.MIN_PRIORITY); // Let other Threads take priority, as this will probably not run for long
    newThread.start(); // Start the Thread
  }

  public static void prefetchIconsInBackground() {
    startBackgroundThread(new Runnable() {
      @Override
      public void run() {
        try {
          alertIcon = javax.imageio.ImageIO.read(PAXChecker.class.getResourceAsStream("/resources/alert.png"));
        } catch (Exception e) {
          System.out.println("Unable to fetch PAX Icon!");
        }
      }
    }, "Prefetch Icons");
  }

  public static String getIconName(String expo) {
    switch (expo.toLowerCase()) { // toLowerCase to lower the possibilities (and readability)
      case "prime":
      case "pax prime":
        return "PAXPrime.png";
      case "east":
      case "pax east":
        return "PAXEast.png";
      case "south":
      case "pax south":
        return "PAXSouth.png";
      case "aus":
      case "pax aus":
        return "PAXAus.png";
      case "dev":
      case "pax dev":
        return "PAXDev.png";
      default:
        System.out.println("getIconName(): Unknown PAX expo: " + expo);
        return "PAXPrime.png";
    }
  }

  public static void setStatusIconInBackground(final String iconName) {
    startBackgroundThread(new Runnable() {
      @Override
      public void run() {
        try {
          if (status != null) {
            status.setIcon(javax.imageio.ImageIO.read(PAXChecker.class.getResourceAsStream("/resources/" + iconName)));
          }
        } catch (Exception e) {
          System.out.println("Unable to load PAX icon: " + iconName);
          e.printStackTrace();
        }
      }
    }, "Set Status Icon");
  }

  public static void startNewProgramInstanceInBackground() {
    startBackgroundThread(new Runnable() {
      @Override
      public void run() {
        try {
          String path = PAXChecker.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
          ProcessBuilder pb = new ProcessBuilder(System.getProperty("java.home") + "\\bin\\javaw.exe", "-jar", new File(path).getAbsolutePath(), "noupdate"); // path can have leading / on it, getAbsolutePath() removes them
          Process p = pb.start();
        } catch (Exception e) {
          ErrorHandler.showErrorWindow("Small Error", "Unable to automatically run update. Program must be restarted manually. Sorry for the inconvenience. :(", null);
        }
      }
    }, "Run New Program Instance");
  }

  public static void loadPatchNotesInBackground() {
    startBackgroundThread(new Runnable() {
      @Override
      public void run() {
        Browser.loadVersionNotes();
        if (Browser.getVersionNotes() != null && setup != null) {
          setup.setPatchNotesText(Browser.getVersionNotes());
        }
      }
    }, "Load Patch Notes");
  }

  public static void savePrefsInBackground() {
    startBackgroundThread(new Runnable() {
      @Override
      public void run() {
        SettingsHandler.saveAllPrefs();
      }
    }, "Save Preferences");
  }

  public static void sendBackgroundTestEmail() {
    if (status == null) {
      return;
    }
    startBackgroundThread(new Runnable() {
      @Override
      public void run() {
        try {
          status.setTextButtonState(false);
          status.setTextButtonText("Sending...");
          if (!Email.testEmail()) {
            status.setTextButtonText("Test Text");
            status.setTextButtonState(true);
            return;
          }
          long timeStarted = System.currentTimeMillis();
          while (System.currentTimeMillis() - timeStarted < 60000) {
            status.setTextButtonText((60 - (int) ((System.currentTimeMillis() - timeStarted) / 1000)) + "");
            Thread.sleep(200);
          }
          status.setTextButtonText("Test Text");
          status.setTextButtonState(true);
        } catch (Exception e) {
          System.out.println("ERROR sending background test email!");
          e.printStackTrace();
          status.setTextButtonText("Test Text");
          status.setTextButtonState(true);
        }
      }
    }, "Send Test Email");
  }
}
