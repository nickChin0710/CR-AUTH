package com.tcb.authGateway.agent;

public interface ICommunicationEventListener {
    public void onClientConnectEvent(CommunicationCommonEvent ev);



    public void onClientDisconnectEvent(CommunicationCommonEvent ev);



    public void onClientReadEvent(CommunicationReadWriteEvent ev);



    public void onClientWriteEvent(CommunicationReadWriteEvent ev);



    public void onClientErrorEvent(CommunicationErrorEvent ev);



    public void onErrorEvent(CommunicationErrorEvent ev);



    public void onOpenEvent(CommunicationCommonEvent ev);



    public void onCloseEvent(CommunicationCommonEvent ev);

}