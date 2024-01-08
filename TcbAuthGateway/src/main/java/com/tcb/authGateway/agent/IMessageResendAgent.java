package com.tcb.authGateway.agent;

import com.tcb.authGateway.queue.IQueue;

//import com.sage.util.IQueue;


/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public interface IMessageResendAgent {
    IQueue getQueue();



    void setQueue(IQueue queue);



    void addMessage(ResendMessage message);



    void start();



    void stop();



    void setCommunicationAgent(ICommunicationAgent agent);
}