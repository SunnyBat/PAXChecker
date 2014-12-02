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

  /**
   * A new Notification object.
   * @param i The ID of the notification
   */
  public Notification(String i) {
    ID = i;
  }

  /**
   * Sets the title of the notification.
   * @param t The title
   */
  public void setTitle(String t) {
    title = t;
  }

  /**
   * Sets the main information of the notification.
   * @param i The main information
   */
  public void setInfo(String i) {
    info = i;
  }

  /**
   * Adds another line of information. This automatically adds
   * a line break BEFORE appending the information.
   * @param i The information to append
   */
  public void addInfo(String i) {
    info += "\n";
    info += i;
  }

  /**
   * Sets the close button text.
   * @param bt The text to set
   */
  public void setButtonText(String bt) {
    buttonText = bt;
  }

  /**
   * Sets the More Information button link.
   * @param bl The link to set
   */
  public void setButtonLink(String bl) {
    buttonLink = bl;
  }

  /**
   * Gets the ID of this notification.
   * @return The ID
   */
  public String getID() {
    return ID;
  }

  /**
   * Gets the title of this notification.
   * @return The title
   */
  public String getTitle() {
    return title;
  }

  /**
   * Gets the main information of this notification.
   * @return The main information
   */
  public String getInfo() {
    return info;
  }

  /**
   * Gets the close button text of this notification.
   * @return The close button text
   */
  public String getButtonText() {
    return buttonText;
  }

  /**
   * Gets the More Information button link of this notification.
   * @return The More Information button link
   */
  public String getButtonLink() {
    return buttonLink;
  }

}
