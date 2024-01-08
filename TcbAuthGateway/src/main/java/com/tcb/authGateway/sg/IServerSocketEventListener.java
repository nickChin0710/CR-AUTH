package com.tcb.authGateway.sg;

import java.util.EventListener;
import com.tcb.authGateway.socket.ClientSocketErrorEvent;
import com.tcb.authGateway.socket.ClientSocketEvent;
import com.tcb.authGateway.socket.ClientSocketReadEvent;
import com.tcb.authGateway.socket.ClientSocketWriteEvent;
import com.tcb.authGateway.socket.ServerSocketErrorEvent;
import com.tcb.authGateway.socket.ServerSocketEvent;

public interface IServerSocketEventListener extends EventListener {
  public void onClientConnectEvent(ClientSocketEvent ev);

  public void onClientDisconnectEvent(ClientSocketEvent ev);

  public void onReadEvent(ClientSocketReadEvent ev);

  public void onWriteEvent(ClientSocketWriteEvent ev);

  public void onClientErrorEvent(ClientSocketErrorEvent ev);

  public void onErrorEvent(ServerSocketErrorEvent ev);

  public void onListen(ServerSocketEvent ev);

  public void onClose(ServerSocketEvent ev);

}