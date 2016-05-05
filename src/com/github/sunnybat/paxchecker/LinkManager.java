package com.github.sunnybat.paxchecker;

import com.github.sunnybat.commoncode.audio.Sound;
import com.github.sunnybat.commoncode.email.EmailAccount;
import com.github.sunnybat.paxchecker.browser.Browser;
import com.github.sunnybat.paxchecker.status.Tickets;
import java.awt.GraphicsEnvironment;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author SunnyBat
 */
public class LinkManager {

  private EmailAccount email;
  private Set<String> linksOpened;
  private Sound alarm;

  public LinkManager(EmailAccount email) {
    this.email = email;
    linksOpened = new HashSet<>();
  }

  /**
   * Sets the sound to play when a new link is opened. If set to null, no sound will be played.
   *
   * @param alarm The Sound to play
   */
  public void setAlarm(Sound alarm) {
    this.alarm = alarm;
  }

  public void openLink(String url, boolean sendEmail) {
    openLink(url, sendEmail, "A new link has been found: " + url);
  }

  public void openLink(String url, boolean sendEmail, String message) {
    if (url != null && !hasOpenedLink(url)) {
      linksOpened.add(url);
      Browser.openLinkInBrowser(url);
      /*if (alarm != null) {
        alarm.play();
      }*/
      Audio.playAlarm();
      if (!GraphicsEnvironment.isHeadless()) {
        Tickets ticketWindow = new Tickets(url); // CHECK: Should I only allow one Tickets at a time?
        ticketWindow.showWindow();
      }
      if (email != null && sendEmail) {
        try {
          email.sendMessage("PAXChecker", message);
        } catch (IllegalStateException e) { // In case we send too fast
          System.out.println("Unable to send email (" + e.getMessage() + ")");
        }
      }
    }
  }

  public boolean hasOpenedLink(String url) {
    if (url == null) {
      return false;
    }
    return linksOpened.contains(url);
  }

}
