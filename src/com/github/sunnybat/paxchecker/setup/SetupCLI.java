package com.github.sunnybat.paxchecker.setup;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * A command-line Setup. Gets all the information needed to run the PAXChecker through System.in and System.out.
 *
 * @author SunnyBat
 */
public class SetupCLI implements Setup {

  private boolean checkPaxWebsite;
  private boolean checkShowclixWebsite;
  private boolean checkKnownEvents;
  private boolean checkTwitter;
  private boolean filterTwitter;
  private String expoToCheck;
  private boolean playAlarm;
  private int refreshTime = 30;
  private String emailAddress;
  private String emailPassword;
  private List<String> emailSendToList = new ArrayList<>();
  private String consumerKey;
  private String consumerSecret;
  private String applicationKey;
  private String applicationSecret;

  /**
   * Creates a new SetupCLI. Settings will be prompted for through the command-line.
   */
  public SetupCLI() {
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
    String name = "a";
    while (name.length() > 0) {
      System.out.print("Email (press ENTER to skip): ");
      try {
        name = myScanner.nextLine();
        if (name.length() == 0) {
          // Do nothing, skipping email
        } else if (name.length() < 5) {
          System.out.println("Invalid username. Please input a valid username, or press ENTER to skip.");
        } else { // Valid email address
          emailAddress = name;
          System.out.print("Password: ");
          emailPassword = new String(System.console().readPassword());
          while (true) {
            System.out.print("Cell Number (can enter multiple, press ENTER to stop entering): ");
            try {
              String cellNum = myScanner.nextLine();
              if (cellNum.length() == 0) {
                break;
              } else {
                emailSendToList.add(cellNum);
              }
            } catch (Exception e) {
              e.printStackTrace();
              System.out.println("Program error. Stopping cell number input. The program will continue to function normally.");
              break;
            }
          }
          break;
        }
      } catch (Exception e) {
        e.printStackTrace();
        System.out.println("Program error. Skipping email input. The program will continue to function normally.");
        break;
      }
    }
    System.out.print("Expo: ");
    try {
      String input = myScanner.nextLine();
      switch (input.toLowerCase()) {
        case "prime":
        case "paxprime":
        case "pax prime":
          expoToCheck = "PAX Prime";
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
          System.out.println("Invalid expo (" + input + ")! Setting to Prime...");
          expoToCheck = "PAX Prime";
          break;
      }
    } catch (Exception e) {
    }
    System.out.print("Check PAX Website (Y/N): ");
    checkPaxWebsite = isResponseYes(myScanner);
    System.out.print("Check Showclix Website (Y/N): ");
    checkShowclixWebsite = isResponseYes(myScanner);
    System.out.print("Check Known Events (Y/N): ");
    checkKnownEvents = isResponseYes(myScanner);
    System.out.print("Check Twitter (Y/N): ");
    checkTwitter = isResponseYes(myScanner);
    if (checkTwitter) { // Checking Twitter
      boolean inputKeys = true;
      if (consumerKey != null) { // Keys have been specified
        System.out.print("Twitter keys have already been specified. Would you like to specify new ones (Y/N)? ");
        inputKeys = isResponseYes(myScanner);
      }
      if (inputKeys) {
        System.out.print("Twitter Consumer Key: ");
        consumerKey = myScanner.nextLine();
        System.out.print("Twitter Consumer Secret: ");
        consumerSecret = myScanner.nextLine();
        System.out.print("Twitter Application Key: ");
        applicationKey = myScanner.nextLine();
        System.out.print("Twitter Application Secret: ");
        applicationSecret = myScanner.nextLine();
      }
      System.out.print("Filter Twitter (Y/N): ");
      filterTwitter = isResponseYes(myScanner);
      System.out.println("The next four prompts are for Twitter authentication. If you do not input valid keys for all of them,"
          + "Twitter scanning will not work. For more information, see http://redd.it/2nct50");
    }
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

  @Override
  public String getEmailUsername() {
    return emailAddress;
  }

  @Override
  public String getEmailPassword() {
    return emailPassword;
  }

  @Override
  public List<String> getEmailAddresses() {
    return new ArrayList(emailSendToList); // New ArrayList in case this method is called more than once, so the ArrayList can be consume
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
  public boolean shouldFilterTwitter() {
    return filterTwitter;
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
  public String getTwitterConsumerKey() {
    return consumerKey;
  }

  @Override
  public String getTwitterConsumerSecret() {
    return consumerSecret;
  }

  @Override
  public String getTwitterApplicationKey() {
    return applicationKey;
  }

  @Override
  public String getTwitterApplicationSecret() {
    return applicationSecret;
  }

}
