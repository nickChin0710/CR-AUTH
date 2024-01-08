package com.tcb.authGateway.socket;

/**
 * <p>Title: Taipei Bank XML Transforming System</p>
 * <p>Description: A middle system between IBM S/390 and R6, is responsible to transform data between fixed format data and XML</p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: Sage Information Corp.</p>
 * @author Roger Chen
 * @version 1.0
 */

public class ClientSocketReadEvent extends ClientSocketEvent {

  public byte data[];
  public ClientSocketReadEvent(Object source) {
    super(source);
  }
  public ClientSocketReadEvent(Object source, byte data[]) {
    super(source);
    this.data = data;
  }

}