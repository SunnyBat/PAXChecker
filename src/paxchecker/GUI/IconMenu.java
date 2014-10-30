package paxchecker.GUI;

import java.awt.*;
import paxchecker.*;

/**
 *
 * @author Sunnybat
 */
public class IconMenu extends PopupMenu {

  private final MenuItem maximizeButton;
  private final MenuItem closeButton;
  private final MenuItem testTextButton;
  private final MenuItem testAlarmButton;
  private final MenuItem forceCheckButton;

  public IconMenu() {
    maximizeButton = new MenuItem("Restore Window");
    closeButton = new MenuItem("Close Program");
    testTextButton = new MenuItem("Test Text");
    testAlarmButton = new MenuItem("Test Alarm");
    forceCheckButton = new MenuItem("Force Check");
    maximizeButton.addActionListener(new java.awt.event.ActionListener() {
      @Override
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        Checker.maximizeStatusWindow();
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
        PAXChecker.sendBackgroundTestEmail();
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
        Checker.forceRefresh();
      }
    });
    addAllComponents();
  }

  public void removeTextButton() {
    remove(testTextButton);
  }

  public void addTextButton() {
    removeAll();
    addAllComponents();
  }

  private void addAllComponents() {
    if (Email.shouldSendEmail()) {
      add(testTextButton);
    }
    if (Audio.soundEnabled()) {
      add(testAlarmButton);
    }
    add(forceCheckButton);
    addSeparator();
    add(maximizeButton);
    add(closeButton);
  }
}
