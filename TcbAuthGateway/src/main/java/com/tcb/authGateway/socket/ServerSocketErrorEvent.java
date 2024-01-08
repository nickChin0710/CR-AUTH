package com.tcb.authGateway.socket;

public class ServerSocketErrorEvent extends ServerSocketEvent {
  public int Code = 0;
  public String Description = "";

  public ServerSocketErrorEvent(Object source) {
    super(source);
  }

  public ServerSocketErrorEvent(Object source, int Code, String Desc) {
    this(source);
    this.Code = Code;
    this.Description = Desc;
  }

}