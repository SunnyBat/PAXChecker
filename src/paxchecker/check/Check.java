package paxchecker.check;

/**
 *
 * @author Sunny
 */
public abstract class Check {

  protected String expoC;
  protected javax.swing.JLabel linkLabel;

  public Check(String e) {
    expoC = e;
  }

  public void init(paxchecker.gui.Status s) {
    linkLabel = s.addLinkJLabel();
  }

  public abstract boolean ticketsFound();

  public abstract void updateLink();

  public abstract String getLink();

  public abstract void updateGUI(paxchecker.gui.Status s);

}
