package com.github.sunnybat.paxchecker.setup;

import com.github.sunnybat.commoncode.email.EmailAddress;
import com.github.sunnybat.commoncode.encryption.Encryption;
import com.github.sunnybat.commoncode.preferences.PreferenceHandler;
import com.github.sunnybat.paxchecker.PAXChecker;
import com.github.sunnybat.paxchecker.browser.Browser;
import com.github.sunnybat.paxchecker.notification.NotificationWindow;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Creates a new Setup GUI for the user to configure the program at will. Saves Preferences independent of the rest of the program for quick loading
 * afterwards.
 *
 * @author SunnyBat
 */
public class SetupGUI extends javax.swing.JFrame implements Setup {

  private java.util.ArrayList<ExtraPhonePanel> extraPhonePanelList;
  private NotificationWindow twitterDisabledWindow;
  private PreferenceHandler prefs;
  private boolean isOpen = true;

  /**
   * Creates new form Setup
   */
  public SetupGUI() {
    extraPhonePanelList = new java.util.ArrayList<>();
    twitterDisabledWindow = new NotificationWindow("Twitter Disabled?", "Twitter is disabled because you did "
        + "not give the PAXChecker any Twitter API keys to use. For more information, click the More Info button.");
    twitterDisabledWindow.setMoreInfoButtonLink("http://redd.it/2nct50");
    prefs = new PreferenceHandler("paxchecker");
    javax.swing.SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        initComponents();
        customComponents();
        loadProgramSettings();
        setTwitterKeysVisible(false);
      }
    });
  }

  /**
   * Creates new form Setup. Note that twitterKeys must be a non-null String array with exactly four elements.
   *
   * @param twitterKeys
   */
  public SetupGUI(String[] twitterKeys) {
    this();
    if (twitterKeys != null && twitterKeys.length == 4) {
      JTFConsumerKey.setText(twitterKeys[0]);
      JTFConsumerSecret.setText(twitterKeys[1]);
      JTFApplicationKey.setText(twitterKeys[2]);
      JTFApplicationSecret.setText(twitterKeys[3]);
      if (JCBCheckTwitter.isSelected()) { // This is loaded after Preferences, so we're good
        setTwitterKeysVisible(true);
      }
    }
  }

  private void customComponents() {
    setTitle("Setup :: PAXChecker v" + PAXChecker.VERSION);
    JTPExtra.setText(loadHtml("/com/github/sunnybat/paxchecker/gui/Extra.html"));
    JTPExtra.setCaretPosition(0);
    JTPInstructions.setText(loadHtml("/com/github/sunnybat/paxchecker/gui/Instructions.html"));
    JTPInstructions.setCaretPosition(0);
  }

  public void loadProgramSettings() {
    // TODO: Change to better method of getting each Preference than raw String

    // Preferences
    JCBSaveEmailSettings.setSelected(prefs.getBooleanPreference("SAVE_EMAIL_SETTINGS"));
    JCBSaveCheckSettings.setSelected(prefs.getBooleanPreference("SAVE_CHECK_SETTINGS"));
    JCBUseBeta.setSelected(prefs.getBooleanPreference("USE_BETA"));
    JCBLoadNotifications.setSelected(prefs.getBooleanPreference("LOAD_NOTIFICATIONS"));
    JCBCheckUpdates.setSelected(prefs.getBooleanPreference("LOAD_UPDATES"));
    if (JCBCheckUpdates.isSelected()) {
      JCBCheckUpdateDaily.setSelected(prefs.getBooleanPreference("DAILY_UPDATES"));
    } else {
      JCBCheckUpdateDaily.setEnabled(false);
    }
    JCBSaveTwitterKeys.setSelected(prefs.getBooleanPreference("SAVE_TWITTER_KEYS"));
    JCBStatistics.setSelected(prefs.getBooleanPreference("ANONYMOUS_STATISTICS"));

    // Main Settings
    String cellNum = prefs.getStringPreference("CELLNUM");
    if (cellNum != null) {
      if (cellNum.contains(";")) {
        // TODO: Add EPPs to the GUI with all emails
        String[] split = cellNum.split(";");
        for (String s : split) {
          s = s.trim(); // Not sure if this will actually work
          ExtraPhonePanel p = new ExtraPhonePanel(this, s.substring(0, s.indexOf("@")), EmailAddress.getProvider(s.substring(s.indexOf("@") + 1)));
        }
      } else {
        JTFCellNum.setText(cellNum);
      }
    }
    JTFEmail.setText(prefs.getStringPreference("EMAIL"));
    JCBCheckWebsite.setSelected(prefs.getBooleanPreference("CHECK_PAX"));
    JCBCheckShowclix.setSelected(prefs.getBooleanPreference("CHECK_SHOWCLIX"));
    JCBCheckTwitter.setSelected(prefs.getBooleanPreference("CHECK_TWITTER"));
    JCBCheckKnownEvents.setSelected(prefs.getBooleanPreference("CHECK_KNOWN_EVENTS"));
    JCBFilterTwitter.setSelected(prefs.getBooleanPreference("FILTER_TWITTER"));
    JCBExpo.setSelectedIndex(getIndexOfEvent(prefs.getStringPreference("EVENT")));
    JCBPlayAlarm.setSelected(prefs.getBooleanPreference("PLAY_ALARM"));
    JSCheckTime.setValue(prefs.getIntegerPreference("REFRESHTIME"));
    try {
      JTFConsumerKey.setText(Encryption.decrypt(prefs.getStringPreference("TWITTER_CONSUMER_KEY")));
      JTFConsumerSecret.setText(Encryption.decrypt(prefs.getStringPreference("TWITTER_CONSUMER_SECRET")));
      JTFApplicationKey.setText(Encryption.decrypt(prefs.getStringPreference("TWITTER_APP_KEY")));
      JTFApplicationSecret.setText(Encryption.decrypt(prefs.getStringPreference("TWITTER_APP_SECRET")));
    } catch (NullPointerException e) {
      System.out.println("Unable to load Twitter keys from Preferences! Program will still function normally.");
    }
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

  private void setTwitterKeysVisible(boolean visible) {
    JPTwitterKeys.setVisible(visible);
    setSize(getWidth(), getHeight() - (visible ? 0 : JPTwitterKeys.getHeight()));
  }

  /**
   * Gets the index of the given Expo for the Setup JComboBox. The proper input for the method is the same Strings as the JComboBox in the Setup GUI.
   *
   * @param eventName The expo ("Pax EXPO") to get the index of
   * @return The index of the given expo, or 0 for incorrect inputs.
   */
  public static final int getIndexOfEvent(String eventName) {
    if (eventName == null) {
      return 0;
    }
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
    if (provider == null) {
      return 0;
    }
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
      case "bell":
        return 6;
      case "rogers":
        return 7;
      case "fido":
        return 8;
      case "koodo":
        return 9;
      case "telus":
        return 10;
      case "virgin (CAN)":
        return 11;
      case "wind":
        return 12;
      case "sasktel":
        return 13;
      case "[other]":
        return 14;
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
    prefs.getPreferenceObject("USE_BETA").setValue(JCBUseBeta.isSelected());
    prefs.getPreferenceObject("LOAD_UPDATES").setValue(JCBCheckUpdates.isSelected());
    prefs.getPreferenceObject("DAILY_UPDATES").setValue(JCBCheckUpdateDaily.isSelected());
    prefs.getPreferenceObject("DAILY_UPDATES").setShouldSave(JCBCheckUpdates.isSelected()); // If update checking is disabled, don't check every 24 hours
    prefs.getPreferenceObject("LOAD_NOTIFICATIONS").setValue(JCBLoadNotifications.isSelected());
    prefs.getPreferenceObject("CELLNUM").setShouldSave(JCBSaveEmailSettings.isSelected());
    prefs.getPreferenceObject("CELLNUM").setValue(getCellNumString());
    prefs.getPreferenceObject("EMAIL").setShouldSave(JCBSaveEmailSettings.isSelected());
    prefs.getPreferenceObject("EMAIL").setValue(JTFEmail.getText());
    prefs.getPreferenceObject("CHECK_PAX").setShouldSave(JCBSaveCheckSettings.isSelected());
    prefs.getPreferenceObject("CHECK_PAX").setValue(JCBCheckWebsite.isSelected());
    prefs.getPreferenceObject("CHECK_SHOWCLIX").setShouldSave(JCBSaveCheckSettings.isSelected());
    prefs.getPreferenceObject("CHECK_SHOWCLIX").setValue(JCBCheckShowclix.isSelected());
    prefs.getPreferenceObject("CHECK_TWITTER").setShouldSave(JCBSaveCheckSettings.isSelected());
    prefs.getPreferenceObject("CHECK_TWITTER").setValue(JCBCheckTwitter.isSelected());
    prefs.getPreferenceObject("CHECK_KNOWN_EVENTS").setShouldSave(JCBSaveCheckSettings.isSelected());
    prefs.getPreferenceObject("CHECK_KNOWN_EVENTS").setValue(JCBCheckKnownEvents.isSelected());
    prefs.getPreferenceObject("FILTER_TWITTER").setShouldSave(JCBSaveCheckSettings.isSelected());
    prefs.getPreferenceObject("FILTER_TWITTER").setValue(JCBFilterTwitter.isSelected());
    prefs.getPreferenceObject("EVENT").setShouldSave(JCBSaveCheckSettings.isSelected());
    prefs.getPreferenceObject("EVENT").setValue(JCBExpo.getSelectedItem().toString());
    prefs.getPreferenceObject("PLAY_ALARM").setShouldSave(JCBSaveCheckSettings.isSelected());
    prefs.getPreferenceObject("PLAY_ALARM").setValue(JCBPlayAlarm.isSelected());
    prefs.getPreferenceObject("REFRESHTIME").setShouldSave(JCBSaveCheckSettings.isSelected());
    prefs.getPreferenceObject("REFRESHTIME").setValue(JSCheckTime.getValue());
    prefs.getPreferenceObject("TWITTER_CONSUMER_KEY").setShouldSave(JCBSaveTwitterKeys.isSelected());
    prefs.getPreferenceObject("TWITTER_CONSUMER_KEY").setValue(Encryption.encrypt(JTFConsumerKey.getText()));
    prefs.getPreferenceObject("TWITTER_CONSUMER_SECRET").setShouldSave(JCBSaveTwitterKeys.isSelected());
    prefs.getPreferenceObject("TWITTER_CONSUMER_SECRET").setValue(Encryption.encrypt(JTFConsumerSecret.getText()));
    prefs.getPreferenceObject("TWITTER_APP_KEY").setShouldSave(JCBSaveTwitterKeys.isSelected());
    prefs.getPreferenceObject("TWITTER_APP_KEY").setValue(Encryption.encrypt(JTFApplicationKey.getText()));
    prefs.getPreferenceObject("TWITTER_APP_SECRET").setShouldSave(JCBSaveTwitterKeys.isSelected());
    prefs.getPreferenceObject("TWITTER_APP_SECRET").setValue(Encryption.encrypt(JTFApplicationSecret.getText()));
    prefs.getPreferenceObject("ANONYMOUS_STATISTICS").setValue(JCBStatistics.isSelected());
    prefs.savePreferences();
  }

  public void disableTwitter() {
    JCBCheckTwitter.setSelected(false);
    setTwitterCheckboxEnabled(false);
  }

  private String getCellNumString() {
    String text = JTFCellNum.getText();
    if (text == null || text.length() < 5) {
      text = "";
    } else if (!text.contains("@")) {
      text += EmailAddress.getCarrierExtension(JCBCarrier.getSelectedItem().toString());
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
          tempText += EmailAddress.getCarrierExtension(panel.getProvider());
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

  private void setTwitterCheckboxEnabled(boolean enabled) {
    JCBCheckTwitter.setEnabled(enabled);
    JLTwitterDisabled.setVisible(!enabled);
    JCBFilterTwitter.setVisible(enabled);
  }

  private void updateElements() {
    JSCheckTime.setEnabled(JCBCheckWebsite.isSelected() || JCBCheckShowclix.isSelected() || JCBCheckKnownEvents.isSelected());
    jLabel6.setEnabled(JCBCheckWebsite.isSelected() || JCBCheckShowclix.isSelected() || JCBCheckKnownEvents.isSelected());
    // TODO: Make JBStart not enable if Twitter checking is not properly set up and only JCBCheckTwitter is selected
    JBStart.setEnabled(JCBCheckWebsite.isSelected() || JCBCheckShowclix.isSelected() || JCBCheckTwitter.isSelected() || JCBCheckKnownEvents.isSelected());
  }

  @Override
  public void promptForSettings() {
    showWindow();
    try {
      while (isOpen) { // Should be valid until disposed
        Thread.sleep(250);
      }
    } catch (InterruptedException iE) {
      iE.printStackTrace();
      System.out.println("Uh... Interrupted while sleeping and waiting for GUI?");
    }
  }

  @Override
  public String getEmailUsername() {
    return JTFEmail.getText();
  }

  @Override
  public String getEmailPassword() {
    return new String(JPFPassword.getPassword());
  }

  @Override
  public List<String> getEmailAddresses() {
    List<String> ret = new ArrayList<>();
    ret.addAll(Arrays.asList(getCellNumString().split(";")));
    return ret;
  }

  @Override
  public boolean shouldCheckPAXWebsite() {
    return JCBCheckWebsite.isSelected();
  }

  @Override
  public boolean shouldCheckShowclix() {
    return JCBCheckShowclix.isSelected();
  }

  @Override
  public boolean shouldCheckKnownEvents() {
    return JCBCheckKnownEvents.isSelected();
  }

  @Override
  public boolean shouldCheckTwitter() {
    return JCBCheckTwitter.isSelected();
  }

  @Override
  public boolean shouldFilterTwitter() {
    return JCBFilterTwitter.isSelected();
  }

  @Override
  public boolean shouldPlayAlarm() {
    return JCBPlayAlarm.isSelected();
  }

  @Override
  public int timeBetweenChecks() {
    return JSCheckTime.getValue();
  }

  @Override
  public String getExpoToCheck() {
    return JCBExpo.getSelectedItem().toString();
  }

  @Override
  public String getTwitterConsumerKey() {
    return JTFConsumerKey.getText();
  }

  @Override
  public String getTwitterConsumerSecret() {
    return JTFConsumerSecret.getText();
  }

  @Override
  public String getTwitterApplicationKey() {
    return JTFApplicationKey.getText();
  }

  @Override
  public String getTwitterApplicationSecret() {
    return JTFApplicationSecret.getText();
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
    JCBFilterTwitter = new javax.swing.JCheckBox();
    JCBCheckKnownEvents = new javax.swing.JCheckBox();
    JPTwitterKeys = new javax.swing.JPanel();
    jLabel7 = new javax.swing.JLabel();
    jLabel8 = new javax.swing.JLabel();
    jLabel9 = new javax.swing.JLabel();
    jLabel10 = new javax.swing.JLabel();
    JTFApplicationSecret = new javax.swing.JTextField();
    JTFApplicationKey = new javax.swing.JTextField();
    JTFConsumerSecret = new javax.swing.JTextField();
    JTFConsumerKey = new javax.swing.JTextField();
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
    JCBSaveEmailSettings = new javax.swing.JCheckBox();
    JCBSaveCheckSettings = new javax.swing.JCheckBox();
    jScrollPane1 = new javax.swing.JScrollPane();
    jTextArea1 = new javax.swing.JTextArea();
    JCBUseBeta = new javax.swing.JCheckBox();
    JBSaveSettings = new javax.swing.JButton();
    JCBLoadNotifications = new javax.swing.JCheckBox();
    JCBCheckUpdates = new javax.swing.JCheckBox();
    JCBSaveTwitterKeys = new javax.swing.JCheckBox();
    JCBStatistics = new javax.swing.JCheckBox();
    JCBCheckUpdateDaily = new javax.swing.JCheckBox();

    setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
    setTitle("PAX Checker Setup");
    setResizable(false);

    jLabel1.setFont(new java.awt.Font("Tahoma", 0, 36)); // NOI18N
    jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    jLabel1.setText("PAXChecker Setup");

    jLabel2.setText("Email");

    JTFEmail.setToolTipText("<html>\nEmail address to send texts with. Currently,<br>\nonly Yahoo! and Gmail are supported.<br>\nPut your full email addres in, otherwise it<br>\ndefaults to @yahoo.com.<br>\nSee the Instructions tab for how to use emails<br>\nother than Yahoo! or Gmail.\n</html>");

    jLabel3.setText("Password");

    jLabel4.setText("Cell Num");

    JTFCellNum.setToolTipText("<html>\nSpecify the number you want to receive texts at.<br>\nOnly put your number - no spaces, no leading 1.<br>\nYou may use dashes -- or perentheses ().<br>\nIf you use a different carrier, you may find their<br>\ntexting email address extension at<br>\nwww.emailtextmessages.com and put it onto the<br>\nend of your number.<br>\nExamples:<br>\n(123)-456-7890 [Verizon selected in dropdown box]<br>\n1234567890@car.rier.net<br>\n123-4567890@car.rier.net<br>\n</html>");

    JCBCarrier.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "AT&T (MMS)", "AT&T (SMS)", "Verizon", "Sprint", "T-Mobile", "U.S. Cellular", "Bell", "Rogers", "Fido", "Koodo", "Telus", "Virgin (CAN)", "Wind", "Sasktel" }));

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
    JCBCheckShowclix.setToolTipText("<html>\nIt is highly recommended that you<br>\nuse this option. It scans the Showclix<br>\nwebsite for updates and is generally the<br>\nfastest possible.<br>\nThis option uses a decent amount of data.\n</html>");
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

    JCBFilterTwitter.setText("Filter by Keywords");
    JCBFilterTwitter.setToolTipText("<html>\nFilters out Tweets that do not contains specific<br>\nkeywords. This limits the amount of links opened<br>\nby the PAXChecker, and may in fact prevent the<br>\nPAXChecker from opening the ticket sale page if<br>\nPAX's Tweet doesn't contain specific words.<br>\nThe use of this is NOT recommended.\n</html>");

    JCBCheckKnownEvents.setText("Scan Known Potential Showclix Events");
    JCBCheckKnownEvents.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        JCBCheckKnownEventsActionPerformed(evt);
      }
    });

    jLabel7.setText("Consumer Key");

    jLabel8.setText("Consumer Secret");

    jLabel9.setText("Application Key");

    jLabel10.setText("Application Secret");

    javax.swing.GroupLayout JPTwitterKeysLayout = new javax.swing.GroupLayout(JPTwitterKeys);
    JPTwitterKeys.setLayout(JPTwitterKeysLayout);
    JPTwitterKeysLayout.setHorizontalGroup(
      JPTwitterKeysLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(JPTwitterKeysLayout.createSequentialGroup()
        .addContainerGap()
        .addGroup(JPTwitterKeysLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addGroup(JPTwitterKeysLayout.createSequentialGroup()
            .addComponent(jLabel10)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
            .addComponent(JTFApplicationSecret))
          .addGroup(JPTwitterKeysLayout.createSequentialGroup()
            .addComponent(jLabel7)
            .addGap(27, 27, 27)
            .addComponent(JTFConsumerKey))
          .addGroup(JPTwitterKeysLayout.createSequentialGroup()
            .addComponent(jLabel8)
            .addGap(14, 14, 14)
            .addComponent(JTFConsumerSecret))
          .addGroup(JPTwitterKeysLayout.createSequentialGroup()
            .addComponent(jLabel9)
            .addGap(23, 23, 23)
            .addComponent(JTFApplicationKey))))
    );
    JPTwitterKeysLayout.setVerticalGroup(
      JPTwitterKeysLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(JPTwitterKeysLayout.createSequentialGroup()
        .addGap(0, 0, 0)
        .addGroup(JPTwitterKeysLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(jLabel7)
          .addComponent(JTFConsumerKey, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(JPTwitterKeysLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(jLabel8)
          .addComponent(JTFConsumerSecret, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(JPTwitterKeysLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(jLabel9)
          .addComponent(JTFApplicationKey, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(JPTwitterKeysLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(jLabel10)
          .addComponent(JTFApplicationSecret, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addGap(0, 0, Short.MAX_VALUE))
    );

    javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
    jPanel1.setLayout(jPanel1Layout);
    jPanel1Layout.setHorizontalGroup(
      jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(jPanel1Layout.createSequentialGroup()
        .addContainerGap()
        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
          .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
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
                .addComponent(JCBCarrier, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
          .addComponent(JPPhonePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
          .addComponent(JBAddPhone, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
          .addComponent(JBStart, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
          .addComponent(JSCheckTime, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
          .addComponent(jLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
          .addGroup(jPanel1Layout.createSequentialGroup()
            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
              .addComponent(JCBCheckKnownEvents)
              .addComponent(JCBCheckWebsite)
              .addComponent(JCBCheckShowclix)
              .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                .addGroup(jPanel1Layout.createSequentialGroup()
                  .addComponent(jLabel5)
                  .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                  .addComponent(JCBExpo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addComponent(JCBPlayAlarm))
              .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(JCBCheckTwitter)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(JLTwitterDisabled)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(JCBFilterTwitter)))
            .addGap(0, 2, Short.MAX_VALUE))
          .addComponent(JPTwitterKeys, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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
        .addComponent(JCBCheckKnownEvents)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(JCBCheckTwitter)
          .addComponent(JLTwitterDisabled)
          .addComponent(JCBFilterTwitter))
        .addGap(0, 0, 0)
        .addComponent(JPTwitterKeys, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addGap(0, 0, 0)
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
        .addContainerGap())
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
      .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 504, Short.MAX_VALUE)
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
      .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 504, Short.MAX_VALUE)
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
      .addComponent(jScrollPane6, javax.swing.GroupLayout.DEFAULT_SIZE, 504, Short.MAX_VALUE)
    );

    jTabbedPane1.addTab("Extra", jPanel3);

    JCBSaveEmailSettings.setText("Save Email Settings");

    JCBSaveCheckSettings.setText("Save Check Settings");
    JCBSaveCheckSettings.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        JCBSaveCheckSettingsActionPerformed(evt);
      }
    });

    jTextArea1.setEditable(false);
    jTextArea1.setColumns(20);
    jTextArea1.setLineWrap(true);
    jTextArea1.setRows(5);
    jTextArea1.setText("Preferences are automatically saved after you start the program. You may choose which preferences you want saved. If you do not save a preference, it loads in its default state.\n\nNote that your password is NEVER saved using Preferences.\n\nAnonymous statistics are collected whenever you load Patch Notes (every time the program is first run), load notifications, or download new versions. For information on this, please see the Orangedox Privacy Policy: https://dropbox.orangedox.com/terms/#privacy");
    jTextArea1.setWrapStyleWord(true);
    jTextArea1.setCaretPosition(0);
    jScrollPane1.setViewportView(jTextArea1);

    JCBUseBeta.setText("Use BETA Versions");
    JCBUseBeta.setToolTipText("<html>\nBETA versions will most likely<br>\ninclude features that could, at any<br>\npoint in time, break the program<br>\nor parts of it. These versions will<br>\ncontain ideas that still need to be<br>\ndebugged, so any help in finding<br>\nthese is greatly appreciated.<br>\nChange information can be found<br>\nin the Patch Notes, and more<br>\ndetailed changes can be found in<br>\nthe GitHub commits.<br>\n<br>\nUse at your own risk.\n</html>");

    JBSaveSettings.setText("Save Settings");
    JBSaveSettings.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        JBSaveSettingsActionPerformed(evt);
      }
    });

    JCBLoadNotifications.setSelected(true);
    JCBLoadNotifications.setText("Load Notificaitons");

    JCBCheckUpdates.setSelected(true);
    JCBCheckUpdates.setText("Check for Updates");
    JCBCheckUpdates.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        JCBCheckUpdatesActionPerformed(evt);
      }
    });

    JCBSaveTwitterKeys.setText("Save Encrypted Twitter Keys");
    JCBSaveTwitterKeys.setToolTipText("<html>\nNOTE: This saves your Twitter API<br>\nkeys in an encrypted format. Your<br>\nkeys will still be obtainable if you or<br>\nsomeone else has access to this<br>\nprogram's source code (which is<br>\npublicly available). Save at your<br>\nown risk!\n</html>");

    JCBStatistics.setText("Anonymous Statistics");
    JCBStatistics.setToolTipText("<html>\nWhen enabled, the program goes through Orangedox<br>\nto collect non-personally identifiable statistics about<br>\nfile downloads, such as the date downloaded and<br>\nthe specific file downloaded.<br>\nNOTE that \"Anonymous Statistics\" only means going<br>\nthrough Dropbox instead of Orangedox. Dropbox's<br>\nPrivacy Policy applies to you, regardless of whether<br>\nor not this option is enabled.\n</html>");

    JCBCheckUpdateDaily.setText("Every 24 hours");
    JCBCheckUpdateDaily.setEnabled(false);

    javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
    jPanel5.setLayout(jPanel5Layout);
    jPanel5Layout.setHorizontalGroup(
      jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(jPanel5Layout.createSequentialGroup()
        .addContainerGap()
        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 391, Short.MAX_VALUE)
          .addGroup(jPanel5Layout.createSequentialGroup()
            .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
              .addComponent(JCBSaveCheckSettings)
              .addComponent(JCBSaveEmailSettings)
              .addGroup(jPanel5Layout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addComponent(JCBCheckUpdateDaily))
              .addComponent(JCBSaveTwitterKeys)
              .addComponent(JCBUseBeta)
              .addComponent(JCBStatistics)
              .addComponent(JCBLoadNotifications)
              .addComponent(JCBCheckUpdates))
            .addGap(0, 0, Short.MAX_VALUE))
          .addComponent(JBSaveSettings, javax.swing.GroupLayout.DEFAULT_SIZE, 391, Short.MAX_VALUE))
        .addContainerGap())
    );
    jPanel5Layout.setVerticalGroup(
      jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(jPanel5Layout.createSequentialGroup()
        .addContainerGap()
        .addComponent(JCBSaveEmailSettings)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(JCBSaveCheckSettings)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(JCBLoadNotifications)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(JCBCheckUpdates)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(JCBCheckUpdateDaily)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(JCBSaveTwitterKeys, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(JCBUseBeta)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(JCBStatistics)
        .addGap(43, 43, 43)
        .addComponent(JBSaveSettings)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 230, Short.MAX_VALUE)
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
      .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 532, Short.MAX_VALUE)
    );

    pack();
  }// </editor-fold>//GEN-END:initComponents

  private void JCBCheckShowclixActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_JCBCheckShowclixActionPerformed
    updateElements();
  }//GEN-LAST:event_JCBCheckShowclixActionPerformed

  private void JCBCheckWebsiteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_JCBCheckWebsiteActionPerformed
    updateElements();
  }//GEN-LAST:event_JCBCheckWebsiteActionPerformed

  private void JBStartActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_JBStartActionPerformed
    // TODO add your handling code here
    dispose();
    isOpen = false;
  }//GEN-LAST:event_JBStartActionPerformed

  private void JCBSaveCheckSettingsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_JCBSaveCheckSettingsActionPerformed
    // TODO add your handling code here:
  }//GEN-LAST:event_JCBSaveCheckSettingsActionPerformed

  private void JBAddPhoneActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_JBAddPhoneActionPerformed
    // TODO add your handling code here:
    addPhonePanel(new ExtraPhonePanel(this));
  }//GEN-LAST:event_JBAddPhoneActionPerformed

  private void JBSaveSettingsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_JBSaveSettingsActionPerformed
    savePreferences();
  }//GEN-LAST:event_JBSaveSettingsActionPerformed

  private void JCBCheckTwitterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_JCBCheckTwitterActionPerformed
    // TODO add your handling code here:
    JCBFilterTwitter.setEnabled(JCBCheckTwitter.isSelected());
    updateElements();
  }//GEN-LAST:event_JCBCheckTwitterActionPerformed

  private void JLTwitterDisabledMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_JLTwitterDisabledMousePressed
    // TODO add your handling code here:
    twitterDisabledWindow.setVisible(true);
  }//GEN-LAST:event_JLTwitterDisabledMousePressed

  private void JCBCheckUpdatesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_JCBCheckUpdatesActionPerformed
    // TODO add your handling code here:
    //JCBCheckUpdateDaily.setEnabled(JCBCheckUpdates.isSelected());
  }//GEN-LAST:event_JCBCheckUpdatesActionPerformed

  private void JCBCheckKnownEventsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_JCBCheckKnownEventsActionPerformed
    // TODO add your handling code here:
    updateElements();
  }//GEN-LAST:event_JCBCheckKnownEventsActionPerformed

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JButton JBAddPhone;
  private javax.swing.JButton JBSaveSettings;
  private javax.swing.JButton JBStart;
  private javax.swing.JComboBox JCBCarrier;
  private javax.swing.JCheckBox JCBCheckKnownEvents;
  private javax.swing.JCheckBox JCBCheckShowclix;
  private javax.swing.JCheckBox JCBCheckTwitter;
  private javax.swing.JCheckBox JCBCheckUpdateDaily;
  private javax.swing.JCheckBox JCBCheckUpdates;
  private javax.swing.JCheckBox JCBCheckWebsite;
  private javax.swing.JComboBox JCBExpo;
  private javax.swing.JCheckBox JCBFilterTwitter;
  private javax.swing.JCheckBox JCBLoadNotifications;
  private javax.swing.JCheckBox JCBPlayAlarm;
  private javax.swing.JCheckBox JCBSaveCheckSettings;
  private javax.swing.JCheckBox JCBSaveEmailSettings;
  private javax.swing.JCheckBox JCBSaveTwitterKeys;
  private javax.swing.JCheckBox JCBStatistics;
  private javax.swing.JCheckBox JCBUseBeta;
  private javax.swing.JLabel JLTwitterDisabled;
  private javax.swing.JPasswordField JPFPassword;
  private javax.swing.JPanel JPPhonePanel;
  private javax.swing.JPanel JPTwitterKeys;
  private javax.swing.JSlider JSCheckTime;
  private javax.swing.JTextField JTFApplicationKey;
  private javax.swing.JTextField JTFApplicationSecret;
  private javax.swing.JTextField JTFCellNum;
  private javax.swing.JTextField JTFConsumerKey;
  private javax.swing.JTextField JTFConsumerSecret;
  private javax.swing.JTextField JTFEmail;
  private javax.swing.JTextPane JTPExtra;
  private javax.swing.JTextPane JTPInstructions;
  private javax.swing.JLabel jLabel1;
  private javax.swing.JLabel jLabel10;
  private javax.swing.JLabel jLabel2;
  private javax.swing.JLabel jLabel3;
  private javax.swing.JLabel jLabel4;
  private javax.swing.JLabel jLabel5;
  private javax.swing.JLabel jLabel6;
  private javax.swing.JLabel jLabel7;
  private javax.swing.JLabel jLabel8;
  private javax.swing.JLabel jLabel9;
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
