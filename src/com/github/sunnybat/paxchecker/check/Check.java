package com.github.sunnybat.paxchecker.check;

import java.util.concurrent.*;

/**
 *
 * @author Sunny
 */
public abstract class Check implements Runnable {

  private javax.swing.JLabel linkLabel;
  private Phaser cycBar;

  public Check() {
  }

  public void init(com.github.sunnybat.paxchecker.gui.Status s, java.util.concurrent.Phaser cB) {
    if (s != null) {
      linkLabel = s.addLinkJLabel();
    }
    updateLabel(s, "Initializing...");
    cycBar = cB;
    cycBar.register();
    reset();
  }

  public final void updateLabel(com.github.sunnybat.paxchecker.gui.Status s, String text) {
    if (s != null) {
      s.updateJLabel(linkLabel, text);
    }
  }

  @Override
  public final void run() {
    updateLink();
    System.out.println("Waiting: " + this.getClass().getSimpleName());
    cycBar.arrive();
  }

  public abstract boolean ticketsFound();

  public abstract void updateLink();

  public abstract String getLink();

  public abstract void reset();

  final void updateLink(String link) {
    if (linkLabel != null) {
      linkLabel.setText("Current Website Link: " + link); // Yea, this should be run on the EDT. I know, I know.
    }
  }

}
