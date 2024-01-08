package com.tcb.authGateway.thread;

public class ThreadStatus {
  private String typeName;

  private static ThreadStatus[] types = new ThreadStatus[5];

  private static int index = 0;

  public static ThreadStatus IDLE = new ThreadStatus("idle");

  public static ThreadStatus BUSY = new ThreadStatus("busy");

  public static ThreadStatus STOPPED = new ThreadStatus("stopped");

  public static ThreadStatus SUSPEND = new ThreadStatus("suspended");

  public static ThreadStatus INIT = new ThreadStatus("initialized");

  private ThreadStatus(String typeName) {
    this.typeName = typeName;
    types[index++] = this;
  }



  public static ThreadStatus getThreadStatus(String statusDesc) {
    for (int i = 0; i < types.length; i++) {
      if (types[i].toString().equals(statusDesc))
        return types[i];
    }

    return null;
  }



  public String toString() {
    return typeName;
  }

}
