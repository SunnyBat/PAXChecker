/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package paxchecker.tickets;

import paxchecker.Audio;
import paxchecker.Email;
import paxchecker.browser.Browser;
import java.util.Scanner;
import paxchecker.check.*;

/**
 *
 * @author Sunny
 */
public class CommandLine extends TicketCheck {

  public CommandLine() {
    super(new Runnable() {
      @Override
      public void run() {
        System.out.println("Stuff!");
      }
    });
  }

  @Override
  public void init() {
    Scanner myScanner = new Scanner(System.in);
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
//    if (Showclix.isCheckingShowclix() && Paxsite.isCheckingPaxWebsite()) {
    System.out.print("Check Showclix Website (Y/N): ");
    try {
      if (!myScanner.next().toLowerCase().startsWith("n")) {
//          Showclix.setCheckShowclix(true);
      } else {
//          Showclix.setCheckShowclix(false);
      }
    } catch (Exception e) {
    }
//    }
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

  @Override
  public void ticketsFound() {
  }

}
