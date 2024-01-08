package com.tcb.authGateway.agent;

public interface IMessageHandler {

    Message doMessageHandle(Message msg, ICommunicationAgent from, ICommunicationAgent[] to) throws Exception;



    void start();



    void stop();



    void setCommunicationAgent(ICommunicationAgent agent);

}