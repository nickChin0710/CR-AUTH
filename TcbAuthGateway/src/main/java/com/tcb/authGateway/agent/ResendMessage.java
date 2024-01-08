package com.tcb.authGateway.agent;

public class ResendMessage {
    private Message message;

    private String agentName;

    private long lastResendTime;

    private int resendCount = -1;

    private String fromId;

    public ResendMessage(String agentName, Message message, String fromId) {
        this.agentName = agentName;
        this.message = message;
        this.fromId = fromId;
        resend();
    }



    public Message getMessage() {
        return this.message;
    }



    public String getAgentName() {
        return this.agentName;
    }



    public long getLastResendTime() {
        return this.lastResendTime;
    }



    public int getResendCount() {
        return this.resendCount;
    }



    public void resend() {
        this.lastResendTime = System.currentTimeMillis();
        resendCount++;
    }



    public String getFromId() {
        return this.fromId;
    }

}
