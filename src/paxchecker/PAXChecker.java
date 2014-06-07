/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package paxchecker;

import java.awt.Color;
import java.io.File;
import paxchecker.GUI.*;

/**
 *
 * @author SunnyBat
 */
public class PAXChecker {

  public static final String VERSION = "1.0.8";
  private static volatile int secondsBetweenRefresh;
  private static volatile boolean forceRefresh;
  private static volatile boolean updateProgram;
  private static volatile java.awt.Image alertIcon;
  // GUIs
  protected static Setup setup;
  protected static Status status;
  protected static Tickets tickets;
  protected static Update update;

  /**
   * @param args the command line arguments
   */
  public static void main(String[] args) throws Exception {
    System.out.println("Initializing...");
    System.out.println(SettingsHandler.getPrefsPath());
    javax.swing.ToolTipManager.sharedInstance().setDismissDelay(600000); // Make Tooltips stay forever
    boolean doUpdate = true;
    if (args.length > 0) {
      System.out.println("Args!");
      for (int a = 0; a < args.length; a++) {
        System.out.println("args[" + a + "] = " + args[a]);
        if (args[a].equals("noupdate")) { // Used by the program when starting the new version just downloaded. Can also be used if you don't want updates
          doUpdate = false;
        }
      }
    }
    Browser.init();
    Email.init();
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
        ErrorManagement.showErrorWindow("ERROR", "An error has occurred while attempting to update the program. If the problem persists, please manually download the latest version.", e);
        ErrorManagement.fatalError();
        return;
      }
    }
    setup = new Setup();
    while (!setup.isVisible()) {
      Thread.sleep(100);
    }
    while (setup.isVisible()) {
      Thread.sleep(100);
    }
    setup = null;
    update = null;
    savePrefsInBackground();
    System.gc();
    status = new Status();
    long startMS;
    int seconds = secondsBetweenRefresh; // Saves time from accessing volatile variable; can be moved to inside do while if secondsBetweenRefresh can be changed when do while is running
    do {
      //status.setLastCheckedText("Checking for updates...");
      startMS = System.currentTimeMillis();
      if (Browser.isPAXWebsiteUpdated()) {
        Email.sendEmailInBackground("PAX Tickets ON SALE!", "The PAX website has been updated!");
        showTicketsWindow();
        Audio.playAlarm();
        Browser.openLinkInBrowser(Browser.parseHRef(Browser.getCurrentButtonLinkLine())); // Last, because Browser.getCurrentButtonLinkLine() takes a while to do
        status.dispose();
        break;
      }
      if (Browser.isShowclixUpdated()) {
        Email.sendEmailInBackground("PAX Tickets ON SALE!", "The Showclix website has been updated!");
        showTicketsWindow();
        Audio.playAlarm();
        Browser.openLinkInBrowser(Browser.getShowclixLink()); // Last, because Browser.getShowclixLink() takes a while to do
        status.dispose();
        break;
      }
      while (System.currentTimeMillis() - startMS < (seconds * 1000)) {
        if (forceRefresh) {
          forceRefresh = false;
          break;
        }
        Thread.sleep(100);
        status.setLastCheckedText(seconds - (int) ((System.currentTimeMillis() - startMS) / 1000));
      }
    } while (status.isVisible());
    System.out.println("Finished!");
  }

  /**
   * Sets the time between checking the PAX Registration website for updates. This can be called at
   * any time, however it is recommended to only call it during Setup.
   *
   * @param seconds The amount of seconds between website updates.
   */
  public static void setRefreshTime(int seconds) {
    secondsBetweenRefresh = seconds;
  }

  /**
   * Forces the program to check the PAX website for updates. Note that this resets the time since
   * last check to 0.
   */
  public static void forceRefresh() {
    forceRefresh = true;
    if (status != null) {
      status.setButtonStatusText("Forced website check!");
    }
  }

  /**
   * Set the updateProgram flag to true. This will start the program updating process. This should
   * only be called by the Update GUI when the main() method is waiting for the prompt.
   */
  public static void startUpdatingProgram() {
    updateProgram = true;
  }

  /**
   * Creates the Tickets window and makes it visible. This should really only be called once, as
   * subsequent calls will rewrite {@link #tickets} and lose the object reference to the previously
   * opened tickets window.
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

  public static void startNewProgramInstance() {
    try {
      String path = PAXChecker.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
      ProcessBuilder pb = new ProcessBuilder(System.getProperty("java.home") + "\\bin\\javaw.exe", "-jar", new File(path).getAbsolutePath()); // path can have leading / on it, getAbsolutePath() removes them
      Process p = pb.start();
    } catch (Exception e) {
      ErrorManagement.showErrorWindow("Small Error", "Unable to automatically run update.", null);
    }
  }

  /**
   * This makes a new daemon, low-priority Thread and runs it. This is currently unused.
   *
   * @param run The Runnable to make into a Thread and run
   */
  public static void startBackgroundThread(Runnable run) {
    Thread newThread = new Thread(run);
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
    });
  }

  public static String getIconName(String expo) {
    switch (expo.toLowerCase()) { // toLowerCase to lower the possibilities (and readability)
      case "prime":
      case "pax prime":
        return "PAXPrime.png";
      case "east":
      case "pax east":
        return "PAXEast.png";
      case "aus":
      case "pax aus":
        return "PAXAus.png";
      case "dev":
      case "pax dev":
        return "PAXDev.png";
      default:
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
        }
      }
    });
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
          ErrorManagement.showErrorWindow("Small Error", "Unable to automatically run update.", null);
        }
      }
    });
  }

  public static void loadPatchNotesInBackground() {
    startBackgroundThread(new Runnable() {
      @Override
      public void run() {
        Browser.loadVersionNotes();
      }
    });
  }

  public static void savePrefsInBackground() {
    startBackgroundThread(new Runnable() {
      @Override
      public void run() {
        SettingsHandler.savePrefs(secondsBetweenRefresh, Browser.isCheckingPaxWebsite(), Browser.isCheckingShowclix(), Audio.soundEnabled(), Browser.getExpo(), Email.getProvider());
      }
    });
  }
}
