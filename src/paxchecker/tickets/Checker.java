package paxchecker.tickets;

import paxchecker.update.UpdateHandler;
import paxchecker.gui.Status;
import paxchecker.gui.Tickets;
import java.awt.Color;
import java.io.IOException;
import java.util.Scanner;
import paxchecker.Audio;
import paxchecker.Browser;
import paxchecker.DataTracker;
import paxchecker.Email;
import paxchecker.KeyboardHandler;
import paxchecker.PAXChecker;
import paxchecker.SettingsHandler;
import paxchecker.check.TicketChecker;

/**
 *
 * @author Sunny
 * @deprecated
 */
public class Checker {

  private static volatile int secondsBetweenRefresh = 10;
  private static volatile boolean forceRefresh;
  private static volatile java.awt.Image alertIcon;
  private static final Scanner myScanner = new Scanner(System.in);
  // GUIs
  private static final Status status = new Status();

  public static void hackedAroundStatus() {
    TicketChecker.init(status);
  }

  /**
   * Starts a new non-daemon Thread that checks the websites for updates. This Thread also updates the Status GUI.
   */
  public static void startCheckingWebsites() {
    PAXChecker.continueProgram(new Runnable() {
      @Override
      public void run() {
        if (!Showclix.checkShowclixLink(SettingsHandler.getLastEvent())) {
          SettingsHandler.saveLastEvent(Showclix.getShowclixLink());
          System.out.println("NOTE: Link has changed since last time!");
        }
        status.setVisible(true);
        setStatusIconInBackground(getIconName(Browser.getExpo()));
        long startMS;
        int seconds = getRefreshTime(); // Saves time from accessing volatile variable; can be moved to inside do while if secondsBetweenRefresh can be changed when do while is running
        do {
          status.setLastCheckedText("Checking for updates...");
          startMS = System.currentTimeMillis();
          if (TicketChecker.isUpdated()) {
            linkFound(TicketChecker.getLinkFound());
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
        System.out.print("Password: ");
        Email.setPassword(myScanner.next());
      } catch (Exception e) {
      }
    }
    if (Email.getAddressList().isEmpty()) {
      System.out.print("Cell Number: ");
      try {
        Email.addEmailAddress(myScanner.next());
      } catch (Exception e) {
      }
    }
    if (Showclix.isCheckingShowclix() && Paxsite.isCheckingPaxWebsite()) {
      System.out.print("Check PAX Website (Y/N): ");
      try {
        if (!myScanner.next().toLowerCase().startsWith("n")) {
          Paxsite.setCheckPax(true);
        } else {
          Paxsite.setCheckPax(false);
        }
      } catch (Exception e) {
      }
    }
    if (Showclix.isCheckingShowclix() && Paxsite.isCheckingPaxWebsite()) {
      System.out.print("Check Showclix Website (Y/N): ");
      try {
        if (!myScanner.next().toLowerCase().startsWith("n")) {
          Showclix.setCheckShowclix(true);
        } else {
          Showclix.setCheckShowclix(false);
        }
      } catch (Exception e) {
      }
    }
    if (getRefreshTime() == 10) {
      System.out.print("Refresh Time (seconds, no input limit at the moment): ");
      try {
        setRefreshTime(Integer.parseInt(myScanner.next(), 10));
      } catch (Exception e) {
      }
    }
    System.out.print("Play Alarm (Y/N): ");
    try {
      if (!myScanner.next().toLowerCase().startsWith("n")) {
        Audio.setPlayAlarm(true);
      }
    } catch (Exception e) {
    }
    myScanner.nextLine(); // Consume mysterious extra input
    if (Browser.getExpo() == null) {
      System.out.print("Expo: ");
      try {
        String input = myScanner.nextLine();
        System.out.println("READ: " + input);
        switch (input.toLowerCase()) {
          case "prime":
          case "paxprime":
          case "pax prime":
            Browser.setExpo("PAX Prime");
            break;
          case "east":
          case "paxeast":
          case "pax east":
            Browser.setExpo("PAX East");
            break;
          case "south":
          case "paxsouth":
          case "pax south":
            Browser.setExpo("PAX South");
            break;
          case "aus":
          case "australia":
          case "paxaus":
          case "pax aus":
          case "paxaustralia":
          case "pax australia":
            Browser.setExpo("PAX Aus");
            break;
          default:
            System.out.println("Invalid expo (" + input + ")! Setting to Prime...");
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
    PAXChecker.startBackgroundThread(new Runnable() {
      @Override
      public void run() {
        String input;
        while (true) {
          try {
            input = myScanner.nextLine();
          } catch (Exception e) {
            //e.printStackTrace();
            System.out.println("Error parsing input -- please try again.");
            continue;
          }
          switch (input.toLowerCase()) {
            case "stop":
            case "exit":
            case "finish":
              System.exit(0);
              break;
            case "testemail":
            case "testtext":
            case "test email":
            case "test text":
              PAXChecker.sendTestEmail();
              break;
            case "testalarm":
            case "test alarm":
              Audio.playAlarm();
              break;
            case "refresh":
            case "check":
              forceRefresh = true;
              break;
            case "updateprogram":
            case "update program":
              UpdateHandler.loadVersionNotes();
              UpdateHandler.autoUpdate();
              break;
            case "list":
            case "listall":
            case "listemails":
            case "list all":
            case "list emails":
              System.out.println("Emails:");
              java.util.Iterator<Email.EmailAddress> it = Email.getAddressList().iterator();
              while (it.hasNext()) {
                System.out.println(it.next().getCompleteAddress());
              }
              break;
            case "test":
              Browser.openLinkInBrowser("https://www.google.com");
              break;
            case "notes":
            case "patchnotes":
            case "versionnotes":
            case "patch notes":
            case "version notes":
              System.out.println(UpdateHandler.getVersionNotes());
              break;
            default:
              if (input.toLowerCase().startsWith("addemail:") || input.toLowerCase().startsWith("add email:")) {
                Email.addEmailAddress(input.substring(input.indexOf(":") + 1).trim());
                continue;
              } else if (input.toLowerCase().startsWith("patchnotes:") || input.toLowerCase().startsWith("versionnotes:")) {
                System.out.println(UpdateHandler.getVersionNotes(input.substring(input.indexOf(":") + 1)).trim());
                continue;
              }
              System.out.println("Unknown command: " + input.toLowerCase());
              System.out.println("------------------Commands------------------");
              System.out.println("exit                - Exit the program");
              System.out.println("testtext            - Send a test text");
              System.out.println("testalarm           - Play the alarm (if enabled)");
              System.out.println("refresh             - Force check");
              System.out.println("check               - Force check");
              System.out.println("list                - Lists all emails in the email list");
              System.out.println("updateprogram       - Updates the program if an update is available");
              System.out.println("addemail:EMAIL      - Adds the specified email address to the program");
              System.out.println("patchnotes:VERSION  - Shows currently loaded Version Notes");
              System.out.println("-------Commands are NOT case sensitive-------");
              break;
          }
        }
      }
    }, "CLI Input Listener");
    PAXChecker.continueProgram(new Runnable() {
      @Override
      public void run() {
        //System.gc();
        int seconds = getRefreshTime(); // Saves time from accessing volatile variable; can be moved to inside do while if secondsBetweenRefresh can be changed when do while is running
        do {
          //status.setLastCheckedText("Checking for updates...");
          long startMS = System.currentTimeMillis();
          if (Paxsite.isPAXWebsiteUpdated()) {
            final String link = Paxsite.getCurrentButtonLink();
            System.out.println("LINK FOUND: " + link);
            Email.sendEmailInBackground("PAX Tickets ON SALE!", "PAX Tickets have been found! URL: " + link);
            Browser.openLinkInBrowser(link);
            Audio.playAlarm();
          } else if (Showclix.isShowclixUpdated()) {
            final String link = Showclix.getShowclixLink();
            System.out.println("LINK FOUND: " + link);
            Email.sendEmailInBackground("PAX Tickets ON SALE!", "PAX Tickets have been found! URL: " + link);
            Browser.openLinkInBrowser(link);
            Audio.playAlarm();
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
      }
    });
  }

  /**
   * Does everything when a Showclix link is found. Opens a Tickets Found window, sends a text, plays an alarm, etc.
   *
   * @param link The link to use for everything
   */
  public static void linkFound(String link) {
    Email.sendEmailInBackground("PAX Tickets ON SALE!", "PAX Tickets have been found! URL: " + link);
    KeyboardHandler.typeLinkNotification(link);
    Browser.openLinkInBrowser(link);
    showTicketsWindow(link);
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
    Tickets tickets = new Tickets(link);
    try {
      tickets.setIconImage(alertIcon);
      tickets.setBackground(Color.RED);
    } catch (Exception e) {
      System.out.println("Unable to set IconImage!");
      e.printStackTrace();
    }
  }

  /**
   * Sets the Status information text.
   *
   * @param s The text to use
   */
  public static void setStatusInformationText(String s) {
    if (status != null) {
      status.setInformationText(s);
    } else {
      System.out.println(s);
    }
  }

  /**
   * Sets the PAX Website link text. Note that this automatically starts with "PAX Website link:"
   *
   * @param s The text to use
   */
  public static void setStatusWebsiteLink(String s) {
    if (status != null) {
      status.setWebsiteLink(s);
    } else {
      System.out.println("PAX Website Event URL = " + s);
    }
  }

  /**
   * Sets the Showclix link text. Note that this automatically starts with "Showclix link:"
   *
   * @param s The text to use
   */
  public static void setStatusShowclixLink(String s) {
    if (status != null) {
      status.setShowclixLink(s);
    } else {
      System.out.println("Showclix Event URL    = " + s);
    }
  }

  /**
   * Sets the Test Text button state.
   *
   * @param enabled True to enable, false to disable
   */
  public static void setStatusTextButtonState(boolean enabled) {
    if (status != null) {
      status.setTextButtonState(enabled);
    }
  }

  /**
   * Sets the Test Text button text.
   *
   * @param s The text to use
   */
  public static void setStatusTextButtonText(String s) {
    if (status != null) {
      status.setTextButtonText(s);
    }
  }

  /**
   * Loads the alert icon to use when tickets go on sale.
   */
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
