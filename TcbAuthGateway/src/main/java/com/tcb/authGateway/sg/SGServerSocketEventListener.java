package com.tcb.authGateway.sg;

import com.tcb.authGateway.socket.ClientSocketErrorEvent;
import com.tcb.authGateway.socket.ClientSocketEvent;
import com.tcb.authGateway.socket.ClientSocketReadEvent;
import com.tcb.authGateway.socket.ClientSocketWriteEvent;
import com.tcb.authGateway.socket.ServerSocketErrorEvent;
import com.tcb.authGateway.socket.ServerSocketEvent;

public abstract class SGServerSocketEventListener implements IServerSocketEventListener {
  public SGServerSocketEventListener() {
  }
  public void onClientConnectEvent(ClientSocketEvent ev) {
    throw new java.lang.UnsupportedOperationException("Method onClientConnectEvent() not yet implemented.");
  }
  public void onClientDisconnectEvent(ClientSocketEvent ev) {
    throw new java.lang.UnsupportedOperationException("Method onClientDisconnectEvent() not yet implemented.");
  }
  public void onReadEvent(ClientSocketReadEvent ev) {
    throw new java.lang.UnsupportedOperationException("Method onReadEvent() not yet implemented.");
  }
  public void onWriteEvent(ClientSocketWriteEvent ev) {
    throw new java.lang.UnsupportedOperationException("Method onWriteEvent() not yet implemented.");
  }
  public void onClientErrorEvent(ClientSocketErrorEvent ev) {
    throw new java.lang.UnsupportedOperationException("Method onClientErrorEvent() not yet implemented.");
  }
  public void onErrorEvent(ServerSocketErrorEvent ev) {
    throw new java.lang.UnsupportedOperationException("Method onErrorEvent() not yet implemented.");
  }
  public void onListen(ServerSocketEvent ev) {
    throw new java.lang.UnsupportedOperationException("Method onListen() not yet implemented.");
  }
  public void onClose(ServerSocketEvent ev) {
    throw new java.lang.UnsupportedOperationException("Method onClose() not yet implemented.");
  }

}