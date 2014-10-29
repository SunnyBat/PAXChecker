package paxchecker;

import java.awt.Color;
import java.io.File;
import java.util.Scanner;
import paxchecker.GUI.*;

/**
 *
 * @author SunnyBat
 */
public class PAXChecker {

  public static final String VERSION = "1.7.2.2";
  private static volatile int secondsBetweenRefresh = 10;
  private static volatile boolean forceRefresh;
  private static volatile java.awt.Image alertIcon;
  private static final Scanner myScanner = new Scanner(System.in);
  // GUIs
  protected static Setup setup;
  protected static Status status;
  protected static Tickets tickets;

  /**
   * @param args the command line arguments
   */
  public static void main(String[] args) {
    loadPatchNotesInBackground();
    prefetchIconsInBackground();
    System.out.println("Initializing...");
    javax.swing.ToolTipManager.sharedInstance().setDismissDelay(600000); // Make Tooltips stay forever
    Email.init();
    KeyboardHandler.init();
    parseCommandLineArgs(args);
  }

  public static void parseCommandLineArgs(String[] args) {
    boolean doUpdate = true;
    boolean autoStart = false;
    boolean commandLine = false;
    if (args.length > 0) {
      System.out.println("Args!");
      boolean checkPax = true;
      boolean checkShowclix = true;
      argsCycle:
      for (int a = 0; a < args.length; a++) {
        System.out.println("args[" + a + "] = " + args[a]);
        switch (args[a].toLowerCase()) {
          case "-noupdate":
            // Used by the program when starting the new version just downloaded. Can also be used if you don't want updates
            doUpdate = false;
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
            System.out.println("Setting check PAX website to false");
            checkPax = false;
            break;
          case "-noshowclix":
            System.out.println("Setting check Showclix website to false");
            checkShowclix = false;
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
          default:
            if (args[a].startsWith("-")) {
              System.out.println("Unknown argument: " + args[a]);
            }
            break;
        }
      }
      if (checkPax) {
        Browser.enablePaxWebsiteChecking();
      }
      if (checkShowclix) {
        Browser.enableShowclixWebsiteChecking();
      }
      if (autoStart && !Browser.isCheckingPaxWebsite() && !Browser.isCheckingShowclix()) {
        System.out.println("ERROR: Program is not checking PAX or Showclix website. Program will now exit.");
        System.exit(0);
      }
    }
    if (commandLine) {
      ErrorHandler.setCommandLine(true);
      if (doUpdate) {
        UpdateHandler.autoUpdate(args);
      }
      Checker.commandLineSettingsInput();
      Checker.startCommandLineWebsiteChecking();
      return;
    }
    if (doUpdate) {
      UpdateHandler.checkUpdate(args);
    }
    if (autoStart) {
      Checker.startCheckingWebsites();
    } else {
      setup = new Setup();
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
   * Starts a new instance of the program with the given arguments.
   *
   * @param args
   */
  public static void startNewProgramInstance(String... args) {
    try {
      String[] nArgs;
      String path = PAXChecker.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
      if (args != null && args.length > 0) {
        nArgs = new String[args.length + 3];
        System.arraycopy(args, 0, nArgs, 3, args.length);
      } else {
        nArgs = new String[3];
      }
      nArgs[0] = System.getProperty("java.home") + "\\bin\\javaw.exe";
      nArgs[1] = "-jar";
      nArgs[2] = new File(path).getAbsolutePath(); // path can have leading / on it, getAbsolutePath() removes them
      ProcessBuilder pb = new ProcessBuilder(nArgs);
      pb.start();
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
   * Loads the program icons in the background. Note that this starts a new Thread and therefore does not block.
   */
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

  /**
   * Gets the icon name for the given expo.
   *
   * @param expo The name of the expo
   * @return The name of the icon for the given expo
   */
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

  /**
   * Sets the icon of the Status window. Note that this checks the /resources/ folder located in the JAR file for the filename, regardless of what the
   * iconName is.
   *
   * @param iconName The name of the icon to load
   */
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

  /**
   * Loads the Patch Notes on a new daemon Thread. This also sets the Patch Notes in the Setup window if possible.
   */
  public static void loadPatchNotesInBackground() {
    startBackgroundThread(new Runnable() {
      @Override
      public void run() {
        UpdateHandler.loadVersionNotes();
        if (UpdateHandler.getVersionNotes() != null && setup != null) {
          setup.setPatchNotesText(UpdateHandler.getVersionNotes());
        }
      }
    }, "Load Patch Notes");
  }

  /**
   * Sends a test email on a daemon Thread. Note that this also updates the Status window if possible.
   */
  public static void sendBackgroundTestEmail() {
    if (status == null) {
      Email.testEmail();
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
