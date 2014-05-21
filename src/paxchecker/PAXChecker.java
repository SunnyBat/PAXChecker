/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package paxchecker;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import paxchecker.GUI.*;

/**
 *
 * @author SunnyBat
 */
public class PAXChecker {

  public static Setup setup;
  public static Status status;
  public static Tickets tickets;
  public static Update update;
  public static boolean forceUpdate;
  public static int secondsBetweenUpdate;
  public static URL updateURL;
  public static boolean updateProgram;

  /**
   * @param args the command line arguments
   */
  public static void main(String[] args) throws Exception {
    Browser.getShowclixInfo();
    javax.swing.ToolTipManager.sharedInstance().setDismissDelay(600000); // Make Tooltips stay forever
    if (args.length > 0) {
      System.out.println("Args!");
    }
    System.out.println(PAXChecker.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
    try {
      updateURL = new URL("https://dl.dropboxusercontent.com/u/16152108/PAXChecker.jar");
      if (updateAvailable()) {
        update = new Update();
        update.setVisible(true);
        while (update.isVisible() && !updateProgram) {
          Thread.sleep(100);
        }
        if (updateProgram) {
          updateProgram();
          return;
        }
        update.dispose();
      }
    } catch (Exception e) {
      ErrorManagement.showErrorWindow("ERROR", "An error has occurred while attempting to update the program. If the problem persists, please manually download the latest version.", e);
      ErrorManagement.fatalError();
      return;
    }
    Email.init();
    setup = new Setup();
    setup.setVisible(true);
    while (setup.isVisible()) {
      Thread.sleep(100);
    }
    setup.dispose();
    status = new Status();
    if (Email.getTextEmail() != null) {
      status.setInfoText(Email.getUsername() + " -- " + Email.getTextEmail());
    } else if (!Email.getEmailList().isEmpty()) {
      status.setInfoText(Email.getUsername() + " -- Multiple Numbers (Mouse Here to View)");
      String list = "<html>";
      for (int a = 0; a < Email.getEmailList().size(); a++) {
        list += Email.getEmailList().get(a);
        if (a + 1 != Email.getEmailList().size()) {
          list += "<br>";
        }
      }
      list += "</html>";
      status.setLabelTooltipText(list);
    } else {
      status.setInfoText("[TEXTING DISABLED]");
      status.setTextButtonState(false);
    }
    try {
      status.setIconImage(javax.imageio.ImageIO.read(PAXChecker.class.getResourceAsStream("/resources/PAX Icon.png")));
    } catch (Exception e) {
      System.out.println("Unable to set IconImage!");
      e.printStackTrace();
    }
    status.setVisible(true);
    long startMS;
    while (true) {
      startMS = System.currentTimeMillis();
      if (Browser.isPAXWebsiteUpdated()) {
        status.setVisible(false);
        status.dispose();
        showTicketsWindow();
        Browser.openLinkInBrowser(Browser.parseHRef(Browser.getCurrentButtonLinkLine())); // Only the best.
        if (Email.getTextEmail() != null) {
          Email.sendMessage("PAX Tickets ON SALE!", "The PAX website has been updated!");
        }
        break;
      } else if (Browser.isShowclixUpdated()) {
        status.setVisible(false);
        status.dispose();
        showTicketsWindow();
        Browser.openLinkInBrowser(Browser.getShowclixLink()); // Only the best.
        if (Email.getTextEmail() != null) {
          Email.sendMessage("PAX Tickets ON SALE!", "The Showclix website has been updated!");
        }
        break;
      }
      while (System.currentTimeMillis() - startMS < (secondsBetweenUpdate * 1000)) {
        if (forceUpdate) {
          forceUpdate = false;
          break;
        }
        Thread.sleep(100);
        status.setLastCheckedText((int) ((System.currentTimeMillis() - startMS) / 1000));
      }
    }
  }

  public static void showTicketsWindow() {
        tickets = new Tickets();
        tickets.setAlwaysOnTop(true);
        try {
          tickets.setIconImage(javax.imageio.ImageIO.read(PAXChecker.class.getResourceAsStream("/resources/alert.png")));
        } catch (Exception e) {
          System.out.println("Unable to set IconImage!");
          e.printStackTrace();
        }
        tickets.setVisible(true);
        tickets.requestFocus();
  }

  /**
   * Set the updateProgram flag to true. This will start the program updating process. This should
   * only be called by the Update GUI when the main() method is waiting for the prompt.
   */
  public static void startUpdatingProgram() {
    updateProgram = true;
  }

  /**
   * Sets the time between checking the PAX Registration website for updates. This can be called at
   * any time, however it is recommended to only call it during Setup.
   *
   * @param seconds The amount of seconds between website updates.
   */
  public static void setUpdateTime(int seconds) {
    secondsBetweenUpdate = seconds;
  }

  /**
   * Forces the program to check the PAX website for updates. Note that this resets the time since
   * last check to 0.
   */
  public static void forceRefresh() {
    forceUpdate = true;
    status.setButtonStatusText("Forced website check!");
  }

  public static void testEmail() {
    if (Email.sendMessage("Test", "The test is successful. The PAX Checker is now set up to text your phone when the website updates!")) {
      status.setButtonStatusText("Text message successfully sent!");
    } else {
      status.setButtonStatusText("There was an error sending your text message.");
    }
  }

  /**
   * Checks whether or not an update to the program is available. Note that this compares the file
   * sizes between the current file and the file on the Dropbox server. This means that if ANY
   * modification is made to the JAR file, it's likely to trigger an update.
   * This THEORETICALLY works well. We'll find out whether or not it will actually work in practice.
   *
   * @return True if an update is available, false if not.
   */
  private static boolean updateAvailable() {
    try {
      URL url = new URL("https://dl.dropboxusercontent.com/u/16152108/PAXChecker.jar");
      URLConnection conn = url.openConnection();
      long updateSize = conn.getContentLengthLong();
      File mF = new File(PAXChecker.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
      long fileSize = mF.length();
      System.out.println("Updatesize = " + updateSize + " -- Filesize = " + fileSize);
      if (updateSize != fileSize) {
        System.out.println("Update available!");
        return true;
      }
    } catch (Exception e) {
      System.out.println("ERROR updating program!");
      ErrorManagement.showErrorWindow("ERROR updating program!", "The program was unable to check for new updates.", e);
    }
    return false;
  }

  /**
   * Downloads the latest JAR file from the Dropbox server. Note that this automatically closes the
   * program once finished. Also note that once this is run, the program WILL eventually close,
   * either through finishing the update or failing to properly update.
   */
  private static void updateProgram() {
    try {
      URL url = new URL("https://dl.dropboxusercontent.com/u/16152108/PAXChecker.jar");
      URLConnection conn = url.openConnection();
      InputStream is = conn.getInputStream();
      long max = conn.getContentLength();
      System.out.println("Downloding file...\nUpdate Size(compressed): " + max + " Bytes");
      BufferedOutputStream fOut = new BufferedOutputStream(new FileOutputStream(new File(PAXChecker.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath())));
      byte[] buffer = new byte[32 * 1024];
      int bytesRead = 0;
      int in = 0;
      while ((bytesRead = is.read(buffer)) != -1) {
        in += bytesRead;
        fOut.write(buffer, 0, bytesRead);
        update.updateProgress((int) (((in * 100) / max)));
      }
      fOut.flush();
      fOut.close();
      is.close();
      System.out.println("Download Complete!");
//      ProcessBuilder pb = new ProcessBuilder("java", "-jar", PAXChecker.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
//      //pb.directory(new File("preferred/working/directory"));
//      pb.start();
      System.exit(0);
    } catch (Exception e) {
      System.out.println("ERROR updating program!");
      ErrorManagement.showErrorWindow("ERROR updating the program", "The program was unable to successfully download the update. Your version is likely corrupt -- please manually download the latest version.", e);
      ErrorManagement.fatalError();
    }
  }
}
