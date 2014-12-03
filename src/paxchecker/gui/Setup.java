package paxchecker.gui;

import paxchecker.browser.Browser;
import paxchecker.*;
import paxchecker.browser.TwitterReader;
import paxchecker.check.*;
import paxchecker.error.*;
import paxchecker.preferences.Preference;
import paxchecker.preferences.PreferenceHandler;
import paxchecker.tickets.*;

/**
 *
 * @author SunnyBat
 */
public class Setup extends javax.swing.JFrame {

  public java.util.ArrayList<ExtraPhonePanel> extraPhonePanelList = new java.util.ArrayList<>();
  private final ErrorWindow twitterDisabledWindow = new ErrorWindow();

  /**
   * Creates new form Setup
   */
  public Setup() {
    twitterDisabledWindow.setTitleText("Twitter Disabled?");
    twitterDisabledWindow.setErrorText("Why is Twitter Disabled?");
    twitterDisabledWindow.setInformationText("Twitter is disabled because the Twitter API is not able to be secured in an open-source application.\nIf you want to check Twitter, follow the instructions here: https://redd.it/2nct50");
    javax.swing.SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        initComponents();
        customComponents();
      }
    });
  }

  private void customComponents() {
    setTitle("Setup :: PAXChecker v" + PAXChecker.VERSION);
    JCBUseBeta.setSelected(PreferenceHandler.getBooleanPreference(Preference.TYPES.USE_BETA));
    JCBLoadNotifications.setSelected(PreferenceHandler.getBooleanPreference(Preference.TYPES.LOAD_NOTIFICATIONS));
    JCBCheckUpdates.setSelected(PreferenceHandler.getBooleanPreference(Preference.TYPES.LOAD_UPDATES));
    JCBSaveTwitterKeys.setSelected(PreferenceHandler.getPreferenceObject(Preference.TYPES.TWITTER_CONSUMER_KEY).shouldSave());
    if (PreferenceHandler.getBooleanPreference(Preference.TYPES.SAVE_PREFS)) {
      JCBSavePreferences.setSelected(true);
      JCBSaveCellnum.setSelected(PreferenceHandler.getPreferenceObject(Preference.TYPES.CELLNUM).shouldSave());
      JCBSaveCheckPax.setSelected(PreferenceHandler.getPreferenceObject(Preference.TYPES.CHECK_PAX).shouldSave());
      JCBSaveCheckShowclix.setSelected(PreferenceHandler.getPreferenceObject(Preference.TYPES.CHECK_SHOWCLIX).shouldSave());
      JCBSaveCheckTwitter.setSelected(PreferenceHandler.getPreferenceObject(Preference.TYPES.CHECK_TWITTER).shouldSave());
      JCBSaveEvent.setSelected(PreferenceHandler.getPreferenceObject(Preference.TYPES.EVENT).shouldSave());
      JCBSavePlayAlarm.setSelected(PreferenceHandler.getPreferenceObject(Preference.TYPES.PLAY_ALARM).shouldSave());
      JCBSaveRefreshTime.setSelected(PreferenceHandler.getPreferenceObject(Preference.TYPES.REFRESHTIME).shouldSave());
      JCBSaveEmail.setSelected(PreferenceHandler.getPreferenceObject(Preference.TYPES.EMAIL).shouldSave());
    } else {
      JCBSaveCellnum.setEnabled(false);
      JCBSaveCheckPax.setEnabled(false);
      JCBSaveCheckShowclix.setEnabled(false);
      JCBSaveCheckTwitter.setEnabled(false);
      JCBSaveEvent.setEnabled(false);
      JCBSavePlayAlarm.setEnabled(false);
      JCBSaveRefreshTime.setEnabled(false);
      JCBSaveEmail.setEnabled(false);
    }
    JTPExtra.setText(loadHtml("/paxchecker/gui/Extra.html"));
    JTPExtra.setCaretPosition(0);
    JTPInstructions.setText(loadHtml("/paxchecker/gui/Instructions.html"));
    JTPInstructions.setCaretPosition(0);
  }

  public void loadProgramSettings() {
    javax.swing.SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        JTFEmail.setText(PreferenceHandler.getStringPreference(Preference.TYPES.EMAIL));
        JCBExpo.setSelectedIndex(getIndexOfEvent(PreferenceHandler.getStringPreference(Preference.TYPES.EVENT)));
        JCBCheckWebsite.setSelected(PreferenceHandler.getBooleanPreference(Preference.TYPES.CHECK_PAX));
        JCBCheckShowclix.setSelected(PreferenceHandler.getBooleanPreference(Preference.TYPES.CHECK_SHOWCLIX));
        JCBCheckTwitter.setSelected(TwitterReader.isInitialized() ? PreferenceHandler.getBooleanPreference(Preference.TYPES.CHECK_TWITTER) : false);
        JCBCheckTwitter.setEnabled(TwitterReader.isInitialized());
        JLTwitterDisabled.setVisible(!TwitterReader.isInitialized());
        JCBPlayAlarm.setSelected(PreferenceHandler.getBooleanPreference(Preference.TYPES.PLAY_ALARM));
        JSCheckTime.setValue(PreferenceHandler.getIntegerPreference(Preference.TYPES.REFRESHTIME));
        if (!JCBCheckWebsite.isSelected() && !JCBCheckShowclix.isSelected() && !JCBCheckTwitter.isSelected()) { // Disable START! button
          JBStart.setEnabled(false);
        }
        java.awt.Dimension d = JTFCellNum.getSize();
        String cellnum = PreferenceHandler.getStringPreference(Preference.TYPES.CELLNUM);
        if (cellnum.contains(";")) {
          System.out.println("Debug: All = " + cellnum);
          String[] specificNumbers = cellnum.replaceAll("; ", ";").split(";");
          JCBCarrier.setSelectedIndex(Setup.getIndexOfProvider(Email.getProvider(specificNumbers[0].substring(specificNumbers[0].indexOf("@")))));
          if (Email.getProvider(specificNumbers[0].substring(specificNumbers[0].indexOf("@"))).equals("[Other]")) {
            JTFCellNum.setText(specificNumbers[0].trim());
          } else {
            JTFCellNum.setText(specificNumbers[0].substring(0, specificNumbers[0].indexOf("@")).trim());
          }
          JTFCellNum.setCaretPosition(0);
          for (int a = 1; a < specificNumbers.length; a++) {
            System.out.println("specificNumbers[" + a + "] = " + specificNumbers[a]);
            addPhonePanel(new ExtraPhonePanel(Setup.this, Email.splitEmail(specificNumbers[a])[0].trim(), Email.splitEmail(specificNumbers[a])[1].trim()));
          }
        } else {
          System.out.println("Normal address");
          JTFCellNum.setText(cellnum);
          JCBCarrier.setSelectedIndex(Setup.getIndexOfProvider(Email.getProvider(cellnum.substring(cellnum.indexOf("@")))));
        }
        JTFCellNum.setSize(d);
      }
    });
  }

  public void showWindow() {
    javax.swing.SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        setVisible(true);
      }
    });
  }

  private static String loadHtml(String localPath) {
    try {
      java.io.InputStream in = Setup.class.getResourceAsStream(localPath);
      java.util.Scanner scan = new java.util.Scanner(in);
      String text = "";
      while (scan.hasNext()) {
        text += scan.nextLine();
      }
      return text;
    } catch (Exception e) {
      e.printStackTrace();
      return "[ERROR LOADING " + localPath + "]";
    }
  }

  public void addPhonePanel(ExtraPhonePanel panel) {
    extraPhonePanelList.add(panel);
    JPPhonePanel.add(panel);
    JPPhonePanel.revalidate();
    pack();
  }

  public void removePhonePanel(ExtraPhonePanel panel) {
    extraPhonePanelList.remove(panel);
    JPPhonePanel.remove(panel);
    JPPhonePanel.revalidate();
    pack();
  }

  /**
   * Gets the index of the given Expo for the Setup JComboBox. The proper input for the method is the same Strings as the JComboBox in the Setup GUI.
   *
   * @param eventName The expo ("Pax EXPO") to get the index of
   * @return The index of the given expo, or 0 for incorrect inputs.
   */
  public static final int getIndexOfEvent(String eventName) {
    switch (eventName.toLowerCase()) {
      case "pax prime":
      default:
        return 0;
      case "pax east":
        return 1;
      case "pax south":
        return 2;
      case "pax aus":
        return 3;
    }
  }

  /**
   * Gets the index of the given provider for the Set JComboBox. The proper input for this method is the same Strings as the JComboBox in the Setup
   * GUI.
   *
   * @param provider The provider to get the index of
   * @return The index of the given provider, or 0 for incorrect inputs.
   */
  public static final int getIndexOfProvider(String provider) {
    switch (provider.toLowerCase()) {
      case "at&t (mms)":
      default:
        return 0;
      case "at&t (sms)":
        return 1;
      case "verizon":
        return 2;
      case "sprint":
        return 3;
      case "t-mobile":
        return 4;
      case "u.s. cellular":
        return 5;
      case "[other]":
        return 6;
    }
  }

  public void setPatchNotesText(final String text) {
    javax.swing.SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        jTextArea4.setText(text);
        jTextArea4.setCaretPosition(0);
      }
    });
  }

  private void savePreferences() {
    PreferenceHandler.getPreferenceObject(Preference.TYPES.SAVE_PREFS).setShouldSave(true);
    PreferenceHandler.getPreferenceObject(Preference.TYPES.SAVE_PREFS).setValue(JCBSavePreferences.isSelected());
    PreferenceHandler.getPreferenceObject(Preference.TYPES.USE_BETA).setShouldSave(true);
    PreferenceHandler.getPreferenceObject(Preference.TYPES.USE_BETA).setValue(JCBUseBeta.isSelected());
    PreferenceHandler.getPreferenceObject(Preference.TYPES.LOAD_UPDATES).setShouldSave(true);
    PreferenceHandler.getPreferenceObject(Preference.TYPES.LOAD_UPDATES).setValue(JCBCheckUpdates.isSelected());
    PreferenceHandler.getPreferenceObject(Preference.TYPES.LOAD_NOTIFICATIONS).setShouldSave(true);
    PreferenceHandler.getPreferenceObject(Preference.TYPES.LOAD_NOTIFICATIONS).setValue(JCBLoadNotifications.isSelected());
    PreferenceHandler.getPreferenceObject(Preference.TYPES.CELLNUM).setShouldSave(JCBSaveCellnum.isSelected());
    PreferenceHandler.getPreferenceObject(Preference.TYPES.CELLNUM).setValue(getCellNumString());
    PreferenceHandler.getPreferenceObject(Preference.TYPES.CHECK_PAX).setShouldSave(JCBSaveCheckPax.isSelected());
    PreferenceHandler.getPreferenceObject(Preference.TYPES.CHECK_PAX).setValue(JCBCheckWebsite.isSelected());
    PreferenceHandler.getPreferenceObject(Preference.TYPES.CHECK_SHOWCLIX).setShouldSave(JCBSaveCheckShowclix.isSelected());
    PreferenceHandler.getPreferenceObject(Preference.TYPES.CHECK_SHOWCLIX).setValue(JCBCheckShowclix.isSelected());
    PreferenceHandler.getPreferenceObject(Preference.TYPES.CHECK_TWITTER).setShouldSave(JCBCheckTwitter.isSelected());
    PreferenceHandler.getPreferenceObject(Preference.TYPES.CHECK_TWITTER).setValue(JCBCheckTwitter.isSelected());
    PreferenceHandler.getPreferenceObject(Preference.TYPES.EVENT).setShouldSave(JCBSaveEvent.isSelected());
    PreferenceHandler.getPreferenceObject(Preference.TYPES.EVENT).setValue(JCBExpo.getSelectedItem().toString());
    PreferenceHandler.getPreferenceObject(Preference.TYPES.PLAY_ALARM).setShouldSave(JCBSavePlayAlarm.isSelected());
    PreferenceHandler.getPreferenceObject(Preference.TYPES.PLAY_ALARM).setValue(JCBPlayAlarm.isSelected());
    PreferenceHandler.getPreferenceObject(Preference.TYPES.REFRESHTIME).setShouldSave(JCBSaveRefreshTime.isSelected());
    PreferenceHandler.getPreferenceObject(Preference.TYPES.REFRESHTIME).setValue(JSCheckTime.getValue());
    PreferenceHandler.getPreferenceObject(Preference.TYPES.EMAIL).setShouldSave(JCBSaveEmail.isSelected());
    PreferenceHandler.getPreferenceObject(Preference.TYPES.EMAIL).setValue(JTFEmail.getText());
    PreferenceHandler.getPreferenceObject(Preference.TYPES.TWITTER_CONSUMER_KEY).setShouldSave(JCBSaveTwitterKeys.isSelected());
    PreferenceHandler.getPreferenceObject(Preference.TYPES.TWITTER_CONSUMER_SECRET).setShouldSave(JCBSaveTwitterKeys.isSelected());
    PreferenceHandler.getPreferenceObject(Preference.TYPES.TWITTER_APP_KEY).setShouldSave(JCBSaveTwitterKeys.isSelected());
    PreferenceHandler.getPreferenceObject(Preference.TYPES.TWITTER_APP_SECRET).setShouldSave(JCBSaveTwitterKeys.isSelected());
    PreferenceHandler.savePreferences();
  }

  public void disableTwitter() {
    JCBCheckTwitter.setSelected(false);
    JCBCheckTwitter.setEnabled(false);
    JLTwitterDisabled.setVisible(true);
  }

  private String getCellNumString() {
    String text = JTFCellNum.getText();
    if (text == null || text.length() < 5) {
      text = "";
    } else if (!text.contains("@")) {
      text += Email.getCarrierExtension(JCBCarrier.getSelectedItem().toString());
    }
    String tempText;
    java.util.Iterator<ExtraPhonePanel> myIt = extraPhonePanelList.iterator();
    while (myIt.hasNext()) {
      ExtraPhonePanel panel = myIt.next();
      tempText = panel.getNumber();
      if (tempText.length() < 4) {
        System.out.println("NOTE: Number is too short! Cannot use!");
        continue;
      }
      String[] split = tempText.split(";");
      tempText = "";
      for (String split1 : split) {
        split1 = split1.trim();
        tempText += split1;
        if (!split1.contains("@")) {
          tempText += Email.getCarrierExtension(panel.getProvider());
        }
        tempText += ";";
      }
      //Validate tempText address?
      text += ";" + tempText;
      System.out.println("Debug: " + tempText);
    }
    System.out.println("Final Text: " + text);
    return text;
  }

  /**
   * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always
   * regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {

    jTabbedPane1 = new javax.swing.JTabbedPane();
    jPanel1 = new javax.swing.JPanel();
    jLabel1 = new javax.swing.JLabel();
    jLabel2 = new javax.swing.JLabel();
    JTFEmail = new javax.swing.JTextField();
    JPFPassword = new javax.swing.JPasswordField();
    jLabel3 = new javax.swing.JLabel();
    jLabel4 = new javax.swing.JLabel();
    JTFCellNum = new javax.swing.JTextField();
    JCBCarrier = new javax.swing.JComboBox();
    jLabel6 = new javax.swing.JLabel();
    JSCheckTime = new javax.swing.JSlider();
    JBStart = new javax.swing.JButton();
    JCBCheckWebsite = new javax.swing.JCheckBox();
    JCBCheckShowclix = new javax.swing.JCheckBox();
    JCBPlayAlarm = new javax.swing.JCheckBox();
    JCBExpo = new javax.swing.JComboBox();
    jLabel5 = new javax.swing.JLabel();
    jPanel2 = new javax.swing.JPanel();
    JPPhonePanel = new javax.swing.JPanel();
    JBAddPhone = new javax.swing.JButton();
    JCBCheckTwitter = new javax.swing.JCheckBox();
    JLTwitterDisabled = new javax.swing.JLabel();
    jPanel6 = new javax.swing.JPanel();
    jScrollPane5 = new javax.swing.JScrollPane();
    JTPInstructions = new javax.swing.JTextPane();
    jPanel4 = new javax.swing.JPanel();
    jScrollPane4 = new javax.swing.JScrollPane();
    jTextArea4 = new javax.swing.JTextArea();
    jPanel3 = new javax.swing.JPanel();
    jScrollPane6 = new javax.swing.JScrollPane();
    JTPExtra = new javax.swing.JTextPane();
    jPanel5 = new javax.swing.JPanel();
    JCBSavePreferences = new javax.swing.JCheckBox();
    JCBSaveEmail = new javax.swing.JCheckBox();
    JCBSaveCellnum = new javax.swing.JCheckBox();
    JCBSaveRefreshTime = new javax.swing.JCheckBox();
    JCBSaveCheckPax = new javax.swing.JCheckBox();
    JCBSaveCheckShowclix = new javax.swing.JCheckBox();
    JCBSavePlayAlarm = new javax.swing.JCheckBox();
    JCBSaveEvent = new javax.swing.JCheckBox();
    jScrollPane1 = new javax.swing.JScrollPane();
    jTextArea1 = new javax.swing.JTextArea();
    JCBUseBeta = new javax.swing.JCheckBox();
    jButton3 = new javax.swing.JButton();
    JCBSaveCheckTwitter = new javax.swing.JCheckBox();
    JCBLoadNotifications = new javax.swing.JCheckBox();
    JCBCheckUpdates = new javax.swing.JCheckBox();
    JCBSaveTwitterKeys = new javax.swing.JCheckBox();

    setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
    setTitle("PAX Checker Setup");
    setResizable(false);

    jLabel1.setFont(new java.awt.Font("Tahoma", 0, 36)); // NOI18N
    jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    jLabel1.setText("PAX Checker Setup");

    jLabel2.setText("Email");

    JTFEmail.setToolTipText("<html>\nEmail address to send texts with. Currently,<br>\nonly Yahoo! and Gmail are supported.<br>\nPut your full email addres in, otherwise it<br>\ndefaults to @yahoo.com.<br>\nSee the Instructions tab for how to use emails<br>\nother than Yahoo! or Gmail.\n</html>");

    jLabel3.setText("Password");

    jLabel4.setText("Cell Num");

    JTFCellNum.setToolTipText("<html>\nSpecify the number you want to receive texts at.<br>\nOnly put your number - no spaces, no leading 1.<br>\nYou may use dashes -- or perentheses ().<br>\nIf you use a different carrier, you may find their<br>\ntexting email address extension at<br>\nwww.emailtextmessages.com and put it onto the<br>\nend of your number.<br>\nExamples:<br>\n(123)-456-7890 [Verizon selected in dropdown box]<br>\n1234567890@car.rier.net<br>\n123-4567890@car.rier.net<br>\n</html>");

    JCBCarrier.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "AT&T (MMS)", "AT&T (SMS)", "Verizon", "Sprint", "T-Mobile", "U.S. Cellular", "[Other]" }));

    jLabel6.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    jLabel6.setText("Seconds Between Website Checks");

    JSCheckTime.setMajorTickSpacing(10);
    JSCheckTime.setMaximum(60);
    JSCheckTime.setMinimum(10);
    JSCheckTime.setMinorTickSpacing(2);
    JSCheckTime.setPaintLabels(true);
    JSCheckTime.setPaintTicks(true);
    JSCheckTime.setPaintTrack(false);
    JSCheckTime.setSnapToTicks(true);
    JSCheckTime.setValue(10);

    JBStart.setText("START!");
    JBStart.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        JBStartActionPerformed(evt);
      }
    });

    JCBCheckWebsite.setSelected(true);
    JCBCheckWebsite.setText("Scan PAX Registration website");
    JCBCheckWebsite.setToolTipText("<html>\nThis option will open the queue closer<br>\nto, if not after, the Showclix website.<br>\nIt may also be slower than the Twitter<br>\nnotification.<br>\nThis option uses a small amount of data.\n</html>");
    JCBCheckWebsite.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        JCBCheckWebsiteActionPerformed(evt);
      }
    });

    JCBCheckShowclix.setSelected(true);
    JCBCheckShowclix.setText("Scan Showclix website");
    JCBCheckShowclix.setToolTipText("<html>\nIt is highly recommended that you<br>\nuse this option. It scans the Showclix<br>\nwebsite for updates and is generally the<br>\nfastest possible.<br>\nThis option uses a very small amount of data.\n</html>");
    JCBCheckShowclix.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        JCBCheckShowclixActionPerformed(evt);
      }
    });

    JCBPlayAlarm.setText("Play Alarm when Tickets Found");
    JCBPlayAlarm.setToolTipText("<html>\nIf checked, the program will play a sound when an update to<br>\nthe PAX Prime website OR the Showclix website (whichever one(s)<br>\nyou have enabled) is found.\n</html>");

    JCBExpo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "PAX Prime", "PAX East", "PAX South", "PAX Aus" }));

    jLabel5.setText("PAX Expo to Check");

    jPanel2.setLayout(new javax.swing.BoxLayout(jPanel2, javax.swing.BoxLayout.LINE_AXIS));

    javax.swing.GroupLayout JPPhonePanelLayout = new javax.swing.GroupLayout(JPPhonePanel);
    JPPhonePanel.setLayout(JPPhonePanelLayout);
    JPPhonePanelLayout.setHorizontalGroup(
      JPPhonePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGap(0, 0, Short.MAX_VALUE)
    );
    JPPhonePanelLayout.setVerticalGroup(
      JPPhonePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGap(0, 0, Short.MAX_VALUE)
    );

    JBAddPhone.setText("Add Another Phone Number");
    JBAddPhone.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        JBAddPhoneActionPerformed(evt);
      }
    });

    JCBCheckTwitter.setText("Scan @Offical_PAX Twitter");
    JCBCheckTwitter.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        JCBCheckTwitterActionPerformed(evt);
      }
    });

    JLTwitterDisabled.setFont(new java.awt.Font("Tahoma", 2, 11)); // NOI18N
    JLTwitterDisabled.setForeground(new java.awt.Color(0, 0, 238));
    JLTwitterDisabled.setText("(Why is this disabled?)");
    JLTwitterDisabled.addMouseListener(new java.awt.event.MouseAdapter() {
      public void mousePressed(java.awt.event.MouseEvent evt) {
        JLTwitterDisabledMousePressed(evt);
      }
    });

    javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
    jPanel1.setLayout(jPanel1Layout);
    jPanel1Layout.setHorizontalGroup(
      jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(jPanel1Layout.createSequentialGroup()
        .addContainerGap()
        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(JBStart, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
          .addComponent(JSCheckTime, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
          .addComponent(jLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
          .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
          .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 391, Short.MAX_VALUE)
          .addGroup(jPanel1Layout.createSequentialGroup()
            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
              .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                .addComponent(jLabel4, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
              .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
              .addComponent(JTFEmail)
              .addComponent(JPFPassword)
              .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(JTFCellNum)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(JCBCarrier, javax.swing.GroupLayout.PREFERRED_SIZE, 82, javax.swing.GroupLayout.PREFERRED_SIZE))))
          .addComponent(JPPhonePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
          .addComponent(JBAddPhone, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
          .addGroup(jPanel1Layout.createSequentialGroup()
            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
              .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                .addGroup(jPanel1Layout.createSequentialGroup()
                  .addComponent(jLabel5)
                  .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                  .addComponent(JCBExpo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addComponent(JCBPlayAlarm)
                .addComponent(JCBCheckShowclix)
                .addComponent(JCBCheckWebsite))
              .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(JCBCheckTwitter)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(JLTwitterDisabled)))
            .addGap(0, 0, Short.MAX_VALUE)))
        .addContainerGap())
    );
    jPanel1Layout.setVerticalGroup(
      jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(jPanel1Layout.createSequentialGroup()
        .addContainerGap()
        .addComponent(jLabel1)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(jLabel2)
          .addComponent(JTFEmail, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(jLabel3)
          .addComponent(JPFPassword, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(jLabel4)
          .addComponent(JTFCellNum, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(JCBCarrier, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addGap(0, 0, 0)
        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addGap(0, 0, 0)
        .addComponent(JPPhonePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(JBAddPhone)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(JCBCheckWebsite)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(JCBCheckShowclix)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(JCBCheckTwitter)
          .addComponent(JLTwitterDisabled))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(JCBPlayAlarm)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(jLabel5)
          .addComponent(JCBExpo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(jLabel6)
        .addGap(0, 0, 0)
        .addComponent(JSCheckTime, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(JBStart)
        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
    );

    JPPhonePanel.setLayout(new javax.swing.BoxLayout(JPPhonePanel, javax.swing.BoxLayout.Y_AXIS));

    jTabbedPane1.addTab("Setup", jPanel1);

    JTPInstructions.setBorder(null);
    JTPInstructions.setContentType("text/html"); // NOI18N
    JTPInstructions.setEditable(false);
    JTPInstructions.setEditorKit(javax.swing.JEditorPane.createEditorKitForContentType("text/html"));
    JTPInstructions.setText("[Located in Instructions.html]");
    JTPInstructions.addHyperlinkListener(new javax.swing.event.HyperlinkListener() {
      public void hyperlinkUpdate(javax.swing.event.HyperlinkEvent e) {
        if (e.getEventType() == javax.swing.event.HyperlinkEvent.EventType.ACTIVATED) {
          Browser.openLinkInBrowser(e.getURL());
        }
      }
    });
    jScrollPane5.setViewportView(JTPInstructions);

    javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
    jPanel6.setLayout(jPanel6Layout);
    jPanel6Layout.setHorizontalGroup(
      jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 411, Short.MAX_VALUE)
    );
    jPanel6Layout.setVerticalGroup(
      jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 387, Short.MAX_VALUE)
    );

    jTabbedPane1.addTab("Instructions", jPanel6);

    jTextArea4.setEditable(false);
    jTextArea4.setColumns(20);
    jTextArea4.setLineWrap(true);
    jTextArea4.setRows(5);
    jTextArea4.setText("Loading patch notes, please wait...");
    jTextArea4.setWrapStyleWord(true);
    jScrollPane4.setViewportView(jTextArea4);

    javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
    jPanel4.setLayout(jPanel4Layout);
    jPanel4Layout.setHorizontalGroup(
      jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 411, Short.MAX_VALUE)
    );
    jPanel4Layout.setVerticalGroup(
      jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 387, Short.MAX_VALUE)
    );

    jTabbedPane1.addTab("Patch Notes", jPanel4);

    JTPExtra.setBorder(null);
    JTPExtra.setContentType("text/html"); // NOI18N
    JTPExtra.setEditable(false);
    JTPExtra.setEditorKit(javax.swing.JEditorPane.createEditorKitForContentType("text/html"));
    JTPExtra.setText("[Located in Extra.html]");
    JTPExtra.setToolTipText("");
    JTPExtra.addHyperlinkListener(new javax.swing.event.HyperlinkListener() {
      public void hyperlinkUpdate(javax.swing.event.HyperlinkEvent e) {
        if (e.getEventType() == javax.swing.event.HyperlinkEvent.EventType.ACTIVATED) {
          Browser.openLinkInBrowser(e.getURL());
        }
      }
    });
    jScrollPane6.setViewportView(JTPExtra);

    javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
    jPanel3.setLayout(jPanel3Layout);
    jPanel3Layout.setHorizontalGroup(
      jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addComponent(jScrollPane6, javax.swing.GroupLayout.DEFAULT_SIZE, 411, Short.MAX_VALUE)
    );
    jPanel3Layout.setVerticalGroup(
      jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addComponent(jScrollPane6, javax.swing.GroupLayout.DEFAULT_SIZE, 387, Short.MAX_VALUE)
    );

    jTabbedPane1.addTab("Extra", jPanel3);

    JCBSavePreferences.setText("Save Preferences");
    JCBSavePreferences.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        JCBSavePreferencesActionPerformed(evt);
      }
    });

    JCBSaveEmail.setText("Save Username");

    JCBSaveCellnum.setText("Save Cell Number");
    JCBSaveCellnum.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        JCBSaveCellnumActionPerformed(evt);
      }
    });

    JCBSaveRefreshTime.setText("Save Refresh Time");

    JCBSaveCheckPax.setText("Save PAX Checking");

    JCBSaveCheckShowclix.setText("Save Showclix Checking");

    JCBSavePlayAlarm.setText("Save Play Alarm");

    JCBSaveEvent.setText("Save Event");

    jTextArea1.setEditable(false);
    jTextArea1.setColumns(20);
    jTextArea1.setLineWrap(true);
    jTextArea1.setRows(5);
    jTextArea1.setText("Preferences are saved after you start the program. You may choose which preferences you want saved. If you do not save a preference, it loads in its default state.\n\nNote that your password is NEVER saved using Preferences.");
    jTextArea1.setWrapStyleWord(true);
    jScrollPane1.setViewportView(jTextArea1);

    JCBUseBeta.setText("Use BETA Versions");
    JCBUseBeta.setToolTipText("<html>\nBETA versions will most likely<br>\ninclude features that could, at any<br>\npoint in time, break the program<br>\nor parts of it. These versions will<br>\ncontain ideas that still need to be<br>\ndebugged, so any help in finding<br>\nthese is greatly appreciated.<br>\nChange information can be found<br>\nin the Patch Notes, and more<br>\ndetailed changes can be found in<br>\nthe GitHub commits.<br>\n<br>\nUse at your own risk.\n</html>");

    jButton3.setText("Save Settings");
    jButton3.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        jButton3ActionPerformed(evt);
      }
    });

    JCBSaveCheckTwitter.setText("Save Twitter Checking");

    JCBLoadNotifications.setSelected(true);
    JCBLoadNotifications.setText("Load Notificaitons");

    JCBCheckUpdates.setSelected(true);
    JCBCheckUpdates.setText("Check for Updates");

    JCBSaveTwitterKeys.setText("Save Twitter Keys");
    JCBSaveTwitterKeys.setToolTipText("<html>\nNOTE: This saves your Twitter API<br>\nkeys in an encrypted format. Your<br>\nkeys will still be obtainable if you or<br>\nsomeone else has access to this<br>\nprogram's source code (which is<br>\npublicly available). Save at your<br>\nown risk!\n</html>");

    javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
    jPanel5.setLayout(jPanel5Layout);
    jPanel5Layout.setHorizontalGroup(
      jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(jPanel5Layout.createSequentialGroup()
        .addContainerGap()
        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addGroup(jPanel5Layout.createSequentialGroup()
            .addComponent(JCBSaveRefreshTime)
            .addGap(149, 149, 149)
            .addComponent(jButton3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
          .addComponent(jScrollPane1)
          .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
            .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
              .addComponent(JCBSavePreferences)
              .addComponent(JCBSavePlayAlarm)
              .addComponent(JCBSaveEvent)
              .addComponent(JCBSaveCellnum)
              .addComponent(JCBSaveCheckShowclix)
              .addComponent(JCBSaveCheckPax)
              .addComponent(JCBSaveCheckTwitter)
              .addComponent(JCBSaveEmail))
            .addGap(125, 125, 125)
            .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
              .addComponent(JCBLoadNotifications, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
              .addComponent(JCBCheckUpdates, javax.swing.GroupLayout.DEFAULT_SIZE, 127, Short.MAX_VALUE)
              .addComponent(JCBSaveTwitterKeys, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
              .addComponent(JCBUseBeta, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
        .addContainerGap())
    );
    jPanel5Layout.setVerticalGroup(
      jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(jPanel5Layout.createSequentialGroup()
        .addContainerGap()
        .addComponent(JCBSavePreferences)
        .addGap(18, 18, 18)
        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(JCBSaveEmail)
          .addComponent(JCBLoadNotifications))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(JCBSaveCellnum)
          .addComponent(JCBCheckUpdates))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(JCBSaveCheckPax)
          .addComponent(JCBSaveTwitterKeys))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(JCBSaveCheckShowclix)
          .addComponent(JCBUseBeta))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(JCBSaveCheckTwitter)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(JCBSavePlayAlarm)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(JCBSaveEvent)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(JCBSaveRefreshTime)
          .addComponent(jButton3))
        .addGap(10, 10, 10)
        .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 132, Short.MAX_VALUE)
        .addContainerGap())
    );

    jTabbedPane1.addTab("Preferences", jPanel5);

    javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
    getContentPane().setLayout(layout);
    layout.setHorizontalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addComponent(jTabbedPane1)
    );
    layout.setVerticalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
    );

    pack();
  }// </editor-fold>//GEN-END:initComponents

  private void JCBCheckShowclixActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_JCBCheckShowclixActionPerformed
    javax.swing.SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        if (!JCBCheckWebsite.isSelected() && !JCBCheckShowclix.isSelected() && (!TwitterReader.isInitialized() || !JCBCheckTwitter.isSelected())) {
          JBStart.setEnabled(false);
        } else {
          JBStart.setEnabled(true);
        }
      }
    });
  }//GEN-LAST:event_JCBCheckShowclixActionPerformed

  private void JCBCheckWebsiteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_JCBCheckWebsiteActionPerformed
    // TODO add your handling code here:
    javax.swing.SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        if (!JCBCheckWebsite.isSelected() && !JCBCheckShowclix.isSelected() && (!TwitterReader.isInitialized() || !JCBCheckTwitter.isSelected())) {
          JBStart.setEnabled(false);
        } else {
          JBStart.setEnabled(true);
        }
      }
    });
  }//GEN-LAST:event_JCBCheckWebsiteActionPerformed

  private void JBStartActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_JBStartActionPerformed
    // TODO add your handling code here:
    JBStart.setText("Starting, please wait...");
    Browser.setExpo(JCBExpo.getSelectedItem().toString());
    if (JCBCheckWebsite.isSelected()) {
      TicketChecker.addChecker(new CheckPaxsite());
    }
    if (JCBCheckShowclix.isSelected()) {
      TicketChecker.addChecker(new CheckShowclix());
    }
    if (JCBCheckTwitter.isSelected() && TwitterReader.isInitialized()) {
      Checker.startTwitterStreaming();
    }
    Audio.setPlayAlarm(JCBPlayAlarm.isSelected());
    Email.setUsername(JTFEmail.getText());
    Email.setPassword(new String(JPFPassword.getPassword()));
    Email.addEmailAddress(getCellNumString());
    Checker.setRefreshTime(JSCheckTime.getValue());
    savePreferences();
    dispose();
    Checker.startCheckingWebsites();
    //TicketCheck.startGUI();
  }//GEN-LAST:event_JBStartActionPerformed

  private void JCBSaveCellnumActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_JCBSaveCellnumActionPerformed
    // TODO add your handling code here:
  }//GEN-LAST:event_JCBSaveCellnumActionPerformed

  private void JCBSavePreferencesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_JCBSavePreferencesActionPerformed
    // TODO add your handling code here:
    boolean selected = JCBSavePreferences.isSelected();
    JCBSaveCellnum.setEnabled(selected);
    JCBSaveCheckPax.setEnabled(selected);
    JCBSaveCheckShowclix.setEnabled(selected);
    JCBSaveCheckTwitter.setEnabled(selected);
    JCBSaveEmail.setEnabled(selected);
    JCBSaveEvent.setEnabled(selected);
    JCBSavePlayAlarm.setEnabled(selected);
    JCBSaveRefreshTime.setEnabled(selected);
  }//GEN-LAST:event_JCBSavePreferencesActionPerformed

  private void JBAddPhoneActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_JBAddPhoneActionPerformed
    // TODO add your handling code here:
    addPhonePanel(new ExtraPhonePanel(this));
  }//GEN-LAST:event_JBAddPhoneActionPerformed

  private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
    savePreferences();
  }//GEN-LAST:event_jButton3ActionPerformed

  private void JCBCheckTwitterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_JCBCheckTwitterActionPerformed
    // TODO add your handling code here:
    javax.swing.SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        if (!JCBCheckWebsite.isSelected() && !JCBCheckShowclix.isSelected() && (!TwitterReader.isInitialized() || !JCBCheckTwitter.isSelected())) {
          JBStart.setEnabled(false);
        } else {
          JBStart.setEnabled(true);
        }
      }
    });
  }//GEN-LAST:event_JCBCheckTwitterActionPerformed

  private void JLTwitterDisabledMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_JLTwitterDisabledMousePressed
    // TODO add your handling code here:
    twitterDisabledWindow.setVisible(true);
  }//GEN-LAST:event_JLTwitterDisabledMousePressed

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JButton JBAddPhone;
  private javax.swing.JButton JBStart;
  private javax.swing.JComboBox JCBCarrier;
  private javax.swing.JCheckBox JCBCheckShowclix;
  private javax.swing.JCheckBox JCBCheckTwitter;
  private javax.swing.JCheckBox JCBCheckUpdates;
  private javax.swing.JCheckBox JCBCheckWebsite;
  private javax.swing.JComboBox JCBExpo;
  private javax.swing.JCheckBox JCBLoadNotifications;
  private javax.swing.JCheckBox JCBPlayAlarm;
  private javax.swing.JCheckBox JCBSaveCellnum;
  private javax.swing.JCheckBox JCBSaveCheckPax;
  private javax.swing.JCheckBox JCBSaveCheckShowclix;
  private javax.swing.JCheckBox JCBSaveCheckTwitter;
  private javax.swing.JCheckBox JCBSaveEmail;
  private javax.swing.JCheckBox JCBSaveEvent;
  private javax.swing.JCheckBox JCBSavePlayAlarm;
  private javax.swing.JCheckBox JCBSavePreferences;
  private javax.swing.JCheckBox JCBSaveRefreshTime;
  private javax.swing.JCheckBox JCBSaveTwitterKeys;
  private javax.swing.JCheckBox JCBUseBeta;
  private javax.swing.JLabel JLTwitterDisabled;
  private javax.swing.JPasswordField JPFPassword;
  private javax.swing.JPanel JPPhonePanel;
  private javax.swing.JSlider JSCheckTime;
  private javax.swing.JTextField JTFCellNum;
  private javax.swing.JTextField JTFEmail;
  private javax.swing.JTextPane JTPExtra;
  private javax.swing.JTextPane JTPInstructions;
  private javax.swing.JButton jButton3;
  private javax.swing.JLabel jLabel1;
  private javax.swing.JLabel jLabel2;
  private javax.swing.JLabel jLabel3;
  private javax.swing.JLabel jLabel4;
  private javax.swing.JLabel jLabel5;
  private javax.swing.JLabel jLabel6;
  private javax.swing.JPanel jPanel1;
  private javax.swing.JPanel jPanel2;
  private javax.swing.JPanel jPanel3;
  private javax.swing.JPanel jPanel4;
  private javax.swing.JPanel jPanel5;
  private javax.swing.JPanel jPanel6;
  private javax.swing.JScrollPane jScrollPane1;
  private javax.swing.JScrollPane jScrollPane4;
  private javax.swing.JScrollPane jScrollPane5;
  private javax.swing.JScrollPane jScrollPane6;
  private javax.swing.JTabbedPane jTabbedPane1;
  private javax.swing.JTextArea jTextArea1;
  private javax.swing.JTextArea jTextArea4;
  // End of variables declaration//GEN-END:variables
}
