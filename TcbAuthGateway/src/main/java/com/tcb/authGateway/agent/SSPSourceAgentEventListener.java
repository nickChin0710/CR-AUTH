package com.tcb.authGateway.agent;

//import com.sage.mgc.CommunicationCommonEvent;


public class SSPSourceAgentEventListener extends MODGatewayAgentEventListener {
    public SSPSourceAgentEventListener() {
    }



    protected void onClientConnectEventImpl(CommunicationCommonEvent ev) {
        try {
            ev.getCommunicationAgent().getTargetCommunicationAgents()[0].open(ev.getClientID());
        }
        catch (Exception ex) {
            super.getMessageGateway().getLogger().logError(ex);
            super.getMessageGateway().getLogger().logDebug(ex);
        }
    }

}