package com.tcb.authGateway.agent;

import com.tcb.authGateway.sg.SGSocketEventListener;
import com.tcb.authGateway.socket.ClientSocket;
import com.tcb.authGateway.socket.ClientSocketErrorEvent;
import com.tcb.authGateway.socket.ClientSocketEvent;
import com.tcb.authGateway.socket.ClientSocketReadEvent;
import com.tcb.authGateway.socket.ClientSocketWriteEvent;
import com.tcb.authGateway.thread.AbstractThread;

//import com.sage.socket.ClientSocket;
//import com.sage.socket.ClientSocketErrorEvent;
//import com.sage.socket.ClientSocketEvent;
//import com.sage.socket.ClientSocketReadEvent;
//import com.sage.socket.ClientSocketWriteEvent;
//import com.sage.socket.SGSocketEventListener;
//import com.sage.util.AbstractThread;

public class MultipleClientSocketAgent extends AbstractSocketAgent {

	private String remoteAddress;

	private int remotePort;

	private AutoReconnectThread autoReconnectThread;



	public MultipleClientSocketAgent() {
	}



	public MultipleClientSocketAgent(int maximumConnections) {
		super(maximumConnections);
	}



	public MultipleClientSocketAgent(String remoteAddress, int remotePort, int maximumConnections) {
		super(maximumConnections);
		this.remoteAddress = remoteAddress;
		this.remotePort = remotePort;
		super.addConnectionTable(new ConnectionTable(maximumConnections));
	}



	public void applyConfigImpl(CommunicationAgentConfig config) {
		this.remoteAddress = config.getIP();
		this.remotePort = config.getPort();
		super.setMaximumConnections(config.getMaxConnections());
		super.addConnectionTable(new ConnectionTable(config.getMaxConnections()));

		try {
			boolean isAutoReconnect = config.isTrue("auto_reconnect");
			int autoReconnectInterval = config.getIntField("auto_reconnect_interval");

			if (isAutoReconnect) {
				initAutoReconnectThread(autoReconnectInterval);
			}
		} catch (Exception ex) {
		}
	}



	private void initAutoReconnectThread(int reconnectInterval) {
		if (autoReconnectThread != null) {
			return;
		}

		autoReconnectThread = new AutoReconnectThread(reconnectInterval);
		showMessage("Auto-reconnect is enabled. (interval=" + reconnectInterval + " ms)");
	}



	public synchronized void close(String ID) throws java.lang.Exception {
		if (!super.isConnectionExists(ID)) {
			return;
		}

		ClientSocket socket = (ClientSocket) super.getConnection(ID);
		socket.close();
		super.removeConnection(ID);
	}



	public synchronized void open(String id) throws java.lang.Exception {
		ClientSocket socket = (ClientSocket) super.getConnection(id);
		if (socket == null) {
			socket = new ClientSocket();
			socket.setRemoteIPAddress(remoteAddress);
			socket.setRemotePort(remotePort);
			socket.addSocketEventListener(new SocketEventListener(this, id));
			super.addConnection(id, socket);
		}

		if (!socket.isConnected()) {
			socket.connect();
		}

	}



	public void suspend(String ID) {
		/** @todo Implement this com.sage.icbc.Gateway.ICommunicationAgent abstract method */
	}



	public void resume(String ID) {
		/** @todo Implement this com.sage.icbc.Gateway.ICommunicationAgent abstract method */
	}



	public void open() throws java.lang.Exception {
		for (int i = 0; i < super.getMaximumCommunicationCount(); i++) {
			open(String.valueOf(i + 1));
		}

		super.sendOnOpenEvent(this);

		if (autoReconnectThread != null) {
			autoReconnectThread.start();
		}
	}



	public void close() throws java.lang.Exception {
		if (autoReconnectThread != null) {
			autoReconnectThread.stopThread();
		}

		for (int i = 0; i < super.getMaximumCommunicationCount(); i++) {
			close(String.valueOf(i + 1));
		}

		super.sendOnCloseEvent(this);
	}



	private class SocketEventListener extends SGSocketEventListener {

		private MultipleClientSocketAgent adaptee;

		private String clientID;



		public SocketEventListener(MultipleClientSocketAgent adaptee, String clientID) {
			this.adaptee = adaptee;
			this.clientID = clientID;
		}



		public void onConnectEvent(ClientSocketEvent ev) {
			adaptee.sendOnClientConnectEvent(adaptee, clientID, ev.getHostIPAddress(), ev.getHostPort());
		}



		public void onDisconnectEvent(ClientSocketEvent ev) {
			try {
				removeConnection(clientID);
				((ClientSocket) ev.getSource()).close();
			} catch (Exception ex) {
			}
			adaptee.sendOnClientDisconnectEvent(adaptee, clientID);
		}



		public void onReadEvent(ClientSocketReadEvent ev) {
			adaptee.sendOnClientReadEvent(adaptee, clientID, ev.data);
		}



		public void onWriteEvent(ClientSocketWriteEvent ev) {
			adaptee.sendOnClientWriteEvent(adaptee, clientID, ev.data);
		}



		public void onErrorEvent(ClientSocketErrorEvent ev) {
			adaptee.sendOnClientErrorEvent(adaptee, clientID, String.valueOf(ev.code), ev.description);
		}

	}



	private class AutoReconnectThread extends AbstractThread {

		private AutoReconnectThread(int reconnectInterval) {
			super(reconnectInterval);
		}



		protected void doMainProcess() {
			for (int i = 0; i < getMaximumCommunicationCount(); i++) {
				String id = String.valueOf(i + 1);
				if (!isClientConnected(id)) {
					showMessage("Connection(" + id + ") is broken. Trying to reconnect....");
					try {
						open(id);
					} catch (Exception ex) {
						showMessage("Reconnect to host failed. " + ex.getMessage());
					}
				}

			}

		}
	}

}