package com.github.sunnybat.paxchecker.setup;

import com.github.sunnybat.commoncode.email.EmailAddress;
import com.github.sunnybat.commoncode.email.account.EmailAccount;
import com.github.sunnybat.commoncode.email.account.GmailAccount;
import com.github.sunnybat.commoncode.email.account.SmtpAccount;
import com.github.sunnybat.commoncode.oauth.OauthStatusUpdater;
import com.github.sunnybat.paxchecker.check.TwitterAccount;
import com.github.sunnybat.paxchecker.resources.ResourceConstants;
import com.google.api.client.util.ArrayMap;
import java.util.Map;
import java.util.Scanner;
import twitter4j.Twitter;

/**
 * A command-line Setup. Gets all the information needed to run the PAXChecker
 * through System.in and System.out.
 *
 * @author SunnyBat
 */
public class SetupCLI implements Setup {

    private boolean checkPaxWebsite;
    private boolean checkShowclixWebsite;
    private boolean filterShowclix;
    private boolean checkKnownEvents;
    private boolean checkTwitter;
    private boolean filterTwitter;
    private boolean textTweets;
    private boolean checkUpdatesDaily;
    private String expoToCheck;
    private boolean playAlarm;
    private int refreshTime = 30;
    private EmailAccount emailAccount;
    private Twitter twitterAccount;
    private String consumerKey;
    private String consumerSecret;
    private String applicationKey;
    private String applicationSecret;

    private Map<Integer, String> carrierList = new ArrayMap<>();

    /**
     * Creates a new SetupCLI. Settings will be prompted for through the
     * command-line.
     */
    public SetupCLI() {
        int i = 0;
        carrierList.put(i++, "AT&T (MMS)");
        carrierList.put(i++, "AT&T (SMS)");
        carrierList.put(i++, "Verizon");
        carrierList.put(i++, "Sprint");
        carrierList.put(i++, "T-Mobile");
        carrierList.put(i++, "U.S. Cellular");
        carrierList.put(i++, "Bell");
        carrierList.put(i++, "Rogers");
        carrierList.put(i++, "Fido");
        carrierList.put(i++, "Koodo");
        carrierList.put(i++, "Telus");
        carrierList.put(i++, "Virgin (CAN)");
        carrierList.put(i++, "Wind");
        carrierList.put(i++, "Sasktel");
    }

    public SetupCLI(String[] twitterKeys) {
        this();
        if (twitterKeys != null && twitterKeys.length == 4) {
            consumerKey = twitterKeys[0];
            consumerSecret = twitterKeys[1];
            applicationKey = twitterKeys[2];
            applicationSecret = twitterKeys[3];
        }
    }

    @Override
    public void promptForSettings() {
        Scanner myScanner = new Scanner(System.in);
        promptForEmail(myScanner);
        promptForExpo(myScanner);
        promptForCheckTypes(myScanner);
        promptForTwitter(myScanner);
        System.out.print("Refresh Time (seconds, 10-120, numbers only): ");
        try {
            refreshTime = Integer.parseInt(myScanner.nextLine());
            refreshTime = Math.max(refreshTime, 10);
            refreshTime = Math.min(refreshTime, 120);
        } catch (Exception e) {
            System.out.println("Error parsing input. Refresh time set to 30 seconds.");
        }
        System.out.print("Play Alarm (Y/N): ");
        playAlarm = isResponseYes(myScanner);
        System.out.println("Check for Updates Every 24 Hours (Y/N): ");
        checkUpdatesDaily = isResponseYes(myScanner);
    }

    private void promptForEmail(Scanner in) {
        while (true) {
            System.out.println("Current email options:");
            System.out.println("1. Gmail API (requires web browser authentication)");
            System.out.println("2. SMTP");
            System.out.println("3. Disable Email");
            System.out.print("Select an option: ");
            int selectedOption = readNumericOption(in);
            switch (selectedOption) {
                case 1:
                    System.out.println("Authenticating with Gmail. This will attempt to open your default browser to authenticate. If it does not open, you must do so manually.");
                    GmailAccount gmailAccount = new GmailAccount("PAXChecker", ResourceConstants.RESOURCE_LOCATION, ResourceConstants.CLIENT_SECRET_JSON_PATH);
                    if (gmailAccount.checkAuthentication()) {
                        System.out.println("Gmail account authenticated.");
                        System.out.println("Account: " + gmailAccount.getEmailAddress());
                        emailAccount = gmailAccount;
                        promptForSendToEmails(in);
                        return;
                    } else {
                        System.out.println("Unable to authenticate Gmail account.");
                        System.out.print("Would you like to try again (Y/N)? ");
                        if (isResponseNo(in)) {
                            return;
                        }
                    }
                    break;
                case 2:
                    System.out.print("Email (leave blank and press ENTER to cancel): ");
                    try {
                        String emailAddress = in.nextLine();
                        if (emailAddress.length() == 0) {
                            // Do nothing, skipping email
                        } else if (emailAddress.length() < 5) {
                            System.out.println("Invalid username. Please input a valid username.");
                        } else { // Valid email address
                            System.out.print("Password: ");
                            String emailPassword = new String(System.console().readPassword());
                            SmtpAccount smtpAccount = new SmtpAccount(emailAddress, emailPassword);
                            if (smtpAccount.checkAuthentication()) {
                                System.out.println("SMTP account authenticated.");
                                System.out.println("Account: " + smtpAccount.getEmailAddress());
                                emailAccount = smtpAccount;
                                promptForSendToEmails(in);
                                return;
                            } else {
                                System.out.println("Unable to authenticate SMTP account.");
                                System.out.print("Would you like to try again (Y/N)? ");
                                if (isResponseNo(in)) {
                                    return;
                                }
                            }
                            break;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.out.println("Program error. Skipping email input. The program will continue to function normally.");
                        return;
                    }
                    break;
                case 3:
                    System.out.println("Email will be disabled.");
                    return;
                case -1:
                    continue;
                default:
                    System.out.println("Invalid option! Please select a valid option.");
                    break;
            }
        }
    }

    private void promptForSendToEmails(Scanner in) {
        while (true) {
            System.out.println("You may enter multiple emails at the same time by delimiting them with semicolons.");
            System.out.println("You may only enter one cell number at a time.");
            System.out.println("Leave the input blank and press ENTER to finish.");
            System.out.print("Cell Number or Email Address(es): ");
            try {
                String cellNum = in.nextLine();
                if (cellNum.length() == 0) {
                    break;
                } else if (cellNum.contains("@")) {
                    emailAccount.addBccEmailAddress(cellNum);
                } else {
                    System.out.println("Carriers:");
                    for (int key : carrierList.keySet()) {
                        System.out.println(key + ". " + carrierList.get(key));
                    }
                    System.out.println("Leave the input blank and press ENTER to discard this cell number.");
                    System.out.print("Carrier number: ");
                    String carrierSelection = in.nextLine();
                    try {
                        int carrierNum = Integer.parseInt(carrierSelection);
                        String carrierExtension = EmailAddress.getCarrierExtension(carrierList.get(carrierNum));
                        emailAccount.addBccEmailAddress(cellNum + carrierExtension);
                    } catch (NumberFormatException nfe) {
                        System.out.println("Invalid carrier selection. Discarding cell number.");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Program error. Stopping cell number input. The program will continue to function normally.");
                break;
            }
        }
        if (emailAccount.getBccAddressList().isEmpty()) {
            System.out.println("You have not entered any emails or cell numbers. Email will be disabled if you do not enter any.");
            System.out.print("Are you sure you want to skip entering emails (Y/N)? ");
            if (isResponseNo(in)) {
                promptForSendToEmails(in); // Can StackOverflow, but I'd be surprised if you accidentally did that.
            }
        }
    }

    private void promptForExpo(Scanner in) {
        System.out.print("Expo: ");
        String input = in.nextLine();
        switch (input.toLowerCase()) {
            case "prime":
            case "paxprime":
            case "pax prime":
            case "west":
            case "paxwest":
            case "pax west":
                expoToCheck = "PAX West";
                break;
            case "east":
            case "paxeast":
            case "pax east":
                expoToCheck = "PAX East";
                break;
            case "south":
            case "paxsouth":
            case "pax south":
                expoToCheck = "PAX South";
                break;
            case "aus":
            case "australia":
            case "paxaus":
            case "pax aus":
            case "paxaustralia":
            case "pax australia":
                expoToCheck = "PAX Aus";
                break;
            default:
                System.out.println("Invalid expo (" + input + ")! Setting to West...");
                expoToCheck = "PAX West";
                break;
        }
    }

    private void promptForCheckTypes(Scanner in) {
        System.out.print("Check PAX Website (Y/N): ");
        checkPaxWebsite = isResponseYes(in);
        System.out.print("Check Showclix Website (Y/N): ");
        checkShowclixWebsite = isResponseYes(in);
        System.out.print("Filter Showclix Website (Y/N): ");
        filterShowclix = isResponseYes(in);
        System.out.print("Check Known Events (Y/N): ");
        checkKnownEvents = isResponseYes(in);
    }

    private void promptForTwitter(Scanner in) {
        System.out.print("Check Twitter (Y/N): ");
        checkTwitter = isResponseYes(in);
        if (checkTwitter) { // Checking Twitter
            System.out.println("Would you like to authenticate with your Twitter account or use Twitter API keys?");
            System.out.println("If you're not sure, it's easier to use your Twitter account.");
            System.out.println("1. Use Twitter account");
            System.out.println("2. Use API keys");
            System.out.println("3. Don't use Twitter");
            System.out.print("Option: ");
            whileLoop:
            while (true) {
                int option = readNumericOption(in);
                switch (option) {
                    case -1: // Invalid option, prompt again
                        continue;
                    case 1:
                        CommandLineTwitterAuth clAuth = new CommandLineTwitterAuth(in);
                        while (!clAuth.isAuthenticated) {
                            System.out.println("Authenticating with Twitter account.");
                            TwitterAccount acc = new TwitterAccount();
                            acc.authenticate(clAuth, true, false);
                            if (!clAuth.isAuthenticated) {
                                System.out.print("Would you like to try authenticating with Twitter again (Y/N)? ");
                                if (isResponseYes(in)) {
                                    break;
                                }
                            } else {
                                twitterAccount = acc.getAccount();
                            }
                        }
                        // TODO Twitter account auth
                        break whileLoop;
                    case 2:
                        boolean inputKeys = true;
                        if (consumerKey != null) { // Keys have been specified
                            System.out.print("Twitter keys have already been specified. Would you like to specify new ones (Y/N)? ");
                            inputKeys = isResponseYes(in);
                        }
                        if (inputKeys) {
                            System.out.println("The next four prompts are for Twitter authentication. If you do not input valid keys for all of them,"
                                + "Twitter scanning will not work. For more information, see http://redd.it/2nct50");
                            System.out.print("Twitter Consumer Key: ");
                            consumerKey = in.nextLine().trim();
                            System.out.print("Twitter Consumer Secret: ");
                            consumerSecret = in.nextLine().trim();
                            System.out.print("Twitter Application Key: ");
                            applicationKey = in.nextLine().trim();
                            System.out.print("Twitter Application Secret: ");
                            applicationSecret = in.nextLine().trim();
                        }
                        break whileLoop;
                    case 3:
                        checkTwitter = false;
                        return;
                    default:
                        System.out.println("Invalid option. Please enter a valid option.");
                }
            }
            System.out.print("Filter Twitter (Y/N): ");
            filterTwitter = isResponseYes(in);
            if (emailAccount != null) {
                System.out.println("Text Tweets via Email [option is redundant if you already receive alerts from Twitter] (Y/N): ");
                textTweets = isResponseYes(in);
            }
        }
    }

    private boolean isResponseYes(Scanner in) {
        try {
            return in.nextLine().toLowerCase().startsWith("y");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("ERROR -- Unable to read input. Defaulting to NO/FALSE. Program will function normally.");
            return false;
        }
    }

    private boolean isResponseNo(Scanner in) {
        try {
            return in.nextLine().toLowerCase().startsWith("n");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("ERROR -- Unable to read input. Defaulting to YES/TRUE. Program will function normally.");
            return true;
        }
    }

    private int readNumericOption(Scanner in) {
        try {
            return Integer.parseInt(in.nextLine());
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Unable to read input. Please only enter a numeric value.");
            return -1;
        }
    }

    @Override
    public EmailAccount getEmailAccount() {
        return emailAccount;
    }

    @Override
    public boolean shouldCheckPAXWebsite() {
        return checkPaxWebsite;
    }

    @Override
    public boolean shouldCheckShowclix() {
        return checkShowclixWebsite;
    }

    @Override
    public boolean shouldCheckKnownEvents() {
        return checkKnownEvents;
    }

    @Override
    public boolean shouldCheckTwitter() {
        return checkTwitter;
    }

    @Override
    public boolean shouldFilterShowclix() {
        return filterShowclix;
    }

    @Override
    public boolean shouldFilterTwitter() {
        return filterTwitter;
    }

    @Override
    public boolean shouldTextTweets() {
        return textTweets;
    }

    @Override
    public boolean shouldPlayAlarm() {
        return playAlarm;
    }

    @Override
    public int timeBetweenChecks() {
        return refreshTime;
    }

    @Override
    public String getExpoToCheck() {
        return expoToCheck;
    }

    @Override
    public Twitter getTwitterAccount() {
        return null;
    }

    @Override
    public boolean shouldCheckForUpdatesDaily() {
        return checkUpdatesDaily;
    }

    private class CommandLineTwitterAuth implements OauthStatusUpdater {

        private boolean isAuthenticated = false;
        private Scanner in;

        public CommandLineTwitterAuth(Scanner in) {
            this.in = in;
        }

        @Override
        public void authFailure() {
            isAuthenticated = false;
            System.out.println("Twitter authentication failure");
        }

        @Override
        public void authSuccess() {
            isAuthenticated = true;
            System.out.println("Successfully authenticated Twitter");
        }

        @Override
        public void setAuthUrl(String url) {
            System.out.println("If you do not have a browser on this machine, you can open it on another one. If your default browser does not automatically open, you must open this URL manually.");
            System.out.println(url);
        }

        @Override
        public String getAuthorizationPin() {
            return in.nextLine();
        }

        @Override
        public void promptForAuthorizationPin() {
            System.out.println("Please complete the setup steps in your browser, then input the resulting PIN into this prompt.");
            System.out.print("Twitter authentication PIN: ");
        }

        @Override
        public void updateStatus(String status) {
            System.out.println(status);
        }

    }

}
