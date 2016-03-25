package com.github.sunnybat.paxchecker;

import com.github.sunnybat.commoncode.startup.Startup;
import com.github.sunnybat.commoncode.update.PatchNotesDownloader;
import com.github.sunnybat.commoncode.update.UpdateDownloader;
import com.github.sunnybat.commoncode.update.UpdatePrompt;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

/**
 *
 * @author SunnyBat
 */
public class Updater {

  private static final String PATCH_NOTES_LINK = "https://dl.orangedox.com/r29siEtUhPNW4FKg7T/PAXCheckerUpdates.txt?dl=1";
  private static final String UPDATE_LINK = "https://dl.orangedox.com/TXu5eUDa2Ds3RSKVUI/PAXChecker.jar?dl=1";
  private static final String BETA_UPDATE_LINK = "https://dl.orangedox.com/BqkMXYrpYjlBEbfVmd/PAXCheckerBETA.jar?dl=1";
  private static final String PATCH_NOTES_LINK_ANONYMOUS = "https://dl.orangedox.com/r29siEtUhPNW4FKg7T/PAXCheckerUpdates.txt?dl=1";
  private static final String UPDATE_LINK_ANONYMOUS = "https://dl.orangedox.com/TXu5eUDa2Ds3RSKVUI/PAXChecker.jar?dl=1";
  private static final String BETA_UPDATE_LINK_ANONYMOUS = "https://dl.orangedox.com/BqkMXYrpYjlBEbfVmd/PAXCheckerBETA.jar?dl=1";
  private final String programVersion;
  private String patchNotes;
  private boolean anonymous = false;

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
      startupOutput.setStatus("Checking for updates...");
      PatchNotesDownloader notesDownloader = new PatchNotesDownloader(getPatchNotesLink());
      notesDownloader.downloadVersionNotes(programVersion);
      notes = notesDownloader.getVersionNotes();
      if (notesDownloader.updateAvailable()) {
        UpdateDownloader myDownloader = new UpdateDownloader(getUpdateLink(), getBetaUpdateLink());
        UpdatePrompt myPrompt = new UpdatePrompt("PAXChecker", myDownloader.getUpdateSize(), notesDownloader.getUpdateLevel(),
            programVersion, notesDownloader.getVersionNotes(programVersion));
        startupOutput.stop();
        myPrompt.showWindow();
        try {
          myPrompt.waitForClose();
        } catch (InterruptedException e) {
        }
        if (myPrompt.shouldUpdateProgram()) {
          myDownloader.updateProgram(myPrompt, new File(PAXChecker.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()));
          System.exit(0); // TODO: Is this actually the right way to kill the program? Or should I pass info out to safely shut down?
        } else {
          startupOutput.start();
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
