package paxchecker.GUI;

import paxchecker.*;
import java.awt.*;

/**
 *
 * @author SunnyBat
 */
public class Status extends javax.swing.JFrame {

  private SystemTray tray;
  private TrayIcon myIcon;
  private IconMenu myMenu;

  /**
   * Creates new form Status
   */
  public Status() {
    initComponents();
    customComponents();
    setVisible(true);
  }

  private void customComponents() {
    tray = SystemTray.getSystemTray();
    myMenu = new IconMenu();
    if (!Paxsite.isCheckingPaxWebsite()) {
      hideWebsiteText();
    }
    if (!Showclix.isCheckingShowclix()) {
      hideShowclixText();
    }
    JLTitle.setText(Browser.getExpo() + " Website Status");
    if (!Email.shouldSendEmail()) {
      setInfoText("[TEXTING DISABLED]");
      setTextButtonState(false);
    } else if (Email.getAddressList().size() == 1) {
      setInfoText(Email.getUsername() + " -- " + Email.getAddressList().get(0).getCompleteAddress());
    } else {
      setInfoText(Email.getUsername() + " -- Multiple Numbers (Mouse Here to View)");
      String list = "<html>";
      String[] allAddresses = Email.convertToString(Email.getAddressList()).split(";");
      for (int a = 0; a < allAddresses.length; a++) {
        list += allAddresses[a].trim();
        if (a + 1 != allAddresses.length) {
          list += "<br>";
        }
      }
      list += "</html>";
      setLabelTooltipText(list);
    }
    if (!Audio.soundEnabled()) {
      setSoundButtonState(false);
    }
    setDataUsageText(DataTracker.getDataUsedMB());
  }

  /**
   * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always
   * regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {

    JLTitle = new javax.swing.JLabel();
    JLWebsiteLink = new javax.swing.JLabel();
    jButton1 = new javax.swing.JButton();
    jButton2 = new javax.swing.JButton();
    jButton3 = new javax.swing.JButton();
    JLLastChecked = new javax.swing.JLabel();
    JLInformation = new javax.swing.JLabel();
    jLabel2 = new javax.swing.JLabel();
    JLShowclixLink = new javax.swing.JLabel();
    JLDataUsage = new javax.swing.JLabel();

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

    JLWebsiteLink.setText("Current Website Link: ");
    JLWebsiteLink.addMouseListener(new java.awt.event.MouseAdapter() {
      public void mouseClicked(java.awt.event.MouseEvent evt) {
        JLWebsiteLinkMouseClicked(evt);
      }
    });

    jButton1.setText("Test Text");
    jButton1.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        jButton1ActionPerformed(evt);
      }
    });

    jButton2.setText("Test Alarm Sound");
    jButton2.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        jButton2ActionPerformed(evt);
      }
    });

    jButton3.setText("Force Check");
    jButton3.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        jButton3ActionPerformed(evt);
      }
    });

    JLLastChecked.setText("Time Until Check:");

    JLInformation.setText(" ");

    jLabel2.setFont(new java.awt.Font("Tahoma", 2, 11)); // NOI18N
    jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    jLabel2.setText("Email -- Phone");

    JLShowclixLink.setText("Current Showclix Link:");
    JLShowclixLink.addMouseListener(new java.awt.event.MouseAdapter() {
      public void mouseClicked(java.awt.event.MouseEvent evt) {
        JLShowclixLinkMouseClicked(evt);
      }
    });

    JLDataUsage.setText("Data Usage:");

    javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
    getContentPane().setLayout(layout);
    layout.setHorizontalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addContainerGap()
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(JLTitle, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
          .addGroup(layout.createSequentialGroup()
            .addComponent(jButton1, javax.swing.GroupLayout.DEFAULT_SIZE, 185, Short.MAX_VALUE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
            .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 185, javax.swing.GroupLayout.PREFERRED_SIZE))
          .addComponent(jButton3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
          .addComponent(JLInformation, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
          .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
          .addComponent(JLWebsiteLink, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
          .addComponent(JLShowclixLink, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
          .addGroup(layout.createSequentialGroup()
            .addComponent(JLLastChecked)
            .addGap(0, 0, Short.MAX_VALUE))
          .addComponent(JLDataUsage, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        .addContainerGap())
    );
    layout.setVerticalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addContainerGap()
        .addComponent(JLTitle)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(jLabel2)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(JLWebsiteLink)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(JLShowclixLink)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(JLLastChecked)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(JLDataUsage)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        .addComponent(JLInformation)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(jButton2)
          .addComponent(jButton1))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(jButton3)
        .addContainerGap())
    );

    pack();
  }// </editor-fold>//GEN-END:initComponents

  private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
    // TODO add your handling code here:
    Checker.forceRefresh();
  }//GEN-LAST:event_jButton3ActionPerformed

  public void minimizeWindow() {
    if (!SystemTray.isSupported() || myIcon == null) {
      return;
    }
    try {
      tray.add(myIcon);
    } catch (Exception e) {
      e.printStackTrace();
    }
    setVisible(false);
  }

  public void maximizeWindow() {
    setExtendedState(javax.swing.JFrame.NORMAL);
    setVisible(true);
    this.setLocationRelativeTo(null);
    this.toFront();
    tray.remove(myIcon); // Fine if myIcon == null or myIcon isn't in tray
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
        jButton1.setEnabled(enabled);
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
        jButton2.setEnabled(enabled);
      }
    });
  }

  public void hideShowclixText() {
    javax.swing.SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        JLShowclixLink.setVisible(false);
        pack();
      }
    });
  }

  public void hideWebsiteText() {
    javax.swing.SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        JLWebsiteLink.setVisible(false);
        pack();
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
        jButton1.setText(s);
      }
    });
  }

  public void setWebsiteLink(final String link) {
    javax.swing.SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        JLWebsiteLink.setText("Current Website Link: " + link);
      }
    });
  }

  public void setShowclixLink(final String link) {
    javax.swing.SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        JLShowclixLink.setText("Current Showclix Link: " + link);
      }
    });
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

  @Deprecated // Hangs EDT/GUI
  public void loadNewIcon(final String iconName) {
    javax.swing.SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        try {
          setIconImage(javax.imageio.ImageIO.read(PAXChecker.class.getResourceAsStream("/resources/" + iconName)));
        } catch (Exception e) {
          System.out.println("ERROR loading PAX icon image: " + iconName);
        }
      }
    });
  }

  public void setIcon(final java.awt.Image image) {
    if (image == null) {
      return;
    }
    javax.swing.SwingUtilities.invokeLater(new Runnable() {
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

  private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
    // TODO add your handling code here:
    PAXChecker.sendBackgroundTestEmail();
  }//GEN-LAST:event_jButton1ActionPerformed

  private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
    // TODO add your handling code here:
//    Browser.openLinkInBrowser("http://prime.paxsite.com");
//    setInformationText("PAX Prime site opened.");
    if (Audio.playAlarm()) {
      setInformationText("Alarm started.");
    } else {
      setInformationText("Unable to play alarm.");
    }
  }//GEN-LAST:event_jButton2ActionPerformed

  private void formWindowIconified(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowIconified
    // TODO add your handling code here:
    minimizeWindow();
  }//GEN-LAST:event_formWindowIconified

  private void JLWebsiteLinkMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_JLWebsiteLinkMouseClicked
    // TODO add your handling code here:
    if (JLWebsiteLink.getText().toLowerCase().contains("http")) {
      Browser.openLinkInBrowser(JLWebsiteLink.getText().substring(JLWebsiteLink.getText().toLowerCase().indexOf("http")));
    }
  }//GEN-LAST:event_JLWebsiteLinkMouseClicked

  private void JLShowclixLinkMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_JLShowclixLinkMouseClicked
    // TODO add your handling code here:
    if (JLShowclixLink.getText().toLowerCase().contains("http")) {
      Browser.openLinkInBrowser(JLShowclixLink.getText().substring(JLShowclixLink.getText().toLowerCase().indexOf("http")));
    }
  }//GEN-LAST:event_JLShowclixLinkMouseClicked

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JLabel JLDataUsage;
  private javax.swing.JLabel JLInformation;
  private javax.swing.JLabel JLLastChecked;
  private javax.swing.JLabel JLShowclixLink;
  private javax.swing.JLabel JLTitle;
  private javax.swing.JLabel JLWebsiteLink;
  private javax.swing.JButton jButton1;
  private javax.swing.JButton jButton2;
  private javax.swing.JButton jButton3;
  private javax.swing.JLabel jLabel2;
  // End of variables declaration//GEN-END:variables
}
