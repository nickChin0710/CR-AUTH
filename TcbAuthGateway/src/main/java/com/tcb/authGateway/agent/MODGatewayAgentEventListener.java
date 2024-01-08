package com.tcb.authGateway.agent;

//import com.sage.mgc.*;


public class MODGatewayAgentEventListener extends CommonAgentEventListener {
    public MODGatewayAgentEventListener() {
    }



    protected void onClientDisconnectEventImpl(CommunicationCommonEvent ev) {
        try {
            ICommunicationAgent ca = ev.getCommunicationAgent().getTargetCommunicationAgents()[0];
            if (ca.isClientConnected(ev.getClientID())) {
                ca.close(ev.getClientID());
            }
        }
        catch (Exception ex) {
            super.getMessageGateway().getLogger().logError(ex);
            super.getMessageGateway().getLogger().logDebug(ex);
        }
    }

}