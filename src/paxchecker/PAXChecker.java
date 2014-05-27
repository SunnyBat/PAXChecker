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
  private static Clip clip;
  private static String version = "1.0.2";

  /**
   * @param args the command line arguments
   */
  public static void main(String[] args) throws Exception {
    javax.swing.ToolTipManager.sharedInstance().setDismissDelay(600000); // Make Tooltips stay forever
    if (args.length > 0) {
      System.out.println("Args!");
      for (int a = 0; a < args.length; a++) {
        System.out.println("args[" + a + "] = " + args[a]);
      }
    }
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
    setup.setTitle("PAXChecker Setup v" + version);
    while (setup.isVisible()) {
      Thread.sleep(100);
    }
    setup.dispose();
    status = new Status();
    try {
      status.setIconImage(javax.imageio.ImageIO.read(PAXChecker.class.getResourceAsStream("/resources/PAX Icon.png")));
    } catch (Exception e) {
      System.out.println("Unable to set IconImage!");
      e.printStackTrace();
    }
    status.setVisible(true);
    long startMS;
    do {
      startMS = System.currentTimeMillis();
      if (Browser.isPAXWebsiteUpdated()) {
        playAlarm();
        showTicketsWindow();
        Browser.openLinkInBrowser(Browser.parseHRef(Browser.getCurrentButtonLinkLine())); // Only the best.
        Email.sendMessage("PAX Tickets ON SALE!", "The PAX website has been updated!");
        status.setVisible(false);
        status.dispose();
        break;
      }
      if (Browser.isShowclixUpdated()) {
        playAlarm();
        showTicketsWindow();
        Browser.openLinkInBrowser(Browser.getShowclixLink()); // Only the best.
        Email.sendMessage("PAX Tickets ON SALE!", "The Showclix website has been updated!");
        status.setVisible(false);
        status.dispose();
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
    } while (status.isVisible());
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
      URLConnection conn = updateURL.openConnection();
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
    try {
      URLConnection conn = updateURL.openConnection();
      InputStream inputStream = conn.getInputStream();
      long remoteFileSize = conn.getContentLength();
      System.out.println("Downloding file...\nUpdate Size(compressed): " + remoteFileSize + " Bytes");
      String path = PAXChecker.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
      BufferedOutputStream buffOutputStream = new BufferedOutputStream(new FileOutputStream(new File(path.substring(0, path.lastIndexOf(".jar")) + ".temp.jar")));
      byte[] buffer = new byte[32 * 1024];
      int bytesRead = 0;
      int in = 0;
      int prevPercent = 0;
      while ((bytesRead = inputStream.read(buffer)) != -1) {
        in += bytesRead;
        buffOutputStream.write(buffer, 0, bytesRead);
        if ((int) (((in * 100) / remoteFileSize)) != prevPercent) {
          prevPercent = (int) (((in * 100) / remoteFileSize));
          update.updateProgress(prevPercent);
        }
      }
      buffOutputStream.flush();
      buffOutputStream.close();
      inputStream.close();
      update.setStatusLabelText("Finishing up...");
      try { // Code to make a copy of the current JAR file
        File inputFile = new File(path.substring(0, path.lastIndexOf(".jar")) + ".temp.jar");
        InputStream fIn = new BufferedInputStream(new FileInputStream(inputFile));
        File outputFile = new File(path);
        buffOutputStream = new BufferedOutputStream(new FileOutputStream(outputFile));
        buffer = new byte[32 * 1024];
        bytesRead = 0;
        in = 0;
        while ((bytesRead = fIn.read(buffer)) != -1) {
          in += bytesRead;
          buffOutputStream.write(buffer, 0, bytesRead);
        }
        buffOutputStream.flush();
        buffOutputStream.close();
        fIn.close();
        inputFile.delete();
      } catch (Exception e) {
        ErrorManagement.showErrorWindow("ERROR updating", "Unable to complete update -- unable to copy temp JAR file to current JAR file.", e);
        ErrorManagement.fatalError();
      }
      System.out.println("Download Complete!");
      try {
        ProcessBuilder pb = new ProcessBuilder(System.getProperty("java.home") + "\\bin\\javaw.exe", "-jar", new File(path).getAbsolutePath()); // path can have leading / on it, getAbsolutePath() removes them
        Process p = pb.start();
      } catch (Exception e) {
        System.out.println("ERROR starting program -- manual restart required.");
      }
    } catch (Exception e) {
      e.printStackTrace();
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
      if (clip != null) {
        clip.stop();
        clip.setFramePosition(0);
      }
      clip = AudioSystem.getClip();
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
   * subsequent calls will rewrite {@link #tickets} and lose the object reference to the previously
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

  /**
   * This makes a new daemon, low-priority Thread and runs it. This is currently unused.
   *
   * @param run The Runnable to make into a Thread and run
   */
  public static void startBackgroundThread(Runnable run) {
    Thread newThread = new Thread(run);
    newThread.setDaemon(true); // Kill the JVM if only daemon threads are running
    newThread.setPriority(Thread.MIN_PRIORITY); // Let other Threads take priority, as this will probably not run for long
    newThread.start(); // Start the Thread
  }
}
