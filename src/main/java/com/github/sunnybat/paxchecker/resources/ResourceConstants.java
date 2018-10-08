package com.github.sunnybat.paxchecker.resources;

import java.util.Map;
import java.util.TreeMap;

/**
 * Constants for Resources.
 *
 * @author SunnyBat
 */
public class ResourceConstants {

	/**
	 * The path to the folder that resources are stored in. Contains a trailing Line.Separator.
	 */
	public static final String RESOURCE_LOCATION = getResourceLocation();
	public static final String LIBRARY_LOCATION = RESOURCE_LOCATION + "lib/v1/";
	public static final String LIBRARY_DOWNLOAD_LINK = "https://www.dropbox.com/s/7q8jqd08lee1wbk/libsV1.zip?dl=1";
	public static final String CLIENT_SECRET_JSON_PATH = "com/github/sunnybat/paxchecker/resources/keys/client_secret.json";
	public static final String TWITTER_KEYS_PATH = "com/github/sunnybat/paxchecker/resources/keys/twitter_secret.ini";
	public static final Map<String, String> DEFAULT_FILE_INFO = getDefaultFileInfo();

	private static Map<String, String> getDefaultFileInfo() { // TODO: Better method than this... Probably file with constants
		Map<String, String> ret = new TreeMap<>();
		ret.put("Alarm.wav", "https://www.dropbox.com/s/9s1frqkgduv226s/Alarm.wav?dl=1");
		ret.put("Alert.png", "https://www.dropbox.com/s/xv1g1f2bzttv3kv/alert.png?dl=1");
		ret.put("PAXWest.png", "https://www.dropbox.com/s/nu6iavrdggv64df/PAXWest.png?dl=1");
		ret.put("PAXEast.png", "https://www.dropbox.com/s/fv3rawxyyc0ihbl/PAXEast.png?dl=1");
		ret.put("PAXSouth.png", "https://www.dropbox.com/s/gku3zea8529b3co/PAXSouth.png?dl=1");
		ret.put("PAXAus.png", "https://www.dropbox.com/s/gku3zea8529b3co/PAXSouth.png?dl=1");
		ret.put("libsV1.zip", "https://www.dropbox.com/s/7q8jqd08lee1wbk/libsV1.zip?dl=1");
		return ret;
	}

	private static String getResourceLocation() {
		String os = System.getProperty("os.name").toLowerCase();
		if (os.contains("windows")) {
			return System.getenv("APPDATA") + "/PAXChecker/"; // Has / and \ in path, but Java apparently doesn't care
		} else if (os.contains("mac")) {
			System.out.println("RD PATH: " + System.getProperty("user.home") + "/Library/Application Support/PAXChecker/");
			return System.getProperty("user.home") + "/Library/Application Support/PAXChecker/";
		} else if (os.contains("linux") || os.contains("ubuntu")) {
			return System.getProperty("user.home") + "/.PAXChecker/";
		} else { // Store in current folder, we have no clue what OS this is
			return "PAXChecker/";
		}
	}

}
