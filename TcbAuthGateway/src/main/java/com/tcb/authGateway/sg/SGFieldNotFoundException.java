package com.tcb.authGateway.sg;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class SGFieldNotFoundException extends RuntimeException {

  public SGFieldNotFoundException(String fieldName) {
    super("Field not found. (" + fieldName + ")");
  }

}