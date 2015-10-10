package com.github.sunnybat.paxchecker.status;

/**
 *
 * @author SunnyBat
 */
public interface Status {

  public void enableEmail();
  public void enableAlarm();
  public void enableTwitter();
  public void setTwitterStatus(boolean enabled);
  public void setTwitterStatus(String status);
  public void twitterStreamKilled();
  public void setInformationText(String text);
  public void setLastCheckedText(String text);
  public void setLastCheckedText(int seconds);
  public void setForceCheckEnabled(boolean enabled);
  public void setDataUsageText(double dataUsed);
  public void setDataUsageText(String dataUsed);
  public int getButtonPressed();
  public void resetButtonPressed();

}
