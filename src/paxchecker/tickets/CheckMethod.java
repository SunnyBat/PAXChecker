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
public abstract class CheckMethod implements Runnable {

  private int refreshTime;
  protected boolean forceRefresh;

  public CheckMethod() {
  }

  @Override
  public abstract void run();

  public abstract void init();

  public abstract void ticketsFound();

  public abstract void checkTickets();

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
