/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package paxchecker;

/**
 *
 * @author SunnyBat
 */
public class PAXChecker {

  public static Setup setup;
  public static Status status;
  public static Tickets tickets;
  public static String textEmail;
  public static boolean forceUpdate;

  public static void setCellnum(String num) {
    if (!num.contains("@")) {
      num += "@mms.att.net";
    }
    textEmail = num;
    System.out.println("textEmail = " + textEmail);
  }

  public static void forceUpdate() {
    forceUpdate = true;
    status.setButtonStatusText("Forced update!");
  }

  public static void testEmail() {
    if (Email.sendMessage(textEmail, "Test", "The test is successful!")) {
      status.setButtonStatusText("Text message successfully sent!");
    } else {
      status.setButtonStatusText("There was an error sending your text message.");
    }
  }

  /**
   * @param args the command line arguments
   */
  public static void main(String[] args) throws Exception {
    Email.init();
    setup = new Setup();
    setup.setVisible(true);
    while (setup.isVisible()) {
      Thread.sleep(100);
    }
    status = new Status();
    status.setVisible(true);
    long startMS;
    while (true) {
      startMS = System.currentTimeMillis();
      if (Browser.isUpdated()) {
        status.setVisible(false);
        status.dispose();
        Email.sendMessage(textEmail, "PAX Tickets!", "The PAX website has (hopefully) been updated!");
        tickets = new Tickets();
        tickets.setAlwaysOnTop(true);
        tickets.setVisible(true);
        tickets.requestFocus();
        Browser.openLinkInBrowser(Browser.parseHRef(Browser.getCurrentButtonLinkLine())); // Only the best.
        break;
      }
      while (System.currentTimeMillis() - startMS < 10000) {
        if (forceUpdate) {
          forceUpdate = false;
          break;
        }
        Thread.sleep(100);
        status.setLastCheckedText((int)((System.currentTimeMillis() - startMS) / 1000));
      }
    }
  }
}
