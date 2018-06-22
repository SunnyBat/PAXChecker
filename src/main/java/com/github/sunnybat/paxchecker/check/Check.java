package com.github.sunnybat.paxchecker.check;

import com.github.sunnybat.paxchecker.status.CheckerInfoOutput;
import java.util.concurrent.Phaser;

/**
 *
 * @author Sunny
 */
public abstract class Check implements Runnable {

  private CheckerInfoOutput infoOutput;
  private Phaser cycBar;

  public Check() {
  }

  /**
   * Initializes this Checker.
   *
   * @param output The non-null CheckerInfoOutput to update with new information
   * @param cB The non-null Phaser to update when done
   * @throws IllegalArgumentException if output or cB is null
   */
  public void init(CheckerInfoOutput output, java.util.concurrent.Phaser cB) {
    if (output == null || cB == null) {
      throw new IllegalArgumentException("output and cB cannot be null");
    }
    infoOutput = output;
    updateWithInfo("Initializing...");
    cycBar = cB;
    cycBar.register();
    reset();
    updateWithInfo("Checker initialized");
  }

  public final void updateWithInfo(String text) {
    infoOutput.update(text);
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
    if (infoOutput != null) {
      if (this.getClass().getSimpleName().length() <= 5) {
        infoOutput.update("Current (???) Link: " + link);
      } else {
        infoOutput.update("Current " + this.getClass().getSimpleName().substring(5) + " Link: " + link);
      }
    }
  }

}
