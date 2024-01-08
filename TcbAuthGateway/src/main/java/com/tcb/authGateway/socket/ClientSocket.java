package com.tcb.authGateway.socket;

import java.io.*;
import java.net.*;


/**
 * <p>Title: Taipei Bank XML Transforming System</p>
 * <p>Description: A middle system between IBM S/390 and R6, is responsible to transform data between fixed format data and XML</p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: Sage Information Corp.</p>
 * @author Roger Chen
 * @version 1.0
 */

public class ClientSocket extends Thread {

    private final static boolean DEBUG = false;

    private String ip = "";

    private int port = 0;

    private String localIP = "";

    private int localPort = 0;

    private IClientSocketEventListener eventListener;

    private Socket socket;

    private BufferedInputStream in;

    private BufferedOutputStream out;

    private boolean isRun = true;

    private boolean isSuspend = true;

    private boolean isSocketAlive = false;

    private int id = -1;

    public static final int errUnknownHost = 100;

    public static final int errConnectIOException = 101;

    public static final int errDisconnectIOException = 102;

    public static final int errReadIOException = 103;

    public static final int errWriteIOException = 104;

    public static final int errUnexceptedException = 999;

    private int receiveBufferSize = 131072;

    public void setId(int id) {
        this.id = id;
    }



    public int getID() {
        return this.id;
    }



    public String getRemoteIPAddress() {
        return ip;
    }



    public int getRemotePort() {
        return port;
    }



    public String getLocalIPAddress() {
        return localIP;
        /*
             try {
          String s = socket.getLocalAddress().toString();
          return s.substring(s.lastIndexOf("/") + 1, s.length());
             }
             catch (Exception ex) {
          return "";
             }
         */
    }



    public int getLocalPort() {
        return localPort;
    }



    public void addSocketEventListener(IClientSocketEventListener el) {
        eventListener = el;
    }



    public ClientSocket() {
        start();
    }



    public ClientSocket(InetAddress host, int port) {
        this(host.toString(), port);
    }



    public ClientSocket(String host, int port) {
        this();
        this.ip = host;
        this.port = port;
    }



    public ClientSocket(Socket soc, IClientSocketEventListener el) {
        this();
        this.eventListener = el;
        this.socket = soc;
        this.ip = soc.getInetAddress().getHostAddress();
        this.port = soc.getPort();
        this.connect();
    }



    public ClientSocket(Socket soc, IClientSocketEventListener el, int connID) {
        this();
        this.id = connID;
        this.eventListener = el;
        this.socket = soc;
        this.ip = soc.getInetAddress().getHostAddress();
        this.port = soc.getPort();
        this.connect();
    }



    public ClientSocket(Socket soc, IClientSocketEventListener el, int connID, int localport) {
        this();
        this.id = connID;
        this.eventListener = el;
        this.socket = soc;
        this.ip = soc.getInetAddress().getHostAddress();
        this.port = soc.getPort();
        this.connect(localport);
    }



    public void connect(int localport) {
        try {
            if (socket == null) {
                socket = new Socket(ip, port, InetAddress.getLocalHost().getLocalHost(), localport);
            }

            in = new BufferedInputStream(socket.getInputStream());
            out = new BufferedOutputStream(socket.getOutputStream());
            this.localIP = socket.getLocalAddress().getHostAddress();
            this.localPort = socket.getLocalPort();

            if (eventListener != null) {
                isSocketAlive = true;
                eventListener.onConnectEvent(new ClientSocketEvent(this));
            }

            resumeThread();

            if (DEBUG) {
                debug("socket connected. (" + id + ")");
            }
        }
        catch (UnknownHostException ex) {
            exceptionHandle(this.errUnknownHost, ex.getMessage());
        }
        catch (IOException ex) {
            exceptionHandle(this.errConnectIOException, ex.getMessage());
        }
    }



    public void connect() {
        try {
            if (socket == null) {
                socket = new Socket(ip, port);
            }

            in = new BufferedInputStream(socket.getInputStream());
            out = new BufferedOutputStream(socket.getOutputStream());
            this.localIP = socket.getLocalAddress().getHostAddress();
            this.localPort = socket.getLocalPort();

            if (eventListener != null) {
                isSocketAlive = true;
                eventListener.onConnectEvent(new ClientSocketEvent(this));
            }

            resumeThread();

            if (DEBUG) {
                debug("socket connected. (" + id + ")");
            }
        }
        catch (UnknownHostException ex) {
            exceptionHandle(this.errUnknownHost, ex.getMessage());
        }
        catch (IOException ex) {
            exceptionHandle(this.errConnectIOException, ex.getMessage());
        }
    }



    public boolean isDataRemained() {
        try {
            if (in.available() > 0)
                return true;
            else
                return false;
        }
        catch (IOException ex) {
            return false;
        }
    }



    public void disconnect() {
        try {
            suspendThread();
            if (out != null)
                out.close();
            if (in != null)
                in.close();
            if (socket != null) {
                socket.close();
                socket = null;
            }

            if (eventListener != null) {
                isSocketAlive = false;
                eventListener.onDisconnectEvent(new ClientSocketEvent(this));
            }

            if (DEBUG) {
                debug("socket disconnected. (" + id + ")");
            }
        }
        catch (IOException ex) {
            exceptionHandle(this.errDisconnectIOException, ex.getMessage());
        }
    }



    public void close() {
        if (isSocketAlive)
            disconnect();
        isRun = false;
        resumeThread();
    }



    public void setRemoteIPAddress(String ip) {
        this.ip = ip;
    }



    public void setRemotePort(int port) {
        this.port = port;
    }



    public void sendData(byte buf[]) {
        if (out == null) {
            exceptionHandle(this.errWriteIOException, "Socket is disconnected.");
            return;
        }

        try {
            out.write(buf);
            out.flush();
            if (eventListener != null)
                eventListener.onWriteEvent(new ClientSocketWriteEvent(this, buf));
        }
        catch (IOException ex) {
            exceptionHandle(this.errWriteIOException, ex.getMessage());
        }
    }



    private void suspendThread() {
        isSuspend = true;
    }



    private synchronized void resumeThread() {
        isSuspend = false;
        notify();

        if (DEBUG) {
            debug("Notify waiting suspend thread. (" + id + ")");
        }
    }



    public boolean isConnected() {
        if (socket == null) {
            return false;
        }

        return socket.isConnected();
    }



    private void setSendBufferSize(int size) throws Exception {
        socket.setSendBufferSize(size);
    }



    public void setReceiveBufferSize(int size) {
        receiveBufferSize = size;
    }



    private int getSendBufferSize() throws Exception {
        return socket.getSendBufferSize();
    }



    public int getReceiveBufferSize() {
        return receiveBufferSize;
    }



    public boolean isBufferEmpty() {
        try {
            if (in.available() > 0) {
                return false;
            }
            else {
                return true;
            }
        }
        catch (IOException ex) {
            return false;
        }
    }



    public void run() {
        int i = 0;
        while (isRun) {
            if (DEBUG) {
                debug("Thread begin running.. (" + id + ")");
            }

            try {
                synchronized (this) {
                    while (isSuspend) {

                        if (DEBUG) {
                            debug("Thread suspending... (" + id + ")");
                        }
                        wait();
                    }
                }
                if (!isRun) {
                    break;
                }

                socket.setReceiveBufferSize(receiveBufferSize);
                // Set Receive buffer size
                byte buffer[] = setReceiveBuffer();

                int size = 0;
                while (in != null && (size = in.read(buffer)) > -1) {
                    if (DEBUG) {
                        debug("Recv data. (" + id + ") len=" + size);
                    }

                    byte revData[] = null;
                    if (size != buffer.length) {
                        revData = new byte[size];
                        System.arraycopy(buffer, 0, revData, 0, revData.length);
                    }
                    else {
                        revData = buffer;
                    }

                    if (eventListener != null) {
                        eventListener.onReadEvent(new ClientSocketReadEvent(this, revData));
                    }

                    // Reset Receive buffer size
                    if (socket == null) {
                        break;
                    }

                    socket.setReceiveBufferSize(receiveBufferSize);
                    buffer = setReceiveBuffer();
                }
                disconnect();
            }
            catch (IOException ex) {
                //if (!isSuspend && isRun && socket != null) {
                //System.out.println("[Error] " + ex.getMessage());
                //ex.printStackTrace();
                exceptionHandle(this.errReadIOException, ex.getMessage());
                //}
            }
            catch (Exception ex) {
                //System.out.println("[Error] " + ex.getMessage());
                ex.printStackTrace();
                exceptionHandle(this.errUnexceptedException, ex.getMessage());
            }
        }

        if (DEBUG) {
            debug("Thread stopped. (" + id + ")");
        }
    }



    private static void debug(String msg) {
        if (DEBUG) {
            System.out.println(msg);
        }
    }



    /**
     * <p> Set a appropriate buffer size to receive data. If the available size is greater than default size, then set it to default size.
     * <p> If not, set it to fit the available size.
     * @return the Receive buffer
     * @throws IOException
     */
    private byte[] setReceiveBuffer() throws IOException {
        byte[] buffer;
        if (in != null && in.available() > 0 && in.available() < receiveBufferSize) {
            buffer = new byte[in.available()];
        }
        else {
            buffer = new byte[receiveBufferSize];
        }

        if (DEBUG) {
            debug("Set Recv buffer. (" + id + ") size=" + buffer.length);
        }
        return buffer;
    }



    private void exceptionHandle(int errcode, String errmsg) {
        ClientSocketErrorEvent err = new ClientSocketErrorEvent(this);
        String dcp = "";
        switch (errcode) {
            case errUnknownHost:
                dcp = "Unknown host.";
                break;
            case errConnectIOException:
                dcp = "I/O error occurred while connecting to host.";
                break;
            case errDisconnectIOException:
                dcp = "I/O error occurred while disconnecting to host.";
                break;
            case errReadIOException:
                dcp = "I/O error occurred while getting data from host.";
                break;
            case errWriteIOException:
                dcp = "I/O error occurred while sending data to host.";
                break;
            case errUnexceptedException:
                dcp = "Unexcepted error occurred.";
                break;
        }
        err.code = errcode;
        err.description = dcp + "; " + errmsg;

        if ( (errcode != errReadIOException) || (errcode == errReadIOException && !isSuspend)) {
            if (eventListener != null) {
                eventListener.onErrorEvent(err);
                disconnect();
            }
        }
    }

}