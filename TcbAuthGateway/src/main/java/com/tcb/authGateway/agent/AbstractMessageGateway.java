package com.tcb.authGateway.agent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import com.tcb.authGateway.sg.SGLogger;

//import com.sage.log.SGLogger;

public abstract class AbstractMessageGateway implements IMessageGateway {

    private List messageAgentList = new ArrayList();

    private SGLogger logger = SGLogger.getLogger();

    /**
     * <p>
     * This method reserved for subclass to decide what to do when this class has been constructed.
     * <p>
     * This abstract class only creates a new vector to store all <I>ICommunicationAgent</I> and
     * another new vector to store all <I>MessageQueue</I> when this class has been constructed. It
     * is not its responsibility to add <I>ICommunicationAgent</I> into MessageAgentList and add
     * <I>MessageQueue</I> into MessageQueueList. The subclass should do this in this method.
     */
    protected abstract void initImpl();

    public abstract CommunicationAgentConfig loadCommunicationAgentConfig(String agentName);
//by Kevin remove
//    protected abstract void startImpl();
//
//    protected abstract void stopImpl();

    public AbstractMessageGateway() {
    }

    public void init() {
        messageAgentList.clear();
        initImpl();
    }

    public SGLogger getLogger() {
        return logger;
    }

    protected void addCommunicationAgent(ICommunicationAgent ca) {
        messageAgentList.add(ca);
    }

    public ICommunicationAgent getCommunicationAgent(String alias) {
        for (Iterator iter = messageAgentList.iterator(); iter.hasNext();) {
            ICommunicationAgent ca = (ICommunicationAgent) iter.next();
            if (ca.getName().equals(alias)) {
                return ca;
            }
        }
        return null;
    }

    public int getCommunicationAgentIndex(String alias) {
        for (int i = 0; i < messageAgentList.size(); i++) {
            ICommunicationAgent agent = (ICommunicationAgent) messageAgentList.get(i);
            if (agent.getName().equals(alias)) {
                return i;
            }
        }

        return -1;
    }

    public final void startGateway() throws Exception {

//        startImpl();

        for (Iterator iter = messageAgentList.iterator(); iter.hasNext();) {
            ICommunicationAgent ca = (ICommunicationAgent) iter.next();
            try {
                ca.open();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public final void startCommunicationAgent(int index) throws Exception {
        if (index < messageAgentList.size()) {
            ICommunicationAgent ca = (ICommunicationAgent) messageAgentList.get(index);
            ca.open();
        } else
            throw new Exception("No such CommunicationAgent exists.");
    }

    public final ICommunicationAgent getCommunicationAgent(int index) {
        if (index < messageAgentList.size()) {
            ICommunicationAgent ca = (ICommunicationAgent) messageAgentList.get(index);
            return ca;
        } else
            return null;
    }

    public int getCommunicationCount() {
        return messageAgentList.size();
    }

    public final void stopGateway() throws Exception {
        for (Iterator iter = messageAgentList.iterator(); iter.hasNext();) {
            ICommunicationAgent ca = (ICommunicationAgent) iter.next();
            try {
                ca.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

//        stopImpl();
    }

    public final void stopCommunicationAgent(int index) throws Exception {
        if (index < messageAgentList.size()) {
            ICommunicationAgent ca = (ICommunicationAgent) messageAgentList.get(index);
            ca.close();
        } else
            throw new Exception("No such CommunicationAgent exists.");
    }

    public final void setDisplay(boolean state) {
        logger.setLogDisplay(state);
        showMessage(state ? "Display is on." : "Display is off.");
    }

    public final void setTrace(boolean state) {
        logger.setCurrentLoggingLevel(state ? 8 : 2);
        showMessage(state ? "Trace is on." : "Trace is off.");
    }

    public final void setLog(boolean state) {
        logger.setLogToFile(state);
        showMessage(state ? "Log is on." : "Log is off.");
    }

    public final void setLogFileName(String fileName) {
        try {
            logger.setLogFileName(fileName);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public final void setLogPath(String path) {
        logger.setLogPath(path);
    }

    public void displayAllConnections() {
        System.out.println("Total CommunicationAgents : " + messageAgentList.size());
        for (Iterator iter = messageAgentList.iterator(); iter.hasNext();) {
            ICommunicationAgent ca = (ICommunicationAgent) iter.next();
            System.out.println("CommunicationAgent "
                    + ca.getName()
                    + " ("
                    + ca.getCommunicationCount()
                    + " Connections/"
                    + ca.getMaximumCommunicationCount()
                    + " Max)");
            System.out.println(ca.getCommunicationAgentStatus());
        }
    }

    protected final void showData(String data) {
        if (logger.isLogVerbose()) {
            logger.logVerbose(1, data);
        }
    }

    protected final void showData(String data, int maxLen) {
        if (logger.isLogVerbose()) {
            if (data.length() > maxLen) {
                data = data.substring(0, maxLen);
            }
            showData(data);
        }
    }

    protected synchronized final void showMessage(String msg) {
        logger.logCritical(msg);
    }

}