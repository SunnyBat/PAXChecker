/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package paxchecker.tickets;

import paxchecker.gui.Status;

/**
 *
 * @author Sunny
 */
public class TicketCheck {

  private static CheckMethod myInstance;
  private static Status status;
  private static boolean isStarted;

  public static final void startCommandLine() {
    if (myInstance != null) {
      System.out.println("Error: Already started!");
    }
    myInstance = new CommandLine();
    start();
  }

  public static final void startGUI() {
    if (myInstance != null) {
      System.out.println("Error: Already started!");
    }
    myInstance = new NormalGUI();
    start();
  }

  public static final boolean isCommandLine() {
    return myInstance instanceof CommandLine;
  }

  public static final boolean isGUI() {
    return myInstance instanceof NormalGUI;
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

  private static synchronized void start() {
    if (isStarted) {
      System.out.println("ERROR: Already started!");
      return;
    }
    myInstance.init();
    myInstance.checkForTickets();
    isStarted = true;
  }
}
