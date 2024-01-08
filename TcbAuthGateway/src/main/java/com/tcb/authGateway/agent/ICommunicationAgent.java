package com.tcb.authGateway.agent;

import java.util.*;
//import com.sage.util.IQueue;
//import com.sage.log.SGLogger;
//import com.sage.document.DocumentConfig;
import com.tcb.authGateway.queue.IQueue;
import com.tcb.authGateway.sg.DocumentConfig;
import com.tcb.authGateway.sg.SGLogger;


public interface ICommunicationAgent {

    /**
     * Open a specific client connection by specified ID.
     * @param id the identifier for the target connection.
     */
    void open(String id) throws Exception;



    /**
     * Close a specific client connection by specified ID.
     * @param id the identifier for the target connection.
     */
    void close(String id) throws Exception;



    /**
     * Open all client connections.
     */
    void open() throws Exception;



    /**
     * Close all client connections.
     */
    void close() throws Exception;



    /**
     * Add an implement of <I>ICommunicationEventListener</I> into this CommunicationAgent.
     * @param el an implement of <I>ICommunicationEventListener</I>
     */
    void addCommunicationEventListener(ICommunicationEventListener el);



    /**
     * Set a correlation table between phsical connection feature and client identifier.
     * @param table a Hashtable or a subclass of Hashtable which contains config information about the connections.
     */
    void setCorrelationTable(Hashtable table);



    void setTargetCommunicationAgents(ICommunicationAgent[] subscribers);



    void setTargetCommunicationAgent(ICommunicationAgent subscriber);



    ICommunicationAgent[] getTargetCommunicationAgents();



    void setMessageResendManager(IMessageResendManager mrm);



    /**
     * Send data to remote via a specific client by specified ID.
     * @param id the target client which is used to send the data to remote.
     * @param data the data to be sent to remote.
     * @throws java.lang.Exception
     */
    void send(String id, byte[] data) throws Exception;



    /**
     * Broadcast data to all remotes.
     * @param data the data to be broadcasted.
     * @throws java.lang.Exception
     */
    void sendAll(byte[] data) throws Exception;



    /**
     * Suspend a specific client by specified ID.
     * When a client is suspended, it would not be able to send or receive any data, but remain the connection with remote.
     * @param id the identifier for the target client to be suspended.
     */
    void suspend(String id);



    /**
     * Suspend all clients.
     * When all clients are suspended, they would not be able to send or receive any data, but remian all connections with remote.
     */
    void suspendAll();



    /**
     * Resume a specific client by specifeid ID.
     * @param id the identifier for the target client to be resumed.
     */
    void resume(String id);



    /**
     * Resume all clients.
     */
    void resumeAll();



    /**
     * Return the number of current active connections.
     * @return the number of current active connections.
     */
    int getCommunicationCount();



    /**
     * Return the maximum number of acceptable connections.
     * @return the maximum number of acceptable connections.
     */
    int getMaximumCommunicationCount();



    String getCommunicationAgentStatus();



    boolean isOpened();



    boolean isClientConnected(String id);



    void setName(String name);



    String getName();



    void setMessageProcessCenter(MessageProcessCenter messageProcessCenter);



    MessageProcessCenter getMessageProcessCenter();



    void setProcessQueue(IQueue queue);



    void setLogger(SGLogger logger);



    SGLogger getLogger();



    void setMessageGateway(IMessageGateway messageGateway);



    IMessageGateway getMessageGateway();



    DocumentConfig loadMessageHandlerConfig();



    void setEnabled(boolean enabled);



    boolean isEnabled();



    boolean isUndeliverableMessageQueueable();

}