package com.tcb.authGateway.thread;

public class ThreadPoolExecutionException extends RuntimeException {

  public ThreadPoolExecutionException() {
  }



  public ThreadPoolExecutionException(String message) {
    super(message);
  }



  public ThreadPoolExecutionException(String message, Throwable cause) {
    super(message, cause);
  }



  public ThreadPoolExecutionException(Throwable cause) {
    super(cause);
  }
}