package com.github.sunnybat.paxchecker.setup;

import com.github.sunnybat.commoncode.email.account.EmailAccount;
import twitter4j.Twitter;

/**
 * A basic Setup for the PAXChecker. Contains all the information needed to run the program.
 *
 * @author SunnyBat
 */
public interface Setup {

  /**
   * Prompts the user for settings. Should be called before retrieving any settings (AKA first). This method will likely block for a long time while
   * the user inputs settings.
   */
  public void promptForSettings();

  /**
   * Gets the GmailAccount to use for sending emails. This will be configured such that all that needs to be called is sendEmail().
   *
   * @return The GmailAccount to use
   */
  public EmailAccount getEmailAccount();

  /**
   * Gets whether or not to check the PAX website for updates.
   *
   * @return True to check, false to not
   */
  public boolean shouldCheckPAXWebsite();

  /**
   * Gets whether or not to check the Showclix API for updates.
   *
   * @return True to check, false to not
   */
  public boolean shouldCheckShowclix();

  /**
   * Gets whether or not to check all known Showclix event pages for updates.
   *
   * @return True to check, false to not
   */
  public boolean shouldCheckKnownEvents();

  /**
   * Gets whether or not to check Twitter for updates. Note that Twitter checking will not necessarily be usable if this is true.
   *
   * @return True to check, false to not
   */
  public boolean shouldCheckTwitter();

  /**
   * Gets whether or not to filter Showclix results.
   *
   * @return True to filter, false to not
   */
  public boolean shouldFilterShowclix();

  /**
   * Gets whether or not to filter Twitter results.
   *
   * @return True to filter, false to not
   */
  public boolean shouldFilterTwitter();

  /**
   * Gets whether or not to send a text when a Tweet with a Showclix link is found.
   *
   * @return True to send texts, false to not
   */
  public boolean shouldTextTweets();

  /**
   * Gets whether or not to play an alarm when a potential link is found.
   *
   * @return True to play, false to not
   */
  public boolean shouldPlayAlarm();

  /**
   * Gets the time to wait between checks. Note that this is not usable for Twitter checking, as Twitter checking is instant.
   *
   * @return The time to wait between checks
   */
  public int timeBetweenChecks();

  /**
   * Gets the name of the expo to check. Values returned include West, South, East, Aus. These may or may not be preceded with "PAX ".
   *
   * @return The name of the expo to check
   */
  public String getExpoToCheck();

  public Twitter getTwitterAccount();

  /**
   * Gets whether or not to check for updates every 24 hours.
   *
   * @return True to check, false to not
   */
  public boolean shouldCheckForUpdatesDaily();

}
