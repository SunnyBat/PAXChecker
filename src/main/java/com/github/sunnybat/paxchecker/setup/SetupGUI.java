package com.github.sunnybat.paxchecker.setup;

import com.github.sunnybat.commoncode.email.account.EmailAccount;
import com.github.sunnybat.commoncode.preferences.PreferenceHandler;
import com.github.sunnybat.paxchecker.PAXChecker;
import com.github.sunnybat.paxchecker.browser.Browser;
import com.github.sunnybat.paxchecker.notification.NotificationWindow;
import com.github.sunnybat.paxchecker.setup.email.EmailSetupGUI;
import javax.swing.SwingWorker;
import twitter4j.Twitter;

/**
 * Creates a new Setup GUI for the user to configure the program at will. Saves Preferences
 * independent of the rest of the program for quick loading afterwards.
 *
 * @author SunnyBat
 */
public class SetupGUI extends com.github.sunnybat.commoncode.javax.swing.JFrame implements Setup {

	private NotificationWindow twitterDisabledWindow;
	private PreferenceHandler prefs;
	private EmailSetupGUI myEmailGui;
	private TwitterSetupGUI myTwitterGui;
	private boolean isOpen = true;

	/**
	 * Creates new form Setup
	 */
	public SetupGUI() {
		twitterDisabledWindow = new NotificationWindow("API Keys", "The Twitter API requires API keys to access it. Unfortunately, putting API keys in "
				+ "an open-source project runs the risk of having the keys disabled due to malicious people. As such, you must use your own Twitter API "
				+ "keys. For more information, press the More Info button.\n\nIt's recommended to check the \"Save Encrypted Twitter Keys\" checkbox in the "
				+ "Preferences tab if you do not want to enter them into the program every time.\nNOTE: You should only save your Twitter keys on a private "
				+ "and trusted computer!");
		twitterDisabledWindow.setMoreInfoButtonLink("https://redd.it/2nct50");
		prefs = new PreferenceHandler("paxchecker");
		myEmailGui = new EmailSetupGUI(prefs);
		myEmailGui.setLocationRelativeTo(this);
		myTwitterGui = new TwitterSetupGUI(prefs);
		myTwitterGui.setLocationRelativeTo(this);
		invokeAndWaitOnEDT(new Runnable() {
			@Override
			public void run() {
				initComponents();
				customComponents();
				updateEmailAccountInfo();
				updateTwitterInfo();
			}
		});
	}

	@Override
	public Twitter getTwitterAccount() {
		return myTwitterGui.getTwitterAccount();
	}

	private void customComponents() {
		setTitle("Setup :: PAXChecker v" + PAXChecker.VERSION);
		JTPExtras.setText(loadHtml("/com/github/sunnybat/paxchecker/resources/html/Extra.html"));
		JTPExtras.setCaretPosition(0);
		JTPInstructions.setText(loadHtml("/com/github/sunnybat/paxchecker/resources/html/Instructions.html"));
		JTPInstructions.setCaretPosition(0);
		// Preferences Tab
		JCBSaveEmailSettings.setSelected(prefs.getBooleanPreference("SAVE_EMAIL_SETTINGS"));
		JCBSaveCheckSettings.setSelected(prefs.getBooleanPreference("SAVE_CHECK_SETTINGS", true));
		JCBUseBeta.setSelected(prefs.getBooleanPreference("USE_BETA"));
		JCBLoadNotifications.setSelected(prefs.getBooleanPreference("LOAD_NOTIFICATIONS", true));
		JCBCheckUpdates.setSelected(prefs.getBooleanPreference("LOAD_UPDATES", true));
		if (JCBCheckUpdates.isSelected()) {
			JCBCheckUpdatesDaily.setSelected(prefs.getBooleanPreference("DAILY_UPDATES"));
		} else {
			JCBCheckUpdatesDaily.setEnabled(false);
		}
		JCBStatistics.setSelected(prefs.getBooleanPreference("ANONYMOUS_STATISTICS"));
		JCBCheckWebsite.setSelected(prefs.getBooleanPreference("CHECK_PAX", true));
		JCBCheckShowclix.setSelected(prefs.getBooleanPreference("CHECK_SHOWCLIX", true));
		JCBCheckKnownEvents.setSelected(prefs.getBooleanPreference("CHECK_KNOWN_EVENTS"));
		JCBExpo.setSelectedIndex(getIndexOfEvent(prefs.getStringPreference("EVENT")));
		JCBPlayAlarm.setSelected(prefs.getBooleanPreference("PLAY_ALARM"));
		JSCheckTime.setValue(prefs.getIntegerPreference("REFRESHTIME"));
		updateStart();
	}

	/**
	 * Gets the index of the given Expo for the Setup JComboBox. The proper input for the method is
	 * the same Strings as the JComboBox in the Setup GUI.
	 *
	 * @param eventName The expo ("Pax EXPO") to get the index of
	 * @return The index of the given expo, or 0 for incorrect inputs.
	 */
	public static final int getIndexOfEvent(String eventName) {
		if (eventName == null) {
			return 0;
		}
		switch (eventName.toLowerCase()) {
			case "pax prime": // Keep for backwards compatibility with Preferences
			case "pax west":
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
	 * Gets the index of the given provider for the Set JComboBox. The proper input for this method is
	 * the same Strings as the JComboBox in the Setup GUI.
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
				JTAPatchNotes.setText(text);
				JTAPatchNotes.setCaretPosition(0);
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

	private void savePreferences() {
		prefs.getPreferenceObject("SAVE_EMAIL_SETTINGS").setValue(JCBSaveEmailSettings.isSelected());
		prefs.getPreferenceObject("SAVE_CHECK_SETTINGS").setValue(JCBSaveCheckSettings.isSelected());
		prefs.getPreferenceObject("LOAD_NOTIFICATIONS").setValue(JCBLoadNotifications.isSelected());
		prefs.getPreferenceObject("LOAD_UPDATES").setValue(JCBCheckUpdates.isSelected());
		prefs.getPreferenceObject("DAILY_UPDATES").setShouldSave(JCBCheckUpdates.isSelected()); // If update checking is disabled, don't check every 24 hours
		prefs.getPreferenceObject("DAILY_UPDATES").setValue(JCBCheckUpdatesDaily.isSelected());
		prefs.getPreferenceObject("USE_BETA").setValue(JCBUseBeta.isSelected());
		prefs.getPreferenceObject("ANONYMOUS_STATISTICS").setValue(JCBStatistics.isSelected());
		prefs.getPreferenceObject("EMAIL").setShouldSave(JCBSaveEmailSettings.isSelected());
		prefs.getPreferenceObject("CELLNUM").setShouldSave(JCBSaveEmailSettings.isSelected());
		prefs.getPreferenceObject("EMAILTYPE").setShouldSave(JCBSaveEmailSettings.isSelected());
		prefs.getPreferenceObject("EMAILENABLED").setShouldSave(JCBSaveEmailSettings.isSelected());
		prefs.getPreferenceObject("CHECK_PAX").setShouldSave(JCBSaveCheckSettings.isSelected());
		prefs.getPreferenceObject("CHECK_PAX").setValue(JCBCheckWebsite.isSelected());
		prefs.getPreferenceObject("CHECK_SHOWCLIX").setShouldSave(JCBSaveCheckSettings.isSelected());
		prefs.getPreferenceObject("CHECK_SHOWCLIX").setValue(JCBCheckShowclix.isSelected());
		prefs.getPreferenceObject("CHECK_KNOWN_EVENTS").setShouldSave(JCBSaveCheckSettings.isSelected());
		prefs.getPreferenceObject("CHECK_KNOWN_EVENTS").setValue(JCBCheckKnownEvents.isSelected());
		prefs.getPreferenceObject("CHECK_TWITTER").setShouldSave(JCBSaveCheckSettings.isSelected());
		prefs.getPreferenceObject("FILTER_TWITTER").setShouldSave(JCBSaveCheckSettings.isSelected());
		prefs.getPreferenceObject("TEXT_TWEETS").setShouldSave(JCBSaveCheckSettings.isSelected());
		prefs.getPreferenceObject("EVENT").setShouldSave(JCBSaveCheckSettings.isSelected());
		prefs.getPreferenceObject("EVENT").setValue(JCBExpo.getSelectedItem().toString());
		prefs.getPreferenceObject("PLAY_ALARM").setShouldSave(JCBSaveCheckSettings.isSelected());
		prefs.getPreferenceObject("PLAY_ALARM").setValue(JCBPlayAlarm.isSelected());
		prefs.getPreferenceObject("REFRESHTIME").setShouldSave(JCBSaveCheckSettings.isSelected());
		prefs.getPreferenceObject("REFRESHTIME").setValue(JSCheckTime.getValue());
		prefs.getPreferenceObject("ANONYMOUS_STATISTICS").setValue(JCBStatistics.isSelected());
		prefs.savePreferences();
	}

	private void updateStart() {
		JSCheckTime.setEnabled(JCBCheckWebsite.isSelected() || JCBCheckShowclix.isSelected() || JCBCheckKnownEvents.isSelected());
		JLSecondsBetweenChecks.setEnabled(JCBCheckWebsite.isSelected() || JCBCheckShowclix.isSelected() || JCBCheckKnownEvents.isSelected());
		JBStart.setEnabled(JCBCheckWebsite.isSelected() || JCBCheckShowclix.isSelected() || myTwitterGui.isTwitterEnabled() || JCBCheckKnownEvents.isSelected());
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
	public EmailAccount getEmailAccount() {
		return myEmailGui.getEmailAccount();
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
		return myTwitterGui.getTwitterAccount() != null;
	}

	@Override
	public boolean shouldFilterTwitter() {
		return myTwitterGui.shouldFilterTwitter();
	}

	@Override
	public boolean shouldTextTweets() {
		return myTwitterGui.shouldTextTweets();
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
	public boolean shouldCheckForUpdatesDaily() {
		return JCBCheckUpdates.isSelected() && JCBCheckUpdatesDaily.isSelected();
	}

	private void updateEmailAccountInfo() {
		EmailAccount myAccount = myEmailGui.getEmailAccount();
		if (myAccount != null) {
			JLEmailType.setText(myEmailGui.getEmailType());
			JLEmailStatus.setText("Enabled");
			JLEmailAddress.setText(myAccount.getEmailAddress());
			String addressList = myEmailGui.getEmailAddressesString();
			JLEmailList.setText(addressList.substring(0, Math.min(addressList.length(), 500)));
		} else {
			JLEmailType.setText("Disabled");
			JLEmailAddress.setText("Disabled");
			JLEmailStatus.setText("Disabled");
			JLEmailList.setText("Disabled");
		}
	}

	private void updateTwitterInfo() {
		if (myTwitterGui.isTwitterEnabled()) {
			JLTwitterStatus.setText("Enabled");
		} else {
			JLTwitterStatus.setText("Disabled");
		}
	}

	/**
	 * This method is called from within the constructor to initialize the form. WARNING: Do NOT
	 * modify this code. The content of this method is always regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        JTPMainPane = new javax.swing.JTabbedPane();
        JPSetup = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        JLSecondsBetweenChecks = new javax.swing.JLabel();
        JSCheckTime = new javax.swing.JSlider();
        JBStart = new javax.swing.JButton();
        JCBCheckWebsite = new javax.swing.JCheckBox();
        JCBCheckShowclix = new javax.swing.JCheckBox();
        JCBPlayAlarm = new javax.swing.JCheckBox();
        JCBExpo = new javax.swing.JComboBox();
        jLabel5 = new javax.swing.JLabel();
        JCBCheckKnownEvents = new javax.swing.JCheckBox();
        JCBFilterShowclix = new javax.swing.JCheckBox();
        jLabel2 = new javax.swing.JLabel();
        JLEmailType = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        JLEmailStatus = new javax.swing.JLabel();
        JBConfigureEmail = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();
        JLEmailAddress = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        JLEmailList = new javax.swing.JLabel();
        JBConfigureTwitter = new javax.swing.JButton();
        JLTwitterStatus = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        JPInstructions = new javax.swing.JPanel();
        jScrollPane5 = new javax.swing.JScrollPane();
        JTPInstructions = new javax.swing.JTextPane();
        JPPatchNotes = new javax.swing.JPanel();
        jScrollPane4 = new javax.swing.JScrollPane();
        JTAPatchNotes = new javax.swing.JTextArea();
        JPExtras = new javax.swing.JPanel();
        jScrollPane6 = new javax.swing.JScrollPane();
        JTPExtras = new javax.swing.JTextPane();
        JPPreferences = new javax.swing.JPanel();
        JCBSaveEmailSettings = new javax.swing.JCheckBox();
        JCBSaveCheckSettings = new javax.swing.JCheckBox();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        JCBUseBeta = new javax.swing.JCheckBox();
        JBSaveSettings = new javax.swing.JButton();
        JCBLoadNotifications = new javax.swing.JCheckBox();
        JCBCheckUpdates = new javax.swing.JCheckBox();
        JCBStatistics = new javax.swing.JCheckBox();
        JCBCheckUpdatesDaily = new javax.swing.JCheckBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("PAXChecker Setup");
        setResizable(false);

        jLabel1.setFont(new java.awt.Font("Tahoma", 0, 36)); // NOI18N
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("PAXChecker Setup");

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
        JCBPlayAlarm.setToolTipText("<html>\nIf checked, the program will play a sound when<br>\na new link is found.\n</html>");

        JCBExpo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "PAX West", "PAX East", "PAX South", "PAX Aus" }));

        jLabel5.setText("PAX Expo to Check");

        JCBCheckKnownEvents.setText("Scan Known Potential Showclix Events");
        JCBCheckKnownEvents.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                JCBCheckKnownEventsActionPerformed(evt);
            }
        });

        JCBFilterShowclix.setText("Strict Filtering");
        JCBFilterShowclix.setToolTipText("<html>\nEnabling this will hopefully reduce the<br>\namount of false positives, however<br>\nmight also cause the PAXChecker to<br>\nmiss the queue. Use at your own risk.\n</html>");

        jLabel2.setText("Email Type:");

        JLEmailType.setText("Disabled");

        jLabel3.setText("Email Status:");

        JLEmailStatus.setText("Disabled");

        JBConfigureEmail.setText("Configure Email");
        JBConfigureEmail.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                JBConfigureEmailActionPerformed(evt);
            }
        });

        jLabel4.setText("Email Account:");

        JLEmailAddress.setText("Disabled");

        jLabel11.setText("Email List:");

        JLEmailList.setText("Disabled");

        JBConfigureTwitter.setText("Configure Twitter");
        JBConfigureTwitter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                JBConfigureTwitterActionPerformed(evt);
            }
        });

        JLTwitterStatus.setText("Disabled");

        jLabel7.setText("Twitter Status");

        javax.swing.GroupLayout JPSetupLayout = new javax.swing.GroupLayout(JPSetup);
        JPSetup.setLayout(JPSetupLayout);
        JPSetupLayout.setHorizontalGroup(
            JPSetupLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(JPSetupLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(JPSetupLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(JPSetupLayout.createSequentialGroup()
                        .addGroup(JPSetupLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(JCBPlayAlarm)
                            .addGroup(JPSetupLayout.createSequentialGroup()
                                .addComponent(JCBCheckShowclix)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(JCBFilterShowclix))
                            .addGroup(JPSetupLayout.createSequentialGroup()
                                .addComponent(jLabel5)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(JCBExpo, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(JPSetupLayout.createSequentialGroup()
                        .addGroup(JPSetupLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(JBStart, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(JSCheckTime, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(JLSecondsBetweenChecks, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(JPSetupLayout.createSequentialGroup()
                                .addGroup(JPSetupLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(JPSetupLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                        .addComponent(jLabel11, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                    .addComponent(jLabel7))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(JPSetupLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(JPSetupLayout.createSequentialGroup()
                                        .addGroup(JPSetupLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(JLEmailType)
                                            .addComponent(JLEmailStatus))
                                        .addGap(18, 18, 18)
                                        .addComponent(JBConfigureEmail, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                    .addGroup(JPSetupLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(JPSetupLayout.createSequentialGroup()
                                            .addComponent(JLTwitterStatus)
                                            .addGap(18, 18, 18)
                                            .addComponent(JBConfigureTwitter, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                        .addGroup(JPSetupLayout.createSequentialGroup()
                                            .addGroup(JPSetupLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                .addComponent(JLEmailAddress)
                                                .addComponent(JLEmailList, javax.swing.GroupLayout.PREFERRED_SIZE, 316, javax.swing.GroupLayout.PREFERRED_SIZE))
                                            .addGap(0, 0, Short.MAX_VALUE)))))
                            .addGroup(JPSetupLayout.createSequentialGroup()
                                .addGroup(JPSetupLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(JCBCheckKnownEvents)
                                    .addComponent(JCBCheckWebsite))
                                .addGap(0, 0, Short.MAX_VALUE)))
                        .addContainerGap())))
        );
        JPSetupLayout.setVerticalGroup(
            JPSetupLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(JPSetupLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addGap(18, 18, 18)
                .addGroup(JPSetupLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(JPSetupLayout.createSequentialGroup()
                        .addGroup(JPSetupLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel2)
                            .addComponent(JLEmailType))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(JPSetupLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel3)
                            .addComponent(JLEmailStatus)))
                    .addComponent(JBConfigureEmail, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(JPSetupLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel4)
                    .addComponent(JLEmailAddress))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(JPSetupLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel11)
                    .addComponent(JLEmailList))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(JPSetupLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(JLTwitterStatus)
                    .addComponent(jLabel7)
                    .addComponent(JBConfigureTwitter))
                .addGap(6, 6, 6)
                .addComponent(JCBCheckWebsite)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(JPSetupLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(JCBCheckShowclix)
                    .addComponent(JCBFilterShowclix))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(JCBCheckKnownEvents)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(JCBPlayAlarm)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(JPSetupLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(JCBExpo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 31, Short.MAX_VALUE)
                .addComponent(JLSecondsBetweenChecks)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(JSCheckTime, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(JBStart)
                .addContainerGap())
        );

        JTPMainPane.addTab("Setup", JPSetup);

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

        javax.swing.GroupLayout JPInstructionsLayout = new javax.swing.GroupLayout(JPInstructions);
        JPInstructions.setLayout(JPInstructionsLayout);
        JPInstructionsLayout.setHorizontalGroup(
            JPInstructionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 412, Short.MAX_VALUE)
        );
        JPInstructionsLayout.setVerticalGroup(
            JPInstructionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 442, Short.MAX_VALUE)
        );

        JTPMainPane.addTab("Instructions", JPInstructions);

        JTAPatchNotes.setEditable(false);
        JTAPatchNotes.setColumns(20);
        JTAPatchNotes.setLineWrap(true);
        JTAPatchNotes.setRows(5);
        JTAPatchNotes.setText("Loading patch notes, please wait...");
        JTAPatchNotes.setWrapStyleWord(true);
        jScrollPane4.setViewportView(JTAPatchNotes);

        javax.swing.GroupLayout JPPatchNotesLayout = new javax.swing.GroupLayout(JPPatchNotes);
        JPPatchNotes.setLayout(JPPatchNotesLayout);
        JPPatchNotesLayout.setHorizontalGroup(
            JPPatchNotesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 412, Short.MAX_VALUE)
        );
        JPPatchNotesLayout.setVerticalGroup(
            JPPatchNotesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 442, Short.MAX_VALUE)
        );

        JTPMainPane.addTab("Patch Notes", JPPatchNotes);

        JTPExtras.setBorder(null);
        JTPExtras.setContentType("text/html"); // NOI18N
        JTPExtras.setEditable(false);
        JTPExtras.setEditorKit(javax.swing.JEditorPane.createEditorKitForContentType("text/html"));
        JTPExtras.setText("[Located in Extra.html]");
        JTPExtras.setToolTipText("");
        JTPExtras.addHyperlinkListener(new javax.swing.event.HyperlinkListener() {
            public void hyperlinkUpdate(javax.swing.event.HyperlinkEvent e) {
                if (e.getEventType() == javax.swing.event.HyperlinkEvent.EventType.ACTIVATED) {
                    Browser.openLinkInBrowser(e.getURL());
                }
            }
        });
        jScrollPane6.setViewportView(JTPExtras);

        javax.swing.GroupLayout JPExtrasLayout = new javax.swing.GroupLayout(JPExtras);
        JPExtras.setLayout(JPExtrasLayout);
        JPExtrasLayout.setHorizontalGroup(
            JPExtrasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane6, javax.swing.GroupLayout.DEFAULT_SIZE, 412, Short.MAX_VALUE)
        );
        JPExtrasLayout.setVerticalGroup(
            JPExtrasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane6, javax.swing.GroupLayout.DEFAULT_SIZE, 442, Short.MAX_VALUE)
        );

        JTPMainPane.addTab("Extra", JPExtras);

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

        JCBStatistics.setText("Anonymous Statistics");
        JCBStatistics.setToolTipText("<html>\nWhen enabled, the program goes through Orangedox<br>\nto collect non-personally identifiable statistics about<br>\nfile downloads, such as the date downloaded and<br>\nthe specific file downloaded.<br>\nNOTE that \"Anonymous Statistics\" only means going<br>\nthrough Dropbox instead of Orangedox. Dropbox's<br>\nPrivacy Policy applies to you, regardless of whether<br>\nor not this option is enabled.\n</html>");

        JCBCheckUpdatesDaily.setText("Every 24 hours");

        javax.swing.GroupLayout JPPreferencesLayout = new javax.swing.GroupLayout(JPPreferences);
        JPPreferences.setLayout(JPPreferencesLayout);
        JPPreferencesLayout.setHorizontalGroup(
            JPPreferencesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(JPPreferencesLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(JPPreferencesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 392, Short.MAX_VALUE)
                    .addGroup(JPPreferencesLayout.createSequentialGroup()
                        .addGroup(JPPreferencesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(JCBSaveCheckSettings)
                            .addComponent(JCBSaveEmailSettings)
                            .addGroup(JPPreferencesLayout.createSequentialGroup()
                                .addGap(21, 21, 21)
                                .addComponent(JCBCheckUpdatesDaily))
                            .addComponent(JCBUseBeta)
                            .addComponent(JCBStatistics)
                            .addComponent(JCBLoadNotifications)
                            .addComponent(JCBCheckUpdates))
                        .addGap(0, 265, Short.MAX_VALUE))
                    .addComponent(JBSaveSettings, javax.swing.GroupLayout.DEFAULT_SIZE, 392, Short.MAX_VALUE))
                .addContainerGap())
        );
        JPPreferencesLayout.setVerticalGroup(
            JPPreferencesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(JPPreferencesLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(JCBSaveEmailSettings)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(JCBSaveCheckSettings)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(JCBLoadNotifications)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(JCBCheckUpdates)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(JCBCheckUpdatesDaily)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(JCBUseBeta)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(JCBStatistics)
                .addGap(25, 25, 25)
                .addComponent(JBSaveSettings)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 209, Short.MAX_VALUE)
                .addContainerGap())
        );

        JTPMainPane.addTab("Preferences", JPPreferences);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(JTPMainPane)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(JTPMainPane)
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

  private void JBSaveSettingsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_JBSaveSettingsActionPerformed
		savePreferences();
  }//GEN-LAST:event_JBSaveSettingsActionPerformed

  private void JCBCheckUpdatesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_JCBCheckUpdatesActionPerformed
		JCBCheckUpdatesDaily.setEnabled(JCBCheckUpdates.isSelected());
  }//GEN-LAST:event_JCBCheckUpdatesActionPerformed

  private void JCBCheckKnownEventsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_JCBCheckKnownEventsActionPerformed
		updateStart();
  }//GEN-LAST:event_JCBCheckKnownEventsActionPerformed

    private void JBConfigureEmailActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_JBConfigureEmailActionPerformed
			new EmailConfiguration().execute();
    }//GEN-LAST:event_JBConfigureEmailActionPerformed

    private void JBConfigureTwitterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_JBConfigureTwitterActionPerformed
			new TwitterConfiguration().execute();
    }//GEN-LAST:event_JBConfigureTwitterActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton JBConfigureEmail;
    private javax.swing.JButton JBConfigureTwitter;
    private javax.swing.JButton JBSaveSettings;
    private javax.swing.JButton JBStart;
    private javax.swing.JCheckBox JCBCheckKnownEvents;
    private javax.swing.JCheckBox JCBCheckShowclix;
    private javax.swing.JCheckBox JCBCheckUpdates;
    private javax.swing.JCheckBox JCBCheckUpdatesDaily;
    private javax.swing.JCheckBox JCBCheckWebsite;
    private javax.swing.JComboBox JCBExpo;
    private javax.swing.JCheckBox JCBFilterShowclix;
    private javax.swing.JCheckBox JCBLoadNotifications;
    private javax.swing.JCheckBox JCBPlayAlarm;
    private javax.swing.JCheckBox JCBSaveCheckSettings;
    private javax.swing.JCheckBox JCBSaveEmailSettings;
    private javax.swing.JCheckBox JCBStatistics;
    private javax.swing.JCheckBox JCBUseBeta;
    private javax.swing.JLabel JLEmailAddress;
    private javax.swing.JLabel JLEmailList;
    private javax.swing.JLabel JLEmailStatus;
    private javax.swing.JLabel JLEmailType;
    private javax.swing.JLabel JLSecondsBetweenChecks;
    private javax.swing.JLabel JLTwitterStatus;
    private javax.swing.JPanel JPExtras;
    private javax.swing.JPanel JPInstructions;
    private javax.swing.JPanel JPPatchNotes;
    private javax.swing.JPanel JPPreferences;
    private javax.swing.JPanel JPSetup;
    private javax.swing.JSlider JSCheckTime;
    private javax.swing.JTextArea JTAPatchNotes;
    private javax.swing.JTextPane JTPExtras;
    private javax.swing.JTextPane JTPInstructions;
    private javax.swing.JTabbedPane JTPMainPane;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JTextArea jTextArea1;
    // End of variables declaration//GEN-END:variables

	private class EmailConfiguration extends SwingWorker<Boolean, Boolean> {

		public Boolean doInBackground() {
			setEnabled(false);
			myEmailGui.setVisible(true);
			try {
				while (myEmailGui.isVisible()) {
					Thread.sleep(250);
				}
			} catch (InterruptedException iE) {
			}
			return true;
		}

		public void done() {
			updateEmailAccountInfo();
			setEnabled(true);
			toFront();
		}
	}

	private class TwitterConfiguration extends SwingWorker {

		public Object doInBackground() {
			setEnabled(false);
			myTwitterGui.setVisible(true);
			try {
				while (myTwitterGui.isVisible()) {
					Thread.sleep(250);
				}
			} catch (InterruptedException iE) {
			}
			return null;
		}

		public void done() {
			updateTwitterInfo();
			updateStart();
			setEnabled(true);
			toFront();
		}
	}
}
