/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package paxchecker.tickets;

import java.awt.Color;
import paxchecker.Audio;
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
    super(null);
  }

  @Override
  public void init() {

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
