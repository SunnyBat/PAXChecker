/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package paxchecker;

import java.awt.Color;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import javax.sound.sampled.*;
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
  public static int secondsBetweenRefresh;
  public static URL updateURL;
  public static boolean updateProgram;
  public static boolean playSound;
  public static long updateSize;

  /**
   * @param args the command line arguments
   */
  public static void main(String[] args) throws Exception {
    javax.swing.ToolTipManager.sharedInstance().setDismissDelay(600000); // Make Tooltips stay forever
    if (args.length > 0) {
      System.out.println("Args!");
    }
    System.out.println(PAXChecker.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
    try {
      updateURL = new URL("https://dl.dropboxusercontent.com/u/16152108/PAXChecker.jar");
      if (updateAvailable()) {
        update = new Update();
        update.setStatusLabelText("Update Size: " + ((double) ((int) ((double) updateSize / 1024 / 1024 * 100)) / 100) + "MB");
        update.setVisible(true);
        while (update.isVisible() && !updateProgram) {
          Thread.sleep(100);
        }
        if (updateProgram) {
          update.setStatusLabelText("Downloading update...");
          updateProgram();
          update.dispose();
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
    if (Email.getUsername().indexOf("@") == 0) {
      status.setInfoText("[TEXTING DISABLED]");
      status.setTextButtonState(false);
    } else if (Email.getTextEmail() != null) {
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
    if (!playSound) {
      status.setSoundButtonState(false);
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
        playAlarm();
        status.setVisible(false);
        status.dispose();
        showTicketsWindow();
        Browser.openLinkInBrowser(Browser.parseHRef(Browser.getCurrentButtonLinkLine())); // Only the best.
        if (Email.getTextEmail() != null) {
          Email.sendMessage("PAX Tickets ON SALE!", "The PAX website has been updated!");
        }
        break;
      }
      if (Browser.isShowclixUpdated()) {
        playAlarm();
        status.setVisible(false);
        status.dispose();
        showTicketsWindow();
        Browser.openLinkInBrowser(Browser.getShowclixLink()); // Only the best.
        if (Email.getTextEmail() != null) {
          Email.sendMessage("PAX Tickets ON SALE!", "The Showclix website has been updated!");
        }
        break;
      }
      while (System.currentTimeMillis() - startMS < (secondsBetweenRefresh * 1000)) {
        if (forceUpdate) {
          forceUpdate = false;
          break;
        }
        Thread.sleep(100);
        status.setLastCheckedText((int) ((System.currentTimeMillis() - startMS) / 1000));
      }
    }
  }

  /**
   * Sets the time between checking the PAX Registration website for updates. This can be called at
   * any time, however it is recommended to only call it during Setup.
   *
   * @param seconds The amount of seconds between website updates.
   */
  public static void setRefreshTime(int seconds) {
    secondsBetweenRefresh = seconds;
  }

  /**
   * Forces the program to check the PAX website for updates. Note that this resets the time since
   * last check to 0.
   */
  public static void forceRefresh() {
    forceUpdate = true;
    if (status != null) {
      status.setButtonStatusText("Forced website check!");
    }
  }

  /**
   * Set the updateProgram flag to true. This will start the program updating process. This should
   * only be called by the Update GUI when the main() method is waiting for the prompt.
   */
  public static void startUpdatingProgram() {
    updateProgram = true;
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
      File mF = new File(PAXChecker.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
      long fileSize = mF.length();
      if (fileSize == 4096) { // No, I do NOT want to update when I'm running in Netbeans
        return false;
      }
      URL url = new URL("https://dl.dropboxusercontent.com/u/16152108/PAXChecker.jar");
      URLConnection conn = url.openConnection();
      updateSize = conn.getContentLengthLong();
      System.out.println("Updatesize = " + updateSize + " -- Filesize = " + fileSize);
      if (updateSize == -1) {
        ErrorManagement.showErrorWindow("ERROR checking for updates!", "PAX Checker was unable to check for updates.", null);
        return false;
      } else if (updateSize != fileSize) {
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
//    try { // Code to make a copy of the current JAR file
//      String path = PAXChecker.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
//      File mF = new File(path.substring(0, path.lastIndexOf(".jar")) + ".2.jar");
//      mF.createNewFile();
//      InputStream fIn = new BufferedInputStream(new FileInputStream(new File(path)));
//      long max = new File(path).length();
//      BufferedOutputStream fOut = new BufferedOutputStream(new FileOutputStream(mF));
//      byte[] buffer = new byte[32 * 1024];
//      int bytesRead = 0;
//      int in = 0;
//      while ((bytesRead = fIn.read(buffer)) != -1) {
//        in += bytesRead;
//        fOut.write(buffer, 0, bytesRead);
//        update.updateProgress((int) (((in * 100) / max)));
//      }
//      fOut.flush();
//      fOut.close();
//      fIn.close();
//    } catch (Exception e) {
//      e.printStackTrace();
//    }
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
      int prevPercent = 0;
      while ((bytesRead = is.read(buffer)) != -1) {
        in += bytesRead;
        fOut.write(buffer, 0, bytesRead);
        if ((int) (((in * 100) / max)) != prevPercent) {
          prevPercent = (int) (((in * 100) / max));
          update.updateProgress(prevPercent);
          update.setStatusLabelText("Progress: " + prevPercent + "%");
        }
      }
      fOut.flush();
      fOut.close();
      is.close();
      System.out.println("Download Complete!");
//      ProcessBuilder pb = new ProcessBuilder("java", "-jar", PAXChecker.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
//      //pb.directory(new File("preferred/working/directory"));
//      pb.start();
//      System.exit(0);
//      ErrorManagement.showErrorWindow("Restart PAXChecker", "Your download has successfully been downloaded! Please restart the program by closing this window and running the JAR file again.", null);
//      ErrorManagement.fatalError();
    } catch (Exception e) {
      System.out.println("ERROR updating program!");
      ErrorManagement.showErrorWindow("ERROR updating the program", "The program was unable to successfully download the update. Your version is likely corrupt -- please manually download the latest version.", e);
      ErrorManagement.fatalError();
    }
  }

  /**
   * Sets whether to play the alarm sound when an update is found. This can be called at any time.
   *
   * @param play True to play sound, false to not
   */
  public static void setPlayAlarm(boolean play) {
    playSound = play;
  }

  public static boolean playAlarm() {
    try {
      Clip clip = AudioSystem.getClip();
      InputStream audioSrc = PAXChecker.class.getResourceAsStream("/resources/Alarm.wav");
      InputStream bufferedIn = new BufferedInputStream(audioSrc);
      AudioInputStream inputStream = AudioSystem.getAudioInputStream(bufferedIn);
      clip.open(inputStream);
      clip.start();
      return true;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return false;
  }

  /**
   * Creates the Tickets window and makes it visible. This should really only be called once, as
   * subsequent calls will rewrite {@link #status} and lose the object reference to the previously
   * opened tickets window.
   */
  public static void showTicketsWindow() {
    tickets = new Tickets();
    tickets.setAlwaysOnTop(true);
    try {
      tickets.setIconImage(javax.imageio.ImageIO.read(PAXChecker.class.getResourceAsStream("/resources/alert.png")));
      tickets.setBackground(Color.RED);
    } catch (Exception e) {
      System.out.println("Unable to set IconImage!");
      e.printStackTrace();
    }
    tickets.setVisible(true);
    tickets.toFront();
    tickets.requestFocus();
  }
}
