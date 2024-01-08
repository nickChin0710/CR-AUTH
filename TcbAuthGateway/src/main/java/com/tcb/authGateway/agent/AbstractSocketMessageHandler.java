package com.tcb.authGateway.agent;

import com.tcb.authGateway.sg.SGSocketPackage;

//import com.sage.message.SGSocketPackage;

public abstract class AbstractSocketMessageHandler implements IMessageHandler {

	private ICommunicationAgent owner;



	public AbstractSocketMessageHandler() {
	}



	public AbstractSocketMessageHandler(ICommunicationAgent owner) {
		this.owner = owner;
	}



	public abstract Message doMessageHandle(Message msg, ICommunicationAgent from, ICommunicationAgent[] to)
			throws Exception;



	protected abstract void startImpl();



	protected abstract void stopImpl();



	protected final byte[] parseMessagePackage(byte[] msg) throws Exception {
		SGSocketPackage sp = new SGSocketPackage();
		sp.loadPackage(msg);
		return sp.getMessage();
	}



	public final void showMessage(String msg) {
		StringBuffer buf = new StringBuffer();
		buf.append("[");
		buf.append(owner.getName());
		buf.append("] ");
		buf.append(msg);
		owner.getLogger().logCritical(buf.toString());
	}



	public final void showTrace(String msg) {
		if (!owner.getLogger().isLogDebug()) {
			return;
		}

		StringBuffer buf = new StringBuffer();
		buf.append("[");
		buf.append(owner.getName());
		buf.append("] ");
		buf.append(msg);
		owner.getLogger().logDebug(buf.toString());

	}



	public ICommunicationAgent getCommunicationAgent() {
		return this.owner;
	}



	public void setCommunicationAgent(ICommunicationAgent agent) {
		this.owner = agent;
	}



	public final void start() {
		startImpl();
	}



	public final void stop() {
		stopImpl();
	}

}