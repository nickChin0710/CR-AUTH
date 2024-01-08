package com.tcb.authGateway.sg;

import java.net.*;
import java.util.Map;
import com.tcb.authGateway.socket.ClientSocket;
import com.tcb.authGateway.socket.ClientSocketErrorEvent;
import com.tcb.authGateway.socket.ClientSocketEvent;
import com.tcb.authGateway.socket.ClientSocketReadEvent;
import com.tcb.authGateway.socket.ClientSocketWriteEvent;
import com.tcb.authGateway.socket.ServerSocketErrorEvent;
import com.tcb.authGateway.socket.ServerSocketEvent;
import java.util.HashMap;
import java.util.*;


/**
 * <p>Title: Taipei Bank Foreign Exchange System</p>
 * <p>Description: A middle system between IBM S/390 and R6.</p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: Sage Information Corp.</p>
 * @author Roger Chen
 * @version 1.0
 */

public class SGNewServerSocket {

    private ServerSocket serverSocket;

    private ServerSocketThread sst;

    private int port = 49999;

    private boolean Exit = false;

    private int maxConnections = 16;

    private Map connTable = new HashMap(16);

    private int index = -1;

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

    public SGNewServerSocket(int port) {
        this.port = port;
    }



    public SGNewServerSocket(int port, int maxConnections) {
        this(port);
        connTable = new HashMap(maxConnections);
        this.maxConnections = maxConnections;
    }



    public void listen() throws Exception {
        serverSocket = new ServerSocket(port);
        sst = new ServerSocketThread();
        sst.start();
        if (eventListener != null) {
            eventListener.onListen(new ServerSocketEvent(serverSocket));
        }
    }



    public void close() throws Exception {
        sst.isRunning = false;
        closeAllConnections();

        if (serverSocket == null) {
            return;
        }

        serverSocket.close();
        if (eventListener != null) {
            eventListener.onClose(new ServerSocketEvent(serverSocket));
        }

        serverSocket = null;
    }



    public void addServerSocketEventListener(IServerSocketEventListener el) {
        this.eventListener = el;
    }



    public int getMaxConnections() {
        return maxConnections;
    }



    public SGSocket getActiveConnection() {
        return ActiveConnection;
    }



    public int getPort() {
        return port;
    }



    public SGSocket[] getConnections() {
        return (SGSocket[])connTable.values().toArray(new SGSocket[connTable.size()]);
    }



    public int getConnectionCount() {
        return connTable.size();
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



    private SGSocket getConnection(int connectionID) {
        synchronized (connTable) {
            return (SGSocket)connTable.get(String.valueOf(connectionID));
        }
    }



    private void addConnection(int connectionID, SGSocket ss, ClientSocketEvent ev) {
        synchronized (connTable) {
            connTable.put(String.valueOf(connectionID), ss);
        }
    }



    private void removeConnection(int connectionID, ClientSocketEvent ev) {
        synchronized (connTable) {
            String sessionId = String.valueOf(connectionID);
            if (connTable.containsKey(sessionId)) {
                SGSocket socket = (SGSocket)connTable.get(sessionId);
                socket.close();
                connTable.remove(sessionId);
            }
        }
//        System.gc();
    }



    private void closeAllConnections() {
        synchronized (connTable) {
            SGSocket[] sockets = new SGSocket[connTable.size()];
            connTable.values().toArray(sockets);
            for (int i = 0; i < sockets.length; i++) {
                sockets[i].close();
            }
        }
//        System.gc();
    }



    private boolean checkDuplicateIP(String ip) {
        synchronized (connTable) {
            for (Iterator iter = connTable.values().iterator(); iter.hasNext(); ) {
                SGSocket socket = (SGSocket)iter.next();
                if (!socket.getRemoteIPAddress().equals(ip)) {
                    continue;
                }

                if (isReplaceOriginalConnection) {
                    socket.disconnect();
                    return false;
                }
                else {
                    return true;
                }
            }
        }
        return false;
    }



    private synchronized int getFreeConnectionID() {
        boolean found = false;
        int ID = -1;
        int i = 0;
        while (!found && connTable.size() < maxConnections) {
            index++;
            if (index >= maxConnections) {
                index = 0;
            }

            if (!connTable.containsKey(String.valueOf(index))) {
                return index;
            }
        }

        return -1;
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
            try {
            //showMessage(RCFunc.getHexTable(ev.Data, "Big5"));
            }
            catch (Exception ex) {
            }
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
