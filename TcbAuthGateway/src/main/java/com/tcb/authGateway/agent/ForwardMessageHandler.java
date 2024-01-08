package com.tcb.authGateway.agent;

//import com.sage.mgc.*;


public class ForwardMessageHandler extends AbstractSocketMessageHandler {

    public ForwardMessageHandler() {
    }



    public ForwardMessageHandler(ICommunicationAgent owner) {
        super(owner);
    }



    public Message doMessageHandle(Message msg, ICommunicationAgent from, ICommunicationAgent[] to) throws Exception {
        return msg;
    }



    protected void startImpl() {
    }



    protected void stopImpl() {
    }
}
