/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package paxchecker;

import java.awt.Color;
import java.io.IOException;
import java.util.Scanner;
import paxchecker.GUI.*;

/**
 *
 * @author Sunny
 */
public class Checker {

  private static volatile int secondsBetweenRefresh = 10;
  private static volatile boolean forceRefresh;
  private static volatile java.awt.Image alertIcon;
  private static final Scanner myScanner = new Scanner(System.in);
  // GUIs
  private static Status status;
  private static Tickets tickets;

  /**
   * Starts a new non-daemon Thread that checks the websites for updates. This Thread also updates the Status GUI.
   */
  public static void startCheckingWebsites() {
    PAXChecker.continueProgram(new Runnable() {
      @Override
      public void run() {
        SettingsHandler.saveAllPrefs();
        if (!Browser.checkShowclixLink(SettingsHandler.getLastEvent())) {
          SettingsHandler.saveLastEvent(Browser.getShowclixLink());
          System.out.println("NOTE: Link has changed since last time!");
        }
        status = new Status();
        setStatusIconInBackground(getIconName(Browser.getExpo()));
        long startMS;
        int seconds = getRefreshTime(); // Saves time from accessing volatile variable; can be moved to inside do while if secondsBetweenRefresh can be changed when do while is running
        do {
          //status.setLastCheckedText("Checking for updates...");
          startMS = System.currentTimeMillis();
          if (Browser.isShowclixUpdated() || Browser.isPAXWebsiteUpdated()) {
            final String link = Browser.parseHRef(Browser.getCurrentButtonLinkLine());
            linkFound(link);
            break;
          }
          status.setDataUsageText(DataTracker.getDataUsedMB());
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
   * Prompts the user for the required program information, including username, password, email, and other options. Note that this does NOT start the
   * command-line website checking.
   */
  public static void commandLineSettingsInput() {
    if (Email.getUsername() == null) {
      System.out.print("Email: ");
      try {
        Email.setUsername(myScanner.next());
        System.out.println("Password: ");
        Email.setPassword(myScanner.next());
      } catch (Exception e) {
      }
    }
    if (Email.getAddressList().isEmpty()) {
      System.out.print("Cell Number: ");
      try {
        Email.addEmailAddress(myScanner.next());
        System.out.println();
      } catch (Exception e) {
      }
    }
    if (Browser.isCheckingPaxWebsite()) {
      System.out.print("Check PAX Website (Y/N): ");
      try {
        if (!myScanner.next().toLowerCase().startsWith("n")) {
          Browser.enablePaxWebsiteChecking();
        }
        System.out.println();
      } catch (Exception e) {
      }
    }
    if (Browser.isCheckingPaxWebsite()) {
      System.out.print("Check Showclix Website (Y/N): ");
      try {
        if (!myScanner.next().toLowerCase().startsWith("n")) {
          Browser.enableShowclixWebsiteChecking();
        }
        System.out.println();
      } catch (Exception e) {
      }
    }
    if (getRefreshTime() == 10) {
      System.out.print("Refresh Time (seconds, no input limit at the moment): ");
      try {
        setRefreshTime(Integer.parseInt(myScanner.next(), 10));
        System.out.println();
      } catch (Exception e) {
      }
    }
    if (!Browser.isCheckingPaxWebsite()) {
      System.out.print("Play Alarm (Y/N): ");
      try {
        if (!myScanner.next().toLowerCase().startsWith("n")) {
          Audio.setPlayAlarm(true);
        }
        System.out.println();
      } catch (Exception e) {
      }
    }
    if (Browser.getExpo() == null) {
      System.out.print("Expo: ");
      try {
        String input = myScanner.next();
        switch (input.toLowerCase()) {
          case "prime":
          case "paxprime":
            Browser.setExpo("PAX Prime");
          case "east":
          case "paxeast":
            Browser.setExpo("PAX East");
          case "south":
          case "paxsouth":
            Browser.setExpo("PAX South");
          case "aus":
          case "australia":
          case "paxaus":
          case "paxaustralia":
            Browser.setExpo("PAX Aus");
          default:
            System.out.println("Invalid expo! Setting to Prime...");
            Browser.setExpo("PAX Prime");
        }
        System.out.println();
      } catch (Exception e) {
      }
    }
  }

  /**
   * Starts checking for website updates and listening for commands given through the console.
   */
  public static void startCommandLineWebsiteChecking() {
    PAXChecker.continueProgram(new Runnable() {
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
            case "testtext":
              PAXChecker.sendTestEmail();
              break;
            case "testalarm":
              Audio.playAlarm();
              break;
            case "refresh":
            case "check":
              forceRefresh = true;
              break;
            default:
              System.out.println("Unknown command: " + input.toLowerCase());
              System.out.println("Commands:");
              System.out.println("exit        - Exit the program");
              System.out.println("testtext    - Send a test text");
              System.out.println("testalarm   - Play the alarm (if enabled)");
              System.out.println("refresh     - Force check");
              System.out.println("check       - Force check");
              System.out.println("Commands are NOT case sensitive.");
          }
        }
      }
    });
    PAXChecker.continueProgram(new Runnable() {
      @Override
      public void run() {
        //System.gc();
        int seconds = getRefreshTime(); // Saves time from accessing volatile variable; can be moved to inside do while if secondsBetweenRefresh can be changed when do while is running
        do {
          //status.setLastCheckedText("Checking for updates...");
          long startMS = System.currentTimeMillis();
          if (Browser.isShowclixUpdated() || Browser.isPAXWebsiteUpdated()) {
            final String link = Browser.parseHRef(Browser.getCurrentButtonLinkLine());
            System.out.println("LINK FOUND: " + link);
            Email.sendEmailInBackground("PAX Tickets ON SALE!", "PAX Tickets have been found! URL: " + link);
            Browser.openLinkInBrowser(link);
            Audio.playAlarm();
            break;
          }
          System.out.println("Data used: " + DataTracker.getDataUsedMB() + "MB");
          while (System.currentTimeMillis() - startMS < (seconds * 1000)) {
            if (forceRefresh) {
              forceRefresh = false;
              break;
            }
            try {
              Thread.sleep(100);
            } catch (InterruptedException iE) {
            }
          }
        } while (true); // Change later
        System.out.println("Finished!");
      }
    });
  }

  /**
   * Does everything when a Showclix link is found. Opens a Tickets Found window, sends a text, plays an alarm, etc.
   *
   * @param link The link to use for everything
   */
  public static void linkFound(String link) {
    KeyboardHandler.typeLinkNotification(link);
    Browser.openLinkInBrowser(link); // Separate Thread because Browser.getShowclixLink() takes a while to do
    Email.sendEmailInBackground("PAX Tickets ON SALE!", "PAX Tickets have been found! URL: " + link);
    showTicketsWindow(link);
    status.dispose();
    Audio.playAlarm();
  }

  /**
   * Gets the time (in seconds) between website checks. This method is thread-safe.
   *
   * @return The amount of time between website checks
   */
  public static int getRefreshTime() {
    return secondsBetweenRefresh;
  }

  /**
   * Maximizes the Status window.
   */
  public static void maximizeStatusWindow() {
    if (status == null) {
      return;
    }
    status.maximizeWindow();
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
      status.setInformationText("Forced website check!");
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

  public static void setStatusButtonText(String s) {
    if (status != null) {
      status.setInformationText(s);
    }
  }

  public static void setStatusWebsiteLink(String s) {
    if (status != null) {
      status.setWebsiteLink(s);
    }
  }

  public static void setStatusShowclixLink(String s) {
    if (status != null) {
      status.setShowclixLink("Unable to to connect to the Showclix website!");
    }
  }

  public static void setStatusTextButtonState(boolean enabled) {
    if (status != null) {
      status.setTextButtonState(enabled);
    }
  }

  public static void setStatusTextButtonText(String s) {
    if (status != null) {
      status.setTextButtonText(s);
    }
  }

  public static void loadAlertIcon() {
    try {
      alertIcon = javax.imageio.ImageIO.read(Checker.class.getResourceAsStream("/resources/alert.png"));
    } catch (IOException iOException) {
    }
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
    try {
      if (Checker.status != null) {
        Checker.status.setIcon(javax.imageio.ImageIO.read(PAXChecker.class.getResourceAsStream("/resources/" + iconName)));
      }
    } catch (Exception e) {
      System.out.println("Unable to load PAX icon: " + iconName);
      e.printStackTrace();
    }
  }

}
