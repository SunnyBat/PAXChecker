package com.github.sunnybat.paxchecker.check;

import com.github.sunnybat.paxchecker.Expo;
import com.github.sunnybat.paxchecker.browser.Browser;
import com.github.sunnybat.paxchecker.browser.PaxsiteReader;
import com.github.sunnybat.paxchecker.status.CheckerInfoOutput;

/**
 *
 * @author Sunny
 */
public class CheckPaxsite extends Check {

	private String lastLinkFound;
	private String currentLinkFound;
	private Expo expoToCheck;
	private PaxsiteReader siteReader;

	/**
	 * Creates a new PaxsiteChecker.
	 *
	 * @param expo The expo to check
	 */
	public CheckPaxsite(Expo expo) {
		super();
		expoToCheck = expo;
		siteReader = new PaxsiteReader(expoToCheck);
	}

	@Override
	public synchronized void init(CheckerInfoOutput s, java.util.concurrent.Phaser cB) {
		super.init(s, cB);
		updateWithInfo("Paxsite initialized.");
	}

	@Override
	public synchronized boolean ticketsFound() {
		if (currentLinkFound == null) {
			return false;
		} else if (currentLinkFound.equals(lastLinkFound) || currentLinkFound.startsWith("[")) {
			return false;
		} else if (currentLinkFound.toLowerCase().contains("showclix.com")) {
			System.out.println("OMG IT'S UPDATED: " + currentLinkFound);
			return true;
		} else {
			return false;
		}
	}

	@Override
	public synchronized final void updateLink() {
		updateLink("[Checking]");
		currentLinkFound = siteReader.getCurrentShowclixLink();
		if (!currentLinkFound.startsWith("[")) {
			String redirectedURL = Browser.unshortenURL(currentLinkFound);
			if (redirectedURL != null) {
				currentLinkFound = redirectedURL;
			}
		}
		updateLink(getLink());
	}

	@Override
	public synchronized String getLink() {
		return currentLinkFound;
	}

	@Override
	public synchronized void reset() {
		if (currentLinkFound == null) {
			lastLinkFound = siteReader.getCurrentShowclixLink();
		} else {
			lastLinkFound = currentLinkFound;
		}
	}

}
