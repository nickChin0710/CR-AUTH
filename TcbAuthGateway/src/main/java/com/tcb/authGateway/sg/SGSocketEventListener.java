package com.tcb.authGateway.sg;

import com.tcb.authGateway.socket.ClientSocketErrorEvent;
import com.tcb.authGateway.socket.ClientSocketEvent;
import com.tcb.authGateway.socket.ClientSocketReadEvent;
import com.tcb.authGateway.socket.ClientSocketWriteEvent;
import com.tcb.authGateway.socket.IClientSocketEventListener;

/**
 * <p>Title: Taipei Bank XML Transforming System</p>
 * <p>Description: A middle system between IBM S/390 and R6, is responsible to transform data between fixed format data and XML</p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: Sage Information Corp.</p>
 * @author Roger Chen
 * @version 1.0
 */

public abstract class SGSocketEventListener implements IClientSocketEventListener {
  public SGSocketEventListener() {
  }
  public void onConnectEvent(ClientSocketEvent ev) {
    /**@todo Implement this com.sage.socket.IClientSocketEventListener method*/
    throw new java.lang.UnsupportedOperationException("Method onConnectEvent() not yet implemented.");
  }
  public void onDisconnectEvent(ClientSocketEvent ev) {
    /**@todo Implement this com.sage.socket.IClientSocketEventListener method*/
    throw new java.lang.UnsupportedOperationException("Method onDisconnectEvent() not yet implemented.");
  }
  public void onReadEvent(ClientSocketReadEvent ev) {
    /**@todo Implement this com.sage.socket.IClientSocketEventListener method*/
    throw new java.lang.UnsupportedOperationException("Method onReadEvent() not yet implemented.");
  }
  public void onWriteEvent(ClientSocketWriteEvent ev) {
    /**@todo Implement this com.sage.socket.IClientSocketEventListener method*/
    throw new java.lang.UnsupportedOperationException("Method onWriteEvent() not yet implemented.");
  }
  public void onErrorEvent(ClientSocketErrorEvent ev) {
    /**@todo Implement this com.sage.socket.IClientSocketEventListener method*/
    throw new java.lang.UnsupportedOperationException("Method onErrorEvent() not yet implemented.");
  }

}