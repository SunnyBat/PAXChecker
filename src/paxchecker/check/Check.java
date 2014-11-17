package paxchecker.check;

import java.util.concurrent.*;

/**
 *
 * @author Sunny
 */
public abstract class Check implements Runnable {

  public static final Object LOCK = new Object();
  protected javax.swing.JLabel linkLabel;
  private Phaser cycBar;

  public Check() {
  }

  public void init(paxchecker.gui.Status s, java.util.concurrent.Phaser cB) {
    linkLabel = s.addLinkJLabel();
    cycBar = cB;
    cycBar.register();
    reset();
  }

  @Override
  public void run() {
    updateLink();
    System.out.println("Waiting: " + this.getClass().getSimpleName());
    cycBar.arriveAndAwaitAdvance();
  }

  public abstract boolean ticketsFound();

  public abstract void updateLink();

  public abstract String getLink();

  public abstract void reset();

  public abstract void updateGUI(paxchecker.gui.Status s);

}
