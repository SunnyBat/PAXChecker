package com.github.sunnybat.paxchecker.gui;

import com.github.sunnybat.commoncode.email.EmailAccount;
import com.github.sunnybat.commoncode.email.EmailAddress;
import com.github.sunnybat.paxchecker.Audio;
import com.github.sunnybat.paxchecker.DataTracker;
import com.github.sunnybat.paxchecker.browser.Browser;
import com.github.sunnybat.paxchecker.notification.NotificationWindow;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.util.List;

/**
 *
 * @author SunnyBat
 */
public class Status extends com.github.sunnybat.commoncode.javax.swing.JFrame {

  private SystemTray tray;
  private TrayIcon myIcon;
  private IconMenu myMenu;
  private final NotificationWindow infoWindow;

  public Status(String expo) {
    this(expo, null, null);
  }

  /**
   * Creates a new Status window. Note that this does not display when created. Also note that this will block until all the components have been
   * created (generally does not take long).
   * @param expo The expo to check -- this String is not checked in any way
   * @param emailAddress The email address used to send emails
   * @param addresses The List of addresses to send emails to
   */
  public Status(final String expo, final String emailAddress, final List<EmailAddress> addresses) {
    invokeAndWaitOnEDT(new Runnable() {
      @Override
      public void run() {
        initComponents();
        customComponents(expo, emailAddress, addresses);
      }
    });
    infoWindow = new NotificationWindow("What's happening???", "If the Showclix scanner is pointing to a seemingly unrelated event, that's expected. "
        + "This is happening because of changes in the Showclix API, which forced me to scan pages with more than just PAX content. This is just the "
        + "most recent event created.\n\n"
        + "There are several reasons why you might be seeing [messages].\n"
        + "For the PAX Website, you might be seeing [NoFind]. Currently, this is only happening with PAX East. This means that the program was "
        + "unable to find the button on the PAX registration page.\n"
        + "For the Showclix Scanner, you might be seeing [No API Connection]. This is a bit more serious -- it means that the PAXChecker was "
        + "unable to connect to the Showclix API, or was unable to read it correctly. If you're seeing this for more than an hour or so, contact "
        + "/u/SunnyBat about the issue.\n"
        + "For the Known Pages option, you'll most likely be seeing [None Found]. This is normal, and it means that no known links have been found. "
        + "There's no need to worry about this one.");
  }

  public void customComponents(final String expo, final String emailAddress, final List<EmailAddress> addresses) {
    try {
      tray = SystemTray.getSystemTray();
      myMenu = new IconMenu() {
        @Override
        public void showWindowPressed() {
          showWindow();
          tray.remove(myIcon);
        }

        @Override
        public void forceCheckPressed() {
          // TODO: Add force check code
        }

        @Override
        public void sendTestEmailPressed() {
          // TODO: Add test email code
        }
      };
    } catch (Exception e) {
      System.out.println("ERROR: System tray is not supported!");
    }
    JLTwitterStatus.setVisible(false); // Enable when Twitter enabled
    JBReconnectTwitter.setVisible(false);
    JLTitle.setText(expo + " Website Status");
    if (addresses == null || emailAddress == null) {
      setInfoText("[TEXTING DISABLED]");
      setTextButtonState(false);
    } else if (addresses.size() == 1) {
      setInfoText(emailAddress + " -- " + addresses.get(0).getCompleteAddress());
    } else {
      setInfoText(emailAddress + " -- Multiple Numbers (Mouse Here to View)");
      String list = "<html>";
      String[] allAddresses = EmailAccount.convertToString(addresses).split(";");
      for (int a = 0; a < allAddresses.length; a++) {
        list += allAddresses[a].trim();
        if (a + 1 != allAddresses.length) {
          list += "<br>";
        }
      }
      list += "</html>";
      setLabelTooltipText(list);
    }
    setDataUsageText(DataTracker.getDataUsedMB());
  }

  public void enableEmail() {
    if (myMenu != null) {
      myMenu.enableEmail();
    }
    JBTestText.setEnabled(true);
  }

  public void enableAlarm() {
    JBTestAlarm.setEnabled(true);
  }

  public void enableTwitter() {
    JLTwitterStatus.setVisible(true);
  }

  public void minimizeWindow() {
    if (!SystemTray.isSupported() || myIcon == null) {
      System.out.println("Unable to minimize window.");
      if (!isVisible()) {
        maximizeWindow();
      }
    } else {
      try {
        tray.add(myIcon);
      } catch (Exception e) {
        e.printStackTrace();
      }
      invokeAndWaitOnEDT(new Runnable() {
        @Override
        public void run() {
          setVisible(false);
        }
      });
    }
  }

  public void maximizeWindow() {
    tray.remove(myIcon); // Fine if myIcon == null or myIcon isn't in tray
    invokeAndWaitOnEDT(new Runnable() {
      @Override
      public void run() {
        setExtendedState(javax.swing.JFrame.NORMAL);
        setVisible(true);
        setLocationRelativeTo(null);
        toFront();
      }
    });
  }

  public void setTwitterStatus(final boolean isEnabled) {
    javax.swing.SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        if (isEnabled) {
          JLTwitterStatus.setText("Twitter Feed: Connected");
        } else {
          JLTwitterStatus.setText("Twitter Feed: Disconnected");
        }
      }
    });
  }

  public void setTwitterStatus(final int timeUntilReconnect) {
    javax.swing.SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        JLTwitterStatus.setText("Twitter Feed: Reconnecting in " + timeUntilReconnect + " seconds");
      }
    });
  }

  public void twitterStreamKilled() {
    javax.swing.SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        JBReconnectTwitter.setVisible(true);
      }
    });
  }

  public void setInformationText(final String text) {
    javax.swing.SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        JLInformation.setText(text);
      }
    });
  }

  public void setLastCheckedText(final String text) {
    javax.swing.SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        JLLastChecked.setText(text);
      }
    });
  }

  public void setLastCheckedText(final int seconds) {
    setLastCheckedText("Time until next check: " + seconds + " seconds");
  }

  public void setInfoText(final String text) {
    javax.swing.SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        jLabel2.setText(text);
      }
    });
  }

  public void setTextButtonState(final boolean enabled) {
    javax.swing.SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        JBTestText.setEnabled(enabled);
        if (enabled) {
          myMenu.addTextButton();
        } else {
          myMenu.removeTextButton();
        }
      }
    });
  }

  public void setSoundButtonState(final boolean enabled) {
    javax.swing.SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        JBTestAlarm.setEnabled(enabled);
      }
    });
  }

  public void setLabelTooltipText(final String s) {
    javax.swing.SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        jLabel2.setToolTipText(s);
      }
    });
  }

  public void setTextButtonText(final String s) {
    javax.swing.SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        JBTestText.setText(s);
      }
    });
  }

  public void setForceButtonState(final boolean enabled) {
    javax.swing.SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        JBForceCheck.setEnabled(enabled);
      }
    });
  }

  public void updateJLabel(final javax.swing.JLabel label, final String text) {
    javax.swing.SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        label.setText(text);
        label.repaint();
      }
    });
  }

  private void openLabelLink(String text) {
    if (text.toLowerCase().contains("http")) {
      Browser.openLinkInBrowser(text.substring(text.toLowerCase().indexOf("http")));
    }
  }

  public javax.swing.JLabel addLinkJLabel() {
    final javax.swing.JLabel jL = new javax.swing.JLabel();
    jL.addMouseListener(new java.awt.event.MouseAdapter() {
      public void mouseClicked(java.awt.event.MouseEvent evt) {
        openLabelLink(jL.getText());
      }
    });
    javax.swing.SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        jL.setText(" ");
        JPLinks.add(jL);
        pack();
      }
    });
    return jL;
  }

  public void setDataUsageText(final String text) {
    javax.swing.SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        JLDataUsage.setText(text);
      }
    });
  }

  public void setDataUsageText(long amount) {
    setDataUsageText("Data Used: " + amount + "MB");
  }

  public void setDataUsageText(double mb) {
    setDataUsageText("Data Used: " + mb + "MB");
  }

  public void setIcon(final java.awt.Image image) {
    if (image == null) {
      System.out.println("Image == null");
      return;
    }
    invokeAndWaitOnEDT(new Runnable() {
      @Override
      public void run() {
        try {
          setIconImage(image);
          myIcon = new TrayIcon(image, "PAXChecker", myMenu);
          myIcon.setImageAutoSize(true);
          myIcon.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
              maximizeWindow();
            }
          });
          System.out.println("Set status icon: " + (myIcon != null));
        } catch (Exception e) {
          System.out.println("ERROR setting status iconImage!");
        }
      }
    });
  }

  @Override
  public void dispose() {
    if (tray != null) {
      if (myIcon != null) {
        tray.remove(myIcon);
      }
    }
    super.dispose();
  }

  /**
   * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always
   * regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {

    JLTitle = new javax.swing.JLabel();
    JBTestText = new javax.swing.JButton();
    JBTestAlarm = new javax.swing.JButton();
    JBForceCheck = new javax.swing.JButton();
    JLLastChecked = new javax.swing.JLabel();
    JLInformation = new javax.swing.JLabel();
    jLabel2 = new javax.swing.JLabel();
    JLDataUsage = new javax.swing.JLabel();
    JPLinks = new javax.swing.JPanel();
    JLTwitterStatus = new javax.swing.JLabel();
    JLLinksExplanation = new javax.swing.JLabel();
    JBReconnectTwitter = new javax.swing.JButton();
    filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(32767, 0));

    setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
    setTitle("PAXChecker");
    setResizable(false);
    addWindowListener(new java.awt.event.WindowAdapter() {
      public void windowIconified(java.awt.event.WindowEvent evt) {
        formWindowIconified(evt);
      }
    });

    JLTitle.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
    JLTitle.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    JLTitle.setText("PAX Website Status");

    JBTestText.setText("Test Text");
    JBTestText.setEnabled(false);
    JBTestText.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        JBTestTextActionPerformed(evt);
      }
    });

    JBTestAlarm.setText("Test Alarm Sound");
    JBTestAlarm.setEnabled(false);
    JBTestAlarm.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        JBTestAlarmActionPerformed(evt);
      }
    });

    JBForceCheck.setText("Force Check");
    JBForceCheck.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        JBForceCheckActionPerformed(evt);
      }
    });

    JLLastChecked.setText("Time Until Check: [Initializing]");

    JLInformation.setText(" ");

    jLabel2.setFont(new java.awt.Font("Tahoma", 2, 11)); // NOI18N
    jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    jLabel2.setText("Email -- Phone");

    JLDataUsage.setText("Data Usage: [Initializing]");
    JLDataUsage.setToolTipText("<html>\nNote that this does NOT include Twitter<br>\ndata usage.<br>\nThis is only a rough estimate of data used.<br>\nFor more accurate data usage, download<br>\nWireShark (or similar software) and monitor<br>\nusage through that.\n</html>");

    JPLinks.setLayout(new javax.swing.BoxLayout(JPLinks, javax.swing.BoxLayout.LINE_AXIS));
    JPLinks.setLayout(new javax.swing.BoxLayout(JPLinks, javax.swing.BoxLayout.Y_AXIS));

    JLTwitterStatus.setText("Twitter Feed: Connecting...");

    JLLinksExplanation.setFont(new java.awt.Font("Tahoma", 2, 10)); // NOI18N
    JLLinksExplanation.setForeground(new java.awt.Color(0, 0, 238));
    JLLinksExplanation.setText("Why are these links pointing to random events??? What's up with [Message]??");
    JLLinksExplanation.addMouseListener(new java.awt.event.MouseAdapter() {
      public void mousePressed(java.awt.event.MouseEvent evt) {
        JLLinksExplanationMousePressed(evt);
      }
    });

    JBReconnectTwitter.setText("Reconnect Twitter Stream");
    JBReconnectTwitter.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        JBReconnectTwitterActionPerformed(evt);
      }
    });

    javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
    getContentPane().setLayout(layout);
    layout.setHorizontalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addContainerGap()
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addGroup(layout.createSequentialGroup()
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
              .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                .addComponent(JLDataUsage, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(JLInformation, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(JLLastChecked, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(JPLinks, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 380, javax.swing.GroupLayout.PREFERRED_SIZE))
              .addComponent(JLTitle, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
              .addGroup(layout.createSequentialGroup()
                .addComponent(JBTestText, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(JBTestAlarm, javax.swing.GroupLayout.PREFERRED_SIZE, 185, javax.swing.GroupLayout.PREFERRED_SIZE))
              .addComponent(JBForceCheck, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
              .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGap(10, 10, 10))
          .addGroup(layout.createSequentialGroup()
            .addComponent(JLTwitterStatus)
            .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
          .addGroup(layout.createSequentialGroup()
            .addComponent(JLLinksExplanation)
            .addGap(0, 0, Short.MAX_VALUE))
          .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
              .addComponent(JBReconnectTwitter, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
              .addComponent(filler1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addContainerGap())))
    );
    layout.setVerticalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addContainerGap()
        .addComponent(JLTitle)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(jLabel2)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(JPLinks, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        .addGap(0, 0, 0)
        .addComponent(JLTwitterStatus)
        .addGap(0, 0, 0)
        .addComponent(JLLinksExplanation)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        .addComponent(JLLastChecked)
        .addGap(0, 0, 0)
        .addComponent(JLDataUsage)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(JLInformation)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(JBTestText)
          .addComponent(JBTestAlarm, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(JBForceCheck)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(filler1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addGap(0, 0, 0)
        .addComponent(JBReconnectTwitter)
        .addContainerGap())
    );

    pack();
  }// </editor-fold>//GEN-END:initComponents

  private void JBForceCheckActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_JBForceCheckActionPerformed
    // TODO: Force Check Pressed
  }//GEN-LAST:event_JBForceCheckActionPerformed

  private void JBTestTextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_JBTestTextActionPerformed
    // TODO: Test Text Pressed
  }//GEN-LAST:event_JBTestTextActionPerformed

  private void JBTestAlarmActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_JBTestAlarmActionPerformed
    if (Audio.playAlarm()) {
      setInformationText("Alarm started.");
    } else {
      setInformationText("Unable to play alarm.");
    }
  }//GEN-LAST:event_JBTestAlarmActionPerformed

  private void formWindowIconified(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowIconified
    minimizeWindow();
  }//GEN-LAST:event_formWindowIconified

  private void JLLinksExplanationMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_JLLinksExplanationMousePressed
    infoWindow.setVisible(true);
  }//GEN-LAST:event_JLLinksExplanationMousePressed

  private void JBReconnectTwitterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_JBReconnectTwitterActionPerformed
    // TODO: Reconnect to Twitter Pressed
  }//GEN-LAST:event_JBReconnectTwitterActionPerformed

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JButton JBForceCheck;
  private javax.swing.JButton JBReconnectTwitter;
  private javax.swing.JButton JBTestAlarm;
  private javax.swing.JButton JBTestText;
  private javax.swing.JLabel JLDataUsage;
  private javax.swing.JLabel JLInformation;
  private javax.swing.JLabel JLLastChecked;
  private javax.swing.JLabel JLLinksExplanation;
  private javax.swing.JLabel JLTitle;
  private javax.swing.JLabel JLTwitterStatus;
  private javax.swing.JPanel JPLinks;
  private javax.swing.Box.Filler filler1;
  private javax.swing.JLabel jLabel2;
  // End of variables declaration//GEN-END:variables
}
