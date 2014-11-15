package paxchecker.gui;

import paxchecker.*;
import paxchecker.check.*;
import paxchecker.tickets.*;
import paxchecker.update.UpdateHandler;

/**
 *
 * @author SunnyBat
 */
public class Setup extends javax.swing.JFrame {

  public java.util.ArrayList<ExtraPhonePanel> extraPhonePanelList = new java.util.ArrayList<>();

  /**
   * Creates new form Setup
   */
  public Setup() {
    javax.swing.SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        initComponents();
        customComponents();
        setVisible(true);
      }
    });
  }

  private void customComponents() {
    setTitle("Setup :: PAXChecker v" + PAXChecker.VERSION);
    if (UpdateHandler.getVersionNotes() != null) {
      setPatchNotesText(UpdateHandler.getVersionNotes());
    }
    JTFEmail.setText(SettingsHandler.getEmail());
    JCBExpo.setSelectedIndex(getIndexOfEvent(SettingsHandler.getExpo()));
    JCBCheckWebsite.setSelected(SettingsHandler.getCheckPaxWebsite());
    JCBCheckShowclix.setSelected(SettingsHandler.getCheckShowclix());
    JCBCheckTwitter.setSelected(SettingsHandler.getCheckTwitter());
    jCheckBox3.setSelected(SettingsHandler.getPlayAlarm());
    JSCheckTime.setValue(SettingsHandler.getDelayTime());
    if (!JCBCheckWebsite.isSelected() && !JCBCheckShowclix.isSelected() && !JCBCheckTwitter.isSelected()) { // Disable START! button
      JBStart.setEnabled(false);
    }
    java.awt.Dimension d = JTFCellNum.getSize();
    String cellnum = SettingsHandler.getCellNumber();
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
        addPhonePanel(new ExtraPhonePanel(this, Email.splitEmail(specificNumbers[a])[0].trim(), Email.splitEmail(specificNumbers[a])[1].trim()));
      }
    } else {
      System.out.println("Normal address");
      JTFCellNum.setText(cellnum);
    }
    JTFCellNum.setSize(d);
    if (SettingsHandler.getSavePrefs()) {
      JCBSavePreferences.setSelected(SettingsHandler.getSavePrefs());
      JCBSaveCellnum.setSelected(SettingsHandler.getSaveCellnum());
      JCBSaveCheckPax.setSelected(SettingsHandler.getSavePax());
      JCBSaveCheckShowclix.setSelected(SettingsHandler.getSaveShowclix());
      JCBSaveCheckTwitter.setSelected(SettingsHandler.getSaveTwitter());
      JCBSaveEvent.setSelected(SettingsHandler.getSaveEvent());
      JCBSavePlayAlarm.setSelected(SettingsHandler.getSaveAlarm());
      JCBSaveRefreshTime.setSelected(SettingsHandler.getSaveRefreshTime());
      JCBSaveEmail.setSelected(SettingsHandler.getSaveEmail());
      JCBUseBeta.setSelected(SettingsHandler.getUseBetaVersion());
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
    UpdateHandler.setUseBeta(JCBUseBeta.isSelected());
    SettingsHandler.setSavePrefs(JCBSavePreferences.isSelected());
    if (JCBSavePreferences.isSelected()) {
      SettingsHandler.setSaveCellnum(JCBSaveCellnum.isSelected());
      SettingsHandler.setSavePax(JCBSaveCheckPax.isSelected());
      SettingsHandler.setSaveShowclix(JCBSaveCheckShowclix.isSelected());
      SettingsHandler.setSaveTwitter(JCBSaveCheckTwitter.isSelected());
      SettingsHandler.setSaveEvent(JCBSaveEvent.isSelected());
      SettingsHandler.setSaveAlarm(JCBSavePlayAlarm.isSelected());
      SettingsHandler.setSaveRefreshTime(JCBSaveRefreshTime.isSelected());
      SettingsHandler.setSaveEmail(JCBSaveEmail.isSelected());
    }
    SettingsHandler.saveAllPrefs(Checker.getRefreshTime(), JCBCheckWebsite.isSelected(), JCBCheckShowclix.isSelected(), JCBCheckTwitter.isSelected(), Audio.soundEnabled(), Browser.getExpo(), UpdateHandler.getUseBeta());
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
    jCheckBox3 = new javax.swing.JCheckBox();
    JCBExpo = new javax.swing.JComboBox();
    jLabel5 = new javax.swing.JLabel();
    jPanel2 = new javax.swing.JPanel();
    JPPhonePanel = new javax.swing.JPanel();
    JBAddPhone = new javax.swing.JButton();
    JCBCheckTwitter = new javax.swing.JCheckBox();
    jPanel6 = new javax.swing.JPanel();
    jScrollPane5 = new javax.swing.JScrollPane();
    jTextPane1 = new javax.swing.JTextPane();
    jPanel4 = new javax.swing.JPanel();
    jScrollPane4 = new javax.swing.JScrollPane();
    jTextArea4 = new javax.swing.JTextArea();
    jPanel3 = new javax.swing.JPanel();
    jScrollPane6 = new javax.swing.JScrollPane();
    jTextPane2 = new javax.swing.JTextPane();
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

    jCheckBox3.setText("Play Alarm when Tickets Found");
    jCheckBox3.setToolTipText("<html>\nIf checked, the program will play a sound when an update to<br>\nthe PAX Prime website OR the Showclix website (whichever one(s)<br>\nyou have enabled) is found.\n</html>");

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
            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
              .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jLabel5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(JCBExpo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
              .addComponent(jCheckBox3)
              .addComponent(JCBCheckTwitter)
              .addComponent(JCBCheckShowclix)
              .addComponent(JCBCheckWebsite))
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
        .addComponent(JCBCheckTwitter)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(jCheckBox3)
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

    jTextPane1.setBorder(null);
    jTextPane1.setContentType("text/html"); // NOI18N
    jTextPane1.setEditable(false);
    jTextPane1.setEditorKit(javax.swing.JEditorPane.createEditorKitForContentType("text/html"));
    jTextPane1.setText("<html>\n    <body>\n        <em>If there is a horizontal scrollbar here, please let\n            <a href=\"https://www.reddit.com/user/SunnyBat\">/u/SunnyBat</a> know!</em>\n\n        <h1>Important Note</h1>\n        You should NOT rely on only one way of PAX Ticket sale notifications.\n        Sign up for the @Official_PAX Twitter notifications\n        <a href=\"https://support.twitter.com/articles/20170004-fast-following-on-sms\">via text</a>.\n        Watch the Reddit Live thread (if there is one). Make sure your friends\n        know to text you if they find out. Do NOT miss out because you relied on only\n        one form of notification!<br>\n        That being said, this program worked perfectly last year. It detected\n        the Showclix link as soon as it went up -- a few minutes before the\n        Twitter notification.\n\n        <h1>Email and Password</h1>\n        <b>If you do not want to receive a text message when tickets are found,\n            simply leave these fields blank.</b><br>\n        Your email is used to send a text message when tickets are found. Currently,\n        there are two internally supported email services: <b>Gmail</b> and <b>Yahoo!</b>.\n        Simply type in your email address to use these. For information on using\n        different email services, see the Additional Information section.<br>\n        Gmail generally takes 30-45 seconds to receive a sent text. <em>This is\n            the recommended option.</em><br>\n        Yahoo! is very inconsistent with texts -- it can take anywhere from 10\n        <em>seconds</em> to 15 <em>minutes</em> to receive a text from Yahoo!<br>\n        <em>For information on why your email is required, see the Additional\n            Information section.</em><br>\n\n        <h1>Cell Number (Texting)</h1>\n        Your cell number is required to text to your phone. The program natively\n        supports AT&T, Verizon, Sprint, T-Mobile, and U.S. Cellular. If you have\n        any of these carriers, simply select yours from the dropdown menu.\n        If you have a different carrier, you'll need to go to\n        <a href=\"http://www.emailtextmessages.com\">emailtextmessages.com</a> and\n        get the ending for your carrier.<br>\n        Please note that standard messaging rates most likely apply. Please\n        ensure that your plan either includes SMS/MMS messages or you're willing\n        to receive additional fees. Most smartphone plans come with unlimited\n        SMS/MMS messages.<br>\n        You can put dashes and parentheses into your number. For example, 012-345-6789\n        is just as valid as (012)-345-6789. Your number is converted\n        into a format with no dashes or parentheses -- just numbers. Overall,\n        the email is sent in the format of 0123456789@car.rier.net. For example,\n        if your number is 0123456789 and your provider is 3 River Wireless,\n        you would put 0123456789@sms.3rivers.net in the Cell Num field.<br>\n        To text multiple numbers, simply add another number using the button\n        provided. There is no hard-coded limit for how many numbers you may\n        text. Each additional number should follow all of the instructions above.<br>\n        <i><b>AH! What's the difference between \"AT&T (MMS)\" and \"AT&T (SMS)\"???</b></i><br>\n        Selecting the MMS option sends the message as a multimedia message.\n        This preserves the email sender information (you receive the text from\n        your email address) and shortens the length. This is the recommended\n        option to use. Unlimited texting plans <i>most likely</i> include\n        multimedia texts.<br>\n        The SMS option sends the message as a text message. You receive\n        it from a 210-extension number, and it includes \"FRM\", \"SUBJ\" and \"MSG\"\n        information, increasing the message length and decreasing readability.\n        Not recommended, as text messages have a character limit of 160\n        characters, and the text from the program is likely to go over 160\n        characters.<br>\n\n        <h1>Scan PAX Registration Website</h1>\n        This scans the selected PAX registration website for the \"Register Now\" button.\n        It checks PAX.paxsite.com/registration (obviously PAX is the expo you're checking).<br>\n        This uses the most bandwidth (still not very much). It's recommended to\n        use this option if you really want tickets.\n\n        <h1>Scan Showclix Website</h1>\n        This scans the Showclix API (<a href=\"https://api.showclix.com\">api.showclix.com</a>) for the\n        selected PAX expo's most recent ticket sale.<br>\n        <b>This option is highly recommended.</b> The Showclix website is updated first in order\n        to get the link to the ticket queue, which this program finds using the Showclix API. It\n        uses a small amount of bandwidth, and will mostly likely find the tickets before the PAX\n        Registration Website option.<br>\n        Credit goes to <a href=\"https://www.reddit.com/user/GrahamArthurBlair\">/u/GrahamArthurBlair</a>\n        for his original Showclix API scanning code.<br><br>\n\n\n        <h1>Additional Information</h1>\n\n        <h2>Why Does This Need my Password?</h2>\n        An email is required to send a text message. It's the simplest way to send\n        a text message, and anyone can use it.<br>\n        There are several reasons why your email and password is required:\n        <ul>\n            <li>You must authenticate your login to send an email using your account\n                username and password</li>\n            <li>This program is open-source. If there was a hard-coded username and\n                password, anyone would be able to find it in this program at any time and\n                disable texting for everyone. Using your email address prevents this from\n                happening.</li>\n            <li>Having one email address send out potentially dozens (modestly) of\n                emails within 60 seconds is very fishy, and multiple logins from different\n                locations will lock out the email after a few emails. This means only a few\n                people would actually get the text.</li>\n            <li>You will recognize the sender of your text</li>\n        </ul>\n\n        <h2>Using a Different Email Provider</h2>\n        For sending an email using a different email provider, several things are required:\n        <ul>\n            <li>The service must use SMTP and TLS</li>\n            <li>You must know the SMTP server address</li>\n            <li>You must know the SMTP server port</li>\n        </ul>\n        Once you have this information, you must input this after your email address\n        and two colons (::). If the server uses a port other than 587, you must\n        add the port after the server and a colon (:).<br>\n        An example:<br>\n        Emailaddress@someprovider.com::provider.smpt.server:999<br>\n        <b>Note that this is very experimental. Make sure to test your text message every time!</b>\n    </body>\n</html>");
    jTextPane1.setCaretPosition(0);
    jTextPane1.addHyperlinkListener(new javax.swing.event.HyperlinkListener() {
      public void hyperlinkUpdate(javax.swing.event.HyperlinkEvent e) {
        if (e.getEventType() == javax.swing.event.HyperlinkEvent.EventType.ACTIVATED) {
          Browser.openLinkInBrowser(e.getURL());
        }
      }
    });
    jScrollPane5.setViewportView(jTextPane1);

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

    jTextPane2.setBorder(null);
    jTextPane2.setContentType("text/html"); // NOI18N
    jTextPane2.setEditable(false);
    jTextPane2.setEditorKit(javax.swing.JEditorPane.createEditorKitForContentType("text/html"));
    jTextPane2.setText("<html>\n    <body>\n        <em>If there is a horizontal scrollbar here, please let\n            <a href=\"https://www.reddit.com/user/SunnyBat\">/u/SunnyBat</a> know!</em>\n\n        <h1>External Links</h1>\n        <ul>\n            <li>This program has been provided for free. If you have been charged for it, get your money back and contact SunnyBat immediately!</li>\n            <li>The source code can be found <a href=\"https://www.github.com/SunnyBat/PAXChecker\">on GitHub</a>.</li>\n            <li>Program published on <a href=\"https://www.github.com/SunnyBat/PAXChecker\">/r/PAX</a> -- If you see it somewhere else, please let SunnyBat know!</li>\n        </ul>\n\n        <h1>Credits</h1>\n        <ul>\n            <li>Program created by <a href=\"https://www.reddit.com/user/SunnyBat\">/u/SunnyBat</a></li>\n            <li>Showclix API scanning code originally created by <a href=\"https://www.reddit.com/user/GrahamArthurBlair\">/u/GrahamArthurBlair</a></li>\n            <li>Reddit Gold guilders: You're all awesome. If you want me to put your name here, let me know!</li>\n            <li><a href=\"https://java.net/projects/javamail/pages/Home\">JavaMail 1.4.7</a> -- For sending emails</li>\n            <li><a href=\"https://code.google.com/p/json-simple/\">JSON.simple</a> -- For parsing Showclix API JSON responses</li>\n        </ul>\n\n        <h1>Contact Me</h1>\n        To contact me, please use one of the following methods:\n        <ul>\n            <li><a href=\"mailto:Sunnybat@yahoo.com\">Email me</a></li>\n            <li><a href=\"https://www.reddit.com/message/compose/?to=SunnyBat\">Message me on Reddit</a></li>\n            <li><a href=\"https://github.com/SunnyBat\">Message me on Github</a></li>\n        </ul>\n    </body>\n</html>");
    jTextPane2.setToolTipText("");
    jTextPane2.setCaretPosition(0);
    jTextPane2.addHyperlinkListener(new javax.swing.event.HyperlinkListener() {
      public void hyperlinkUpdate(javax.swing.event.HyperlinkEvent e) {
        if (e.getEventType() == javax.swing.event.HyperlinkEvent.EventType.ACTIVATED) {
          Browser.openLinkInBrowser(e.getURL());
        }
      }
    });
    jScrollPane6.setViewportView(jTextPane2);

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

    jButton3.setText("Save Use BETA");
    jButton3.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        jButton3ActionPerformed(evt);
      }
    });

    JCBSaveCheckTwitter.setText("Save Twitter Checking");

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
          .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 391, Short.MAX_VALUE)
          .addGroup(jPanel5Layout.createSequentialGroup()
            .addComponent(JCBSavePreferences)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(JCBUseBeta))
          .addGroup(jPanel5Layout.createSequentialGroup()
            .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
              .addComponent(JCBSavePlayAlarm)
              .addComponent(JCBSaveEvent)
              .addComponent(JCBSaveCellnum)
              .addComponent(JCBSaveCheckShowclix)
              .addComponent(JCBSaveCheckPax)
              .addComponent(JCBSaveCheckTwitter)
              .addComponent(JCBSaveEmail))
            .addGap(0, 0, Short.MAX_VALUE)))
        .addContainerGap())
    );
    jPanel5Layout.setVerticalGroup(
      jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(jPanel5Layout.createSequentialGroup()
        .addContainerGap()
        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(JCBSavePreferences)
          .addComponent(JCBUseBeta))
        .addGap(18, 18, 18)
        .addComponent(JCBSaveEmail)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(JCBSaveCellnum)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(JCBSaveCheckPax)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(JCBSaveCheckShowclix)
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
    // TODO add your handling code here:
    javax.swing.SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        if (!JCBCheckWebsite.isSelected() && !JCBCheckShowclix.isSelected() && !JCBCheckTwitter.isSelected()) {
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
        if (!JCBCheckWebsite.isSelected() && !JCBCheckShowclix.isSelected() && !JCBCheckTwitter.isSelected()) {
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
    if (JCBCheckTwitter.isSelected()) {
      TicketChecker.addChecker(new CheckTwitter());
    }
    Audio.setPlayAlarm(jCheckBox3.isSelected());
    Email.setUsername(JTFEmail.getText());
    Email.setPassword(new String(JPFPassword.getPassword()));
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
    Email.addEmailAddress(text);
    Checker.setRefreshTime(JSCheckTime.getValue());
    savePreferences();
    this.dispose();
    Checker.startCheckingWebsites();
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
    //savePreferences();
    SettingsHandler.saveUseBeta(JCBUseBeta.isSelected());
  }//GEN-LAST:event_jButton3ActionPerformed

  private void JCBCheckTwitterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_JCBCheckTwitterActionPerformed
    // TODO add your handling code here:
    javax.swing.SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        if (!JCBCheckWebsite.isSelected() && !JCBCheckShowclix.isSelected() && !JCBCheckTwitter.isSelected()) {
          JBStart.setEnabled(false);
        } else {
          JBStart.setEnabled(true);
        }
      }
    });
  }//GEN-LAST:event_JCBCheckTwitterActionPerformed

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JButton JBAddPhone;
  private javax.swing.JButton JBStart;
  private javax.swing.JComboBox JCBCarrier;
  private javax.swing.JCheckBox JCBCheckShowclix;
  private javax.swing.JCheckBox JCBCheckTwitter;
  private javax.swing.JCheckBox JCBCheckWebsite;
  private javax.swing.JComboBox JCBExpo;
  private javax.swing.JCheckBox JCBSaveCellnum;
  private javax.swing.JCheckBox JCBSaveCheckPax;
  private javax.swing.JCheckBox JCBSaveCheckShowclix;
  private javax.swing.JCheckBox JCBSaveCheckTwitter;
  private javax.swing.JCheckBox JCBSaveEmail;
  private javax.swing.JCheckBox JCBSaveEvent;
  private javax.swing.JCheckBox JCBSavePlayAlarm;
  private javax.swing.JCheckBox JCBSavePreferences;
  private javax.swing.JCheckBox JCBSaveRefreshTime;
  private javax.swing.JCheckBox JCBUseBeta;
  private javax.swing.JPasswordField JPFPassword;
  private javax.swing.JPanel JPPhonePanel;
  private javax.swing.JSlider JSCheckTime;
  private javax.swing.JTextField JTFCellNum;
  private javax.swing.JTextField JTFEmail;
  private javax.swing.JButton jButton3;
  private javax.swing.JCheckBox jCheckBox3;
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
  private javax.swing.JTextPane jTextPane1;
  private javax.swing.JTextPane jTextPane2;
  // End of variables declaration//GEN-END:variables
}
