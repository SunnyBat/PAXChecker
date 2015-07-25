package com.github.sunnybat.paxchecker.status;

/**
 *
 * @author SunnyBat
 */
public abstract class Status {

  public abstract void doProcessing();

  protected void sendEmail(String subject, String message) {
    throw new UnsupportedOperationException("Not implemented yet.");
  }

}
