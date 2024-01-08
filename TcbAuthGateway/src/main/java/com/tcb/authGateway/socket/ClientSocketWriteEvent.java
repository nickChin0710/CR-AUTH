package com.tcb.authGateway.socket;

/**
 * <p>Title: Taipei Bank XML Transforming System</p>
 * <p>Description: A middle system between IBM S/390 and R6, is responsible to transform data between fixed format data and XML</p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: Sage Information Corp.</p>
 * @author Roger Chen
 * @version 1.0
 */

public class ClientSocketWriteEvent extends ClientSocketEvent {

  public byte data[];
  public ClientSocketWriteEvent(Object source) {
    super(source);
  }

  public ClientSocketWriteEvent(Object source, byte data[]) {
    this(source);
    this.data = data;
  }
}