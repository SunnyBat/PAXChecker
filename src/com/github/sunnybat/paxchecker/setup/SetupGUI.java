package com.github.sunnybat.paxchecker.setup;

import com.github.sunnybat.commoncode.email.EmailAddress;
import com.github.sunnybat.commoncode.preferences.PreferenceHandler;
import com.github.sunnybat.commoncode.utilities.Encryption;
import com.github.sunnybat.paxchecker.PAXChecker;
import com.github.sunnybat.paxchecker.browser.Browser;
import com.github.sunnybat.paxchecker.notification.NotificationWindow;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Creates a new Setup GUI for the user to configure the program at will. Saves Preferences independent of the rest of the program for quick loading
 * afterwards.
 *
 * @author SunnyBat
 */
public class SetupGUI extends com.github.sunnybat.commoncode.javax.swing.JFrame implements Setup {

  private java.util.ArrayList<ExtraPhonePanel> extraPhonePanelList;
  private NotificationWindow twitterDisabledWindow;
  private PreferenceHandler prefs;
  private boolean isOpen = true;

  /**
   * Creates new form Setup
   */
  public SetupGUI() {
    extraPhonePanelList = new java.util.ArrayList<>();
    twitterDisabledWindow = new NotificationWindow("API Keys", "The Twitter API requires API keys to access it. Unfortunately, putting API keys in "
        + "an open-source project runs the risk of having the keys disabled due to malicious people. As such, you must use your own Twitter API "
        + "keys. For more information, press the More Info button.\n\nIt's recommended to check the \"Save Encrypted Twitter Keys\" checkbox in the "
        + "Preferences tab if you do not want to enter them into the program every time.\nNOTE: You should only save your Twitter keys on a private "
        + "and trusted computer!");
    twitterDisabledWindow.setMoreInfoButtonLink("https://redd.it/2nct50");
    prefs = new PreferenceHandler("paxchecker");
    invokeAndWaitOnEDT(new Runnable() {
      @Override
      public void run() {
        initComponents();
        customComponents();
      }
    });
  }

  /**
   * Creates new form Setup. Note that twitterKeys must be a non-null String array with exactly four elements.
   *
   * @param twitterKeys
   */
  public SetupGUI(final String[] twitterKeys) {
    this();
    invokeAndWaitOnEDT(new Runnable() {
      @Override
      public void run() {
        if (twitterKeys != null && twitterKeys.length == 4) {
          if (twitterKeys[0] != null) { // In case these are loaded from Preferences, we don't want to overwrite them
            JTFConsumerKey.setText(twitterKeys[0]);
          }
          if (twitterKeys[1] != null) {
            JTFConsumerSecret.setText(twitterKeys[1]);
          }
          if (twitterKeys[2] != null) {
            JTFApplicationKey.setText(twitterKeys[2]);
          }
          if (twitterKeys[3] != null) {
            JTFApplicationSecret.setText(twitterKeys[3]);
          }
        }
      }
    });
  }

  private void customComponents() {
    setTitle("Setup :: PAXChecker v" + PAXChecker.VERSION);
    JTPExtra.setText(loadHtml("/com/github/sunnybat/paxchecker/setup/Extra.html"));
    JTPExtra.setCaretPosition(0);
    JTPInstructions.setText(loadHtml("/com/github/sunnybat/paxchecker/setup/Instructions.html"));
    JTPInstructions.setCaretPosition(0);
    // Preferences Tab
    JCBSaveEmailSettings.setSelected(prefs.getBooleanPreference("SAVE_EMAIL_SETTINGS"));
    JCBSaveCheckSettings.setSelected(prefs.getBooleanPreference("SAVE_CHECK_SETTINGS", true));
    JCBUseBeta.setSelected(prefs.getBooleanPreference("USE_BETA"));
    JCBLoadNotifications.setSelected(prefs.getBooleanPreference("LOAD_NOTIFICATIONS", true));
    JCBCheckUpdates.setSelected(prefs.getBooleanPreference("LOAD_UPDATES", true));
//    if (JCBCheckUpdates.isSelected()) {
//      JCBCheckUpdateDaily.setSelected(prefs.getBooleanPreference("DAILY_UPDATES"));
//    } else {
//      JCBCheckUpdateDaily.setEnabled(false);
//    }
    JCBSaveTwitterKeys.setSelected(prefs.getBooleanPreference("SAVE_TWITTER_KEYS"));
    JCBStatistics.setSelected(prefs.getBooleanPreference("ANONYMOUS_STATISTICS"));

    // Main Settings Window
    String cellNum = prefs.getStringPreference("CELLNUM");
    if (cellNum != null) {
      System.out.println("Total: " + cellNum);
      if (cellNum.contains(";")) {
        String[] split = cellNum.split(";");
        boolean first = false;
        for (String s : split) {
          if (!s.contains("@") || s.endsWith("@")) {
            System.out.println("Invalid email: " + s);
            continue;
          }
          s = s.trim();
          if (!first) {
            JCBCarrier.setSelectedIndex(getIndexOfProvider(EmailAddress.getProvider(s.substring(s.indexOf("@") + 1))));
            if (JCBCarrier.getSelectedItem().equals("[Other]")) {
              JTFCellNum.setText(s);
            } else {
              JTFCellNum.setText(s.substring(0, s.indexOf("@")));
            }
            first = true;
          } else {
            ExtraPhonePanel p = new ExtraPhonePanel(this, s.substring(0, s.indexOf("@")), s.substring(s.indexOf("@") + 1));
            addPhonePanel(p);
          }
        }
      } else {
        JTFCellNum.setText(cellNum);
      }
    }
    JTFEmail.setText(prefs.getStringPreference("EMAIL"));
    JCBCheckWebsite.setSelected(prefs.getBooleanPreference("CHECK_PAX", true));
    JCBCheckShowclix.setSelected(prefs.getBooleanPreference("CHECK_SHOWCLIX", true));
    JCBCheckTwitter.setSelected(prefs.getBooleanPreference("CHECK_TWITTER"));
    checkTwitterAction(JCBCheckTwitter.isSelected());
    //JCBCheckKnownEvents.setSelected(prefs.getBooleanPreference("CHECK_KNOWN_EVENTS")); // TODO: Enable this when new known events are found
    JCBFilterTwitter.setSelected(prefs.getBooleanPreference("FILTER_TWITTER") && JCBCheckTwitter.isSelected());
    JCBTextTweets.setSelected(prefs.getBooleanPreference("TEXT_TWEETS") && JCBCheckTwitter.isSelected());
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
    updateStart();
  }

  public void addPhonePanel(final ExtraPhonePanel panel) {
    invokeAndWaitOnEDT(new Runnable() {
      @Override
      public void run() {
        extraPhonePanelList.add(panel);
        JPPhonePanel.add(panel);
        JPPhonePanel.revalidate();
        pack();
      }
    });
  }

  public void removePhonePanel(final ExtraPhonePanel panel) {
    invokeAndWaitOnEDT(new Runnable() {
      @Override
      public void run() {
        extraPhonePanelList.remove(panel);
        JPPhonePanel.remove(panel);
        JPPhonePanel.revalidate();
        pack();
      }
    });
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

  private void checkTwitterAction(boolean checking) {
    if (JPTwitterKeys.isVisible() != checking) { // Prevent duplicate calls improperly resizing GUI
      JCBFilterTwitter.setEnabled(JCBCheckTwitter.isSelected());
      JCBTextTweets.setEnabled(JCBCheckTwitter.isSelected());
      JPTwitterKeys.setVisible(checking);
      setSize(getWidth(), getHeight() + JPTwitterKeys.getPreferredSize().height * (checking ? 1 : -1));
    }
  }

  private void savePreferences() {
    prefs.getPreferenceObject("SAVE_EMAIL_SETTINGS").setValue(JCBSaveEmailSettings.isSelected());
    prefs.getPreferenceObject("SAVE_CHECK_SETTINGS").setValue(JCBSaveCheckSettings.isSelected());
    prefs.getPreferenceObject("LOAD_NOTIFICATIONS").setValue(JCBLoadNotifications.isSelected());
    prefs.getPreferenceObject("LOAD_UPDATES").setValue(JCBCheckUpdates.isSelected());
    prefs.getPreferenceObject("DAILY_UPDATES").setShouldSave(JCBCheckUpdates.isSelected()); // If update checking is disabled, don't check every 24 hours
    prefs.getPreferenceObject("DAILY_UPDATES").setValue(JCBCheckUpdateDaily.isSelected());
    prefs.getPreferenceObject("SAVE_TWITTER_KEYS").setValue(JCBSaveTwitterKeys.isSelected());
    prefs.getPreferenceObject("USE_BETA").setValue(JCBUseBeta.isSelected());
    prefs.getPreferenceObject("ANONYMOUS_STATISTICS").setValue(JCBStatistics.isSelected());
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
    prefs.getPreferenceObject("FILTER_TWITTER").setValue(JCBFilterTwitter.isSelected() && JCBCheckTwitter.isSelected());
    prefs.getPreferenceObject("TEXT_TWEETS").setShouldSave(JCBSaveCheckSettings.isSelected());
    prefs.getPreferenceObject("TEXT_TWEETS").setValue(JCBTextTweets.isSelected() && JCBCheckTwitter.isSelected());
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

  private String getCellNumString() {
    String text = JTFCellNum.getText();
    text = formatEmails(text, EmailAddress.getCarrierExtension(JCBCarrier.getSelectedItem().toString()));
    for (ExtraPhonePanel panel : extraPhonePanelList) {
      text += ";" + formatEmails(panel.getNumber(), EmailAddress.getCarrierExtension(panel.getProvider()));
    }
    System.out.println("Old = " + text);
    text = text.replaceAll("(;)\\1+", ";"); // Replace multiple ;'s in a row with just one ;
    text = text.replaceAll("^;|$;", ""); // Remove ; at beginning or end
    System.out.println("New = " + text);
    return text;
  }

  /**
   * Formats the given email into a String to save to the Preferences. Can handle multiple emails separated with ';'. If toParse or ending are null or
   * an empty String, this method returns an empty String.
   *
   * @param toParse Email to parse
   * @param ending Email ending, with or without @
   * @return The formatted email to save to Preferences, potentially with ; clumped together or at the beginning or end (but not necessarily)
   */
  private String formatEmails(String toParse, String ending) {
    if (toParse == null || toParse.length() == 0 || ending == null || ending.length() == 0) {
      return "";
    }
    String toReturn = "";
    String[] splitEmails = toParse.split(";");
    for (String email : splitEmails) {
      email = email.trim();
      if (email.length() > 0) {
        if (!email.contains("@")) {
          email += ending;
        }
        toReturn += email + ";";
      }
    }
    return toReturn;
  }

  private void updateStart() {
    JSCheckTime.setEnabled(JCBCheckWebsite.isSelected() || JCBCheckShowclix.isSelected() || JCBCheckKnownEvents.isSelected());
    JLSecondsBetweenChecks.setEnabled(JCBCheckWebsite.isSelected() || JCBCheckShowclix.isSelected() || JCBCheckKnownEvents.isSelected());
    JBStart.setEnabled(JCBCheckWebsite.isSelected() || JCBCheckShowclix.isSelected() || twitterSetUpCorrectly() || JCBCheckKnownEvents.isSelected());
  }

  private boolean twitterSetUpCorrectly() {
    return JCBCheckTwitter.isSelected() && JTFApplicationKey.getText().length() > 10 && JTFApplicationSecret.getText().length() > 10
        && JTFConsumerKey.getText().length() > 10 && JTFConsumerSecret.getText().length() > 10;
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
    return JCBCheckTwitter.isSelected() && twitterSetUpCorrectly();
  }

  @Override
  public boolean shouldFilterTwitter() {
    return JCBFilterTwitter.isSelected();
  }

  @Override
  public boolean shouldTextTweets() {
    return JCBTextTweets.isSelected();
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
  public boolean shouldFilterShowclix() {
    return JCBFilterShowclix.isSelected();
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
    JLSecondsBetweenChecks = new javax.swing.JLabel();
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
    filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 32767));
    jLabel6 = new javax.swing.JLabel();
    JCBFilterShowclix = new javax.swing.JCheckBox();
    JCBTextTweets = new javax.swing.JCheckBox();
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
    setTitle("PAXChecker Setup");
    setResizable(false);

    jLabel1.setFont(new java.awt.Font("Tahoma", 0, 36)); // NOI18N
    jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    jLabel1.setText("PAXChecker Setup");

    jLabel2.setText("Email");

    JTFEmail.setToolTipText("<html>\nEmail address to send texts with. Currently,<br>\nonly Yahoo! and Gmail are supported.<br>\nPut your full email addres in, otherwise it<br>\ndefaults to @yahoo.com.<br>\nSee the Instructions tab for how to use emails<br>\nother than Yahoo! or Gmail.\n</html>");

    jLabel3.setText("Password");

    jLabel4.setText("Cell Num");

    JTFCellNum.setToolTipText("<html>\nSpecify the number you want to receive texts at.<br>\nOnly put your number - no spaces, no leading 1.<br>\nYou may use dashes -- or perentheses ().<br>\nIf you use a different carrier, you may find their<br>\ntexting email address extension at<br>\nwww.emailtextmessages.com and put it onto the<br>\nend of your number.<br>\nExamples:<br>\n(123)-456-7890 [Verizon selected in dropdown box]<br>\n1234567890@car.rier.net<br>\n123-4567890@car.rier.net<br>\n</html>");

    JCBCarrier.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "AT&T (MMS)", "AT&T (SMS)", "Verizon", "Sprint", "T-Mobile", "U.S. Cellular", "Bell", "Rogers", "Fido", "Koodo", "Telus", "Virgin (CAN)", "Wind", "Sasktel", "[Other]" }));

    JLSecondsBetweenChecks.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    JLSecondsBetweenChecks.setText("Seconds Between Website Checks");

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

    JCBFilterTwitter.setText("Filter by Keywords");
    JCBFilterTwitter.setToolTipText("<html>\nFilters out Tweets that do not contains specific<br>\nkeywords. This limits the amount of links opened<br>\nby the PAXChecker, and may in fact prevent the<br>\nPAXChecker from opening the ticket sale page if<br>\nPAX's Tweet doesn't contain specific words.<br>\nThe use of this is NOT recommended.\n</html>");

    JCBCheckKnownEvents.setText("Scan Known Potential Showclix Events");
    JCBCheckKnownEvents.setEnabled(false);
    JCBCheckKnownEvents.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        JCBCheckKnownEventsActionPerformed(evt);
      }
    });

    jLabel7.setText("Consumer Key");

    jLabel8.setText("Consumer Secret");

    jLabel9.setText("Application Key");

    jLabel10.setText("Application Secret");

    jLabel6.setFont(new java.awt.Font("Tahoma", 2, 11)); // NOI18N
    jLabel6.setForeground(new java.awt.Color(0, 0, 238));
    jLabel6.setText("(How do I Get These?)");
    jLabel6.addMouseListener(new java.awt.event.MouseAdapter() {
      public void mouseClicked(java.awt.event.MouseEvent evt) {
        jLabel6MouseClicked(evt);
      }
    });

    javax.swing.GroupLayout JPTwitterKeysLayout = new javax.swing.GroupLayout(JPTwitterKeys);
    JPTwitterKeys.setLayout(JPTwitterKeysLayout);
    JPTwitterKeysLayout.setHorizontalGroup(
      JPTwitterKeysLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(JPTwitterKeysLayout.createSequentialGroup()
        .addContainerGap()
        .addGroup(JPTwitterKeysLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addGroup(JPTwitterKeysLayout.createSequentialGroup()
            .addGroup(JPTwitterKeysLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
              .addComponent(jLabel10)
              .addComponent(jLabel7)
              .addComponent(jLabel8)
              .addComponent(jLabel9))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(filler1, javax.swing.GroupLayout.PREFERRED_SIZE, 6, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(JPTwitterKeysLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
              .addComponent(JTFConsumerKey, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 280, Short.MAX_VALUE)
              .addComponent(JTFConsumerSecret, javax.swing.GroupLayout.Alignment.TRAILING)
              .addComponent(JTFApplicationKey)
              .addComponent(JTFApplicationSecret, javax.swing.GroupLayout.Alignment.TRAILING)))
          .addGroup(JPTwitterKeysLayout.createSequentialGroup()
            .addGap(0, 0, Short.MAX_VALUE)
            .addComponent(jLabel6))))
    );
    JPTwitterKeysLayout.setVerticalGroup(
      JPTwitterKeysLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(JPTwitterKeysLayout.createSequentialGroup()
        .addGroup(JPTwitterKeysLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addGroup(JPTwitterKeysLayout.createSequentialGroup()
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
              .addComponent(JTFApplicationSecret, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
          .addComponent(filler1, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addGap(0, 0, Short.MAX_VALUE)
        .addComponent(jLabel6))
    );

    JCBFilterShowclix.setText("Strict Filtering");
    JCBFilterShowclix.setToolTipText("<html>\nEnabling this will hopefully reduce the<br>\namount of false positives, however<br>\nmight also cause the PAXChecker to<br>\nmiss the queue. Use at your own risk.\n</html>");

    JCBTextTweets.setText("Text Tweets");
    JCBTextTweets.setToolTipText("<html>\nSend a text to the given email address<br>\nif a link is found in a Tweet.If you receive<br>\nTweets directly from Twitter, this option<br>\nwill likely be redundant (and therefore not<br>\nrecommended).\n</html>");

    javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
    jPanel1.setLayout(jPanel1Layout);
    jPanel1Layout.setHorizontalGroup(
      jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(jPanel1Layout.createSequentialGroup()
        .addContainerGap()
        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
          .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 392, Short.MAX_VALUE)
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
          .addComponent(JLSecondsBetweenChecks, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
          .addComponent(JPTwitterKeys, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
          .addGroup(jPanel1Layout.createSequentialGroup()
            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
              .addComponent(JCBCheckKnownEvents)
              .addComponent(JCBCheckWebsite)
              .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(JCBCheckShowclix)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(JCBFilterShowclix))
              .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                .addGroup(jPanel1Layout.createSequentialGroup()
                  .addComponent(jLabel5)
                  .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                  .addComponent(JCBExpo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addComponent(JCBPlayAlarm))
              .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(JCBCheckTwitter)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(JCBFilterTwitter)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(JCBTextTweets)))
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
        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(JCBCheckShowclix)
          .addComponent(JCBFilterShowclix))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(JCBCheckKnownEvents)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(JCBCheckTwitter)
          .addComponent(JCBFilterTwitter)
          .addComponent(JCBTextTweets))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(JPTwitterKeys, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(JCBPlayAlarm)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(jLabel5)
          .addComponent(JCBExpo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(JLSecondsBetweenChecks)
        .addGap(0, 0, 0)
        .addComponent(JSCheckTime, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(JBStart)
        .addGap(0, 0, 0))
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
      .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 412, Short.MAX_VALUE)
    );
    jPanel6Layout.setVerticalGroup(
      jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 517, Short.MAX_VALUE)
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
      .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 412, Short.MAX_VALUE)
    );
    jPanel4Layout.setVerticalGroup(
      jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 517, Short.MAX_VALUE)
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
      .addComponent(jScrollPane6, javax.swing.GroupLayout.DEFAULT_SIZE, 412, Short.MAX_VALUE)
    );
    jPanel3Layout.setVerticalGroup(
      jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addComponent(jScrollPane6, javax.swing.GroupLayout.DEFAULT_SIZE, 517, Short.MAX_VALUE)
    );

    jTabbedPane1.addTab("Extra", jPanel3);

    JCBSaveEmailSettings.setText("Save Email Settings");

    JCBSaveCheckSettings.setSelected(true);
    JCBSaveCheckSettings.setText("Save Check Settings");

    jTextArea1.setEditable(false);
    jTextArea1.setColumns(20);
    jTextArea1.setLineWrap(true);
    jTextArea1.setRows(5);
    jTextArea1.setText("Preferences are automatically saved after you start the program. You may choose which preferences you want saved. If you do not save a preference, it loads in its default state.\n\nNote that your password is NEVER saved using Preferences.\n\nAnonymous statistics are collected whenever you load Patch Notes (every time the program is first run), load notifications, or download new versions. This is purely for me to see how many people are using the program. You may opt out of this by selecting the \"Anonymous Statistics\" checkbox, at which point all downloads will be directly between you and Dropbox. For information on this, please see the Orangedox Privacy Policy: https://dropbox.orangedox.com/terms/#privacy");
    jTextArea1.setWrapStyleWord(true);
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
          .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 392, Short.MAX_VALUE)
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
          .addComponent(JBSaveSettings, javax.swing.GroupLayout.DEFAULT_SIZE, 392, Short.MAX_VALUE))
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
        .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 243, Short.MAX_VALUE)
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
      .addComponent(jTabbedPane1)
    );

    pack();
  }// </editor-fold>//GEN-END:initComponents

  private void JCBCheckShowclixActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_JCBCheckShowclixActionPerformed
    updateStart();
  }//GEN-LAST:event_JCBCheckShowclixActionPerformed

  private void JCBCheckWebsiteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_JCBCheckWebsiteActionPerformed
    updateStart();
  }//GEN-LAST:event_JCBCheckWebsiteActionPerformed

  private void JBStartActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_JBStartActionPerformed
    dispose();
    savePreferences();
    isOpen = false;
  }//GEN-LAST:event_JBStartActionPerformed

  private void JBAddPhoneActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_JBAddPhoneActionPerformed
    addPhonePanel(new ExtraPhonePanel(this));
  }//GEN-LAST:event_JBAddPhoneActionPerformed

  private void JBSaveSettingsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_JBSaveSettingsActionPerformed
    savePreferences();
  }//GEN-LAST:event_JBSaveSettingsActionPerformed

  private void JCBCheckTwitterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_JCBCheckTwitterActionPerformed
    checkTwitterAction(JCBCheckTwitter.isSelected());
    updateStart();
  }//GEN-LAST:event_JCBCheckTwitterActionPerformed

  private void JCBCheckUpdatesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_JCBCheckUpdatesActionPerformed
    //JCBCheckUpdateDaily.setEnabled(JCBCheckUpdates.isSelected());
  }//GEN-LAST:event_JCBCheckUpdatesActionPerformed

  private void JCBCheckKnownEventsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_JCBCheckKnownEventsActionPerformed
    updateStart();
  }//GEN-LAST:event_JCBCheckKnownEventsActionPerformed

  private void jLabel6MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel6MouseClicked
    twitterDisabledWindow.setVisible(true);
  }//GEN-LAST:event_jLabel6MouseClicked

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
  private javax.swing.JCheckBox JCBFilterShowclix;
  private javax.swing.JCheckBox JCBFilterTwitter;
  private javax.swing.JCheckBox JCBLoadNotifications;
  private javax.swing.JCheckBox JCBPlayAlarm;
  private javax.swing.JCheckBox JCBSaveCheckSettings;
  private javax.swing.JCheckBox JCBSaveEmailSettings;
  private javax.swing.JCheckBox JCBSaveTwitterKeys;
  private javax.swing.JCheckBox JCBStatistics;
  private javax.swing.JCheckBox JCBTextTweets;
  private javax.swing.JCheckBox JCBUseBeta;
  private javax.swing.JLabel JLSecondsBetweenChecks;
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
  private javax.swing.Box.Filler filler1;
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
