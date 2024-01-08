package com.tcb.authGateway.socket;

/**
 * <p>Title: Taipei Bank XML Transforming System</p>
 * <p>Description: A middle system between IBM S/390 and R6, is responsible to transform data between fixed format data and XML</p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: Sage Information Corp.</p>
 * @author Roger Chen
 * @version 1.0
 */

public class ClientSocketErrorEvent extends ClientSocketEvent {

  public int code = 0;
  public String description = "";
  public ClientSocketErrorEvent(Object source) {
    super(source);
  }

  public ClientSocketErrorEvent(Object source, int code, String desc) {
    this(source);
    this.code = code;
    this.description = desc;
  }
}