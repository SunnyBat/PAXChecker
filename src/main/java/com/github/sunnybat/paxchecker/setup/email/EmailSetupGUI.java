/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.sunnybat.paxchecker.setup.email;

import com.github.sunnybat.commoncode.email.EmailAddress;
import com.github.sunnybat.commoncode.email.account.EmailAccount;
import com.github.sunnybat.commoncode.preferences.PreferenceHandler;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.LayoutStyle;
import javax.swing.WindowConstants;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author SunnyBat
 */
public class EmailSetupGUI extends javax.swing.JFrame {

	private AuthenticationCallback myCallback = new AuthenticationCallback();
	private AuthGmail authGmail = new AuthGmail(myCallback);
	private AuthSMTP authSmtp = new AuthSMTP(myCallback);
	private PreferenceHandler prefs;
	private EmailAccount finalizedEmailAccount;
	private boolean disableEmail = false;
	private List<EmailAddress> savedEmailAddresses;
	private boolean savedIsGmail;

	/**
	 * Creates new form EmailUIWrapper
	 *
	 * @param prefs The Preferences to save email configuration settings to and load from
	 */
	public EmailSetupGUI(PreferenceHandler prefs) {
		this.prefs = prefs;
		initComponents();
		customComponents();
	}

	private void customComponents() {
		String smtpAddress = prefs.getStringPreference("EMAIL");
		String emailString = prefs.getStringPreference("CELLNUM");
		String emailType = prefs.getStringPreference("EMAILTYPE");

		// TODO: we need to initialize everything here, including Send To
		// addresses and the EmailAccount we're using
		if (emailType != null && emailType.equalsIgnoreCase("SMTP")) {
			JRBSMTP.setSelected(true);
			setAuthPanel(authSmtp);
			savedIsGmail = false;
			if (smtpAddress != null) {
				authSmtp.setEmailAddress(smtpAddress);
			} else {
				System.out.println("smtpIsNull");
			}
			authSmtp.recordCurrentFields();
		} else {
			JRBGmail.setSelected(true);
			setAuthPanel(authGmail);
			savedIsGmail = true;
			if (emailType != null) { // Assumed to be Gmail
				authGmail.authenticate();
			}
			authGmail.recordCurrentFields();
		}

		if (emailString != null) {
			List<EmailAddress> addresses = EmailAddress.convertToList(emailString);
			for (EmailAddress address : addresses) {
				DefaultTableModel table = (DefaultTableModel) JTCellNumbers.getModel();
				table.addRow(new Object[]{address.getCarrierName().equalsIgnoreCase("[Other]") ? address.getCompleteAddress() : address.getAddressBeginning(), address.getCarrierName()});
			}
		}

		savedEmailAddresses = getCurrentEmails();
		savedIsGmail = JRBGmail.isSelected();
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				int result = JOptionPane.showConfirmDialog(null,
						"Would you like to save your changes?\r\nYes: Save Changes\r\nNo: Disable Email\r\nCancel: Discard changes\r\n[X] Button: Keep window open",
						"Save Changes",
						JOptionPane.YES_NO_CANCEL_OPTION,
						JOptionPane.QUESTION_MESSAGE);
				if (result == JOptionPane.YES_OPTION) {
					saveChanges();
				} else if (result == JOptionPane.NO_OPTION) {
					disableEmail();
				} else if (result == JOptionPane.CANCEL_OPTION) {
					cancelChanges();
				}
			}
		});
	}

	/**
	 * Gets the currently configured EmailAccount. This includes the email addresses currently
	 * configured to be sent to. The EmailAccount must be successfully authenticated, otherwise null
	 * will be returned.
	 *
	 * @return The EmailAccount configured, or null if not set up
	 */
	public EmailAccount getEmailAccount() {
		if (disableEmail) {
			return null;
		}

		EmailAccount account;
		if (JRBGmail.isSelected() && authGmail.isAuthenticated()) {
			account = authGmail.getEmailAccount();
		} else if (JRBSMTP.isSelected() && authSmtp.isAuthenticated()) {
			account = authSmtp.getEmailAccount();
		} else {
			return null;
		}

		// === Add emails to account ===
		account.clearAllSendAddresses(); // TODO Check to make sure this is the right thing to do, or if there's a better way
		DefaultTableModel tableModel = (DefaultTableModel) JTCellNumbers.getModel();
		if (tableModel.getRowCount() == 0) {
			return null;
		} else {
			for (int i = 0; i < tableModel.getRowCount(); i++) {
				EmailAddress toAdd;
				String emailBeginning = (String) tableModel.getValueAt(i, 0);
				String emailCarrier = (String) tableModel.getValueAt(i, 1);
				if (emailCarrier.equalsIgnoreCase("[Other]")) {
					toAdd = new EmailAddress(emailBeginning);
				} else {
					toAdd = new EmailAddress(emailBeginning + EmailAddress.getCarrierExtension(emailCarrier));
				}
				account.addBccEmailAddress(toAdd);
			}
		}

		finalizedEmailAccount = account;
		return finalizedEmailAccount;
	}

	public String getEmailType() {
		if (JRBGmail.isSelected() && authGmail.isAuthenticated()) {
			return "Gmail API";
		} else if (JRBSMTP.isSelected() && authSmtp.isAuthenticated()) {
			return "SMTP";
		} else {
			return "Disabled";
		}
	}

	/**
	 * Gets the semicolon-delimited String representing all of the email addresses configured.
	 *
	 * @return All the configured email addresses
	 */
	public String getEmailAddressesString() {
		List<EmailAddress> addresses = getCurrentEmails();
		StringBuilder allAddresses = new StringBuilder();
		for (int i = 0; i < addresses.size(); i++) {
			if (i > 0) {
				allAddresses.append(";");
			}
			allAddresses.append(addresses.get(i).getCompleteAddress());
		}
		return allAddresses.toString();
	}

	private List<EmailAddress> getCurrentEmails() {
		List<EmailAddress> ret = new ArrayList<>();
		DefaultTableModel tableModel = (DefaultTableModel) JTCellNumbers.getModel();
		for (int i = 0; i < tableModel.getRowCount(); i++) {
			try {
				EmailAddress toAdd;
				String emailBeginning = (String) tableModel.getValueAt(i, 0);
				String emailCarrier = (String) tableModel.getValueAt(i, 1);
				if (emailCarrier.equalsIgnoreCase("[Other]")) {
					toAdd = new EmailAddress(emailBeginning);
				} else {
					toAdd = new EmailAddress(emailBeginning + EmailAddress.getCarrierExtension(emailCarrier));
				}

				ret.add(toAdd);
			} catch (IllegalArgumentException iae) {
				System.out.println("Invalid email address: " + tableModel.getValueAt(i, 0) + tableModel.getValueAt(i, 1));
			}
		}
		return ret;
	}

	private void addEmail() {
		String cellNumber = JTFCellNumber.getText();
		String carrier = JCBCarrier.getSelectedItem().toString();
		if (!cellNumber.isEmpty()) {
			if (cellNumber.contains("@")) { // Full email configured
				carrier = "[Other]";
			}
			((DefaultTableModel) JTCellNumbers.getModel()).addRow(new Object[]{cellNumber, carrier});
			JTFCellNumber.setText(null);
			JCBCarrier.setSelectedIndex(0);
			JTFCellNumber.requestFocus();
		}
	}

	private void resetChanges() {
		DefaultTableModel model = (DefaultTableModel) JTCellNumbers.getModel();
		for (int i = model.getRowCount() - 1; i >= 0; i--) {
			model.removeRow(i);
		}
		if (savedEmailAddresses != null) {
			for (EmailAddress address : savedEmailAddresses) {
				model.addRow(new Object[]{address.getAddressBeginning(), EmailAddress.getProvider(address.getAddressEnding())});
			}
		}
		if (savedIsGmail) {
			JRBGmail.setSelected(true);
			setAuthPanel(authGmail);
		} else {
			JRBSMTP.setSelected(true);
			setAuthPanel(authSmtp);
		}
	}

	private void setAuthPanel(JPanel toUse) {
		JPAuthInfo.removeAll();
		JPAuthInfo.add(toUse);
		JPAuthInfo.revalidate();
		JPAuthInfo.repaint();
		pack();
	}

	private void resetUserInputFields() {
		JTPComponents.setSelectedIndex(0);
		JTFCellNumber.setText(null);
		JCBCarrier.setSelectedIndex(0);
	}

	private void updatePreferences() {
		EmailAccount toSave = getEmailAccount();
		if (!disableEmail && toSave != null) {
			prefs.getPreferenceObject("EMAIL").setValue(toSave.getEmailAddress());
			prefs.getPreferenceObject("CELLNUM").setValue(getEmailAddressesString());
			prefs.getPreferenceObject("EMAILTYPE").setValue(getEmailType());
			prefs.getPreferenceObject("EMAILENABLED").setValue(true);
		} else {
			prefs.getPreferenceObject("EMAIL").setValue(null);
			prefs.getPreferenceObject("CELLNUM").setValue(null);
			prefs.getPreferenceObject("EMAILTYPE").setValue(null);
			prefs.getPreferenceObject("EMAILENABLED").setValue(false);
		}
	}

	private void saveChanges() {
		if (getCurrentEmails().isEmpty()) {
			int result = JOptionPane.showConfirmDialog(null, "You have no Send To emails configured. This means emails will still be disabled.\r\nAre you sure you want to save your changes?\r\nPress Yes to save your changes, or No to add email addresses.",
					"No Emails Input",
					JOptionPane.YES_NO_OPTION,
					JOptionPane.QUESTION_MESSAGE);
			if (result == JOptionPane.NO_OPTION) {
				JTPComponents.setSelectedComponent(JPSendTo);
				return;
			}
		}
		authGmail.recordCurrentFields();
		authSmtp.recordCurrentFields();
		savedEmailAddresses = getCurrentEmails();
		savedIsGmail = JRBGmail.isSelected();
		disableEmail = false;
		setVisible(false);
		resetUserInputFields();
		updatePreferences();
	}

	private void cancelChanges() {
		authGmail.resetChanges();
		authSmtp.resetChanges();
		resetChanges();
		setVisible(false);
		resetUserInputFields();
		updatePreferences();
	}

	private void disableEmail() {
		disableEmail = true;
		authGmail.resetChanges();
		authSmtp.resetChanges();
		resetChanges();
		setVisible(false);
		resetUserInputFields();
		updatePreferences();
	}

	/**
	 * This method is called from within the constructor to initialize the form. WARNING: Do NOT
	 * modify this code. The content of this method is always regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {

    BGAuthType = new ButtonGroup();
    JTPComponents = new JTabbedPane();
    JPAuthentication = new JPanel();
    JRBGmail = new JRadioButton();
    JRBSMTP = new JRadioButton();
    JPAuthInfo = new JPanel();
    JPSendTo = new JPanel();
    JTFCellNumber = new JTextField();
    JBAddNumber = new JButton();
    JCBCarrier = new JComboBox<>();
    jLabel1 = new JLabel();
    jScrollPane1 = new JScrollPane();
    JTCellNumbers = new JTable();
    JPFinish = new JPanel();
    JBSaveChanges = new JButton();
    JBCancelChanges = new JButton();
    JBDisableEmail = new JButton();

    setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    setTitle("Email Setup");
    setResizable(false);

    BGAuthType.add(JRBGmail);
    JRBGmail.setText("Gmail API");
    JRBGmail.setToolTipText("<html>\n<i>English</i>\n<p width=\"500\">Authenticates with Google through your browser. Recommended.</p>\n<i>Tech</i>\n<p width=\"500\">Used for authenticating with Google via OAuth2.<br></p>\n</html>");
    JRBGmail.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        JRBGmailActionPerformed(evt);
      }
    });

    BGAuthType.add(JRBSMTP);
    JRBSMTP.setText("SMTP");
    JRBSMTP.setToolTipText("<html>\n<i>English</i>\n<p width=\"500\">Authenticates with any email service. Not recommended.</p>\n<i>Tech</i>\n<p width=\"500\">Authenticates with any mailserver using SMTP. Issues with this have cropped up in the past, and it's hard to detect where the problem lies. My guess is ISPs or routers blocking SMTP traffic (insane), but I don't know for sure.</p>\n</html>");
    JRBSMTP.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        JRBSMTPActionPerformed(evt);
      }
    });

    JPAuthInfo.setLayout(new BoxLayout(JPAuthInfo, BoxLayout.LINE_AXIS));

    GroupLayout JPAuthenticationLayout = new GroupLayout(JPAuthentication);
    JPAuthentication.setLayout(JPAuthenticationLayout);
    JPAuthenticationLayout.setHorizontalGroup(JPAuthenticationLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
      .addGroup(JPAuthenticationLayout.createSequentialGroup()
        .addContainerGap()
        .addComponent(JRBGmail)
        .addGap(18, 18, 18)
        .addComponent(JRBSMTP)
        .addContainerGap(249, Short.MAX_VALUE))
      .addComponent(JPAuthInfo, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
    );
    JPAuthenticationLayout.setVerticalGroup(JPAuthenticationLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
      .addGroup(JPAuthenticationLayout.createSequentialGroup()
        .addContainerGap()
        .addGroup(JPAuthenticationLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
          .addComponent(JRBGmail)
          .addComponent(JRBSMTP))
        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(JPAuthInfo, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
    );

    JTPComponents.addTab("Authentication", JPAuthentication);

    JTFCellNumber.addKeyListener(new KeyAdapter() {
      public void keyPressed(KeyEvent evt) {
        JTFCellNumberKeyPressed(evt);
      }
    });

    JBAddNumber.setText("Add Number");
    JBAddNumber.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        JBAddNumberActionPerformed(evt);
      }
    });
    JBAddNumber.addKeyListener(new KeyAdapter() {
      public void keyPressed(KeyEvent evt) {
        JBAddNumberKeyPressed(evt);
      }
    });

    JCBCarrier.setModel(new DefaultComboBoxModel<>(new String[] { "AT&T (MMS)", "AT&T (SMS)", "Verizon", "Sprint", "T-Mobile", "U.S. Cellular", "Bell", "Rogers", "Fido", "Koodo", "Telus", "Virgin (CAN)", "Wind", "Sasktel", "[Other]" }));
    JCBCarrier.addKeyListener(new KeyAdapter() {
      public void keyPressed(KeyEvent evt) {
        JCBCarrierKeyPressed(evt);
      }
    });

    jLabel1.setText("Cell Number");

    JTCellNumbers.setModel(new DefaultTableModel(
      new Object [][] {

      },
      new String [] {
        "Cell Number", "Carrier"
      }
    ) {
      Class[] types = new Class [] {
        String.class, String.class
      };
      boolean[] canEdit = new boolean [] {
        false, false
      };

      public Class getColumnClass(int columnIndex) {
        return types [columnIndex];
      }

      public boolean isCellEditable(int rowIndex, int columnIndex) {
        return canEdit [columnIndex];
      }
    });
    JTCellNumbers.setToolTipText("Delete emails by selecting them and pressing the DEL key");
    JTCellNumbers.setColumnSelectionAllowed(true);
    JTCellNumbers.addKeyListener(new KeyAdapter() {
      public void keyPressed(KeyEvent evt) {
        JTCellNumbersKeyPressed(evt);
      }
    });
    jScrollPane1.setViewportView(JTCellNumbers);

    GroupLayout JPSendToLayout = new GroupLayout(JPSendTo);
    JPSendTo.setLayout(JPSendToLayout);
    JPSendToLayout.setHorizontalGroup(JPSendToLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
      .addGroup(JPSendToLayout.createSequentialGroup()
        .addComponent(jLabel1)
        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(JTFCellNumber, GroupLayout.DEFAULT_SIZE, 147, Short.MAX_VALUE)
        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(JCBCarrier, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(JBAddNumber))
      .addComponent(jScrollPane1, GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
    );
    JPSendToLayout.setVerticalGroup(JPSendToLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
      .addGroup(GroupLayout.Alignment.TRAILING, JPSendToLayout.createSequentialGroup()
        .addContainerGap()
        .addGroup(JPSendToLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
          .addComponent(JTFCellNumber, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
          .addComponent(JBAddNumber)
          .addComponent(JCBCarrier, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
          .addComponent(jLabel1))
        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(jScrollPane1, GroupLayout.DEFAULT_SIZE, 132, Short.MAX_VALUE))
    );

    JTPComponents.addTab("Send To", JPSendTo);

    JBSaveChanges.setText("Save Changes");
    JBSaveChanges.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        JBSaveChangesActionPerformed(evt);
      }
    });

    JBCancelChanges.setText("Cancel Changes");
    JBCancelChanges.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        JBCancelChangesActionPerformed(evt);
      }
    });

    JBDisableEmail.setText("Disable Email");
    JBDisableEmail.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        JBDisableEmailActionPerformed(evt);
      }
    });

    GroupLayout JPFinishLayout = new GroupLayout(JPFinish);
    JPFinish.setLayout(JPFinishLayout);
    JPFinishLayout.setHorizontalGroup(JPFinishLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
      .addGroup(JPFinishLayout.createSequentialGroup()
        .addContainerGap()
        .addGroup(JPFinishLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
          .addComponent(JBSaveChanges, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
          .addComponent(JBCancelChanges, GroupLayout.DEFAULT_SIZE, 375, Short.MAX_VALUE)
          .addComponent(JBDisableEmail, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        .addContainerGap())
    );
    JPFinishLayout.setVerticalGroup(JPFinishLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
      .addGroup(GroupLayout.Alignment.TRAILING, JPFinishLayout.createSequentialGroup()
        .addContainerGap()
        .addComponent(JBSaveChanges)
        .addGap(18, 18, 18)
        .addComponent(JBCancelChanges)
        .addGap(18, 18, 18)
        .addComponent(JBDisableEmail)
        .addContainerGap(56, Short.MAX_VALUE))
    );

    JTPComponents.addTab("Finish", JPFinish);

    GroupLayout layout = new GroupLayout(getContentPane());
    getContentPane().setLayout(layout);
    layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
      .addComponent(JTPComponents)
    );
    layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
      .addComponent(JTPComponents)
    );

    pack();
  }// </editor-fold>//GEN-END:initComponents

    private void JRBGmailActionPerformed(ActionEvent evt) {//GEN-FIRST:event_JRBGmailActionPerformed
			setAuthPanel(authGmail);
    }//GEN-LAST:event_JRBGmailActionPerformed

    private void JRBSMTPActionPerformed(ActionEvent evt) {//GEN-FIRST:event_JRBSMTPActionPerformed
			setAuthPanel(authSmtp);
    }//GEN-LAST:event_JRBSMTPActionPerformed

    private void JBAddNumberActionPerformed(ActionEvent evt) {//GEN-FIRST:event_JBAddNumberActionPerformed
			addEmail();
    }//GEN-LAST:event_JBAddNumberActionPerformed

    private void JTCellNumbersKeyPressed(KeyEvent evt) {//GEN-FIRST:event_JTCellNumbersKeyPressed
			if (evt.getKeyCode() == KeyEvent.VK_DELETE) {
				int[] selectedIndeces = JTCellNumbers.getSelectedRows();
				for (int i = selectedIndeces.length - 1; i >= 0; i--) { // Iterate from the bottom up
					((DefaultTableModel) JTCellNumbers.getModel()).removeRow(selectedIndeces[i]);
				}
			} else if (evt.getKeyCode() == KeyEvent.VK_TAB) {
				this.transferFocus();
				evt.consume();
			}
    }//GEN-LAST:event_JTCellNumbersKeyPressed

    private void JTFCellNumberKeyPressed(KeyEvent evt) {//GEN-FIRST:event_JTFCellNumberKeyPressed
			if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
				addEmail();
			}
    }//GEN-LAST:event_JTFCellNumberKeyPressed

    private void JCBCarrierKeyPressed(KeyEvent evt) {//GEN-FIRST:event_JCBCarrierKeyPressed
			if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
				addEmail();
			}
    }//GEN-LAST:event_JCBCarrierKeyPressed

    private void JBAddNumberKeyPressed(KeyEvent evt) {//GEN-FIRST:event_JBAddNumberKeyPressed
			if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
				addEmail();
			}
    }//GEN-LAST:event_JBAddNumberKeyPressed

    private void JBSaveChangesActionPerformed(ActionEvent evt) {//GEN-FIRST:event_JBSaveChangesActionPerformed
			saveChanges();
    }//GEN-LAST:event_JBSaveChangesActionPerformed

    private void JBCancelChangesActionPerformed(ActionEvent evt) {//GEN-FIRST:event_JBCancelChangesActionPerformed
			cancelChanges();
    }//GEN-LAST:event_JBCancelChangesActionPerformed

    private void JBDisableEmailActionPerformed(ActionEvent evt) {//GEN-FIRST:event_JBDisableEmailActionPerformed
			disableEmail();
    }//GEN-LAST:event_JBDisableEmailActionPerformed

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private ButtonGroup BGAuthType;
  private JButton JBAddNumber;
  private JButton JBCancelChanges;
  private JButton JBDisableEmail;
  private JButton JBSaveChanges;
  private JComboBox<String> JCBCarrier;
  private JPanel JPAuthInfo;
  private JPanel JPAuthentication;
  private JPanel JPFinish;
  private JPanel JPSendTo;
  private JRadioButton JRBGmail;
  private JRadioButton JRBSMTP;
  private JTable JTCellNumbers;
  private JTextField JTFCellNumber;
  private JTabbedPane JTPComponents;
  private JLabel jLabel1;
  private JScrollPane jScrollPane1;
  // End of variables declaration//GEN-END:variables

	private class AuthenticationCallback implements Runnable {

		private boolean nextEnabledState = false;

		public void run() {
			JBSaveChanges.setEnabled(nextEnabledState);
			JBCancelChanges.setEnabled(nextEnabledState);
			JBDisableEmail.setEnabled(nextEnabledState);
			JRBGmail.setEnabled(nextEnabledState);
			JRBSMTP.setEnabled(nextEnabledState);
			nextEnabledState = !nextEnabledState; // Invert
		}
	}
}
