/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package paxchecker.tickets;

import java.awt.Color;
import paxchecker.Audio;
import paxchecker.DataTracker;
import paxchecker.Email;
import paxchecker.KeyboardHandler;
import paxchecker.browser.Browser;
import paxchecker.check.TicketChecker;
import paxchecker.gui.Status;

/**
 *
 * @author Sunny
 */
public class NormalGUI extends CheckMethod {

  private Status status;

  public NormalGUI() {
    super();
  }

  @Override
  public void init() {
    status.setupComponents();
    status.showWindow();
    //Checker.setStatusIcon(Checker.getIconName(Browser.getExpo()));
  }

  @Override
  public void run() {
    long startMS;
    int seconds = getRefreshTime(); // Saves time from accessing volatile variable; can be moved to inside do while if secondsBetweenRefresh can be changed when do while is running
    if (!TicketChecker.isCheckingPaxsite() && !TicketChecker.isCheckingShowclix()) {
      status.setLastCheckedText("[Only Scanning Twitter]");
    } else {
      do {
        status.setLastCheckedText("Checking for updates...");
        startMS = System.currentTimeMillis();
        if (TicketChecker.isUpdated()) {
          ticketsFound();
          continue; // Immediately re-check in case other services have found updates
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
    }
  }

  @Override
  public void checkTickets() {
    long startMS;
    int seconds = getRefreshTime(); // Saves time from accessing volatile variable; can be moved to inside do while if secondsBetweenRefresh can be changed when do while is running
    if (!TicketChecker.isCheckingPaxsite() && !TicketChecker.isCheckingShowclix()) {
      status.setLastCheckedText("[Only Scanning Twitter]");
    } else {
      status.setLastCheckedText("Checking for updates...");
      startMS = System.currentTimeMillis();
      if (TicketChecker.isUpdated()) {
        ticketsFound();
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
    }
  }

  @Override
  public void ticketsFound() {
    String link = TicketChecker.getLinkFound();
    Email.sendEmailInBackground("PAX Tickets ON SALE!", "PAX Tickets have been found! URL: " + link);
    KeyboardHandler.typeLinkNotification(link);
    Browser.openLinkInBrowser(link);
    showTicketsWindow(link);
    Audio.playAlarm();
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
      //tickets.setIconImage(alertIcon);
      tickets.setBackground(Color.RED);
    } catch (Exception e) {
      System.out.println("Unable to set IconImage!");
      e.printStackTrace();
    }
  }
}
