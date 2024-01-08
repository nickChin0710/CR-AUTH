package com.tcb.authGateway.sg;

import java.net.*;
import com.tcb.authGateway.socket.ClientSocket;
import com.tcb.authGateway.socket.ClientSocketErrorEvent;
import com.tcb.authGateway.socket.ClientSocketEvent;
import com.tcb.authGateway.socket.ClientSocketReadEvent;
import com.tcb.authGateway.socket.ClientSocketWriteEvent;
import com.tcb.authGateway.socket.ServerSocketErrorEvent;
import com.tcb.authGateway.socket.ServerSocketEvent;


/**
 * <p>Title: Taipei Bank Foreign Exchange System</p>
 * <p>Description: A middle system between IBM S/390 and R6.</p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: Sage Information Corp.</p>
 * @author Roger Chen
 * @version 1.0
 */

public class SGServerSocket {

    private ServerSocket serverSocket;

    private ServerSocketThread sst;

    private int port = 49999;

    private boolean Exit = false;

    private int MaxConnections = 16;

    private SGSocket connList[] = new SGSocket[MaxConnections];

    private IServerSocketEventListener eventListener;

    private SGSocket ActiveConnection;

    /**
     * <p> Is server allowed more than one connection at the same from the same client IP address.
     * <p> If true, server will accept more one connection at the same from the same client IP address.
     * If false, server will no longer accept another connection from the same client once it alread had one.
     */
    private boolean isAllowedDuplicateIP = true;

    /**
     * <p> Will server replace the original connection from the same client IP address when server figure out the duplicated IP address
     * from the same client.
     * <p> If true, server will disconnect the old connection when server get a new connection from the same client IP address.
     * If false, server will do nothing.
     */
    private boolean isReplaceOriginalConnection = false;

    public static final int errUnknownHost = 100;

    public static final int errConnectIOException = 101;

    public static final int errDisconnectIOException = 102;

    public static final int errReadIOException = 103;

    public static final int errWriteIOException = 104;

    public static final int errUnexceptedException = 999;

    public SGServerSocket(int port) {
        this.port = port;
    }



    public SGServerSocket(int port, int MaxConnections) {
        this(port);
        connList = new SGSocket[MaxConnections];
        this.MaxConnections = MaxConnections;
    }



    public void listen() throws Exception {
        serverSocket = new ServerSocket(port);
        sst = new ServerSocketThread();
        sst.start();
        if (eventListener != null)
            eventListener.onListen(new ServerSocketEvent(serverSocket));
    }



    public void close() throws Exception {
        sst.isRunning = false;
        closeAllConnections();
        serverSocket.close();
        if (eventListener != null)
            eventListener.onClose(new ServerSocketEvent(serverSocket));
        serverSocket = null;
    }



    public void addServerSocketEventListener(IServerSocketEventListener el) {
        this.eventListener = el;
    }



    public int getMaxConnections() {
        return MaxConnections;
    }



    public SGSocket getActiveConnection() {
        return ActiveConnection;
    }



    public int getPort() {
        return port;
    }



    public SGSocket[] getConnections() {
        synchronized (connList) {
            return connList;
        }
    }



    public int getConnectionCount() {
        int count = 0;
        synchronized (connList) {
            for (int i = 0; i < connList.length; i++) {
                if (connList[i] != null)
                    count++;
            }
        }
        return count;
    }



    public boolean isAllowedDuplicateIP() {
        return isAllowedDuplicateIP;
    }



    public void setAllowdDuplicateIP(boolean s) {
        isAllowedDuplicateIP = s;
    }



    public boolean isReplaceOriginalConnection() {
        return isReplaceOriginalConnection;
    }



    public void setReplaceOriginalConnection(boolean s) {
        isReplaceOriginalConnection = s;
    }



    private SGSocket getConnection(int ConnectionID) {
        synchronized (connList) {
            return connList[ConnectionID];
        }
    }



    private void addConnection(int ConnectionID, SGSocket ss, ClientSocketEvent ev) {
        synchronized (connList) {
            connList[ConnectionID] = ss;

            // Added by Roger in 2005-0413
            // moved from CLClientSocketEventListener.onConnectEvent
            // In order to avoid very quickly connection and disconnect action from Client.
//         // Remarked by Roger in 2008-05-27. Synchronizing event handle here cause that we cannot
//         // do heavy work in event handle. We use a little delay to ensure the event handle sequence.
//         if (eventListener != null)
//            eventListener.onClientDisconnectEvent(ev);

            try {
                Thread.sleep(50);
            }
            catch (InterruptedException ex1) {
            }

        }
    }



    private void removeConnection(int ConnectionID, ClientSocketEvent ev) {
        synchronized (connList) {
            if (ConnectionID > -1 && connList[ConnectionID] != null) {
                connList[ConnectionID].close();
                connList[ConnectionID] = null;
            }

            // Added by Roger in 2005-0413
            // moved from CLClientSocketEventListener.onDisonnectEvent
            // In order to avoid very quickly connection and disconnect action from Client.
//         // Remarked by Roger in 2008-05-27. Synchronizing event handle here cause that we cannot
//         // do heavy work in event handle. We use a little delay to ensure the event handle sequence.
//         if (eventListener != null)
//            eventListener.onClientDisconnectEvent(ev);
            try {
                Thread.sleep(50);
            }
            catch (InterruptedException ex1) {
            }

        }
//        System.gc();
    }



    private void closeAllConnections() {
        synchronized (connList) {
            for (int i = 0; i < connList.length; i++) {
                if (connList[i] != null) {
                    connList[i].disconnect();
                }
            }
        }
//        System.gc();
    }



    private boolean checkDuplicateIP(String ip) {
        boolean rtn = false;
        synchronized (connList) {
            for (int i = 0; i < connList.length; i++) {
                if (connList[i] != null && connList[i].getRemoteIPAddress().equals(ip)) {
                    if (isReplaceOriginalConnection) {
                        connList[i].disconnect();
                        rtn = false;
                    }
                    else {
                        rtn = true;
                    }
                }
            }
        }
        return rtn;
    }



    private int getFreeConnectionID() {
        boolean found = false;
        int ID = -1;
        int i = 0;
        synchronized (connList) {
            while (i < connList.length && !found) {
                if (connList[i] == null) {
                    ID = i;
                    found = true;
                }
                i++;
            }
        }

        return ID;
    }



    private void exceptionHandle(int errcode) {
        String dcp = "";
        switch (errcode) {
            case errUnknownHost:
                dcp = "Unknown host.";
                break;
            case errConnectIOException:
                dcp = "I/O error occurred while connecting to client.";
                break;
            case errDisconnectIOException:
                dcp = "I/O error occurred while disconnecting to client.";
                break;
            case errReadIOException:
                dcp = "I/O error occurred while getting data from client.";
                break;
            case errWriteIOException:
                dcp = "I/O error occurred while sending data to client.";
                break;
            case errUnexceptedException:
                dcp = "Unexcepted error occurred.";
                break;
        }
        if (eventListener != null) {
            eventListener.onErrorEvent(new ServerSocketErrorEvent(serverSocket, errcode, dcp));
        }

    }



    private void showMessage(String msg) {
        System.out.println(msg);
    }



    class CLClientSocketEventListener extends SGSocketEventListener {

        public void onConnectEvent(ClientSocketEvent ev) {
            //showMessage("Accepted client connection from " + ev.getHostIPAddress());
            ClientSocket cs = (ClientSocket)ev.getSource();
            addConnection(cs.getID(), new SGSocket(cs), ev);

            if (eventListener != null) {
                eventListener.onClientConnectEvent(ev);
            }
        }



        public void onDisconnectEvent(ClientSocketEvent ev) {
            ClientSocket cs = (ClientSocket)ev.getSource();
            //showMessage("Disconnected from client " + ev.getHostIPAddress());
            removeConnection(cs.getID(), ev);

            if (eventListener != null) {
                eventListener.onClientDisconnectEvent(ev);
            }
        }



        public void onReadEvent(ClientSocketReadEvent ev) {
            ClientSocket cs = (ClientSocket)ev.getSource();

            ActiveConnection = getConnection(cs.getID());
            if (ActiveConnection == null) {
                try {
                    Thread.sleep(100);
                }
                catch (InterruptedException ex1) {
                }
                ActiveConnection = getConnection(cs.getID());
            }

            if (eventListener != null)
                eventListener.onReadEvent(ev);
                //showMessage("Received data from client ID=" + cs.getID() + ". Data length = " + ev.Data.length);
//            try {
//            //showMessage(RCFunc.getHexTable(ev.Data, "Big5"));
//            }
//            catch (Exception ex) {
//            }
        }



        public void onWriteEvent(ClientSocketWriteEvent ev) {
            //showMessage("Sent data to client. Data lenth = " + ev.Data.length);
            if (eventListener != null)
                eventListener.onWriteEvent(ev);
//            try {
//            //showMessage(RCFunc.getHexTable(ev.Data, "Big5"));
//            }
//            catch (Exception ex) {
//            }
        }



        public void onErrorEvent(ClientSocketErrorEvent ev) {
            //showMessage("[Error] " + ev.Code + ":" + ev.Description);
            if (eventListener != null)
                eventListener.onClientErrorEvent(ev);
        }

    }



    class ServerSocketThread extends Thread {
        private boolean isRunning = true;

        public void run() {
            while (isRunning) {
                try {
                    Socket soc = serverSocket.accept();
                    String ip = soc.getInetAddress().getHostAddress();
                    int ID = 0;
                    if (isRunning && (!checkDuplicateIP(ip) | isAllowedDuplicateIP) && (ID = getFreeConnectionID()) > -1) {
                        SGSocket ss = new SGSocket(soc, new CLClientSocketEventListener(), ID);
                        ss = null;
                    }
                    else {
                        soc.close();
                    }
                }
                catch (Exception ex) {
                    if (isRunning)
                        exceptionHandle(errConnectIOException);
                        //ex.printStackTrace();
                }
            }
        }
    }

}