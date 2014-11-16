package paxchecker.check;

/**
 *
 * @author Sunny
 */
public abstract class Check {

  protected javax.swing.JLabel linkLabel;

  public Check() {
  }

  public void init(paxchecker.gui.Status s) {
    linkLabel = s.addLinkJLabel();
  }

  public abstract boolean ticketsFound();

  public abstract void updateLink();

  public abstract String getLink();

  public abstract void reset();

  public abstract void updateGUI(paxchecker.gui.Status s);

}
