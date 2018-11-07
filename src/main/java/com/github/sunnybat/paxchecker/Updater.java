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
	private static final String PATCH_NOTES_LINK_ANONYMOUS = "https://www.dropbox.com/s/fr3cg7nc99pztkk/PAXCheckerUpdates.txt?dl=1";
	private static final String UPDATE_LINK_ANONYMOUS = "https://www.dropbox.com/s/4rzsmbvtblm8ee9/PAXChecker.jar?dl=1";
	private static final String BETA_UPDATE_LINK_ANONYMOUS = "https://www.dropbox.com/s/ugctx94a0vmsv6n/PAXCheckerBETA.jar?dl=1";
	private final String programVersion;
	private String patchNotes;
	private boolean anonymous = false;
	private boolean headless = false;
	private boolean beta = false;

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
	 * Enables BETA updating
	 */
	public void enableBetaUpdates() {
		beta = true;
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
	 * Checks for updates. This loads Patch Notes and prompts the user if they want to update. If the
	 * user does update, it kills the PAXChecker.
	 *
	 * @param startupOutput The Startup object to update with information as updates are loaded
	 * @param args The arguments to restart the PAXChecker with
	 * @return True if updates were checked for successfully, false if not
	 */
	public boolean loadUpdates(Startup startupOutput, String[] args) {
		String notes = null;
		try {
			if (startupOutput != null) {
				startupOutput.setStatus("Checking for updates...");
			}
			PatchNotesDownloader notesDownloader = new PatchNotesDownloader(getPatchNotesLink());
			if (beta) {
				notesDownloader.enableBetaDownload();
			}
			notesDownloader.downloadVersionNotes(programVersion);
			notes = notesDownloader.getVersionNotes();
			if (notesDownloader.updateAvailable()) {
				// Prompt for update
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

				// Update
				if (shouldUpdate) {
					if (notesDownloader.getUpdateLevel() == 1) {
						myDownloader.setUseBeta();
					}
					File jarToDownloadTo = new File(PAXChecker.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
					myDownloader.updateProgram(myPrompt, jarToDownloadTo);
					if (!headless) { // Don't want to start a new instance on the command-line -- it's harder to access than just manually starting the PAXChecker again
						myPrompt.setStatusLabelText("Starting new instance of the PAXChecker...");
						runNewProgramInstance(args, jarToDownloadTo);
					}
					System.exit(0);
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

	private void runNewProgramInstance(String[] args, File jarToRun) {
		try {
			StringBuilder command = new StringBuilder();
			command.append("java -jar ").append(jarToRun.getPath());
			for (String arg : args) {
				command.append(" ").append(arg);
			}
			command.append(" -noupdate"); // Force no rechecking for updates
			Runtime.getRuntime().exec(command.toString(), args);
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
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
