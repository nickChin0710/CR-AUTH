package com.tcb.authGateway.agent;

public class CommunicationReadWriteEvent extends CommunicationCommonEvent {

    private byte[] data;

    public CommunicationReadWriteEvent(ICommunicationAgent source, String clientId, byte[] data) {
        super(source, clientId);
        this.data = data;
    }



    public byte[] getData() {
        return data;
    }

}