package com.tcb.authGateway.sg;

public class SGTypeMismatchException extends RuntimeException {

  public SGTypeMismatchException(String fieldName) {
    super("Value type mismatch. (" + fieldName + ")");
  }

}