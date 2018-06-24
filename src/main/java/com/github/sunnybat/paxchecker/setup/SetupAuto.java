package com.github.sunnybat.paxchecker.setup;

import com.github.sunnybat.commoncode.email.account.EmailAccount;
import com.github.sunnybat.commoncode.email.account.GmailAccount;
import com.github.sunnybat.commoncode.email.account.SmtpAccount;
import com.github.sunnybat.paxchecker.check.TwitterAccount;
import com.github.sunnybat.paxchecker.check.TwitterAccountAuth;
import com.github.sunnybat.paxchecker.resources.ResourceConstants;
import twitter4j.Twitter;

/**
 *
 * @author SunnyBat
 */
public class SetupAuto implements Setup {

    private String[] args;

    public SetupAuto(String[] args) {
        this.args = new String[args.length];
        System.arraycopy(args, 0, this.args, 0, args.length);
    }

    @Override
    public void promptForSettings() {
        // Do not need to do anything
    }

    private boolean hasArg(String arg) {
        for (String s : args) {
            if (s.equals(arg)) {
                return true;
            }
        }
        return false;
    }

    private String getArg(String arg) {
        return getArg(arg, 1);
    }

    private String getArg(String arg, int indexesOut) {
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals(arg) && i < args.length - indexesOut) {
                return args[i + indexesOut];
            }
        }
        return ""; // Probably shouldn't return null?
    }

    @Override
    public EmailAccount getEmailAccount() {
        EmailAccount toAuth;
        if (hasArg("-gmailapi")) {
            toAuth = new GmailAccount("PAXChecker", ResourceConstants.RESOURCE_LOCATION, ResourceConstants.CLIENT_SECRET_JSON_PATH);
        } else {
            String username = getArg("-username");
            String password = getArg("-password");
            if (username != null && password != null) {
                toAuth = new SmtpAccount(username, password);
            } else {
                return null;
            }
        }
        if (toAuth.checkAuthentication()) {
            String emails = getArg("-cellnum");
            if (emails != null) {
                toAuth.addBccEmailAddress(emails);
                return toAuth;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    @Override
    public boolean shouldCheckPAXWebsite() {
        return !hasArg("-nopax");
    }

    @Override
    public boolean shouldCheckShowclix() {
        return !hasArg("-noshowclix");
    }

    @Override
    public boolean shouldCheckKnownEvents() {
        return !hasArg("-noknownevents");
    }

    @Override
    public boolean shouldCheckTwitter() {
        return !hasArg("-notwitter");
    }

    @Override
    public boolean shouldFilterShowclix() {
        return hasArg("-filtershowclix");
    }

    @Override
    public boolean shouldFilterTwitter() {
        return hasArg("-filtertwitter");
    }

    @Override
    public boolean shouldTextTweets() {
        return hasArg("-texttweets");
    }

    @Override
    public boolean shouldPlayAlarm() {
        return hasArg("-alarm");
    }

    @Override
    public int timeBetweenChecks() {
        try {
            return Integer.parseInt(getArg("-delay"));
        } catch (NumberFormatException nfe) {
            return 10;
        }
    }

    @Override
    public String getExpoToCheck() {
        return getArg("-expo");
    }

    @Override
    public Twitter getTwitterAccount() {
        final TwitterAccount myAccount;
        if (hasArg("-twitterkeys")) {
            myAccount = new TwitterAccount(getArg("-twitterkeys", 1), getArg("-twitterkeys", 2), getArg("-twitterkeys", 3), getArg("-twitterkeys", 4));
        } else {
            myAccount = new TwitterAccount();
        }

        // TODO Timeout after 60 seconds
        myAccount.authenticate(new TwitterAccountAuth() {
            @Override
            public void authFailure() {
                System.out.println("Unable to authenticate Twitter account.");
            }

            @Override
            public void authSuccess() {
                System.out.println("Twitter account authenticated");
            }

            @Override
            public void setAuthUrl(String url) {
                System.out.println("Authorization URL: " + url);
                System.out.println("You'll need to manually verify Twitter via command-line aruments before you can use it with auto-start.");
                myAccount.interrupt();
            }

            @Override
            public String getAuthorizationPin() {
                return null;
            }

            @Override
            public void promptForAuthorizationPin() {
                System.out.println("PIN authorization request. Interrupting.");
                myAccount.interrupt();
            }

            @Override
            public void updateStatus(String status) {
                System.out.println(status);
            }
        }, true); // Force PIN auth just so we don't try to open a ServerSocket
        return myAccount.getAccount();
    }

    @Override
    public boolean shouldCheckForUpdatesDaily() {
        return hasArg("-dailyupdates");
    }

}
