package com.tcb.authGateway.socket;

/**
 * <p>Title: Taipei Bank XML Transforming System</p>
 * <p>Description: A middle system between IBM S/390 and R6, is responsible to transform data between fixed format data and XML</p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: Sage Information Corp.</p>
 * @author Roger Chen
 * @version 1.0
 */

public class ClientSocketEvent extends java.util.EventObject {

  public String getHostIPAddress() {
    return ((ClientSocket)this.source).getRemoteIPAddress();
  }

  public int getHostPort() {
    return ((ClientSocket)this.source).getRemotePort();
  }

  public String getLocalIPAddress() {
    return ((ClientSocket)this.source).getLocalIPAddress();
  }


  public int getLocalPort() {
    return ((ClientSocket)this.source).getLocalPort();
  }

  public ClientSocketEvent(Object source) {
    super(source);
  }

}