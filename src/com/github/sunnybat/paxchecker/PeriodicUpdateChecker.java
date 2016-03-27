package com.github.sunnybat.paxchecker;

/**
 *
 * @author SunnyBat
 */
public class PeriodicUpdateChecker implements Runnable {

  private Updater myUpdater;
  private long nanosBetweenChecks;
  private long lastCheckTime;

  public PeriodicUpdateChecker(Updater update) {
    this(update, 60 * 24); // 1 day between checks
  }

  public PeriodicUpdateChecker(Updater update, int minutesBetweenChecks) {
    myUpdater = update;
    nanosBetweenChecks = (long) minutesBetweenChecks * 60 * 1000 * 1000000; // Convert minutes -> seconds -> milliseconds -> nanoseconds
  }

  @Override
  public void run() {
    lastCheckTime = System.nanoTime();
    while (true) {
      if (System.nanoTime() - lastCheckTime > nanosBetweenChecks) {
        lastCheckTime = System.nanoTime();
        myUpdater.loadUpdates(null);
        if (System.nanoTime() - nanosBetweenChecks * 0.5 > lastCheckTime) { // It's taken over 50% of the time between checks for the user to respond
          lastCheckTime = System.nanoTime(); // So reset the last check time so we don't prompt too often (or even twice in a row)
        }
      }
      try {
        Thread.sleep(1000);
      } catch (InterruptedException iE) {
      }
    }
  }

}
