package com.github.sunnybat.paxchecker;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 *
 * @author SunnyBat
 */
public class ResourceDownloader {

  private final String RESOURCE_LOCATION;
  private final String BASE_URL = "https://dl.dropboxusercontent.com/u/16152108/PAXCheckerResources/";
  private final String ALARM_FILE_NAME = "Alarm.wav";
  private File alarmFile;
  private boolean redownload;

  public ResourceDownloader() {
    RESOURCE_LOCATION = getResourceLocation();
    File resourceFolder = new File(RESOURCE_LOCATION);
    if (!resourceFolder.exists() && resourceFolder.getParentFile().exists()) {
      resourceFolder.mkdirs();
    } else {
      System.out.println("Error creating directory!");
    }
    alarmFile = new File(RESOURCE_LOCATION + ALARM_FILE_NAME);
  }

  public void forceRedownload() {
    redownload = true;
  }

  public void downloadResources() {
    if (!alarmFile.exists() || redownload) {
      try {
        System.out.println("Downloading Alarm file.");
        downloadResource(BASE_URL + ALARM_FILE_NAME, alarmFile);
      } catch (IOException ioe) {
        ioe.printStackTrace();
      }
    }
  }

  private void downloadResource(String fullURL, File downloadTo) throws IOException {
    URLConnection conn = new URL(fullURL).openConnection();
    InputStream inputStream = conn.getInputStream();
    long remoteFileSize = conn.getContentLength();
    System.out.println(downloadTo.getAbsolutePath());
    if (downloadTo.exists()) { // CHECK: Do I need to do this?
      downloadTo.delete();
    }
    downloadTo.createNewFile();
    BufferedOutputStream buffOutputStream = new BufferedOutputStream(new FileOutputStream(downloadTo));
    byte[] buffer = new byte[1024];
    int bytesRead;
    int total = 0;
    while ((bytesRead = inputStream.read(buffer)) != -1) {
      buffOutputStream.write(buffer, 0, bytesRead);
      total += bytesRead;
      int percent = (int) (total * 100 / remoteFileSize);
      filePercentage(percent);
    }
    buffOutputStream.flush();
    buffOutputStream.close();
    inputStream.close();
  }

  private String getResourceLocation() { // TODO: Paths for different OS, which will use different file separators
    String os = System.getProperty("os.name").toLowerCase();
    if (os.contains("windows")) {
      String userName = System.getProperty("user.name");
      return "C:/Users/" + userName + "/AppData/Roaming/PAXChecker/";
    } else if (os.contains("mac")) {
      return null; // TODO: Mac location
    } else if (os.contains("linux")) {
      return "~/.config/PAXChecker/";
    } else {
      return "./PAXChecker/";
    }
  }

  /**
   * Override this to get the current percentage of the file downloaded.
   *
   * @param percent The percent downloaded
   */
  public void filePercentage(int percent) {
  }

}
