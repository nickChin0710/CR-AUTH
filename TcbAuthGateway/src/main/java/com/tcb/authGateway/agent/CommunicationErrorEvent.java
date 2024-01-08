package com.tcb.authGateway.agent;

public class CommunicationErrorEvent extends CommunicationCommonEvent {

    private String errCode = "";

    private String errMsg = "";

    public CommunicationErrorEvent(ICommunicationAgent source, String clientId, String errCode, String errMsg) {
        super(source, clientId);
        this.errCode = errCode;
        this.errMsg = errMsg;
    }



    public String getErrorCode() {
        return errCode;
    }



    public String getErrorMessage() {
        return errMsg;
    }
}