package com.github.sunnybat.paxchecker.resources;

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

  private final String BASE_URL = "https://dl.dropboxusercontent.com/u/16152108/PAXCheckerResources/";
  private boolean redownload;

  public ResourceDownloader() {
    File resourceFolder = new File(ResourceConstants.RESOURCE_LOCATION);
    if (!resourceFolder.exists() && resourceFolder.getParentFile().exists()) {
      resourceFolder.mkdirs();
    }
  }

  public void forceRedownload() {
    redownload = true;
  }

  public void downloadResources() {
    for (String fileName : ResourceConstants.DEFAULT_FILE_NAMES) {
      downloadIfNecessary(fileName, new File(ResourceConstants.RESOURCE_LOCATION + fileName));
    }
  }

  private void downloadIfNecessary(String fileName, File downloadTo) {
    if (!downloadTo.exists() || redownload) {
      startingFile(fileName);
      try {
        downloadResource(BASE_URL + fileName, downloadTo);
      } catch (IOException ioe) {
        ioe.printStackTrace();
      }
      finishedFile(fileName);
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
      filePercentage((int) (total * 100 / remoteFileSize));
    }
    buffOutputStream.flush();
    buffOutputStream.close();
    inputStream.close();
  }

  /**
   * Override this to get the name of the file that is currently being downloaded.
   *
   * @param fileName The filename
   */
  public void startingFile(String fileName) {
  }

  /**
   * Override this to get the current percentage of the file downloaded.
   *
   * @param percent The percent downloaded
   */
  public void filePercentage(int percent) {
  }

  /**
   * Override this to get the name of the file that has been fully downloaded.
   *
   * @param fileName The filename
   */
  public void finishedFile(String fileName) {
  }

}
