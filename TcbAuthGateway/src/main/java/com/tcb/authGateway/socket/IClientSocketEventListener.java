package com.tcb.authGateway.socket;

/**
 * <p>Title: Taipei Bank XML Transforming System</p>
 * <p>Description: A middle system between IBM S/390 and R6, is responsible to transform data between fixed format data and XML</p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: Sage Information Corp.</p>
 * @author Roger Chen
 * @version 1.0
 */

public interface IClientSocketEventListener extends java.util.EventListener {
  public void onConnectEvent(ClientSocketEvent ev);

  public void onDisconnectEvent(ClientSocketEvent ev);

  public void onReadEvent(ClientSocketReadEvent ev);

  public void onWriteEvent(ClientSocketWriteEvent ev);

  public void onErrorEvent(ClientSocketErrorEvent ev);
}