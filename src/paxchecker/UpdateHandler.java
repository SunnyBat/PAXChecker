/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package paxchecker;

import java.io.*;
import java.net.*;
import java.util.concurrent.CountDownLatch;

/**
 *
 * @author SunnyBat
 */
public class UpdateHandler {

  private static volatile String versionNotes;
  private static volatile boolean useBetaVersion;
  private static volatile int updateLevel;
  private static volatile boolean updateProgram;
  private static long updateSize;
  private static final String UPDATE_LINK = "https://dl.dropboxusercontent.com/u/16152108/PAXChecker.jar";
  private static final String BETA_UPDATE_LINK = "https://dl.dropboxusercontent.com/u/16152108/PAXCheckerBETA.jar";
  private static final String PATCH_NOTES_LINK = "https://dl.dropboxusercontent.com/u/16152108/PAXCheckerUpdates.txt";
  public static paxchecker.GUI.Update update;

  /**
   * Returns the current Version Notes found. This returns all of the notes after the supplied version (useful for things like patch notes when
   * updating). Note that the version must be the same as in the update notes, otherwise this will return
   *
   * @param version The Version (raw String of version number)
   * @return The version notes after the given version, or null if notes have not been retrieved yet
   */
  public static String getVersionNotes(String version) {
    String versNotes = getVersionNotes();
    if (versNotes == null) {
      return null;
    }
    try {
      versNotes = versNotes.substring(0, versNotes.indexOf("~~~" + version + "~~~")).trim();
    } catch (IndexOutOfBoundsException e) {
      System.out.println("ERROR: Unable to find update notes for version " + version);
    }
    return versNotes;
  }

  /**
   * Gets the currently loaded version notes. This returns all of the notes in one String. Note that this returns null if the version notes have not
   * been loaded yet.
   *
   * @return The currently loaded version notes, or null if notes are not loaded yet.
   */
  public static String getVersionNotes() {
    return versionNotes;
  }

  /**
   * Loads the current version notes from online. This retreives all of the version notes possible and stores them in one String, with each line
   * separated by a line break (\n). Note that this method takes several seconds to load and should be run in a background Thread. Note that this also
   * parses tokens from the version notes (and does not add them into the version notes String). Currently the only token read is SETSHOWCLIXID, which
   * is obsolete due to the program loading the most recent Showclix ID after the version notes.
   *
   * @see paxchecker.PAXChecker#loadPatchNotesInBackground()
   * @see #getVersionNotes()
   */
  public static void loadVersionNotes() {
    URLConnection inputConnection;
    InputStream textInputStream;
    BufferedReader myReader = null;
    try {
      URL patchNotesURL = new URL(PATCH_NOTES_LINK);
      inputConnection = patchNotesURL.openConnection();
      textInputStream = inputConnection.getInputStream();
      myReader = new BufferedReader(new InputStreamReader(textInputStream));
      String line;
      String lineSeparator = System.getProperty("line.separator", "\n");
      String allText = "";
      boolean versionFound = false;
      while ((line = myReader.readLine()) != null) {
        DataTracker.addDataUsed(line.length());
        line = line.trim();
        if (line.contains("~~~" + PAXChecker.VERSION + "~~~")) {
          versionFound = true;
        }
        if (line.startsWith("TOKEN:")) {
          try {
            String d = line.substring(6);
            if (d.startsWith("SETSHOWCLIXID:")) { // Obsolete, if I'm not mistaken
              String load = d.substring(14);
              System.out.println("Load = " + load);
              Browser.setShowclixID(Integer.parseInt(load));
            } else if (d.startsWith("UPDATETYPE:")) {
              if (!versionFound) {
                String load = d.substring(11);
                if (load.equals("BETA")) {
                  setUpdateLevel(1);
                } else if (load.equals("UPDATE")) {
                  setUpdateLevel(2);
                } else if (load.equals("MAJORUPDATE")) {
                  setUpdateLevel(3);
                }
              }
            } else {
              System.out.println("Unknown token: " + d);
            }
          } catch (NumberFormatException numberFormatException) {
            System.out.println("Unable to set token: " + line);
          }
        } else {
          allText += line + lineSeparator;
        }
      }
      versionNotes = allText.trim();
      if (update != null) {
        update.setYesButtonText(getUpdateLevel());
      }
    } catch (Exception e) {
      System.out.println("Unable to load version notes!");
      e.printStackTrace();
    } finally {
      try {
        if (myReader != null) {
          myReader.close();
        }
      } catch (IOException e) {
        // nothing to see here
      }
    }
  }

  /**
   * Sets whether or not to use BETA versions of the program. Note that this should be called before {@link #updateAvailable()} is called, otherwise
   * it will be essentially useless.
   *
   * @param use True to use BETA versions, false to not
   */
  public static void setUseBeta(boolean use) {
    System.out.println("Browser Use Beta =  " + use);
    useBetaVersion = use;
  }

  /**
   * Checks whether or not the program should use BETA versions.
   *
   * @return True for use BETA, false for not
   */
  public static boolean getUseBeta() {
    return useBetaVersion;
  }

  /**
   * Checks whether or not an update is available and prompts the user to update if there is.
   *
   * @param args The command-line arguments to use when starting a new program instance
   */
  public static void checkUpdate(String[] args) {
    try {
      System.out.println("Checking for updates...");
      if (UpdateHandler.updateAvailable()) {
        CountDownLatch cdl = new CountDownLatch(1);
        update = new paxchecker.GUI.Update(cdl);
        try {
          cdl.await();
        } catch (InterruptedException iE) {
          System.out.println("CDL interrupted, continuing...");
        }
        if (UpdateHandler.shouldUpdateProgram()) {
          update.setStatusLabelText("Downloading update...");
          UpdateHandler.updateProgram();
          PAXChecker.startNewProgramInstance(args);
          update.dispose();
          System.exit(0);
        }
      }
    } catch (Exception e) {
      ErrorHandler.showErrorWindow("ERROR", "An error has occurred while attempting to update the program. If the problem persists, please manually download the latest version.", e);
      ErrorHandler.fatalError();
    }
  }

  /**
   * Checks for program updates and automatically updates if found.
   *
   * @param args The command-line arguments to use when starting a new program instance
   */
  public static void autoUpdate(String[] args) {
    try {
      System.out.println("Checking for updates...");
      if (UpdateHandler.updateAvailable()) {
        System.out.println("Update found, downloading update...");
        UpdateHandler.updateProgram();
        System.out.println("Update finished, restarting program...");
        PAXChecker.startNewProgramInstance(args);
        System.exit(0);
      }
    } catch (Exception e) {
      ErrorHandler.showErrorWindow("ERROR", "An error has occurred while attempting to update the program. If the problem persists, please manually download the latest version.", e);
      ErrorHandler.fatalError();
    }
  }

  /**
   * Returns the size of the update file found online. This will return 0 if the size has not been loaded yet.
   *
   * @return The size of the update file found online, or 0 if the size has not been loaded yet
   */
  public static long getUpdateSize() {
    return updateSize;
  }

  /**
   * Sets the level of the update. Note that this can only increase the level -- attempting to set the update level lower will have no effect.<br>
   * Level 0 = Unknown (should be treated the same as Level 2 in program)<br>
   * Level 1 = BETA<br>
   * Level 2 = Update<br>
   * Level 3 = Major Update
   *
   * @param level The level to set the update to
   */
  public static void setUpdateLevel(int level) {
    if (updateLevel < level) {
      updateLevel = level;
      if (update != null) {
        update.setYesButtonText(updateLevel);
      }
    }
  }

  /**
   * Gets the current level of update available.<br>
   * 0 = Unknown update level<br>
   * 1 = BETA version<br>
   * 2 = Minor version<br>
   * 3 = Major version
   *
   * @return 0-3 depending on the update level available
   */
  public static int getUpdateLevel() {
    return updateLevel;
  }

  /**
   * Set the updateProgram flag to true. This will start the program updating process. This should only be called by the Update GUI when the main()
   * method is waiting for the prompt.
   */
  public static void startUpdatingProgram() {
    updateProgram = true;
  }

  public static boolean shouldUpdateProgram() {
    return updateProgram;
  }

  /**
   * Checks whether or not an update to the program is available. Note that this compares the file sizes between the current file and the file on the
   * Dropbox server. This means that if ANY modification is made to the JAR file, it's likely to trigger an update. This THEORETICALLY works well.
   * We'll find out whether or not it will actually work in practice.
   *
   * @return True if an update is available, false if not.
   */
  public static boolean updateAvailable() {
    try {
      File mF = new File(PAXChecker.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
      long fileSize = mF.length();
      if (fileSize == 4097) { // No, I do NOT want to update when I'm running in Netbeans
        return false;
      }
      URL updateURL = new URL(UPDATE_LINK);
      URLConnection conn = updateURL.openConnection();
      updateSize = conn.getContentLengthLong();
      System.out.println("Update size = " + updateSize + " -- Program size = " + fileSize);
      if (getUpdateLevel() == 1 && !SettingsHandler.getUseBetaVersion()) {
        System.out.println("Update available, but not opted into BETA versions");
        return false;
      }
      if (updateSize == -1) {
        ErrorHandler.showErrorWindow("ERROR checking for updates!", "Update size listed as -1 -- Program most likely unable to connect!", null);
        return false;
      } else if (updateSize != fileSize) {
        System.out.println("Update available!");
        return true;
      }
    } catch (Exception e) {
      System.out.println("ERROR updating program!");
      ErrorHandler.showErrorWindow("ERROR updating program!", "The program was unable to check for new updates.", e);
    }
    return false;
  }

  /**
   * Downloads the latest JAR file from the Dropbox server. Note that this automatically closes the program once finished. Also note that once this is
   * run, the program WILL eventually close, either through finishing the update or failing to properly update.
   */
  public static void updateProgram() {
    try {
      URL updateURL = new URL(UPDATE_LINK);
      URLConnection conn = updateURL.openConnection();
      InputStream inputStream = conn.getInputStream();
      long remoteFileSize = conn.getContentLength();
      System.out.println("Downloding file...\nUpdate Size(compressed): " + remoteFileSize + " Bytes");
      String path = PAXChecker.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
      BufferedOutputStream buffOutputStream = new BufferedOutputStream(new FileOutputStream(new File(path.substring(0, path.lastIndexOf(".jar")) + ".temp.jar")));
      byte[] buffer = new byte[32 * 1024];
      int bytesRead;
      int in = 0;
      int prevPercent = 0;
      while ((bytesRead = inputStream.read(buffer)) != -1) {
        in += bytesRead;
        buffOutputStream.write(buffer, 0, bytesRead);
        if (update != null) {
          if ((int) (((in * 100) / remoteFileSize)) != prevPercent) {
            prevPercent = (int) (((in * 100) / remoteFileSize));
            update.updateProgress(prevPercent);
          }
        }
      }
      buffOutputStream.flush();
      buffOutputStream.close();
      inputStream.close();
      if (update != null) {
        update.setStatusLabelText("Finishing up...");
      }
      try { // Code to make a copy of the current JAR file
        File inputFile = new File(path.substring(0, path.lastIndexOf(".jar")) + ".temp.jar");
        InputStream fIn = new BufferedInputStream(new FileInputStream(inputFile));
        File outputFile = new File(path);
        buffOutputStream = new BufferedOutputStream(new FileOutputStream(outputFile));
        buffer = new byte[32 * 1024];
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
        ErrorHandler.showErrorWindow("ERROR updating", "Unable to complete update -- unable to copy temp JAR file to current JAR file.", e);
        ErrorHandler.fatalError();
      }
      System.out.println("Download Complete!");
    } catch (Exception e) {
      e.printStackTrace();
      System.out.println("ERROR updating program!");
      ErrorHandler.showErrorWindow("ERROR updating the program", "The program was unable to successfully download the update. If the problem continues, please manually download the latest version at " + UPDATE_LINK, e);
      ErrorHandler.fatalError();
    }
  }
}
