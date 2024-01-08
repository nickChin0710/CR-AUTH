package com.tcb.authGateway.agent;

import com.tcb.authGateway.sg.SGLogger;

//import com.sage.log.SGLogger;


public interface IMessageGateway {

    public void startGateway() throws Exception;



    public void startCommunicationAgent(int index) throws Exception;



    public void stopGateway() throws Exception;



    public void stopCommunicationAgent(int index) throws Exception;



    public void setDisplay(boolean state);



    public void setTrace(boolean state);



    public void setLog(boolean state);



    public void setLogFileName(String fileName);



    public void displayAllConnections();



    public SGLogger getLogger();



    public CommunicationAgentConfig loadCommunicationAgentConfig(String agentName);



    public String getName();



    public String getType();
}