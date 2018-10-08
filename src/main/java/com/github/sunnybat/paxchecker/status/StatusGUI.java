package com.github.sunnybat.paxchecker.status;

import com.github.sunnybat.commoncode.email.EmailAddress;
import com.github.sunnybat.paxchecker.DataTracker;
import com.github.sunnybat.paxchecker.Expo;
import com.github.sunnybat.paxchecker.browser.Browser;
import com.github.sunnybat.paxchecker.notification.NotificationWindow;
import com.github.sunnybat.paxchecker.resources.ResourceLoader;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.LayoutStyle;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.WindowConstants;

/**
 *
 * @author SunnyBat
 */
public class StatusGUI extends com.github.sunnybat.commoncode.javax.swing.JFrame implements Status {

	private final NotificationWindow infoWindow;
	private ACTION_TYPE actionRequested;
	private SwingWorker infoReset = new InformationResetter();
	private MenuItem maximizeButton = new MenuItem("Restore Window");
	private MenuItem closeButton = new MenuItem("Close Program");
	private MenuItem forceCheckButton = new MenuItem("Force Check");
	private MenuItem testTextButton = new MenuItem("Test Text");
	private MenuItem testAlarmButton = new MenuItem("Test Alarm");
	private MenuItem reconnectTwitterButton = new MenuItem("Reconnect to Twitter");
	private final int TEXT_DELAY_TIME = 300; // Seconds
	private final int INFORMATION_CLEAR_DELAY_TIME = 15; // Seconds

	public StatusGUI(Expo expo) {
		this(expo, null, null);
	}

	/**
	 * Creates a new Status window. Note that this does not display when created. Also note that this
	 * will block until all the components have been created (generally does not take long).
	 *
	 * @param expo The expo to check -- this String is not checked in any way
	 * @param emailAddress The email address used to send emails
	 * @param addresses The List of addresses to send emails to
	 */
	public StatusGUI(final Expo expo, final String emailAddress, final List<EmailAddress> addresses) {
		invokeAndWaitOnEDT(new Runnable() {
			@Override
			public void run() {
				initComponents();
				customComponents(expo, emailAddress, addresses);
			}
		});
		infoWindow = new NotificationWindow("What's happening???", "If a link is pointing to a seemingly unrelated event, that's expected. "
				+ "This is happening because the PAX website has not removed the current Showclix link (which normally 404s after the event has taken "
				+ "place). This is fine.\n\n"
				+ "There are several reasons why you might be seeing [messages].\n"
				+ "For the PAX Website, you might be seeing [NoFind]. Currently, this is only happening with PAX East. This means that the program was "
				+ "unable to find the button on the PAX registration page.\n"
				+ "For the Showclix Scanner, you might be seeing [No New Events]. This is also fine. It means that the PAXChecker did not find any new PAX "
				+ "events in the Showclix database -- in other words, nothing has changed. \n"
				+ "For the Known Pages option, you'll most likely be seeing [None Found]. This is normal, and it means that no known links have been found. "
				+ "There's no need to worry about this one.\n\n"
				+ "Feel free to message /u/SunnyBat if you have any further questions.");
	}

	public void customComponents(final Expo expo, final String emailAddress, final List<EmailAddress> addresses) {
		maximizeButton.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				restoreFromTray();
			}
		});
		closeButton.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				System.exit(0);
			}
		});
		forceCheckButton.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				buttonPressed(ACTION_TYPE.FORCE_CHECK);
			}
		});
		testTextButton.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				buttonPressed(ACTION_TYPE.TEST_TEXT);
			}
		});
		testAlarmButton.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				buttonPressed(ACTION_TYPE.TEST_ALARM);
			}
		});
		reconnectTwitterButton.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				buttonPressed(ACTION_TYPE.RECONNECT_TWITTER);
			}
		});
		getPopupMenu().add(maximizeButton);
		getPopupMenu().add(closeButton);
		getPopupMenu().addSeparator();
		getPopupMenu().add(forceCheckButton);
		try {
			setIcon(javax.imageio.ImageIO.read(ResourceLoader.loadResource(expo.toString().replaceAll("\\ ", "") + ".png"))); // CHECK: This seems hacky...
		} catch (Exception e) {
			e.printStackTrace();
		}
		JLTwitterStatus.setVisible(false); // Enable when Twitter enabled
		JBReconnectTwitter.setVisible(false);
		JLTitle.setText(expo + " Website Status");
		if (addresses == null || emailAddress == null) {
			JLEmailPhone.setText("[TEXTING DISABLED]");
			JBTestText.setEnabled(false);
		} else if (addresses.size() == 1) {
			JLEmailPhone.setText(emailAddress + " -- " + addresses.get(0).getCompleteAddress());
		} else {
			JLEmailPhone.setText(emailAddress + " -- Multiple Numbers (Mouse Here to View)");
			String list = "<html>";
			String[] allAddresses = EmailAddress.convertToString(addresses).split(";");
			for (int a = 0; a < allAddresses.length; a++) {
				list += allAddresses[a].trim();
				if (a + 1 != allAddresses.length) {
					list += "<br>";
				}
			}
			list += "</html>";
			JLEmailPhone.setToolTipText(list);
		}
		setDataUsageText(DataTracker.getDataUsedMB());
	}

	@Override
	public void enableEmail() {
		getPopupMenu().add(testTextButton);
		JBTestText.setEnabled(true);
	}

	@Override
	public void enableAlarm() {
		getPopupMenu().add(testAlarmButton);
		JBTestAlarm.setEnabled(true);
	}

	@Override
	public void enableTwitter() {
		JLTwitterStatus.setVisible(true);
		pack();
	}

	@Override
	public void setTwitterStatus(final boolean isEnabled) {
		if (isEnabled) {
			setTwitterStatus("Twitter Feed: Connected");
		} else {
			setTwitterStatus("Twitter Feed: Disconnected");
		}
	}

	@Override
	public void setTwitterStatus(final String text) {
		invokeAndWaitOnEDT(new Runnable() {
			@Override
			public void run() {
				JLTwitterStatus.setText(text);
			}
		});
	}

	@Override
	public void twitterStreamKilled() {
		invokeAndWaitOnEDT(new Runnable() {
			@Override
			public void run() {
				getPopupMenu().add(reconnectTwitterButton);
				JBReconnectTwitter.setVisible(true);
				pack();
			}
		});
	}

	@Override
	public void setInformationText(final String text) {
		if (infoReset != null) { // CHECK: Synchronization?
			infoReset.cancel(true);
		}
		invokeAndWaitOnEDT(new Runnable() {
			@Override
			public void run() {
				JLInformation.setText(text);
			}
		});
		infoReset = new InformationResetter();
		infoReset.execute();
	}

	@Override
	public void setLastCheckedText(final String text) {
		invokeAndWaitOnEDT(new Runnable() {
			@Override
			public void run() {
				JLLastChecked.setText(text);
			}
		});
	}

	@Override
	public void setLastCheckedText(final int seconds) {
		setLastCheckedText("Time until next check: " + seconds + " seconds");
	}

	@Override
	public void setForceCheckEnabled(final boolean enabled) {
		invokeAndWaitOnEDT(new Runnable() {
			@Override
			public void run() {
				JBForceCheck.setEnabled(enabled);
				if (enabled) {
					getPopupMenu().add(forceCheckButton);
				} else {
					getPopupMenu().remove(forceCheckButton);
				}
			}
		});
	}

	@Override
	public void setDataUsageText(final String text) {
		invokeAndWaitOnEDT(new Runnable() {
			@Override
			public void run() {
				JLDataUsage.setText(text);
			}
		});
	}

	@Override
	public void setDataUsageText(double mb) {
		setDataUsageText("Data Used: " + mb + "MB");
	}

	/**
	 * Updates the given JLabel on the EDT.
	 *
	 * @param label
	 * @param text
	 */
	public void updateJLabel(final javax.swing.JLabel label, final String text) {
		invokeAndWaitOnEDT(new Runnable() {
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

	public CheckerInfoOutputGUI createNewInfoOutput() {
		final CheckerInfoOutputGUI jL = new CheckerInfoOutputGUI();
		jL.addMouseListener(new java.awt.event.MouseAdapter() {
			public void mouseClicked(java.awt.event.MouseEvent evt) {
				openLabelLink(jL.getText());
			}
		});
		invokeAndWaitOnEDT(new Runnable() {
			@Override
			public void run() {
				jL.setText(" ");
				JPLinks.add(jL);
				pack();
			}
		});
		return jL;
	}

	public void setIcon(final Image image) {
		invokeAndWaitOnEDT(new Runnable() {
			@Override
			public void run() {
				setIconImage(image);
				setTrayIcon("PAXChecker", image);
			}
		});
	}

	@Override
	public ACTION_TYPE getActionRequested() {
		synchronized (this) {
			return actionRequested;
		}
	}

	@Override
	public void resetButtonPressed() {
		synchronized (this) {
			actionRequested = null;
		}
	}

	private void buttonPressed(ACTION_TYPE button) {
		synchronized (this) {
			this.actionRequested = button;
		}
	}

	private class TextCountdown extends SwingWorker<Object, String> {

		@Override
		protected Object doInBackground() throws Exception { // TODO: Thread safety?
			// Start
			long startTime = System.nanoTime();
			long delayTime = TEXT_DELAY_TIME * 1000000000L;
			int lastSecondsLeft = -1;
			do {
				int secondsLeft = (int) ((delayTime - (System.nanoTime() - startTime)) / 1000000000L);
				if (secondsLeft != lastSecondsLeft) {
					publish("(" + secondsLeft + ")");
					setProgress((TEXT_DELAY_TIME - secondsLeft) / TEXT_DELAY_TIME * 100);
					lastSecondsLeft = secondsLeft;
				}
				Thread.sleep(250);
			} while (System.nanoTime() - startTime < delayTime);
			// Complete
			publish("Test Text");
			setProgress(100);
			return null;
		}

		@Override
		protected void process(final java.util.List<String> chunks) {
			if (!chunks.isEmpty()) {
				invokeAndWaitOnEDT(new Runnable() {
					@Override
					public void run() {
						JBTestText.setText(chunks.get(chunks.size() - 1));
					}
				});
			}
		}

		@Override
		protected void done() {
			JBTestText.setEnabled(true);
		}
	}

	private class InformationResetter extends SwingWorker<Object, String> {

		@Override
		protected Object doInBackground() throws Exception { // TODO: Thread safety?
			// Start
			long startTime = System.nanoTime();
			long delayTime = INFORMATION_CLEAR_DELAY_TIME * 1000000000L;
			do {
				int secondsLeft = (int) ((delayTime - (System.nanoTime() - startTime)) / 1000000000L);
				setProgress((INFORMATION_CLEAR_DELAY_TIME - secondsLeft) / INFORMATION_CLEAR_DELAY_TIME * 100);
				Thread.sleep(250);
			} while (System.nanoTime() - startTime < delayTime && !super.isCancelled());
			// Complete
			setProgress(100);
			return null;
		}

		@Override
		protected void done() {
			if (!super.isCancelled()) {
				invokeAndWaitOnEDT(new Runnable() {
					@Override
					public void run() {
						JLInformation.setText(" ");
					}
				});
			}
		}
	}

	/**
	 * This method is called from within the constructor to initialize the form. WARNING: Do NOT
	 * modify this code. The content of this method is always regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {

    JLTitle = new JLabel();
    JBTestText = new JButton();
    JBTestAlarm = new JButton();
    JBForceCheck = new JButton();
    JLLastChecked = new JLabel();
    JLInformation = new JLabel();
    JLEmailPhone = new JLabel();
    JLDataUsage = new JLabel();
    JPLinks = new JPanel();
    JLTwitterStatus = new JLabel();
    JLLinksExplanation = new JLabel();
    JBReconnectTwitter = new JButton();
    filler1 = new Box.Filler(new Dimension(0, 0), new Dimension(0, 0), new Dimension(32767, 0));

    setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    setTitle("PAXChecker");
    setResizable(false);
    addWindowListener(new WindowAdapter() {
      public void windowIconified(WindowEvent evt) {
        formWindowIconified(evt);
      }
    });

    JLTitle.setFont(new Font("Tahoma", 0, 24)); // NOI18N
    JLTitle.setHorizontalAlignment(SwingConstants.CENTER);
    JLTitle.setText("PAX Website Status");
    JLTitle.setToolTipText("");

    JBTestText.setText("Test Text");
    JBTestText.setEnabled(false);
    JBTestText.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        JBTestTextActionPerformed(evt);
      }
    });

    JBTestAlarm.setText("Test Alarm Sound");
    JBTestAlarm.setEnabled(false);
    JBTestAlarm.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        JBTestAlarmActionPerformed(evt);
      }
    });

    JBForceCheck.setText("Force Check");
    JBForceCheck.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        JBForceCheckActionPerformed(evt);
      }
    });

    JLLastChecked.setText("Time Until Check: [Initializing]");

    JLInformation.setText(" ");

    JLEmailPhone.setFont(new Font("Tahoma", 2, 11)); // NOI18N
    JLEmailPhone.setHorizontalAlignment(SwingConstants.CENTER);
    JLEmailPhone.setText("Email -- Phone");

    JLDataUsage.setText("Data Usage: [Initializing]");
    JLDataUsage.setToolTipText("<html>\nNote that this does NOT include Twitter<br>\ndata usage.<br>\nThis is only a rough estimate of data used.<br>\nFor more accurate data usage, download<br>\nWireShark (or similar software) and monitor<br>\nusage through that.\n</html>");

    JPLinks.setLayout(new BoxLayout(JPLinks, BoxLayout.LINE_AXIS));
    JPLinks.setLayout(new BoxLayout(JPLinks, BoxLayout.Y_AXIS));

    JLTwitterStatus.setText("Twitter Feed: Connecting...");

    JLLinksExplanation.setFont(new Font("Tahoma", 2, 10)); // NOI18N
    JLLinksExplanation.setForeground(new Color(0, 0, 238));
    JLLinksExplanation.setText("Why are these links pointing to random events? What's up with [Message]?");
    JLLinksExplanation.addMouseListener(new MouseAdapter() {
      public void mousePressed(MouseEvent evt) {
        JLLinksExplanationMousePressed(evt);
      }
    });

    JBReconnectTwitter.setText("Reconnect Twitter Stream");
    JBReconnectTwitter.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        JBReconnectTwitterActionPerformed(evt);
      }
    });

    GroupLayout layout = new GroupLayout(getContentPane());
    getContentPane().setLayout(layout);
    layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addContainerGap()
        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
          .addGroup(layout.createSequentialGroup()
            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
              .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING, false)
                .addComponent(JLDataUsage, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(JLInformation, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(JLLastChecked, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(JPLinks, GroupLayout.Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 380, GroupLayout.PREFERRED_SIZE))
              .addComponent(JLTitle, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
              .addGroup(layout.createSequentialGroup()
                .addComponent(JBTestText, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(JBTestAlarm, GroupLayout.PREFERRED_SIZE, 185, GroupLayout.PREFERRED_SIZE))
              .addComponent(JBForceCheck, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
              .addComponent(JLEmailPhone, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGap(10, 10, 10))
          .addGroup(layout.createSequentialGroup()
            .addComponent(JLTwitterStatus)
            .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
          .addGroup(layout.createSequentialGroup()
            .addComponent(JLLinksExplanation)
            .addGap(0, 0, Short.MAX_VALUE))
          .addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
              .addComponent(JBReconnectTwitter, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
              .addComponent(filler1, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addContainerGap())))
    );
    layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addContainerGap()
        .addComponent(JLTitle)
        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(JLEmailPhone)
        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(JPLinks, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        .addGap(0, 0, 0)
        .addComponent(JLTwitterStatus)
        .addGap(0, 0, 0)
        .addComponent(JLLinksExplanation)
        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        .addComponent(JLLastChecked)
        .addGap(0, 0, 0)
        .addComponent(JLDataUsage)
        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(JLInformation)
        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
          .addComponent(JBTestText)
          .addComponent(JBTestAlarm))
        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(JBForceCheck)
        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(filler1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
        .addGap(0, 0, 0)
        .addComponent(JBReconnectTwitter)
        .addContainerGap())
    );

    pack();
  }// </editor-fold>//GEN-END:initComponents

  private void JBForceCheckActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_JBForceCheckActionPerformed
		buttonPressed(ACTION_TYPE.FORCE_CHECK);
  }//GEN-LAST:event_JBForceCheckActionPerformed

  private void JBTestTextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_JBTestTextActionPerformed
		JBTestText.setEnabled(false); // TODO: Move this to TextCountdown so it doesn't have to be called here
		SwingWorker worker = new TextCountdown();
		worker.execute();
		buttonPressed(ACTION_TYPE.TEST_TEXT);
  }//GEN-LAST:event_JBTestTextActionPerformed

  private void JBTestAlarmActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_JBTestAlarmActionPerformed
		buttonPressed(ACTION_TYPE.TEST_ALARM);
  }//GEN-LAST:event_JBTestAlarmActionPerformed

  private void formWindowIconified(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowIconified
		minimizeToTray();
  }//GEN-LAST:event_formWindowIconified

  private void JLLinksExplanationMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_JLLinksExplanationMousePressed
		infoWindow.setVisible(true);
  }//GEN-LAST:event_JLLinksExplanationMousePressed

  private void JBReconnectTwitterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_JBReconnectTwitterActionPerformed
		buttonPressed(ACTION_TYPE.RECONNECT_TWITTER);
		setTwitterStatus("Twitter Feed: Reconnecting...");
		JBReconnectTwitter.setVisible(false);
		getPopupMenu().remove(reconnectTwitterButton);
  }//GEN-LAST:event_JBReconnectTwitterActionPerformed

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private JButton JBForceCheck;
  private JButton JBReconnectTwitter;
  private JButton JBTestAlarm;
  private JButton JBTestText;
  private JLabel JLDataUsage;
  private JLabel JLEmailPhone;
  private JLabel JLInformation;
  private JLabel JLLastChecked;
  private JLabel JLLinksExplanation;
  private JLabel JLTitle;
  private JLabel JLTwitterStatus;
  private JPanel JPLinks;
  private Box.Filler filler1;
  // End of variables declaration//GEN-END:variables
}
