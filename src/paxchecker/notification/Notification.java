package paxchecker.notification;

/**
 *
 * @author Sunny
 */
public class Notification {

  private final String ID;
  private String title;
  private String info;
  private String buttonText;
  private String buttonLink;

  public Notification(String i) {
    ID = i;
  }

  public void setTitle(String t) {
    title = t;
  }

  public void setInfo(String i) {
    info = i;
  }

  /**
   * Adds another line of information. This automatically adds a line break.
   * @param i The information to append to the end
   */
  public void addInfo(String i) {
    info += "\n";
    info += i;
  }

  public void setButtonText(String bt) {
    buttonText = bt;
  }

  public void setButtonLink(String bl) {
    buttonLink = bl;
  }

  public String getID() {
    return ID;
  }

  public String getTitle() {
    return title;
  }

  public String getInfo() {
    return info;
  }

  public String getButtonText() {
    return buttonText;
  }

  public String getButtonLink() {
    return buttonLink;
  }

}
