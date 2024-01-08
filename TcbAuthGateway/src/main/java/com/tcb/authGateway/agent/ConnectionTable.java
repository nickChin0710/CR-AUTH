package com.tcb.authGateway.agent;

import java.util.HashMap;
import java.util.Map;
import com.tcb.authGateway.socket.ClientSocket;
//import com.sage.socket.*;
import java.util.Iterator;


public class ConnectionTable extends HashMap implements IConnectionTable {

    public ConnectionTable(int maximumSize) {
        super(maximumSize);
    }



    public synchronized void putConnection(String id, ClientSocket cs) {
        super.put(id, cs);
    }



    public synchronized boolean isConnectionExists(String id) {
        return super.containsKey(id);
    }



    public synchronized ClientSocket getConnection(String id) {
        Object o = get(id);
        return o != null ? (ClientSocket)o : null;
    }



    public synchronized void removeConnection(String id) {
        super.remove(id);
    }



    public synchronized void putConnection(Object identifier, Object connection) {
        super.put(identifier, connection);
    }



    public synchronized Object getConnection(Object identifier) {
        return super.get(identifier);
    }



    public synchronized void removeConnection(Object identifier) {
        super.remove(identifier);
    }



    public synchronized Iterator getIdentifiers() {
        return keySet().iterator();
    }



    public synchronized Iterator getConnections() {
        return values().iterator();
    }



    public synchronized void clear() {
        super.clear();
    }



    public synchronized int size() {
        return super.size();
    }

}