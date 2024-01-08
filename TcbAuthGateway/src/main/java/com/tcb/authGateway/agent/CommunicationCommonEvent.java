package com.tcb.authGateway.agent;

import java.util.EventObject;


public class CommunicationCommonEvent extends EventObject {

    private String clientId = "";

    private String ip;

    private int port;

    public CommunicationCommonEvent(ICommunicationAgent source) {
        super(source);
    }



    public CommunicationCommonEvent(ICommunicationAgent source, String clientId) {
        this(source);
        this.clientId = clientId;
    }



    public CommunicationCommonEvent(ICommunicationAgent source, String clientId, String ip, int port) {
        this(source);
        this.clientId = clientId;
        this.ip = ip;
        this.port = port;
    }



    public ICommunicationAgent getCommunicationAgent() {
        return (ICommunicationAgent)source;
    }



    public String getClientID() {
        return clientId;
    }



    public String getIPAddress() {
        return ip;
    }



    public int getPort() {
        return port;
    }

}