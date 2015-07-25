package com.github.sunnybat.paxchecker.gui;

import com.github.sunnybat.paxchecker.Audio;
import java.awt.MenuItem;
import java.awt.PopupMenu;

/**
 *
 * @author Sunnybat
 */
public abstract class IconMenu extends PopupMenu {

  private final MenuItem maximizeButton;
  private final MenuItem closeButton;
  private final MenuItem testTextButton;
  private final MenuItem testAlarmButton;
  private final MenuItem forceCheckButton;
  private boolean sendEmail;

  public IconMenu() {
    maximizeButton = new MenuItem("Restore Window");
    closeButton = new MenuItem("Close Program");
    testTextButton = new MenuItem("Test Text");
    testAlarmButton = new MenuItem("Test Alarm");
    forceCheckButton = new MenuItem("Force Check");
    maximizeButton.addActionListener(new java.awt.event.ActionListener() {
      @Override
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        showWindowPressed();
      }
    });
    closeButton.addActionListener(new java.awt.event.ActionListener() {
      @Override
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        System.exit(0);
      }
    });
    testTextButton.addActionListener(new java.awt.event.ActionListener() {
      @Override
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        sendTestEmailPressed();
      }
    });
    testAlarmButton.addActionListener(new java.awt.event.ActionListener() {
      @Override
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        Audio.playAlarm();
      }
    });
    forceCheckButton.addActionListener(new java.awt.event.ActionListener() {
      @Override
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        forceCheckPressed();
      }
    });
    addAllComponents();
  }

  public void enableEmail() {
    sendEmail = true;
  }

  public void removeTextButton() {
    remove(testTextButton);
  }

  public void addTextButton() {
    removeAll();
    addAllComponents();
  }

  private void addAllComponents() {
    if (sendEmail) {
      add(testTextButton);
    }
    if (Audio.playAlarm()) {
      add(testAlarmButton);
    }
    add(forceCheckButton);
    addSeparator();
    add(maximizeButton);
    add(closeButton);
  }

  public abstract void showWindowPressed();

  public abstract void forceCheckPressed();

  public abstract void sendTestEmailPressed();
}
