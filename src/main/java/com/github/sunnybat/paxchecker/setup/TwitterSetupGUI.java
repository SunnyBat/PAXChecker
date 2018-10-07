package com.github.sunnybat.paxchecker.setup;

import com.github.sunnybat.commoncode.oauth.OauthStatusUpdater;
import com.github.sunnybat.commoncode.preferences.PreferenceHandler;
import com.github.sunnybat.paxchecker.check.TwitterAccount;
import java.awt.Color;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.LayoutStyle;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.WindowConstants;
import twitter4j.Twitter;

/**
 *
 * @author SunnyBat
 */
public class TwitterSetupGUI extends com.github.sunnybat.commoncode.javax.swing.JFrame implements OauthStatusUpdater {

    private CountDownLatch authPinCountdown;
    private TwitterAccount myTwitterAccount = new TwitterAccount();
    private final Object authLock = new Object();
    private boolean isAuthenticating;
    private boolean disableTwitter = false;
    private PreferenceHandler prefs;

    /**
     * Creates new form TwitterSetupGUI
     */
    public TwitterSetupGUI(PreferenceHandler prefs) {
        this.prefs = prefs;
        initComponents();
        customComponents();
    }

    private void customComponents() {
        waitForAutoAuthentication();
        JCBFilterTwitter.setSelected(prefs.getBooleanPreference("FILTER_TWITTER"));
        JCBTextTweets.setSelected(prefs.getBooleanPreference("TEXT_TWEETS"));
    }

    @Override
    public void authFailure() {
        JBAuthenticate.setEnabled(true); // Possible interrupted, so this would be disabled
        JBAuthenticate.setText("Authenticate");
    }

    @Override
    public void authSuccess() {
        JBAuthenticate.setEnabled(false);
        JBAuthenticate.setText("Authenticate");
        JBClearAuthentication.setEnabled(true);
    }

    @Override
    public void setAuthUrl(final String url) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                JLAuthUrl.setEnabled(true);
                JTFAuthUrl.setEnabled(true);
                JTFAuthUrl.setBackground(Color.WHITE);
                JTFAuthUrl.setText(url);
                JTFAuthUrl.setCaretPosition(0);
            }
        });
    }

    @Override
    public void promptForAuthorizationPin() {
        setPinInputState(true);
        authPinCountdown = new CountDownLatch(1);
    }

    @Override
    public void cancelAuthorizationPinPrompt() {
        setPinInputState(false);
        if (authPinCountdown != null) {
            authPinCountdown.countDown();
        }
    }

    @Override
    public String getAuthorizationPin() {
        try {
            authPinCountdown.await();
        } catch (InterruptedException ex) {
            System.out.println("Interrupted while waiting for PIN input, proceeding");
        }
        String pin = getTextFromComponent(JTFBackupPin).trim();
        setPinInputState(false);
        return pin;
    }

    @Override
    public void updateStatus(final String status) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                JLStatus.setText(status);
            }
        });
    }

    public Twitter getTwitterAccount() {
        return myTwitterAccount.getAccount();
    }

    public void waitForAutoAuthentication() {
        try {
            executeAuthentication(true).get();
        } catch (InterruptedException | ExecutionException e) {
        }
        if (getTwitterAccount() != null) {
            JLStatus.setText("Authenticated");
        } else {
            JLStatus.setText("Not Authenticated");
        }
    }

    public boolean shouldFilterTwitter() {
        return JCBFilterTwitter.isSelected();
    }

    public boolean shouldTextTweets() {
        return JCBTextTweets.isSelected();
    }

    public boolean isTwitterEnabled() {
        return !disableTwitter && myTwitterAccount.getAccount() != null;
    }

    private void setPinInputState(boolean enabled) {
        JBSubmitPin.setEnabled(enabled);
        JTFBackupPin.setEnabled(enabled);
        JLBackupPin.setEnabled(enabled);
        if (!enabled) {
            JTFBackupPin.setText(null);
        }
    }

    private void setAuthenticationState(boolean authenticating) {
            synchronized (authLock) {
                isAuthenticating = authenticating;
                if (authenticating) {
                    JBSave.setEnabled(false);
                    JCBForcePinAuth.setEnabled(false);
                    JBCopyUrl.setEnabled(true);
                    JBCopyUrl.setText("Copy URL");
                } else {
                    setPinInputState(false);
                    JBSave.setEnabled(true);
                    JCBForcePinAuth.setEnabled(true);
                    JBCopyUrl.setEnabled(false);
                    JLAuthUrl.setEnabled(false);
                    JTFAuthUrl.setEnabled(false);
                    JTFAuthUrl.setText(null);
                    JTFAuthUrl.setBackground(new Color(240, 240, 240));
                }
            }
    }

    private AuthWorker executeAuthentication(boolean failIfNoAutoAuth) {
        AuthWorker myAuthWorker = new AuthWorker(this, JCBForcePinAuth.isSelected(), failIfNoAutoAuth);
        myAuthWorker.execute();
        return myAuthWorker;
    }

    private void updatePreferences() {
        if (isTwitterEnabled()) {
            prefs.getPreferenceObject("CHECK_TWITTER").setValue(true);
            prefs.getPreferenceObject("FILTER_TWITTER").setValue(JCBFilterTwitter.isSelected());
            prefs.getPreferenceObject("TEXT_TWEETS").setValue(JCBTextTweets.isSelected());
        } else {
            prefs.getPreferenceObject("CHECK_TWITTER").setValue(false);
            prefs.getPreferenceObject("FILTER_TWITTER").setValue(false);
            prefs.getPreferenceObject("TEXT_TWEETS").setValue(false);
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

        JTPMain = new JTabbedPane();
        jPanel1 = new JPanel();
        jLabel2 = new JLabel();
        JLStatus = new JLabel();
        JLAuthUrl = new JLabel();
        JTFAuthUrl = new JTextField();
        JLBackupPin = new JLabel();
        JTFBackupPin = new JTextField();
        JBCopyUrl = new JButton();
        JCBForcePinAuth = new JCheckBox();
        JBSubmitPin = new JButton();
        JBAuthenticate = new JButton();
        JBClearAuthentication = new JButton();
        jPanel2 = new JPanel();
        JCBFilterTwitter = new JCheckBox();
        JCBTextTweets = new JCheckBox();
        jPanel3 = new JPanel();
        JBSave = new JButton();
        JBDisableTwitter = new JButton();

        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        jLabel2.setText("Authentication Status:");

        JLStatus.setText("Not Authenticated");

        JLAuthUrl.setText("Auth URL");
        JLAuthUrl.setEnabled(false);

        JTFAuthUrl.setEditable(false);
        JTFAuthUrl.setToolTipText("<html>\n<p width=\"500\">If your default browser does not automatically open up with this URL, you will need to copy+paste this into your browser to continue.<br>\nNote that this URL MUST be opened on this machine if you're not using PIN authentication.<br>\nUse PIN authentication if you're unable to use a browser on this computer.</p>\n</html>");
        JTFAuthUrl.setEnabled(false);

        JLBackupPin.setText("Auth PIN");
        JLBackupPin.setEnabled(false);

        JTFBackupPin.setEnabled(false);
        JTFBackupPin.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent evt) {
                JTFBackupPinKeyTyped(evt);
            }
        });

        JBCopyUrl.setText("Copy URL");
        JBCopyUrl.setEnabled(false);
        JBCopyUrl.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                JBCopyUrlActionPerformed(evt);
            }
        });

        JCBForcePinAuth.setText("Force PIN authentication");
        JCBForcePinAuth.setToolTipText("<html>\n<p width=\"500\">Use this if you need to open the Auth URL on a different computer or if the default method does not work for you.</p>\n</html>");

        JBSubmitPin.setText("Submit");
        JBSubmitPin.setEnabled(false);
        JBSubmitPin.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                JBSubmitPinActionPerformed(evt);
            }
        });

        JBAuthenticate.setText("Start Authentication");
        JBAuthenticate.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                JBAuthenticateActionPerformed(evt);
            }
        });

        JBClearAuthentication.setText("Clear Authentication");
        JBClearAuthentication.setEnabled(false);
        JBClearAuthentication.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                JBClearAuthenticationActionPerformed(evt);
            }
        });

        GroupLayout jPanel1Layout = new GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(JLStatus, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                            .addComponent(JLBackupPin, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(JLAuthUrl, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addComponent(JTFAuthUrl)
                            .addComponent(JTFBackupPin))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                            .addComponent(JBCopyUrl)
                            .addComponent(JBSubmitPin, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(JCBForcePinAuth)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(JBAuthenticate)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 115, Short.MAX_VALUE)
                        .addComponent(JBClearAuthentication)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(JLStatus))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(JTFAuthUrl, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(JLAuthUrl)
                    .addComponent(JBCopyUrl))
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(JLBackupPin)
                    .addComponent(JTFBackupPin, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(JBSubmitPin))
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(JCBForcePinAuth)
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(JBAuthenticate)
                    .addComponent(JBClearAuthentication))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        JTPMain.addTab("Authentication", jPanel1);

        JCBFilterTwitter.setText("Filter by Keywords");

        JCBTextTweets.setText("Text Tweets");

        GroupLayout jPanel2Layout = new GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(jPanel2Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(JCBFilterTwitter)
                    .addComponent(JCBTextTweets))
                .addContainerGap(274, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(jPanel2Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(JCBFilterTwitter)
                .addGap(10, 10, 10)
                .addComponent(JCBTextTweets)
                .addContainerGap(109, Short.MAX_VALUE))
        );

        JTPMain.addTab("Configuration", jPanel2);

        JBSave.setText("Save");
        JBSave.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                JBSaveActionPerformed(evt);
            }
        });

        JBDisableTwitter.setText("Disable Twitter");
        JBDisableTwitter.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                JBDisableTwitterActionPerformed(evt);
            }
        });

        GroupLayout jPanel3Layout = new GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(jPanel3Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(JBSave, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(JBDisableTwitter, GroupLayout.DEFAULT_SIZE, 375, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(jPanel3Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(JBSave)
                .addGap(18, 18, 18)
                .addComponent(JBDisableTwitter)
                .addContainerGap(97, Short.MAX_VALUE))
        );

        JTPMain.addTab("Finish", jPanel3);

        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addComponent(JTPMain)
        );
        layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addComponent(JTPMain)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void JBAuthenticateActionPerformed(ActionEvent evt) {//GEN-FIRST:event_JBAuthenticateActionPerformed
        synchronized (authLock) {
            if (isAuthenticating) {
                JBAuthenticate.setEnabled(false);
                myTwitterAccount.interrupt();
                cancelAuthorizationPinPrompt();
            } else {
                JBAuthenticate.setText("Cancel Auth");
                executeAuthentication(false);
            }
        }
    }//GEN-LAST:event_JBAuthenticateActionPerformed

    private void JBSubmitPinActionPerformed(ActionEvent evt) {//GEN-FIRST:event_JBSubmitPinActionPerformed
        authPinCountdown.countDown();
    }//GEN-LAST:event_JBSubmitPinActionPerformed

    private void JBClearAuthenticationActionPerformed(ActionEvent evt) {//GEN-FIRST:event_JBClearAuthenticationActionPerformed
        int result = JOptionPane.showConfirmDialog(null,
            "This will clear your saved Twitter credentials, and you will have to log in again through your browser in order to use Twitter. This is not reversible.\r\nAre you sure you want to delete your Twitter credentials?",
            "Delete Credentials",
            JOptionPane.YES_NO_CANCEL_OPTION,
            JOptionPane.WARNING_MESSAGE);
        if (result == JOptionPane.YES_OPTION) {
            myTwitterAccount.clearAccessToken();
            myTwitterAccount = new TwitterAccount();
            JBAuthenticate.setEnabled(true);
            JBClearAuthentication.setEnabled(false);
            JLStatus.setText("Authentication cleared");
        }
    }//GEN-LAST:event_JBClearAuthenticationActionPerformed

    private void JTFBackupPinKeyTyped(KeyEvent evt) {//GEN-FIRST:event_JTFBackupPinKeyTyped
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            authPinCountdown.countDown();
        }
    }//GEN-LAST:event_JTFBackupPinKeyTyped

    private void JBSaveActionPerformed(ActionEvent evt) {//GEN-FIRST:event_JBSaveActionPerformed
        setVisible(false);
        disableTwitter = false;
        if (myTwitterAccount.getAccount() != null) {
            JLStatus.setText("Authenticated");
        } else {
            JLStatus.setText("Not Authenticated");
        }
        updatePreferences();
    }//GEN-LAST:event_JBSaveActionPerformed

    private void JBCopyUrlActionPerformed(ActionEvent evt) {//GEN-FIRST:event_JBCopyUrlActionPerformed
        StringSelection authUrl = new StringSelection(JTFAuthUrl.getText());
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(authUrl, authUrl);
        JBCopyUrl.setText("Copied");
    }//GEN-LAST:event_JBCopyUrlActionPerformed

    private void JBDisableTwitterActionPerformed(ActionEvent evt) {//GEN-FIRST:event_JBDisableTwitterActionPerformed
        synchronized (authLock) {
            if (isAuthenticating) {
                JBAuthenticate.setEnabled(false);
                myTwitterAccount.interrupt();
                cancelAuthorizationPinPrompt();
            }
            disableTwitter = true;
        }
        setVisible(false);
        updatePreferences();
    }//GEN-LAST:event_JBDisableTwitterActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JButton JBAuthenticate;
    private JButton JBClearAuthentication;
    private JButton JBCopyUrl;
    private JButton JBDisableTwitter;
    private JButton JBSave;
    private JButton JBSubmitPin;
    private JCheckBox JCBFilterTwitter;
    private JCheckBox JCBForcePinAuth;
    private JCheckBox JCBTextTweets;
    private JLabel JLAuthUrl;
    private JLabel JLBackupPin;
    private JLabel JLStatus;
    private JTextField JTFAuthUrl;
    private JTextField JTFBackupPin;
    private JTabbedPane JTPMain;
    private JLabel jLabel2;
    private JPanel jPanel1;
    private JPanel jPanel2;
    private JPanel jPanel3;
    // End of variables declaration//GEN-END:variables

    private class AuthWorker extends SwingWorker<Boolean, Integer> {

        private OauthStatusUpdater authInterface;
        private boolean forcePinAuth;
        private boolean failIfNoAutoAuth;

        public AuthWorker(OauthStatusUpdater authInterface, boolean forcePinAuth, boolean failIfNoAutoAuth) {
            this.authInterface = authInterface;
            this.forcePinAuth = forcePinAuth;
            this.failIfNoAutoAuth = failIfNoAutoAuth;
        }

        @Override
        protected Boolean doInBackground() {
            setAuthenticationState(true);
            myTwitterAccount.authenticate(authInterface, forcePinAuth, failIfNoAutoAuth);
            setAuthenticationState(false);
            return isTwitterEnabled();
        }
    }

}
