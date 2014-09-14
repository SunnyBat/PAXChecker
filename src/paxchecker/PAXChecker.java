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

  public static final String VERSION = "1.6.6";
  public static final String REDDIT_THREAD_LINK = "http://www.reddit.com/r/PAX/comments/25inam/pax_registration_website_checker_java/";
  private static volatile int secondsBetweenRefresh = 10;
  private static volatile boolean forceRefresh;
  private static volatile boolean updateProgram;
  private static volatile java.awt.Image alertIcon;
  private static boolean shouldTypeLink;
  private static final Scanner myScanner = new Scanner(System.in);
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
            shouldTypeLink = true;
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
            setRefreshTime(Integer.getInteger(args[a + 1], 15));
            System.out.println("Set refresh time to " + getRefreshTime());
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
    Browser.init();
    Email.init();
    KeyboardHandler.init();
    prefetchIconsInBackground();
    loadPatchNotesInBackground();
    if (commandLine) {
      if (doUpdate) {
        try {
          System.out.println("Checking for updates...");
          if (Browser.updateAvailable()) {
            System.out.println("Update found, downloading update...");
            Browser.updateProgram();
            startNewProgramInstance(args);
            return;
          }
        } catch (Exception e) {
          ErrorHandler.showErrorWindow("ERROR", "An error has occurred while attempting to update the program. If the problem persists, please manually download the latest version.", e);
          ErrorHandler.fatalError();
          return;
        }
      }
      commandLineSettingsInput();
      startCommandLineWebsiteChecking();
      return;
    }
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
            PAXChecker.startNewProgramInstance();
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
    if (autoStart) {
      startCheckingWebsites();
    } else {
      setup = new Setup();
    }
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

  public static void commandLineSettingsInput() {
    if (Email.getUsername() == null) {
      System.out.print("Username: ");
      try {
        Email.setUsername(myScanner.next());
        System.out.println("Password: ");
        Email.setPassword(myScanner.next());
      } catch (Exception e) {}
    }
    if (Email.getAddressList().isEmpty()) {
      System.out.print("Cell Number: ");
      try {
        Email.addEmailAddress(myScanner.next());
        System.out.println();
      } catch (Exception e) {}
    }
    if (Browser.isCheckingPaxWebsite()) {
      System.out.print("Check PAX Website (Y/N): ");
      try {
        if (!myScanner.next().toLowerCase().startsWith("n")) {
          Browser.enablePaxWebsiteChecking();
        }
        System.out.println();
      } catch (Exception e) {}
    }
    if (Browser.isCheckingPaxWebsite()) {
      System.out.print("Check Showclix Website (Y/N): ");
      try {
        if (!myScanner.next().toLowerCase().startsWith("n")) {
          Browser.enableShowclixWebsiteChecking();
        }
        System.out.println();
      } catch (Exception e) {}
    }
    if (getRefreshTime() == 10) {
      System.out.print("Refresh Time (seconds, no input limit at the moment): ");
      try {
        setRefreshTime(Integer.parseInt(myScanner.next(), 10));
        System.out.println();
      } catch (Exception e) {}
    }
    if (!Browser.isCheckingPaxWebsite()) {
      System.out.print("Play Alarm (Y/N): ");
      try {
        if (!myScanner.next().toLowerCase().startsWith("n")) {
          Audio.setPlayAlarm(true);
        }
        System.out.println();
      } catch (Exception e) {}
    }
    if (Browser.getExpo() == null) {
      System.out.print("Expo: ");
      try {
        String input = myScanner.next();
        switch (input.toLowerCase()) {
          case "prime":
          case "paxprime":
          case "pax prime":
            Browser.setExpo("PAX Prime");
          case "east":
          case "paxeast":
          case "pax east":
            Browser.setExpo("PAX East");
          case "south":
          case "paxsouth":
          case "pax south":
            Browser.setExpo("PAX South");
          case "aus":
          case "australia":
          case "paxaus":
          case "paxaustralia":
          case "pax aus":
          case "pax australia":
            Browser.setExpo("PAX Aus");
          default:
            System.out.println("Invalid expo! Setting to Prime...");
            Browser.setExpo("PAX Prime");
        }
        System.out.println();
      } catch (Exception e) {}
    }
  }

  public static void startCommandLineWebsiteChecking() {
    continueProgram(new Runnable() {
      @Override
      public void run() {
        String input;
        while (true) {
          try {
            input = myScanner.next();
          } catch (Exception e) {
            //e.printStackTrace();
            System.out.println("Error parsing input -- please try again.");
            continue;
          }
          switch (input.toLowerCase()) {
            case "exit":
              System.exit(0);
              break;
            case "test text":
              sendBackgroundTestEmail();
              break;
            case "test alaram":
              Audio.playAlarm();
              break;
            case "refresh":
            case "force check":
            case "check":
              forceRefresh = true;
              break;
            default:
              System.out.println("Unknown command: " + input.toLowerCase());
              System.out.println("Commands:");
              System.out.println("exit        - Exit the program");
              System.out.println("test text   - Send a test text");
              System.out.println("test alarm  - Play the alarm (if enabled)");
              System.out.println("refresh     - Force check");
              System.out.println("check       - Force check");
              System.out.println("force check - Force check");
              System.out.println("Commands are NOT case sensitive.");
          }
        }
      }
    });
    continueProgram(new Runnable() {
      @Override
      public void run() {
        if (!Browser.checkShowclixLink(SettingsHandler.getLastEvent())) {
          SettingsHandler.saveLastEvent(Browser.getShowclixLink());
        }
        //System.gc();
        long startMS;
        int seconds = getRefreshTime(); // Saves time from accessing volatile variable; can be moved to inside do while if secondsBetweenRefresh can be changed when do while is running
        do {
          //status.setLastCheckedText("Checking for updates...");
          startMS = System.currentTimeMillis();
          if (Browser.isPAXWebsiteUpdated()) {
            final String link = Browser.parseHRef(Browser.getCurrentButtonLinkLine());
            System.out.println("LINK FOUND: " + link);
            //Browser.openLinkInBrowser(link);
            Email.sendEmailInBackground("PAX Tickets ON SALE!", "The PAX website has been updated! URL found (in case of false positives): " + link);
            Audio.playAlarm();
            break;
          }
          if (Browser.isShowclixUpdated()) {
            final String link = Browser.getShowclixLink();
            System.out.println("LINK FOUND: " + link);
            //Browser.openLinkInBrowser(link); // Separate Thread because Browser.getShowclixLink() takes a while to do
            Email.sendEmailInBackground("PAX Tickets ON SALE!", "The Showclix website has been updated! URL found (in case of false positives): " + link);
            Audio.playAlarm();
            break;
          }
          //status.setDataUsageText(Browser.getDataUsedMB());
          System.out.println("Data used: " + Browser.getDataUsedMB() + "MB");
          while (System.currentTimeMillis() - startMS < (seconds * 1000)) {
            if (forceRefresh) {
              forceRefresh = false;
              break;
            }
            try {
              Thread.sleep(100);
            } catch (InterruptedException interruptedException) {
            }
            //status.setLastCheckedText(seconds - (int) ((System.currentTimeMillis() - startMS) / 1000));
          }
        } while (true); // Change later
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
      nArgs[2] = new File(path).getAbsolutePath();
      ProcessBuilder pb = new ProcessBuilder(nArgs); // path can have leading / on it, getAbsolutePath() removes them
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
          ProcessBuilder pb = new ProcessBuilder(System.getProperty("java.home") + "\\bin\\javaw.exe", "-jar", new File(path).getAbsolutePath(), "-noupdate"); // path can have leading / on it, getAbsolutePath() removes them
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
