package paxchecker.tickets;

/**
 * Interface for different check methods.
 * @author SunnyBat
 */
public interface CheckMethod {

  public void init();
  public void checkForUpdates();
  public void linkFound(String link);

}
