/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package paxchecker;

import java.io.*;
import java.util.jar.*;
import java.net.MalformedURLException;
import java.net.*;
import java.lang.ProcessBuilder;

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
  public static URL updateURL;

  /**
   * @param args the command line arguments
   */
  public static void main(String[] args) throws Exception {
    if (args.length > 0) {
      System.out.println("Args!");
    }
    System.out.println(PAXChecker.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
    updateURL = new URL("https://dl.dropboxusercontent.com/u/16152108/PAXChecker.jar");
    if (updateAvailable()) {
      //updateProgram();
      return;
    }
    Email.init();
    setup = new Setup();
    setup.setVisible(true);
    while (setup.isVisible()) {
      Thread.sleep(100);
    }
    status = new Status();
    status.setTitle(Email.getUsername() + " - " + textEmail);
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
        status.setLastCheckedText((int) ((System.currentTimeMillis() - startMS) / 1000));
      }
    }
  }

  public static void setCellnum(String num) {
    if (!num.contains("@")) {
      num += "@mms.att.net";
    }
    textEmail = num;
    System.out.println("textEmail = " + textEmail);
  }

  public static void forceUpdate() {
    forceUpdate = true;
    status.setButtonStatusText("Forced website check!");
  }

  public static void testEmail() {
    if (Email.sendMessage(textEmail, "Test", "The test is successful!")) {
      status.setButtonStatusText("Text message successfully sent!");
    } else {
      status.setButtonStatusText("There was an error sending your text message.");
    }
  }

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
    }
    return false;
  }

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
      }
      fOut.flush();
      fOut.close();
      is.close();
      System.out.println("Download Complete!");
      ProcessBuilder pb = new ProcessBuilder("java", "-jar", PAXChecker.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
      //pb.directory(new File("preferred/working/directory"));
      pb.start();
      System.exit(0);
    } catch (Exception e) {
      System.out.println("ERROR updating program!");
    }
  }
}
