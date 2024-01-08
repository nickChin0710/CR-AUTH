package com.tcb.authGateway.socket;

import java.util.EventObject;
import java.net.*;

public class ServerSocketEvent extends EventObject {
  public ServerSocketEvent(Object source) {
    super(source);
  }

  public String getHostIPAddress() {
    ServerSocket ss = (ServerSocket)source;
    return ss.getInetAddress().getHostAddress();
  }

  public int getHostPort() {
    ServerSocket ss = (ServerSocket)source;
    return ss.getLocalPort();
  }


}