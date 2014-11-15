package paxchecker.check;

/**
 *
 * @author Sunny
 */
public abstract class Check {

  protected String expoC;

  public Check(String e) {
    expoC = e;
  }

  public abstract void init();

  public abstract boolean ticketsFound();

  public abstract void updateLink();

  public abstract String getLink();

  public abstract void updateGUI(paxchecker.gui.Status s);

}
