/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package paxchecker.tickets;

/**
 *
 * @author Sunny
 */
public abstract class CheckMethod {

  private int refreshTime;
  protected boolean forceRefresh;
  protected Runnable checkRunnable;
  private Thread running;

  public CheckMethod() {
  }

  public CheckMethod(Runnable r) {
    checkRunnable = r;
  }

  public abstract void init();

  public abstract void ticketsFound();

  public final void checkForTickets() {
    if (checkRunnable == null) {
      System.out.println("ERROR: Check for tickets not properly configured!");
      return;
    }
    if (running == null || !running.isAlive()) {
      new Thread(checkRunnable).start();
    } else {
      System.out.println("ERROR: Thread is currently running!");
    }
  }

  public final void setRefreshTime(int time) {
    if (time > 60) {
      time = 60;
    } else if (time < 10) {
      time = 10;
    }
    refreshTime = time;
  }

  public final int getRefreshTime() {
    return refreshTime;
  }

  public final void forceRefresh() {
    forceRefresh = true;
  }

}
