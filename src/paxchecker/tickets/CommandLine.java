/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package paxchecker.tickets;

import paxchecker.Audio;
import paxchecker.Email;
import paxchecker.browser.Browser;
import paxchecker.DataTracker;
import paxchecker.PAXChecker;
import paxchecker.check.*;
import paxchecker.update.UpdateHandler;
import java.util.Scanner;

/**
 *
 * @author Sunny
 */
public class CommandLine extends CheckMethod {

  private static final Scanner myScanner = new Scanner(System.in);

  public CommandLine() {
    super();
  }

  @Override
  public void init() {
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
    if (!TicketChecker.isCheckingPaxsite()) {
      System.out.print("Check PAX Website (Y/N): ");
      try {
        if (!myScanner.next().toLowerCase().startsWith("n")) {
          TicketChecker.addChecker(new CheckPaxsite());
        }
      } catch (Exception e) {
      }
    }
    if (!TicketChecker.isCheckingShowclix()) {
      System.out.print("Check Showclix Website (Y/N): ");
      try {
        if (!myScanner.next().toLowerCase().startsWith("n")) {
          TicketChecker.addChecker(new CheckShowclix());
        }
      } catch (Exception e) {
      }
    }
    if (!TicketChecker.isCheckingTwitter()) {
      System.out.print("Check Twitter (Y/N): ");
      try {
        if (!myScanner.next().toLowerCase().startsWith("n")) {
          Checker.startTwitterStreaming();
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
              Email.testEmail();
              break;
            case "testalarm":
            case "test alarm":
              Audio.playAlarm();
              break;
            case "refresh":
            case "check":
              forceRefresh();
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
              } else if (input.toLowerCase().startsWith("removeemail:") || input.toLowerCase().startsWith("remove email:")) {
                Email.removeEmailAddress(input.substring(input.indexOf(":") + 1).trim());
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
              System.out.println("removeemail:EMAIL   - Removes the specified email address to the program");
              System.out.println("list                - Shows all email addresses currently registered with the program");
              System.out.println("patchnotes          - Shows all Version Notes");
              System.out.println("patchnotes:VERSION  - Shows currently loaded Version Notes since supplied VERSION");
              System.out.println("-------Commands are NOT case sensitive-------");
              break;
          }
        }
      }
    }, "CLI Input Listener");
  }

  @Override
  public void run() {
    do {
      long startMS = System.currentTimeMillis();
      if (TicketChecker.isUpdated()) {
        System.out.println("Tickets found!");
        ticketsFound();
      }
      System.out.println("Data used: " + DataTracker.getDataUsedMB() + "MB");
      while (System.currentTimeMillis() - startMS < (getRefreshTime() * 1000)) {
        if (forceRefresh) {
          forceRefresh = false;
          break;
        }
        try {
          Thread.sleep(100);
        } catch (InterruptedException iE) {
        }
      }
    } while (true);
  }

  @Override
  public void checkTickets() {
    long startMS = System.currentTimeMillis();
    if (TicketChecker.isUpdated()) {
      System.out.println("Tickets found!");
      ticketsFound();
    }
    System.out.println("Data used: " + DataTracker.getDataUsedMB() + "MB");
    while (System.currentTimeMillis() - startMS < (getRefreshTime() * 1000)) {
      if (forceRefresh) {
        forceRefresh = false;
        break;
      }
      try {
        Thread.sleep(100);
      } catch (InterruptedException iE) {
      }
    }
  }

  @Override
  public void ticketsFound() {
    final String link = TicketChecker.getLinkFound();
    System.out.println("LINK FOUND: " + link);
    Email.sendEmailInBackground("PAX Tickets ON SALE!", "PAX Tickets have been found! URL: " + link);
    Browser.openLinkInBrowser(link);
    Audio.playAlarm();
  }

}
