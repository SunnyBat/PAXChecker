package com.github.sunnybat.paxchecker.check;

import com.github.sunnybat.paxchecker.browser.Browser;
import com.github.sunnybat.paxchecker.resources.ResourceConstants;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URLDecoder;
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
    private static final String CALLBACK_URL_FORMAT = "http://localhost:%1$d/PAXChecker/twittercallback";
    private static final String CALLBACK_URL_GET_PATH = "/PAXChecker/twittercallback";
    private static final String RESPONSE_HTML_SUCCESS = "HTTP/1.0 200 OK\r\n\r\n<html><head><title>Success</title></head><body>The PAXChecker successfully authenticated. You may close this window.</body></html>";
    private static final String RESPONSE_HTML_FAILURE = "HTTP/1.0 401 Unauthorized\r\n\r\n<html><head><title>Failure</title></head><body>The PAXChecker was unable to be authenticated with Twitter. Please try again or <a href=\"https://www.reddit.com/message/compose/?to=SunnyBat\">contact /u/SunnyBat</a>.</body></html>";
    private static final String AUTH_TOKEN_FILE_PATH = ResourceConstants.RESOURCE_LOCATION + "TwitterToken";
    private static final String OAUTH_CREDENTIALS_FILE_PATH = ResourceConstants.TWITTER_KEYS_PATH;

    private String[] apiKeys;
    private Twitter twitterAccount;
    private AccessToken accessToken;
    private OauthClientKeys clientKeys;
    private boolean interrupt = false;
    private final Object interruptLock = new Object();

    public TwitterAccount() {
        clientKeys = getClientKeys();
        if (clientKeys == null) {
            throw new NullPointerException("Unable to find clientKeys");
        }
    }

    public TwitterAccount(String consumerKey, String consumerSecret, String applicationKey, String applicationSecret) {
        if (consumerKey == null || consumerSecret == null || applicationKey == null || applicationSecret == null) {
            throw new IllegalArgumentException("All keys must not be null");
        } else {
            apiKeys = new String[]{consumerKey, consumerSecret, applicationKey, applicationSecret};
        }
    }

    public TwitterAccount(String[] apiKeys) {
        if (apiKeys == null || apiKeys.length != 4) {
            throw new IllegalArgumentException("apiKeys must contain the consumerKey, consumerSecret, applicationKey, and applicationSecet, in that order, with no extra elements.");
        } else {
            this.apiKeys = new String[]{apiKeys[0], apiKeys[1], apiKeys[2], apiKeys[3]};
        }
    }

    public void authenticate(final TwitterAccountAuth userInteractor, boolean forcePinAuth) {
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
            } else { // No Twitter token found
                // Create new Twitter to use for authentication
                twitterAccount = new TwitterFactory().getInstance();
                twitterAccount.setOAuthConsumer(clientKeys.consumerKey, clientKeys.consumerSecret);

                // Create callback service
                ServerSocket callbackListener = null;
                if (!forcePinAuth) {
                    callbackListener = openNewServerSocket();
                }
                if (callbackListener != null) {
                    String callbackUrl = String.format(CALLBACK_URL_FORMAT, callbackListener.getLocalPort());
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

                    // Set the socket listen timeout to 250ms, so it times out every
                    // 250ms. This allows us to check for interrupts.
                    callbackListener.setSoTimeout(250);

                    // Listen for callback
                    userInteractor.updateStatus("Waiting for authorization");
                    Socket s;
                    while (true) {
                        try {
                            s = callbackListener.accept();
                            callbackListener.close();
                            break;
                        } catch (SocketTimeoutException ste) {
                            synchronized (interruptLock) {
                                if (interrupt) {
                                    callbackListener.close();
                                    authFailed(userInteractor, "Authentication cancelled");
                                    return;
                                }
                            }
                        } catch (IOException ioe) {
                            authFailed(userInteractor, "Unable to obtain Request Token");
                            return;
                        }
                    }

                    // Read the callback
                    InputStream toRead = s.getInputStream();
                    OutputStream responseStream = s.getOutputStream();
                    String finalFirstString = readFirstLineOfHttpResponse(toRead);

                    // Parse the callback
                    OauthCallback callbackInfo = parseGetCallbackUrlLine(finalFirstString);

                    // Act on the callback
                    if (callbackInfo == null) {
                        userInteractor.updateStatus("Error receiving authorization codes");
                        respondWithPage(responseStream, false);
                    } else if (callbackInfo.oauth_token == null || !callbackInfo.oauth_token.equals(requestToken.getToken())) {
                        authFailed(userInteractor, "Request and OAuth tokens don't match");
                        System.out.println("Error: Request Token and Oauth Token are different. Rejecting authentication.");
                        System.out.println("Request Token: " + requestToken.getToken());
                        System.out.println("Oauth Token:   " + callbackInfo.oauth_token);
                        respondWithPage(responseStream, false);
                    } else {
                        userInteractor.updateStatus("Received Twitter authorization, logging in");
                        accessToken = twitterAccount.getOAuthAccessToken(requestToken, callbackInfo.oauth_verifier);
                        respondWithPage(responseStream, true);
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
                    String pin;
                    while ((pin = userInteractor.getAuthorizationPin()) == null) {
                        synchronized (interruptLock) {
                            if (interrupt) {
                                authFailed(userInteractor, "Authentication cancelled");
                                return;
                            }
                        }
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

    private void authFailed(TwitterAccountAuth userInteractor, String message) {
        userInteractor.updateStatus("Error: " + message);
        twitterAccount = null;
        userInteractor.authFailure();
        interrupt = false;
    }

    public void interrupt() {
        synchronized (interruptLock) {
            interrupt = true;
        }
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
            BufferedReader inputStream = new BufferedReader(new InputStreamReader(ClassLoader.getSystemResourceAsStream(OAUTH_CREDENTIALS_FILE_PATH)));
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
        } catch (IOException ioe) {
            return null;
        }
    }

    private String readFirstLineOfHttpResponse(InputStream toRead) throws IOException {
        byte[] buff = new byte[128];
        StringBuilder allReadContent = new StringBuilder();
        while (toRead.read(buff) != -1) {
            String currentInput = new String(buff);
            System.out.print(currentInput);
            if (currentInput.contains("\r\n")) {
                allReadContent.append(currentInput.substring(0, currentInput.indexOf("\r\n")));
                break;
            } else {
                allReadContent.append(currentInput);
            }
        }
        return allReadContent.toString();
    }

    private void respondWithPage(OutputStream toWrite, boolean success) throws IOException {
        if (success) {
            toWrite.write(RESPONSE_HTML_SUCCESS.getBytes());
        } else {
            toWrite.write(RESPONSE_HTML_FAILURE.getBytes());
        }
        toWrite.close();
    }

    private ServerSocket openNewServerSocket() {
        for (int currentPort = MINIMUM_PORT_NUMBER; currentPort <= MAXIMUM_PORT_NUMBER; currentPort++) {
            try {
                ServerSocket toUse = new ServerSocket(currentPort, 0, InetAddress.getLoopbackAddress());
                System.out.println("Using port " + currentPort + " for callback");
                return toUse;
            } catch (IOException ioe) {
                System.out.println("Port " + currentPort + " is already open");
            }
        }
        System.out.println("All ports are open. We can't receive a callback.");
        return null;
    }

    private OauthCallback parseGetCallbackUrlLine(String urlLine) {
        OauthCallback ret = new OauthCallback();
        if (urlLine.contains(CALLBACK_URL_GET_PATH) // Check for valid callback path
            && urlLine.contains("GET") // Ensure proper request
            && urlLine.contains("?") // Ensure URL parameters specified
            && urlLine.contains("HTTP/1.1")) { // Ensure valid HTTP request we understand

            // 1. Strip to start of URL parameters
            String urlParameters = urlLine.substring(urlLine.indexOf("?") + 1);
            // 2. Strip off end of GET request to only URL parameters
            urlParameters = urlParameters.substring(0, urlParameters.indexOf("HTTP/1.1") - 1);
            // 3. Split URL parameters by &, so we get "key=value" in each index
            String[] paramsSplit = urlParameters.split("&");

            // 4. Evaluate each Key and Value
            for (String keyValueString : paramsSplit) {
                // Split by = -- index 0 is the key and index 1 is the value
                String[] keyValueSplit = keyValueString.split("=");
                if (keyValueSplit.length == 2) {
                    String keyName = keyValueSplit[0];
                    String keyValue = keyValueSplit[1];

                    // At this point, we need to URL decode the key and value
                    // We can't do it before, since if there is a URL encoded
                    // & or = in the key or value, it will mess up our splitting
                    try {
                        keyName = URLDecoder.decode(keyName, "UTF-8");
                        keyValue = URLDecoder.decode(keyValue, "UTF-8");
                    } catch (UnsupportedEncodingException uee) {
                        System.out.println("Unable to decode URL parameter value; attempting to parse and strip");
                    }

                    // Now, we need to assign the key and value to the field
                    // in our OauthCallback object
                    try {
                        // Reflect in and get the field that has the same name
                        // as the key
                        Field callbackField = ret.getClass().getField(keyName);
                        // And set the value
                        callbackField.set(ret, keyValue);
                    } catch (NoSuchFieldException nsfe) {
                        System.out.println("Unrecognized keyValuePair in Twitter callback URL: " + keyValueString);
                    } catch (IllegalAccessException | SecurityException e) {
                        System.out.println("Unable to set OauthCallback value " + keyName + " to " + keyValue + " -- something has likely gone wrong.");
                        System.out.println("Exception: " + e.getMessage());
                    }
                } else {
                    System.out.println("Invalid keyValuePair in Twitter callback URL: " + keyValueString);
                }
            }

            // Verify that all of our expected fields have been saved
            // Later on, if we have optional fields that we're reading, we can
            // make an @Optional custom annotation and ignore fields that are
            // decorated with this.
            try {
                for (Field toCheck : ret.getClass().getFields()) { // Get all public fields
                    if (toCheck.get(ret) == null) { // Check to make sure it's been set
                        System.out.println("Unable to parse full Twitter callback response: " + urlLine);
                        System.out.println("Twitter will not be authenticated.");
                        return null;
                    }
                }
            } catch (IllegalArgumentException | IllegalAccessException iae) {
                System.out.println("Internal error while verifying callback response: " + urlLine);
                System.out.println("Twitter will not be authenticated.");
                return null;
            }
        }
        return ret;
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
     * Represents the URL arguments in a success GET callback from the Twitter
     * API.
     */
    private class OauthCallback {

        public String oauth_token;
        public String oauth_verifier;
    }

}
