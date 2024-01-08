package com.tcb.authGateway.sg;

import java.net.Socket;
import com.tcb.authGateway.socket.ClientSocket;
import com.tcb.authGateway.socket.IClientSocketEventListener;


public class SGSocket {
    private ClientSocket cs;

    public SGSocket(String IP, int Port) {
        cs = new ClientSocket(IP, Port);
    }



    public SGSocket(Socket soc, IClientSocketEventListener el) {
        cs = new ClientSocket(soc, el);
    }



    public SGSocket(Socket soc, IClientSocketEventListener el, int connID) {
        cs = new ClientSocket(soc, el, connID);
    }



    public SGSocket(Socket soc, IClientSocketEventListener el, int connID, int localport) {
        cs = new ClientSocket(soc, el, connID, localport);
    }



    public SGSocket(ClientSocket cs) {
        this.cs = cs;
    }



    public SGSocket() {
        cs = new ClientSocket();
    }



    public boolean isDataRemained() {
        return cs.isDataRemained();
    }



    public String getRemoteIPAddress() {
        return cs.getRemoteIPAddress();
    }



    public void setIPAddress(String IP) {
        cs.setRemoteIPAddress(IP);
    }



    public int getRemotePort() {
        return cs.getRemotePort();
    }



    public String getLocalIPAddress() {
        return cs.getLocalIPAddress();
    }



    public int getLocalPort() {
        return cs.getLocalPort();
    }



    public void setReceiveBufferSize(int size) {
        cs.setReceiveBufferSize(size);
    }



    public int getReceiveBufferSize() {
        return cs.getReceiveBufferSize();
    }



    public int getID() {
        return cs.getID();
    }



    public void setID(int ID) {
        cs.setId(ID);
    }



    public void setPort(int port) {
        cs.setRemotePort(port);
    }



    public void connect() {
        cs.connect();
    }



    public void connect(int localport) {
        cs.connect(localport);
    }



    public void addSocketEventListener(IClientSocketEventListener el) {
        cs.addSocketEventListener(el);
    }



    public void disconnect() {
        cs.disconnect();
    }



    public void close() {
        cs.close();
    }



    public void sendString(String str) {
        cs.sendData(str.getBytes());
    }



    public void sendBytes(byte buf[]) {
        cs.sendData(buf);
    }



    public boolean isConnected() {
        return cs.isConnected();
    }

}