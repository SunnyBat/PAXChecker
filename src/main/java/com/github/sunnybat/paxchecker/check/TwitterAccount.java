package com.github.sunnybat.paxchecker.check;

import com.github.sunnybat.commoncode.oauth.OauthCallbackServer;
import com.github.sunnybat.commoncode.oauth.OauthRequired;
import com.github.sunnybat.commoncode.oauth.OauthStatusUpdater;
import com.github.sunnybat.paxchecker.browser.Browser;
import com.github.sunnybat.paxchecker.resources.ResourceConstants;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.ConfigurationBuilder;

/**
 *
 * @author SunnyBat
 */
public class TwitterAccount {

	private static final int MINIMUM_PORT_NUMBER = 32685;
	private static final int MAXIMUM_PORT_NUMBER = 32694;
	private static final String AUTH_TOKEN_FILE_PATH = ResourceConstants.RESOURCE_LOCATION + "TwitterToken";

	private String[] apiKeys;
	private Twitter twitterAccount;
	private AccessToken accessToken;
	private OauthClientKeys clientKeys;
	private OauthCallbackServer<TwitterCallbackParameters> callbackServer;

	public TwitterAccount() {
		clientKeys = getClientKeys();
		if (clientKeys == null) {
			throw new NullPointerException("Unable to find clientKeys");
		}
		createNewCallbackServer();
	}

	public TwitterAccount(String consumerKey, String consumerSecret, String applicationKey, String applicationSecret) {
		if (consumerKey == null || consumerSecret == null || applicationKey == null || applicationSecret == null) {
			throw new IllegalArgumentException("All keys must not be null");
		} else {
			apiKeys = new String[]{consumerKey, consumerSecret, applicationKey, applicationSecret};
		}
		createNewCallbackServer();
	}

	private void createNewCallbackServer() {
		int[] ports = new int[MAXIMUM_PORT_NUMBER - MINIMUM_PORT_NUMBER + 1];
		for (int port = MINIMUM_PORT_NUMBER; port <= MAXIMUM_PORT_NUMBER; port++) {
			ports[port - MINIMUM_PORT_NUMBER] = port;
		}
		callbackServer = new OauthCallbackServer<>(ports, "/PAXChecker/twittercallback");
	}

	public TwitterAccount(String[] apiKeys) {
		if (apiKeys == null || apiKeys.length != 4) {
			throw new IllegalArgumentException("apiKeys must contain the consumerKey, consumerSecret, applicationKey, and applicationSecet, in that order, with no extra elements.");
		} else {
			this.apiKeys = new String[]{apiKeys[0], apiKeys[1], apiKeys[2], apiKeys[3]};
		}
	}

	public void authenticate(final OauthStatusUpdater userInteractor, boolean forcePinAuth, boolean failIfNoAutoAuth) {
		try {
			userInteractor.updateStatus("Attempting to read saved auth token");
			// Read stored Twitter token
			accessToken = getStoredAccessToken();

			if (accessToken != null) { // Stored Twitter token read
				userInteractor.updateStatus("Attempting to authenticate with saved auth token");
				ConfigurationBuilder config = new ConfigurationBuilder();
				config.setOAuthConsumerKey(clientKeys.consumerKey);
				config.setOAuthConsumerSecret(clientKeys.consumerSecret);
				twitterAccount = new TwitterFactory(config.build()).getInstance(accessToken);
			} else if (apiKeys != null) {
				userInteractor.updateStatus("Attempting to authenticate with API keys");
				ConfigurationBuilder cb = new ConfigurationBuilder();
				cb.setOAuthConsumerKey(apiKeys[0])
						.setOAuthConsumerSecret(apiKeys[1])
						.setOAuthAccessToken(apiKeys[2])
						.setOAuthAccessTokenSecret(apiKeys[3]);
				TwitterFactory tf = new TwitterFactory(cb.build());
				twitterAccount = tf.getInstance();
				accessToken = twitterAccount.getOAuthAccessToken();
			} else if (!failIfNoAutoAuth) { // No Twitter token found
				// Create new Twitter to use for authentication
				twitterAccount = new TwitterFactory().getInstance();
				twitterAccount.setOAuthConsumer(clientKeys.consumerKey, clientKeys.consumerSecret);

				if (!forcePinAuth && callbackServer.openListener()) {
					String callbackUrl = callbackServer.getLocalCallbackUri();
					RequestToken requestToken;
					try {
						requestToken = twitterAccount.getOAuthRequestToken(callbackUrl);
					} catch (Exception e) {
						userInteractor.updateStatus("Unable to obtain Request Token");
						twitterAccount = null;
						userInteractor.authFailure();
						return;
					}

					// Authorization will always ask the user to log in and grant
					// access, regardless of whether the user has previously granted
					// access.
					// Desktop apps are required to use authorization, so we use it.
					// Authentication will automatically redirect if the user has
					// previously granted access to the application. The "Sign in
					// with Twitter" option must be enabled in the application for
					// this to work.
					userInteractor.setAuthUrl(requestToken.getAuthorizationURL());
					Browser.openLinkInBrowser(requestToken.getAuthorizationURL());

					// Listen for callback
					userInteractor.updateStatus("Waiting for authorization");
					TwitterCallbackParameters callbackInfo = new TwitterCallbackParameters();

					// Act on the callback
					if (callbackServer.listenForConnection(callbackInfo)) {
						userInteractor.updateStatus("Received Twitter authorization, logging in");
						accessToken = twitterAccount.getOAuthAccessToken(requestToken, callbackInfo.oauth_verifier);
					} else {
						userInteractor.updateStatus("Error receiving authorization codes");
					}
				} else { // Backup in case we can't open a callback listener
					RequestToken requestToken;
					try {
						requestToken = twitterAccount.getOAuthRequestToken("oob");
					} catch (Exception e) {
						authFailed(userInteractor, "Unable to obtain Request Token");
						return;
					}

					// Request authorization
					userInteractor.setAuthUrl(requestToken.getAuthorizationURL());
					Browser.openLinkInBrowser(requestToken.getAuthorizationURL());

					// Authenticate with PIN
					userInteractor.updateStatus("Waiting for authorization");
					userInteractor.promptForAuthorizationPin();
					String pin = userInteractor.getAuthorizationPin();
					if (pin == null || pin.isEmpty()) {
						authFailed(userInteractor, "PIN entry cancelled");
						return;
					}
					try {
						accessToken = twitterAccount.getOAuthAccessToken(requestToken, pin);
					} catch (TwitterException te) {
						te.printStackTrace();
						if (te.getStatusCode() == 401) {
							authFailed(userInteractor, "Invalid PIN, please try again");
						} else {
							authFailed(userInteractor, "Unable to authenticate");
						}
						return;
					}
				}
			}

			if (accessToken != null) {
				twitterAccount.verifyCredentials(); // Not sure whether we need to call this or not; this gets just a User object...
				userInteractor.updateStatus("Successfully authenticated Twitter");
				userInteractor.authSuccess();
				System.out.println("Twitter successfully authenticated!");
				// Store for future use.
				storeAccessToken(accessToken);
			} else {
				authFailed(userInteractor, "Unable to authenticate with Twitter API.");
			}
		} catch (Exception e) {
			authFailed(userInteractor, "Unknown error while authenticating Twitter account");
			System.out.println("Error while authenticating Twitter account: " + e.getMessage());
		}
	}

	private void authFailed(OauthStatusUpdater userInteractor, String message) {
		userInteractor.updateStatus("Error: " + message);
		twitterAccount = null;
		userInteractor.authFailure();
	}

	public void interrupt() {
		callbackServer.cancelListeningForConnection();
	}

	public Twitter getAccount() {
		return twitterAccount;
	}

	/**
	 * Stores the given Access Token
	 *
	 * @param useId
	 * @param accessToken
	 */
	public void storeAccessToken(AccessToken accessToken) throws IOException {
		FileOutputStream fout = new FileOutputStream(AUTH_TOKEN_FILE_PATH);
		ObjectOutputStream oos = new ObjectOutputStream(fout);
		oos.writeObject(accessToken);
		oos.close();
	}

	public void clearAccessToken() {
		File accessTokenFile = new File(AUTH_TOKEN_FILE_PATH);
		if (accessTokenFile.exists()) {
			accessTokenFile.delete();
		} else {
			System.out.println("Unable to delete " + accessTokenFile.getAbsolutePath());
		}
	}

	private OauthClientKeys getClientKeys() {
		try {
			OauthClientKeys ret = new OauthClientKeys();
			BufferedReader inputStream = new BufferedReader(new InputStreamReader(ClassLoader.getSystemResourceAsStream(ResourceConstants.TWITTER_KEYS_PATH)));
			String nextLine;
			while ((nextLine = inputStream.readLine()) != null) {
				String[] lineSplit = nextLine.split("=");
				if (lineSplit.length == 2) {
					switch (lineSplit[0].toLowerCase()) {
						case "key":
							ret.consumerKey = lineSplit[1];
							break;
						case "secret":
							ret.consumerSecret = lineSplit[1];
							break;
					}
				}
			}

			if (ret.consumerSecret != null && ret.consumerKey != null) {
				return ret;
			} else {
				return null;
			}
		} catch (Exception e) {
			return null;
		}
	}

	private AccessToken getStoredAccessToken() throws IOException {
		try {
			FileInputStream streamIn = new FileInputStream(AUTH_TOKEN_FILE_PATH);
			ObjectInputStream ois = new ObjectInputStream(streamIn);
			AccessToken retToken = (AccessToken) ois.readObject();
			ois.close();
			return retToken;
		} catch (FileNotFoundException fnfe) {
			System.out.println("Twitter AccessToken not found.");
		} catch (ClassNotFoundException cnfe) {
			System.out.println("Internal error: Unable to find class represented by stored AccessToken. Twitter will need to be reauthenticated.");
		}
		return null;
	}

	private class OauthClientKeys {

		public String consumerKey;
		public String consumerSecret;
	}

	/**
	 * Represents the URL arguments in a success GET callback from the Twitter API.
	 */
	public class TwitterCallbackParameters {

		@OauthRequired
		public String oauth_token;
		@OauthRequired
		public String oauth_verifier;
	}

}
