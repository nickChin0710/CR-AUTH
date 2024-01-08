package com.tcb.authGateway.sg;

public class SGDocumentAccessException extends RuntimeException {
  public SGDocumentAccessException(String message) {
    super(message);
  }



  public SGDocumentAccessException(String message, Throwable cause) {
    super(message, cause);
  }



  public SGDocumentAccessException(Throwable cause) {
    super(cause);
  }

}