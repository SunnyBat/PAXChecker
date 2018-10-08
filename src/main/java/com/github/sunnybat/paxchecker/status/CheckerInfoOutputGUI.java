package com.github.sunnybat.paxchecker.status;

import javax.swing.JLabel;

/**
 *
 * @author SunnyBat
 */
public class CheckerInfoOutputGUI extends JLabel implements CheckerInfoOutput {

	@Override
	public void update(final String output) {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				if (output != null && output.length() > 0) {
					setText(output);
				} else {
					setText(" ");
				}
			}
		});
	}

}
