package com.tcb.authGateway.agent;

import java.util.*;
import com.tcb.authGateway.socket.ClientSocketEvent;

/**
 * 這一版是為了與FESC連線，由於FESC端回覆訊息後會自動斷線，造成訊息無法傳送到另一端， 故調整為斷線後，將延遲一秒以傳送訊息。
 */
public class FESCSingleServerSocketAgent extends SingleServerSocketAgent {

	private int disconnectDelay = 1000;

	private Hashtable idTable = new Hashtable(100);



	public FESCSingleServerSocketAgent() {
	}



	public FESCSingleServerSocketAgent(int port) {
		this(port, 16);
	}



	public FESCSingleServerSocketAgent(int port, int maximumConnections) {
		this(port, maximumConnections, false, false);
	}



	public FESCSingleServerSocketAgent(int port, int maximumConnections, boolean isSpecificOpenCommandNeeded,
			boolean isKickUnopenedAllowedConnection) {
		super(port, maximumConnections, isSpecificOpenCommandNeeded, isKickUnopenedAllowedConnection);
	}



	public void applyConfigImpl(CommunicationAgentConfig config) {
		super.applyConfigImpl(config);

		if (config.hasAdvancedConfig()) {
			this.disconnectDelay = config.getAdvancedConfig().getIntField("disconnectDelay");
		}
	}



	public void onClientDisconnectEvent(ClientSocketEvent ev) {
		try {
			Thread.sleep(disconnectDelay);
		} catch (InterruptedException ex) {
		}
		super.onClientDisconnectEvent(ev);
	}

}
