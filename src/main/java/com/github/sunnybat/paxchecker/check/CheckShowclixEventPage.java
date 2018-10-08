package com.github.sunnybat.paxchecker.check;

import com.github.sunnybat.paxchecker.browser.Browser;
import com.github.sunnybat.paxchecker.status.CheckerInfoOutput;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author SunnyBat
 */
public class CheckShowclixEventPage extends Check {

	private static final String SHOWCLIX_EVENT_BASE_URL = "http://www.showclix.com/event/"; // Would do HTTPS, however I've had issues with this before
	private List<String> pageCheckList = new ArrayList<>();                                 // and will follow redirects to HTTPS instead
	private String validPageURL = null;

	public CheckShowclixEventPage() {
		pageCheckList.add(SHOWCLIX_EVENT_BASE_URL + "PAXWest16"); // Thanks for the convenient IDs, Showclix/PA
		pageCheckList.add(SHOWCLIX_EVENT_BASE_URL + "PAXWest2016");
		pageCheckList.add(SHOWCLIX_EVENT_BASE_URL + "PAXPrime16");
		pageCheckList.add(SHOWCLIX_EVENT_BASE_URL + "PAXPrime2016");
	}

	@Override
	public synchronized void init(CheckerInfoOutput s, java.util.concurrent.Phaser cB) {
		super.init(s, cB);
		updateWithInfo("Event Checking initialized.");
	}

	@Override
	public synchronized boolean ticketsFound() {
		return validPageURL != null;
	}

	@Override
	public synchronized final void updateLink() {
		updateLink("[Checking]");
		for (String url : pageCheckList) {
			try {
				URL connectTo = new URL(url);
				if (testURL(connectTo)) {
					break; // Found a link, we're done
				}
			} catch (MalformedURLException mue) {
				System.out.println("CSEP: Invalid URL: " + url);
			}
		}
		updateLink(getLink());
	}

	private boolean testURL(URL connectTo) {
		if (connectTo == null) {
			return false;
		}
		HttpURLConnection conn = null;
		try {
			conn = Browser.setUpConnection(connectTo);
			if (conn == null) { // In case it fails to set up correctly
				System.out.println("CSEP: URLConnection failed to set up for " + connectTo);
				return false;
			}
			if (conn.getResponseCode() >= 400 && conn.getResponseCode() < 500) { // getResponseCode() will throw IOE if unable to properly set up connection
				if (conn.getResponseCode() != 404) { // I don't think it should ever be 404, but if it is, we don't need to report it
					System.out.println("CSEP: Unexpected error response code " + conn.getResponseCode());
				}
				return false;
			} else if (conn.getResponseCode() >= 300 && conn.getResponseCode() < 400) { // Redirect, however it's probably going from HTTP
				System.out.println("CSEP: Location = " + conn.getHeaderField("Location"));      // to HTTPS, which Java does not do for security
				return testURL(new URL(conn.getHeaderField("Location"))); // Will throw MalformedURLException if getHF() is null, we're catching this
			} else if (conn.getResponseCode() >= 200 && conn.getResponseCode() < 300) {
				System.out.println("CSEP: Found :: URL = " + conn.getURL() + " :: Code = " + conn.getResponseCode());
				validPageURL = connectTo.toString();
				return true;
			} else {
				System.out.println("Unexpected response code " + conn.getResponseCode());
				return false;
			}
		} catch (IOException ioe) {
			if (conn != null) {
				System.out.println("CSEP: Unsure of URL " + conn.getURL() + " (" + connectTo + ")");
				ioe.printStackTrace();
			} else {
				System.out.println("CSEP: Unable to find link from " + connectTo);
			}
			return false;
		}
	}

	@Override
	public synchronized String getLink() {
		if (validPageURL == null) {
			return "[None Found]";
		} else {
			return validPageURL;
		}
	}

	@Override
	public synchronized void reset() {
		if (validPageURL != null) {
			pageCheckList.remove(validPageURL);
			validPageURL = null;
		}
	}

}
