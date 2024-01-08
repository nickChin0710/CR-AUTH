package com.tcb.authGateway.agent;

import java.util.*;
import com.tcb.authGateway.sg.SGNewServerSocket;
import com.tcb.authGateway.sg.SGServerSocketEventListener;
import com.tcb.authGateway.socket.ClientSocket;
import com.tcb.authGateway.socket.ClientSocketErrorEvent;
import com.tcb.authGateway.socket.ClientSocketEvent;
import com.tcb.authGateway.socket.ClientSocketReadEvent;
import com.tcb.authGateway.socket.ClientSocketWriteEvent;
import com.tcb.authGateway.socket.ServerSocketErrorEvent;
import com.tcb.authGateway.socket.ServerSocketEvent;

//import com.sage.socket.*;


public class SingleServerSocketAgent extends AbstractSocketAgent {

    private SGNewServerSocket ss;

    private Properties permitConnTable;

    /**
     * This flag indicates whether it needs the specific open connection command to allow client to connect.
     * The specific command will come from the other <I>ICommunicatonAgent</I>.<BR>
     * <P><B>true</B> Client is allowed connecting as long as SingleServerSocketAgent has executed open().<BR>
     * <B>false</B> Client is allowed connecting only if SingleServerSocketAgent has received open command from the other <I>ICommunicationAgent</I>.
     */
    private boolean isSpecificOpenCommandNeeded = false;

    /**
     * <p>This flag is only useful when isSpecificOpenCommandNeeded is true.
     * <P><B>true</B> SingleServerSocketAgent will disconnect the client when there is no open command for this client.<BR>
     * <B>false</B> Client is still allowed to connect, but can not receive or send any message.
     */
    private boolean isKickUnopenedAllowedConnection = false;

    public SingleServerSocketAgent() {
    }



    public SingleServerSocketAgent(int port) {
        this(port, 16);
    }



    public SingleServerSocketAgent(int port, int MaximumConnections) {
        this(port, MaximumConnections, false, false);
    }



    public SingleServerSocketAgent(int port, int MaximumConnections, boolean isReplaceOriginConnection) {
        super(MaximumConnections);
        super.addConnectionTable(new ConnectionTable(MaximumConnections));
        this.isSpecificOpenCommandNeeded = isSpecificOpenCommandNeeded;
        this.isKickUnopenedAllowedConnection = isKickUnopenedAllowedConnection;
        initServerSocket(port, MaximumConnections, true, isReplaceOriginConnection);
    }



    public SingleServerSocketAgent(int port, int MaximumConnections, boolean isSpecificOpenCommandNeeded, boolean isKickUnopenedAllowedConnection) {
        super(MaximumConnections);
        super.addConnectionTable(new ConnectionTable(MaximumConnections));
        this.isSpecificOpenCommandNeeded = isSpecificOpenCommandNeeded;
        this.isKickUnopenedAllowedConnection = isKickUnopenedAllowedConnection;
        initServerSocket(port, MaximumConnections);
    }



    private void initServerSocket(int port, int MaximumConnections) {
        permitConnTable = new Properties();
        ss = new SGNewServerSocket(port, MaximumConnections);
        ss.addServerSocketEventListener(new SingleServerSocketEventListener(this));
        ss.setAllowdDuplicateIP(true);
        ss.setReplaceOriginalConnection(true);
    }



    private void initServerSocket(int port, int MaximumConnections, boolean isAllowdDuplicateIP, boolean isReplaceOriginConnection) {
        permitConnTable = new Properties();
        ss = new SGNewServerSocket(port, MaximumConnections);
        ss.addServerSocketEventListener(new SingleServerSocketEventListener(this));
        ss.setAllowdDuplicateIP(isAllowdDuplicateIP);
        ss.setReplaceOriginalConnection(isReplaceOriginConnection);
    }



    public void applyConfigImpl(CommunicationAgentConfig config) {
        super.addConnectionTable(new ConnectionTable(config.getMaxConnections()));
        initServerSocket(config.getPort(), config.getMaxConnections());

        if (config.hasAdvancedConfig()) {
            ss.setAllowdDuplicateIP(config.getAdvancedConfig().isTrue("isAllowdDuplicateIP"));
            ss.setReplaceOriginalConnection(config.getAdvancedConfig().isTrue("isReplaceOriginConnection"));
            this.isSpecificOpenCommandNeeded = config.getAdvancedConfig().isTrue("isSpecificOpenCommandNeeded");
            this.isKickUnopenedAllowedConnection = config.getAdvancedConfig().isTrue("isKickUnopenedAllowedConnection");
        }
    }



    protected final void sendData(String ID, byte[] Data) throws Exception {
        ClientSocket cs = getClientSocketConnection(ID);
        if (cs != null) {
            if (!isSpecificOpenCommandNeeded || (isSpecificOpenCommandNeeded && !isKickUnopenedAllowedConnection && permitConnTable.containsKey(ID))) {
                cs.sendData(Data);
            }
        }
        else {
            throw new Exception("Specified connection no longer exists. (" + ID + ")");
        }
    }



    public void close(String ID) throws Exception {
        try {
            ClientSocket cs = getClientSocketConnection(ID);
            synchronized (permitConnTable) {
                if (permitConnTable.containsKey(ID)) {
                    permitConnTable.remove(ID);
                }
            }
            if (cs != null) {
                cs.disconnect();
                cs.close();
            }
            else
                throw new Exception("Specified connection no longer exists. (" + ID + ")");
        }
        catch (Exception ex) {
            super.sendOnErrorEvent(this, "", emh.emCloseFailed, emh.getErrorMessage(emh.emCloseFailed) + ex.getMessage());
            throw ex;
        }
    }



    public void open(String ID) throws Exception {
        if (!permitConnTable.containsKey(ID)) {
            synchronized (permitConnTable) {
                permitConnTable.setProperty(ID, ID);
                //System.out.println("Open connection of " + ID + ".");
            }
        }
    }



    public void suspend(String parm1) {
        throw new UnsupportedOperationException("Method suspend(String parm1) not supports in this class. ");
    }



    public void resume(String parm1) {
        throw new UnsupportedOperationException("Method resume(String parm1) not supports in this class. ");
    }



    public void open() throws Exception {
        if (super.isOpened()) {
            super.sendOnErrorEvent(this, "", emh.emDuplicatedOpenOperation, emh.getErrorMessage(emh.emDuplicatedOpenOperation));
            return;
        }

        try {
            ss.listen();
        }
        catch (Exception ex) {
            super.sendOnErrorEvent(this, "", emh.emOpenFailed, emh.getErrorMessage(emh.emOpenFailed) + ex.getMessage());
            throw ex;
        }
    }



    public void close() throws Exception {
        if (!super.isOpened()) {
            super.sendOnErrorEvent(this, "", emh.emDuplicatedCloseOperation, emh.getErrorMessage(emh.emDuplicatedCloseOperation));
            return;
        }

        try {
            ss.close();
        }
        catch (Exception ex) {
            super.sendOnErrorEvent(this, "", emh.emCloseFailed, emh.getErrorMessage(emh.emCloseFailed) + ex.getMessage());
            throw ex;
        }
    }



    protected String findClientID(int clientID, String clientAddress) {
        String ID = null;
        try {
            if (isCorrelTableExist())
                ID = getID(clientAddress);
            else
                ID = String.valueOf(clientID + 1);
        }
        catch (Exception ex1) {
            sendOnErrorEvent(this, ID, emh.emGetIDFailed, emh.getErrorMessage(emh.emGetIDFailed) + " " + ex1.getMessage());
        }
        return ID;
    }



    protected void onClientConnectEvent(ClientSocketEvent ev) {
    		if(isHideSensitivityLog())
          showMessage("Accepted connection from client " + ev.getHostIPAddress() + ":" + ev.getHostPort());
    		else
    			showMessage("Accepted connection from client " + "******" + ":" + "***");

        boolean isError = true;

        ClientSocket cs = (ClientSocket)ev.getSource();
        String ID = findClientID(cs.getID(), cs.getRemoteIPAddress());
        if (ID != null) {
            try {
                if (!isSpecificOpenCommandNeeded || (isSpecificOpenCommandNeeded && isKickUnopenedAllowedConnection && permitConnTable.containsKey(ID))
                    || (isSpecificOpenCommandNeeded && !isKickUnopenedAllowedConnection)) {

                    addConnection(ID, cs);
                    isError = false;

                    if (!isSpecificOpenCommandNeeded || (isSpecificOpenCommandNeeded && permitConnTable.containsKey(ID)))
                        sendOnClientConnectEvent(this, ID, ev.getHostIPAddress(), ev.getHostPort());
                }
                else {
                    sendOnErrorEvent(this, ID, emh.emConnectionNotOpenYet, emh.getErrorMessage(emh.emConnectionNotOpenYet));
                }
            }
            catch (Exception ex) {
                sendOnErrorEvent(this, ID, emh.emAddNewConnectionFailed, emh.getErrorMessage(emh.emAddNewConnectionFailed) + " " + ex.getMessage());
            }
        }
        else {
            sendOnErrorEvent(this, ID, emh.emUndefinedConnection, emh.getErrorMessage(emh.emUndefinedConnection));
        }

        if (isError) {
            cs.disconnect();
            cs.close();
        }
    }



    protected void onClientDisconnectEvent(ClientSocketEvent ev) {
    	if(isHideSensitivityLog())
        showMessage("disconnected from client " + ev.getHostIPAddress() + ":" + ev.getHostPort());
    	else
    		showMessage("disconnected from client " + "******" + ":" + "***");
    	
        boolean isError = true;
        ClientSocket cs = (ClientSocket)ev.getSource();
        String ID = findClientID(cs.getID(), cs.getRemoteIPAddress());
        if (ID != null) {
            try {
                removeConnection(ID);
                isError = false;
            }
            catch (Exception ex) {
                sendOnErrorEvent(this, ID, emh.emRemoveConnectionFailed, emh.getErrorMessage(emh.emRemoveConnectionFailed) + " " + ex.getMessage());
            }
        }
        if (!isError) {
            sendOnClientDisconnectEvent(this, ID);
        }
    }



    protected void onReadEvent(ClientSocketReadEvent ev) {
    	if(isHideSensitivityLog())
    		showMessage("Receive data from client " + ev.getHostIPAddress() + ":" + ev.getHostPort() + " data len = " + ev.data.length);
    	else
    		showMessage("Receive data from client " + "******" + ":" + "***" + " data len = " + ev.data.length);
        
        ClientSocket cs = (ClientSocket)ev.getSource();
        String ID = findClientID(cs.getID(), cs.getRemoteIPAddress());
        if (ID != null) {
            if (isSpecificOpenCommandNeeded && !isKickUnopenedAllowedConnection) {
                if (permitConnTable.containsKey(ID)) {
                    sendOnClientReadEvent(this, ID, ev.data);
                }
                else {
                    sendOnErrorEvent(this, ID, emh.emSendingMessageNotPermitted, emh.getErrorMessage(emh.emSendingMessageNotPermitted));
                }
            }
            else {
                sendOnClientReadEvent(this, ID, ev.data);
            }
        }
        else {
            sendOnErrorEvent(this, ID, emh.emReceiveDataFromUndefinedConnection, emh.getErrorMessage(emh.emReceiveDataFromUndefinedConnection));
        }

    }



    protected void onWriteEvent(ClientSocketWriteEvent ev) {
    	if(isHideSensitivityLog())
    		showMessage("Sent data to client " + ev.getHostIPAddress() + ":" + ev.getHostPort() + " data len = " + ev.data.length);
    	else
    		showMessage("Sent data to client " + "******" + ":" + "***" + " data len = " + ev.data.length);
        
        ClientSocket cs = (ClientSocket)ev.getSource();
        String ID = findClientID(cs.getID(), cs.getRemoteIPAddress());
        if (ID != null) {
            sendOnClientWriteEvent(this, ID, ev.data);
        }
        else
            sendOnErrorEvent(this, ID, emh.emSentDataToUndefinedConnecton, emh.getErrorMessage(emh.emSentDataToUndefinedConnecton));
    }



    protected void onClientErrorEvent(ClientSocketErrorEvent ev) {
        showMessage("Client Error occurred. " + ev.code + ":" + ev.description);
        ClientSocket cs = (ClientSocket)ev.getSource();
        String ID = findClientID(cs.getID(), cs.getRemoteIPAddress());
        sendOnClientErrorEvent(this, ID, String.valueOf(ev.code), ev.description);
    }



    protected void onErrorEvent(ServerSocketErrorEvent ev) {
        showMessage("Server Error occurred. " + ev.Code + ":" + ev.Description);
        String ID = null;
        sendOnErrorEvent(this, ID, String.valueOf(ev.Code), ev.Description);
    }



    protected void onListen(ServerSocketEvent ev) {
    		if(isHideSensitivityLog())
    			showMessage("Server is listening to port " + ev.getHostPort());
    		else
    			showMessage("Server is listening to port " + "***");
        setOpened(true);
        sendOnOpenEvent(this);
    }



    protected void onClose(ServerSocketEvent ev) {
        showMessage("Server is closed. ");
        setOpened(false);
        sendOnCloseEvent(this);
    }



    class SingleServerSocketEventListener extends SGServerSocketEventListener {
        SingleServerSocketAgent adaptee;

        SingleServerSocketEventListener(SingleServerSocketAgent adaptee) {
            this.adaptee = adaptee;
        }



        public void onClientConnectEvent(ClientSocketEvent ev) {
            adaptee.onClientConnectEvent(ev);
        }



        public void onClientDisconnectEvent(ClientSocketEvent ev) {
            adaptee.onClientDisconnectEvent(ev);
        }



        public void onReadEvent(ClientSocketReadEvent ev) {
            adaptee.onReadEvent(ev);
        }



        public void onWriteEvent(ClientSocketWriteEvent ev) {
            adaptee.onWriteEvent(ev);
        }



        public void onClientErrorEvent(ClientSocketErrorEvent ev) {
            adaptee.onClientErrorEvent(ev);
        }



        public void onErrorEvent(ServerSocketErrorEvent ev) {
            adaptee.onErrorEvent(ev);
        }



        public void onListen(ServerSocketEvent ev) {
            adaptee.onListen(ev);
        }



        public void onClose(ServerSocketEvent ev) {
            adaptee.onClose(ev);
        }

    }
}