package com.tcb.authGateway.thread;

import com.tcb.authGateway.sg.SGLogger;

//import com.sage.log.SGLogger;


public abstract class AbstractThread extends Thread {

  private boolean isStop = false;

  private boolean isSuspend = false;

  private int interval = 50;

  public AbstractThread() {
  }



  public AbstractThread(int interval) {
    this.interval = interval;
  }



  public AbstractThread(ThreadGroup group, String name, int interval) {
    super(group, name);
    this.interval = interval;
  }



  protected abstract void doMainProcess();



  public final void resumeThread() {
    isSuspend = false;
    synchronized (this) {
      notify();
    }
  }



  public final void suspendThread() {
    isSuspend = true;
  }



  public final void stopThread() {
    isStop = true;
  }



  public final void run() {
    while (!isStop) {
      if (isSuspend) {
        synchronized (this) {
          try {
            wait();
          }
          catch (InterruptedException ex) {
          }
        }
      }
      try {
        doMainProcess();
      }
      catch (Throwable ex) {
        SGLogger.getLogger().logError(ex);
      }
      try {
        sleep(interval);
      }
      catch (InterruptedException ex1) {
      }
    }
  }

}