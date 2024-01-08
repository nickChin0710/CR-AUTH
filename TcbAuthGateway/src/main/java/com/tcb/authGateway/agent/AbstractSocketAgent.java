package com.tcb.authGateway.agent;

import com.tcb.authGateway.socket.ClientSocket;

//import com.sage.socket.*;


public abstract class AbstractSocketAgent extends AbstractCommunicationAgent {

    public AbstractSocketAgent() {
        super(16);
    }



    public AbstractSocketAgent(int maximumConnections) {
        super(maximumConnections);
    }



    public abstract void open(String id) throws Exception;



    public abstract void close(String id) throws Exception;



    public abstract void open() throws Exception;



    public abstract void close() throws Exception;



    public abstract void suspend(String id);



    public abstract void resume(String id);



    protected final void addClientSocketConnection(String id, ClientSocket cs) throws Exception {
        super.addConnection(id, cs);
    }



    protected final ClientSocket getClientSocketConnection(String id) throws Exception {
        Object o = super.getConnection(id);
        return o != null ? (ClientSocket)o : null;
    }



    protected void sendData(String id, byte[] data) throws Exception {
        ClientSocket cs = getClientSocketConnection(id);
        if (cs != null)
            cs.sendData(data);
        else {
            throw new Exception("Specified connection no longer exists. ");
        }
    }



    public final boolean isClientConnected(String id) {
        try {
            ClientSocket cs = getClientSocketConnection(id);
            return cs.isConnected();
        }
        catch (Exception ex) {
            return false;
        }
    }

}
