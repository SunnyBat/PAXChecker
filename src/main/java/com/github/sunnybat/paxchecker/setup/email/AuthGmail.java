/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.sunnybat.paxchecker.setup.email;

import com.github.sunnybat.commoncode.email.account.GmailAccount;
import com.github.sunnybat.commoncode.error.ErrorBuilder;
import com.github.sunnybat.commoncode.oauth.OauthStatusUpdater;
import com.github.sunnybat.paxchecker.resources.ResourceConstants;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.datatransfer.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.LayoutStyle;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

/**
 *
 * @author SunnyBat
 */
public class AuthGmail extends com.github.sunnybat.commoncode.javax.swing.JPanel implements AuthEmail, OauthStatusUpdater {

    private GmailAccount savedGmailAccount;
    private GmailAccount currentGmailAccount;
    private Runnable authCallback;
    private CountDownLatch authPinCountdown;
    private boolean authenticating;

    /**
     * Creates new form AuthGmail
     *
     * @param authCallback The callback for authentication
     */
    public AuthGmail(Runnable authCallback) {
        this.authCallback = authCallback;
        initComponents();
    }

    @Override
    public void authFailure() {
        setPinInputState(false);
        JBAuthenticate.setEnabled(true); // Possible interrupted, so this would be disabled
        JBAuthenticate.setText("Authenticate");
        JTFAuthUrl.setText(null);
    }

    @Override
    public void authSuccess() {
        setPinInputState(false);
        JTFAuthUrl.setText(null);
        JBAuthenticate.setEnabled(false);
        JBAuthenticate.setText("Authenticate");
        JBResetLogin.setEnabled(true);
    }

    @Override
    public void setAuthUrl(final String url) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                JTFAuthUrl.setText(url);
                JTFAuthUrl.setCaretPosition(0);
                JBCopyAuthUrl.setEnabled(url != null);
            }
        });
    }

    @Override
    public void promptForAuthorizationPin() {
        setPinInputState(true);
        authPinCountdown = new CountDownLatch(1);
    }

    @Override
    public String getAuthorizationPin() {
        try {
            authPinCountdown.await();
        } catch (InterruptedException ex) {
            System.out.println("Interrupted while waiting for PIN input, proceeding");
        }
        String pin = getTextFromComponent(JTFPin).trim();
        setPinInputState(false);
        return pin;
    }

    @Override
    public void updateStatus(final String status) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                JLAuthStatus.setText(status);
            }
        });
    }

    @Override
    public void recordCurrentFields() {
        savedGmailAccount = currentGmailAccount;
    }

    @Override
    public void resetChanges() {
        currentGmailAccount = savedGmailAccount;
        updatePanel(isAuthenticated());
    }

    @Override
    public boolean isAuthenticated() {
        return currentGmailAccount != null;
    }

    @Override
    public GmailAccount getEmailAccount() {
        return currentGmailAccount;
    }

    public void authenticate() {
        try {
            authWithoutWait(true).get();
        } catch (InterruptedException | ExecutionException e) {
        }
    }

    private AuthenticationWorker authWithoutWait(boolean failIfNoAutoAuth) {
        authCallback.run();
        currentGmailAccount = new GmailAccount("PAXChecker", ResourceConstants.RESOURCE_LOCATION, ResourceConstants.CLIENT_SECRET_JSON_PATH);
        JBAuthenticate.setEnabled(false);
        JLAuthStatus.setText("<Authenticating>");
        AuthenticationWorker myAuthWorker = new AuthenticationWorker(currentGmailAccount, failIfNoAutoAuth, JCBForcePinAuth.isSelected(), this);
        myAuthWorker.execute();
        return myAuthWorker;
    }

    private void updatePanel(boolean authSuccessful) {
        if (authSuccessful) {
            JLAuthStatus.setText("Success");
            JBAuthenticate.setText("(Already authenticated)");
            JBResetLogin.setEnabled(true);
        } else {
            JLAuthStatus.setText("Failed");
            JBAuthenticate.setText("Authenticate");
            currentGmailAccount = null;
            JBAuthenticate.setEnabled(true);
            JBResetLogin.setEnabled(false);
        }
        JBCopyAuthUrl.setEnabled(false);
        JTFAuthUrl.setBackground(new Color(240, 240, 240));
    }

    private void setPinInputState(boolean enabled) {
        JBSubmitPin.setEnabled(enabled);
        JTFPin.setEnabled(enabled);
        JLPin.setEnabled(enabled);
        if (!enabled) {
            JTFPin.setText(null);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new JLabel();
        JLAuthStatus = new JLabel();
        JBAuthenticate = new JButton();
        JBCopyAuthUrl = new JButton();
        JBResetLogin = new JButton();
        JTFAuthUrl = new JTextField();
        JLAuthUrl = new JLabel();
        JLPin = new JLabel();
        JBSubmitPin = new JButton();
        JTFPin = new JTextField();
        JCBForcePinAuth = new JCheckBox();

        setPreferredSize(new Dimension(395, 140));

        jLabel1.setText("Authentication Status:");

        JLAuthStatus.setText("<Not Attempted>");

        JBAuthenticate.setText("Authenticate");
        JBAuthenticate.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                JBAuthenticateActionPerformed(evt);
            }
        });

        JBCopyAuthUrl.setText("Copy URL");
        JBCopyAuthUrl.setEnabled(false);
        JBCopyAuthUrl.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                JBCopyAuthUrlActionPerformed(evt);
            }
        });

        JBResetLogin.setText("Reset Login");
        JBResetLogin.setEnabled(false);
        JBResetLogin.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                JBResetLoginActionPerformed(evt);
            }
        });

        JTFAuthUrl.setEditable(false);

        JLAuthUrl.setText("Auth URL");

        JLPin.setText("Auth PIN");

        JBSubmitPin.setText("Submit");
        JBSubmitPin.setEnabled(false);
        JBSubmitPin.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                JBSubmitPinActionPerformed(evt);
            }
        });

        JTFPin.setEnabled(false);
        JTFPin.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent evt) {
                JTFPinKeyTyped(evt);
            }
        });

        JCBForcePinAuth.setText("Force PIN Auth");

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(JBAuthenticate)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(JCBForcePinAuth)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 70, Short.MAX_VALUE)
                        .addComponent(JBResetLogin, GroupLayout.PREFERRED_SIZE, 105, GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addGap(18, 18, 18)
                        .addComponent(JLAuthStatus)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addComponent(JLAuthUrl)
                            .addComponent(JLPin, GroupLayout.PREFERRED_SIZE, 45, GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addComponent(JTFPin)
                            .addComponent(JTFAuthUrl))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addComponent(JBCopyAuthUrl, GroupLayout.Alignment.TRAILING)
                            .addComponent(JBSubmitPin, GroupLayout.Alignment.TRAILING, GroupLayout.PREFERRED_SIZE, 79, GroupLayout.PREFERRED_SIZE))))
                .addContainerGap())
        );
        layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(JLAuthStatus))
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(JLAuthUrl)
                    .addComponent(JTFAuthUrl, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(JBCopyAuthUrl))
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(JBSubmitPin)
                    .addComponent(JTFPin, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(JLPin))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 13, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(JBAuthenticate)
                        .addComponent(JCBForcePinAuth))
                    .addComponent(JBResetLogin, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void JBAuthenticateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_JBAuthenticateActionPerformed
        if (authenticating) {
            System.out.println("Currently authenticating?");
        } else {
            authWithoutWait(false);
        }
    }//GEN-LAST:event_JBAuthenticateActionPerformed

    private void JBCopyAuthUrlActionPerformed(ActionEvent evt) {//GEN-FIRST:event_JBCopyAuthUrlActionPerformed
        if (JTFAuthUrl.getText() != null && !JTFAuthUrl.getText().isEmpty()) {
            StringSelection stringSelection = new StringSelection(JTFAuthUrl.getText());
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(stringSelection, null);
            System.out.println("Copied to clipboard");
        }
    }//GEN-LAST:event_JBCopyAuthUrlActionPerformed

    private void JBResetLoginActionPerformed(ActionEvent evt) {//GEN-FIRST:event_JBResetLoginActionPerformed
        try {
            int result = JOptionPane.showConfirmDialog(null,
                "This will clear your saved Gmail credentials, and you will have to log in again through your browser in order to use the Gmail API email option. This is not reversible.\r\nAre you sure you want to delete your Gmail credentials?",
                "Delete Credentials",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.WARNING_MESSAGE);
            if (result == JOptionPane.YES_OPTION) {
                System.out.println("Delete!");
                currentGmailAccount.deleteCredentials();
                currentGmailAccount = null;
                savedGmailAccount = null;
                updatePanel(false);
                // TODO Force save all options
            } else {
                System.out.println("No delete");
            }
        } catch (IOException ioe) {
            new ErrorBuilder()
                .setErrorMessage("Unable to delete credentials. If you wish to "
                    + "delete them manually, delete the .credentials folder in "
                    + ResourceConstants.RESOURCE_LOCATION)
                .buildWindow();
        }
    }//GEN-LAST:event_JBResetLoginActionPerformed

    private void JBSubmitPinActionPerformed(ActionEvent evt) {//GEN-FIRST:event_JBSubmitPinActionPerformed
        authPinCountdown.countDown();
    }//GEN-LAST:event_JBSubmitPinActionPerformed

    private void JTFPinKeyTyped(KeyEvent evt) {//GEN-FIRST:event_JTFPinKeyTyped
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            authPinCountdown.countDown();
        }
    }//GEN-LAST:event_JTFPinKeyTyped


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JButton JBAuthenticate;
    private JButton JBCopyAuthUrl;
    private JButton JBResetLogin;
    private JButton JBSubmitPin;
    private JCheckBox JCBForcePinAuth;
    private JLabel JLAuthStatus;
    private JLabel JLAuthUrl;
    private JLabel JLPin;
    private JTextField JTFAuthUrl;
    private JTextField JTFPin;
    private JLabel jLabel1;
    // End of variables declaration//GEN-END:variables

    private class AuthenticationWorker extends SwingWorker<Boolean, Integer> {

        private GmailAccount toCheck;
        private boolean onlyAutoAuth;
        private boolean forcePinAuth;
        private OauthStatusUpdater userInteractor;

        public AuthenticationWorker(GmailAccount toCheck, boolean onlyAutoAuth, boolean forcePinAuth, OauthStatusUpdater userInteractor) {
            this.toCheck = toCheck;
            this.onlyAutoAuth = onlyAutoAuth;
            this.forcePinAuth = forcePinAuth;
            this.userInteractor = userInteractor;
        }

        @Override
        protected Boolean doInBackground() throws Exception {
            authenticating = true;
            boolean wasAuthSuccessful;
            if (onlyAutoAuth) {
                wasAuthSuccessful =  toCheck.checkAutoAuth();
            } else {
                JTFAuthUrl.setBackground(Color.WHITE);
                JCBForcePinAuth.setEnabled(false);
                wasAuthSuccessful = toCheck.checkAuthentication(true, !forcePinAuth, true, userInteractor);
            }
            updatePanel(wasAuthSuccessful);
            authenticating = false;
            return wasAuthSuccessful;
        }

        @Override
        protected void done() {
            authCallback.run();
        }
    }

}
