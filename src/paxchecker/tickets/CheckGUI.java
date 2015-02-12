/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package paxchecker.tickets;

import java.io.IOException;
import paxchecker.Audio;
import paxchecker.DataTracker;
import paxchecker.Email;
import paxchecker.KeyboardHandler;
import paxchecker.browser.Browser;
import paxchecker.check.TicketChecker;
import paxchecker.gui.Status;

/**
 *
 * @author SunnyBat
 */
public class CheckGUI implements CheckMethod {

  private Status status;
  private static volatile java.awt.Image alertIcon;

  @Override
  public void init() {
    status = new Status();
    loadAlertIcon();
  }

  @Override
  public void checkForUpdates() {
    status.setLastCheckedText("Checking for updates...");
    long startMS = System.currentTimeMillis();
    if (TicketChecker.isUpdated()) {
      linkFound(TicketChecker.getLinkFound());
      return; // Immediately re-check in case other services have found updates
    }
    status.setDataUsageText(DataTracker.getDataUsedMB());
    while (System.currentTimeMillis() - startMS < (Checker.getRefreshTime() * 1000)) {
//      if (forceRefresh) {
//        forceRefresh = false;
//        break;
//      }
      try {
        Thread.sleep(100);
      } catch (InterruptedException interruptedException) {
        System.out.println("Interrupted.");
      }
      status.setLastCheckedText(Checker.getRefreshTime() - (int) ((System.currentTimeMillis() - startMS) / 1000));
    }
  }

  @Override
  public void linkFound(String link) {
    Email.sendEmailInBackground("PAXChecker", "An update has been found! URL: " + link);
    KeyboardHandler.typeLinkNotification(link);
    Browser.openLinkInBrowser(link);
    Checker.showTicketsWindow(link);
    Audio.playAlarm();
  }

  /**
   * Loads the alert icon to use when tickets go on sale.
   */
  private static void loadAlertIcon() {
    try {
      alertIcon = javax.imageio.ImageIO.read(Checker.class.getResourceAsStream("/resources/alert.png"));
    } catch (IOException iOException) {
    }
  }

}
