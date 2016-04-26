package com.github.sunnybat.paxchecker;

import com.github.sunnybat.commoncode.startup.Startup;
import com.github.sunnybat.commoncode.update.PatchNotesDownloader;
import com.github.sunnybat.commoncode.update.UpdateDownloader;
import com.github.sunnybat.commoncode.update.UpdatePrompt;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Scanner;

/**
 *
 * @author SunnyBat
 */
public class Updater {

  private static final String PATCH_NOTES_LINK = "https://dl.orangedox.com/r29siEtUhPNW4FKg7T/PAXCheckerUpdates.txt?dl=1";
  private static final String UPDATE_LINK = "https://dl.orangedox.com/TXu5eUDa2Ds3RSKVUI/PAXChecker.jar?dl=1";
  private static final String BETA_UPDATE_LINK = "https://dl.orangedox.com/BqkMXYrpYjlBEbfVmd/PAXCheckerBETA.jar?dl=1";
  private static final String PATCH_NOTES_LINK_ANONYMOUS = "https://dl.dropboxusercontent.com/u/16152108/PAXCheckerUpdates.txt?dl=1";
  private static final String UPDATE_LINK_ANONYMOUS = "https://dl.dropboxusercontent.com/u/16152108/PAXChecker.jar?dl=1";
  private static final String BETA_UPDATE_LINK_ANONYMOUS = "https://dl.dropboxusercontent.com/u/16152108/PAXCheckerBETA.jar?dl=1";
  private final String programVersion;
  private String patchNotes;
  private boolean anonymous = false;
  private boolean headless = false;

  public Updater(String programVersion) {
    this.programVersion = programVersion;
  }

  /**
   * Enables anonymous downloads
   */
  public void enableAnonymousMode() {
    anonymous = true;
  }

  /**
   * Enables Headless Mode
   */
  public void enableHeadlessMode() {
    headless = true;
  }

  /**
   * Gets the Patch Notes currently loaded.
   *
   * @return The Patch Notes, or null if none are loaded
   */
  public String getPatchNotes() {
    return patchNotes;
  }

  /**
   * Checks for updates. This loads Patch Notes and prompts the user if they want to update. If the user does update, it kills the PAXChecker.
   *
   * @param startupOutput The Startup object to update with information as updates are loaded
   * @return True if updates were checked for successfully, false if not
   */
  public boolean loadUpdates(Startup startupOutput) {
    String notes = null;
    try {
      if (startupOutput != null) {
        startupOutput.setStatus("Checking for updates...");
      }
      PatchNotesDownloader notesDownloader = new PatchNotesDownloader(getPatchNotesLink());
      notesDownloader.downloadVersionNotes(programVersion);
      notes = notesDownloader.getVersionNotes();
      if (notesDownloader.updateAvailable()) {
        UpdateDownloader myDownloader = new UpdateDownloader(getUpdateLink(), getBetaUpdateLink());
        UpdatePrompt myPrompt = null;
        boolean shouldUpdate;
        if (!headless) {
          myPrompt = new UpdatePrompt("PAXChecker", myDownloader.getUpdateSize(), notesDownloader.getUpdateLevel(),
              programVersion, notesDownloader.getVersionNotes(programVersion));
          if (startupOutput != null) {
            startupOutput.stop();
          }
          myPrompt.showWindow();
          try {
            myPrompt.waitForClose();
          } catch (InterruptedException e) {
          }
          shouldUpdate = myPrompt.shouldUpdateProgram();
        } else {
          System.out.println("Update found!");
          System.out.println("Current version: " + programVersion);
          System.out.println("New version level = " + notesDownloader.getUpdateLevel());
          System.out.println("Download size: " + myDownloader.getUpdateSize());
          System.out.print("Update to new version (Y/N): ");
          Scanner in = new Scanner(System.in);
          shouldUpdate = in.nextLine().toUpperCase().startsWith("Y");
        }
        if (shouldUpdate) {
          if (notesDownloader.getUpdateLevel() == 1) {
            myDownloader.setUseBeta();
          }
          myDownloader.updateProgram(myPrompt, new File(PAXChecker.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()));
          System.exit(0); // TODO: Is this actually the right way to kill the program? Or should I pass info out to safely shut down?
        } else if (startupOutput != null) {
          startupOutput.start(); // Show Startup again
        }
      }
    } catch (IOException | URISyntaxException e) {
      e.printStackTrace();
      return false;
    }
    patchNotes = notes;
    return true;
  }

  private String getPatchNotesLink() {
    if (anonymous) {
      return PATCH_NOTES_LINK_ANONYMOUS;
    } else {
      return PATCH_NOTES_LINK;
    }
  }

  private String getUpdateLink() {
    if (anonymous) {
      return UPDATE_LINK_ANONYMOUS;
    } else {
      return UPDATE_LINK;
    }
  }

  private String getBetaUpdateLink() {
    if (anonymous) {
      return BETA_UPDATE_LINK_ANONYMOUS;
    } else {
      return BETA_UPDATE_LINK;
    }
  }

}
