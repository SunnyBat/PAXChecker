package com.github.sunnybat.paxchecker.browser;

import com.github.sunnybat.commoncode.error.ErrorBuilder;
import com.github.sunnybat.paxchecker.DataTracker;
import com.github.sunnybat.paxchecker.Expo;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;

/**
 *
 * @author Sunny
 */
public class PaxsiteReader {

	private Expo expoToCheck;

	/**
	 * Creates a new PaxsiteReader for the given Expo. If toCheck is null, this defaults to Expo.WEST.
	 *
	 * @param toCheck The Expo to check
	 */
	public PaxsiteReader(Expo toCheck) {
		if (toCheck == null) {
			toCheck = Expo.WEST;
		}
		expoToCheck = toCheck;
	}

	/**
	 * Gets the current Showclix link on the registration page.
	 *
	 * @return The first Showclix link found, or "[STATUS]" if an exception occurs
	 */
	public String getCurrentShowclixLink() {
		try {
			URL urlToConnectTo = new URL(getWebsiteLink(expoToCheck) + "/registration");
			String link = findShowclixLink(urlToConnectTo);
			return link;
		} catch (MalformedURLException mue) {
		}
		return "[NoConnection]";
	}

	/**
	 * Finds the first Showclix link on the given page.
	 *
	 * @param urlToConnectTo The URL to connect to
	 * @return The first Showclix link found, or "[STATUS]" if an exception occurs.
	 */
	private String findShowclixLink(URL urlToConnectTo) {
		BufferedReader lineReader = null;
		try {
			String line;
			lineReader = setUpConnection(urlToConnectTo);
			while ((line = lineReader.readLine()) != null) {
				DataTracker.addDataUsed(line.length());
				line = line.trim();
				if (line.contains("www.showclix.com")) {
					String parseRef = line.toLowerCase();
					String ret;
					if (parseRef.contains("http://")) {
						ret = line.substring(parseRef.indexOf("www.showclix.com") - 7);
					} else if (parseRef.contains("https://")) {
						ret = line.substring(parseRef.indexOf("www.showclix.com") - 8);
					} else {
						ret = line.substring(parseRef.indexOf("www.showclix.com"));
					}
					parseRef = ret.toLowerCase();
					ret = ret.substring(0, parseRef.indexOf("\""));
					return ret;
				}
			}
		} catch (UnknownHostException | MalformedURLException | SocketTimeoutException e) {
			return "[NoConnection]";
		} catch (IOException ioe) {
			return "[IOException]";
		} catch (Exception e) {
			new ErrorBuilder()
					.setError(e)
					.setErrorMessage("An unknown error has occurred while attempting to read the PAX website.")
					.buildWindow();
			System.out.println("ERROR");
			return "[ERROR]";
		} finally {
			try {
				if (lineReader != null) {
					lineReader.close();
				}
			} catch (IOException ioe) {
				// nothing to see here
				System.out.println("Note: Unable to close InputStream for getCurrentButtonLinkLine()");
				ioe.printStackTrace();
			}
		}
		return "[NoFind]";
	}

	/**
	 * Sets up a BufferedReader to read from the given URL.
	 *
	 * @param urlToConnectTo The URL to read from
	 * @return The BufferedReader to read from
	 * @throws IOException If an IOException occurs while setting up the connection
	 */
	private BufferedReader setUpConnection(URL urlToConnectTo) throws IOException {
		InputStream rawInputStream = null;
		BufferedReader lineReader;
		HttpURLConnection httpCon = Browser.setUpConnection(urlToConnectTo);
		rawInputStream = httpCon.getInputStream();
		lineReader = new BufferedReader(new InputStreamReader(rawInputStream));
		return lineReader;
	}

	/**
	 * Returns the HTTP address of the given PAX Expo. Be sure to only use the name of the expo (ex:
	 * west) OR the full name (ex: pax west) as the argument.
	 *
	 * @param expo The PAX expo to get the website link for
	 * @return The website link of the specified expo, or the PAX West link if invalid.
	 */
	public String getWebsiteLink(Expo expo) {
		if (expo == null) {
			return "http://west.paxsite.com";
		}
		switch (expo) {
			case WEST:
				return "http://west.paxsite.com";
			case EAST:
				return "http://east.paxsite.com";
			case SOUTH:
				return "http://south.paxsite.com";
			case AUS:
				return "http://aus.paxsite.com";
			default:
				System.out.println("Expo not found: " + expo);
				return "http://west.paxsite.com";
		}
	}

}
