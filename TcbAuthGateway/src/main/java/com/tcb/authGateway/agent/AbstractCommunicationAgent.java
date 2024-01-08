package com.tcb.authGateway.agent;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import com.tcb.authGateway.queue.IQueue;
import com.tcb.authGateway.sg.DocumentConfig;
import com.tcb.authGateway.sg.IDocumentConfigurable;
import com.tcb.authGateway.sg.SGLogger;
import com.tcb.authGateway.utils.RCFunc;

//import com.sage.document.DocumentConfig;
//import com.sage.log.SGLogger;
//import com.sage.roger.RCFunc;
//import com.sage.util.IQueue;

public abstract class AbstractCommunicationAgent implements ICommunicationAgent, IDocumentConfigurable {

	private SGLogger logger = SGLogger.getLogger();

	private String name = "";

	private boolean isTrace = false;

	private boolean isOpened = false;

	private boolean isQueueUndeliverableMessage = false;

	private boolean isMessageForwardDirectly = true;

	private boolean isEnabled = true;
	
	private boolean isHideSensitivityLog = true;

	private IMessageGateway messageGateway;

	private ICommunicationAgent[] subscribers;

	private List agentEventListeners = new ArrayList();

	private IMessageResendManager mrm;

	// Add for testing 2004-0609
	private boolean isSend = true;

	/**
	 * A correlation table between an identifier and a connection object
	 */
	private IConnectionTable connTable;

	/**
	 * <p>
	 * A correlation table between physical connection feature and an identifier.
	 * <p>
	 * This table is used to find the correlated indentifier with the physical connection feature.<BR>
	 * A physical connection feature can be got when a connection has been established. It could be
	 * a IP address or a port number.<BR>
	 * This table is predefined. It will be added on runtime. When a new connection is established,
	 * the subclass of this class will use this table to find the correlated identifier, and add
	 * this new connection into ConnectionTable with found identifier.
	 */
	private Properties correlTable;

	private IQueue processQueue;

	private IQueue resendQueue;

	private int maximumConnections = 16;

	protected ErrorMessageHandler emh;

	private MessageProcessCenter messageProcessCenter;

	private HeartBeatHandler heartBeatHandler;



	public AbstractCommunicationAgent() {
		this(16);
	}



	public AbstractCommunicationAgent(int maximumConnections) {
		this.maximumConnections = maximumConnections;
	}



	public void reset() {
		messageProcessCenter.stop();
		processQueue.clear();
		messageProcessCenter.start();
	}



	public abstract void open(String id) throws Exception;



	public abstract void close(String id) throws Exception;



	public abstract void open() throws Exception;



	public abstract void close() throws Exception;



	public abstract void suspend(String id);



	public abstract void resume(String id);



	protected abstract void sendData(String id, byte[] data) throws Exception;



	public abstract boolean isClientConnected(String id);



	protected abstract void applyConfigImpl(CommunicationAgentConfig config);



	public void applyConfig(DocumentConfig config) {
		CommunicationAgentConfig agentConfig = (CommunicationAgentConfig) config;

		if (agentConfig.getAdvancedConfig().containsField("isQueueUndeliverableMessage")) {
			isQueueUndeliverableMessage = agentConfig.getAdvancedConfig().isTrue("isQueueUndeliverableMessage");
		}
		if (agentConfig.getAdvancedConfig().containsField("isMessageForwardDirectly")) {
			isMessageForwardDirectly = agentConfig.getAdvancedConfig().isTrue("isMessageForwardDirectly");
		}

		applyConfigImpl(agentConfig);

		if (agentConfig.hasHeartBeatHandlerConfig()) {
			DocumentConfig heartBeatHandlerConfig = agentConfig.getHeartBeatHandlerConfig();
			initHeartBeatHandler(heartBeatHandlerConfig);

			if (heartBeatHandler != null) {
				heartBeatHandler.setLogger(logger);
			}
		}

	}



	public DocumentConfig loadMessageHandlerConfig() {
		CommunicationAgentConfig config = messageGateway.loadCommunicationAgentConfig(getName());

		if (config == null) {
			throw new RuntimeException(MessageFormat.format("Config for CommunicationAgent({0}) not found.",
															new Object[] { getName() }));
		}
		if (config.getMessageHandlerConfig() == null) {
			throw new RuntimeException(
					MessageFormat.format(	"MessageHandlerConfig for CommunicationAgent({0}) not found.",
											new Object[] { getName() }));
		}
		return config.getMessageHandlerConfig();
	}



	public void setMessageGateway(IMessageGateway messageGateway) {
		this.messageGateway = messageGateway;
	}



	public IMessageGateway getMessageGateway() {
		return this.messageGateway;
	}



	private void initHeartBeatHandler(DocumentConfig config) {
		if (config.isTrue("enabled")) {
			byte[] heartBeatMessage = RCFunc.getBinaryDataFromHexString(config.getField("message"));
			int sendFrequence = config.getIntField("send_frequence");
			int receiveTimeout = config.getIntField("receive_timeout");
			heartBeatHandler = new HeartBeatHandler(this, heartBeatMessage, sendFrequence, receiveTimeout);
		}
	}



	public final boolean isOpened() {
		return isOpened;
	}



	protected final void setOpened(boolean state) {
		isOpened = state;
	}



	public final void setUndeliverableMessageQueueable(boolean isQueueable) {
		isQueueUndeliverableMessage = isQueueable;
	}



	public final void setMessageForwardDirectly(boolean isDirect) {
		isMessageForwardDirectly = isDirect;
	}



	public final boolean isMessageForwardDirectly() {
		return isMessageForwardDirectly;
	}



	public final boolean isUndeliverableMessageQueueable() {
		return isQueueUndeliverableMessage;
	}



	public final void send(String id, byte[] data) throws Exception {
		if (!isSend)
			return;

		sendData(id, data);
	}



	public void setIsSend(boolean state) {
		isSend = state;
	}



	public Iterator getConnectionIDs() {
		return connTable.getIdentifiers();
	}



	protected synchronized final void addConnection(String id, Object connection) throws Exception {
		if (connTable == null) {
			throw new Exception("ConnectionTable not found.");
		}
		connTable.putConnection(id, connection);
	}



	protected synchronized final Object getConnection(String id) throws Exception {
		if (connTable == null)
			throw new Exception("ConnectionTable not found.");

		Object o = connTable.getConnection(id);
		return o;
	}



	protected synchronized final boolean isConnectionExists(String id) {
		return connTable.isConnectionExists(id);
	}



	protected synchronized final void removeConnection(String id) throws Exception {
		if (connTable == null)
			throw new Exception("ConnectionTable not found.");

		connTable.removeConnection(id);
		showMessage("Removed connection. (" + id + ")");
	}



	protected synchronized final void removeAllConnection() throws Exception {
		if (connTable == null)
			throw new Exception("ConnectionTable not found.");
		connTable.clear();
	}



	protected final String getID(String feature) throws Exception {
		if (correlTable == null)
			throw new Exception("CorrelationTable not found.");

		return correlTable.getProperty(feature);
	}



	protected final boolean isCorrelTableExist() {
		return correlTable != null;
	}



	protected final String getFeature(String id) throws Exception {
		if (correlTable == null)
			throw new Exception("CorrelationTable not found.");

		String rtn = null;
		Enumeration e = correlTable.keys();
		while (e.hasMoreElements() && rtn == null) {
			String key = (String) e.nextElement();
			String value = correlTable.getProperty(key);
			if (value.equals(id))
				rtn = key;
		}
		return rtn;
	}



	protected final void addConnectionTable(IConnectionTable conntable) {
		this.connTable = conntable;
	}



	public final void setCorrelationTable(Hashtable table) {
		this.correlTable = (Properties) table;
	}



	public final void addCommunicationEventListener(ICommunicationEventListener el) {
		this.agentEventListeners.add(el);
	}



	public final void setTargetCommunicationAgents(ICommunicationAgent[] subscribers) {
		this.subscribers = subscribers;
	}



	public final void setTargetCommunicationAgent(ICommunicationAgent subscriber) {
		this.subscribers = new ICommunicationAgent[] { subscriber };
	}



	public final ICommunicationAgent[] getTargetCommunicationAgents() {
		return this.subscribers;
	}



	public final void setName(String name) {
		this.name = name;
	}



	public final String getName() {
		return name;
	}



	public final void setMessageResendManager(IMessageResendManager mrm) {
		this.mrm = mrm;
	}



	public void setMessageProcessCenter(MessageProcessCenter messageProcessCenter) {
		this.messageProcessCenter = messageProcessCenter;
	}



	public MessageProcessCenter getMessageProcessCenter() {
		return this.messageProcessCenter;
	}



	public void setProcessQueue(IQueue queue) {
		this.processQueue = queue;
	}



	public IQueue getProcessQueue() {
		return processQueue;
	}



	public void setResendQueue(IQueue queue) {
		this.resendQueue = queue;
	}



	public IQueue getResendQueue() {
		return resendQueue;
	}



	public void setMaximumConnections(int maximumConnections) {
		this.maximumConnections = maximumConnections;
	}



	public final void sendAll(byte[] data) throws Exception {
		for (Iterator i = connTable.getIdentifiers(); i.hasNext();) {
			String id = (String) i.next();
			send(id, data);
		}
	}



	public final void suspendAll() {
		for (Iterator i = connTable.getIdentifiers(); i.hasNext();) {
			String id = (String) i.next();
			suspend(id);
		}
	}



	public final void resumeAll() {
		for (Iterator i = connTable.getIdentifiers(); i.hasNext();) {
			String id = (String) i.next();
			resume(id);
		}
	}



	public final int getCommunicationCount() {
		return connTable.size();
	}



	public final int getMaximumCommunicationCount() {
		return maximumConnections;
	}



	public void showMessage(String msg) {
		if (logger != null && logger.isLogDebug()) {
			logger.logDebug(MessageFormat.format("[{0}] {1}", new Object[] { name, msg }));
		}
	}



	public void setLogger(SGLogger logger) {
		this.logger = logger;

		if (heartBeatHandler != null) {
			heartBeatHandler.setLogger(logger);
		}
	}



	public SGLogger getLogger() {
		return this.logger;
	}



	public void setEnabled(boolean enabled) {
		this.isEnabled = enabled;
	}



	public boolean isEnabled() {
		return this.isEnabled;
	}
	
	
	public void setHideSensitivityLog(boolean hideSensitivityLog){
		this.isHideSensitivityLog = hideSensitivityLog;
	}
	
	public boolean isHideSensitivityLog(){
		return this.isHideSensitivityLog;
	}
	
	public final String getCommunicationAgentStatus() {
		String status[] = new String[connTable.size()];
		int j = 0;
		for (Iterator i = connTable.getIdentifiers(); i.hasNext();) {
			String id = (String) i.next();
			status[j] = "   " + id;
			status[j] += "\r\n";
			j++;
		}

		Arrays.sort(status);
		String rtn = "Queued message count = " + String.valueOf(processQueue.size());
		for (int i = 0; i < status.length; i++) {
			rtn += status[i];
		}
		return rtn;
	}



	protected final void sendOnClientConnectEvent(ICommunicationAgent sender, String id, String ip, int port) {
		if (heartBeatHandler != null) {
			heartBeatHandler.registerConnection(id);
		}

		for (int i = 0; i < agentEventListeners.size(); i++) {
			ICommunicationEventListener el = (ICommunicationEventListener) agentEventListeners.get(i);
			el.onClientConnectEvent(new CommunicationCommonEvent(sender, id, ip, port));
		}
	}



	protected final void sendOnClientDisconnectEvent(ICommunicationAgent sender, String id) {
		if (heartBeatHandler != null) {
			heartBeatHandler.unregisterConnection(id);
		}

		for (int i = 0; i < agentEventListeners.size(); i++) {
			ICommunicationEventListener el = (ICommunicationEventListener) agentEventListeners.get(i);
			el.onClientDisconnectEvent(new CommunicationCommonEvent(sender, id));
		}
	}



	protected final void sendOnClientReadEvent(ICommunicationAgent sender, String id, byte[] data) {
		try {
			if (heartBeatHandler != null) {
				if (heartBeatHandler.isBeginWithHeartBeatMessage(data)) {
					data = heartBeatHandler.registerHeartBeat(id, data);
				} else {
					heartBeatHandler.registerMessageReceived(id);
				}
			}

			if (data == null) {
				return;
			}

			if (isMessageForwardDirectly) {
				processQueue.put(new Message(id, data));
			}
			if (mrm != null) {
				try {
					mrm.addResendMessage(new Message(id, data));
				} catch (Exception ex1) {
					sendOnClientErrorEvent(	sender,
											id,
											ErrorMessageHandler.emAddMessagetoResendManagerFailed,
											ErrorMessageHandler.getErrorMessage(ErrorMessageHandler.emAddMessagetoResendManagerFailed));
				}
			}
		} catch (Exception ex) {
			sendOnClientErrorEvent(	sender,
									id,
									ErrorMessageHandler.emPutMessageIntoQueueFailed,
									ErrorMessageHandler.getErrorMessage(ErrorMessageHandler.emPutMessageIntoQueueFailed));
		}

		for (int i = 0; i < agentEventListeners.size(); i++) {
			ICommunicationEventListener el = (ICommunicationEventListener) agentEventListeners.get(i);
			el.onClientReadEvent(new CommunicationReadWriteEvent(sender, id, data));
		}
	}



	protected final void sendOnClientWriteEvent(ICommunicationAgent sender, String id, byte[] data) {
		for (int i = 0; i < agentEventListeners.size(); i++) {
			ICommunicationEventListener el = (ICommunicationEventListener) agentEventListeners.get(i);
			el.onClientWriteEvent(new CommunicationReadWriteEvent(sender, id, data));
		}
	}



	protected final void sendOnClientErrorEvent(ICommunicationAgent sender, String id, String errCode, String errMsg) {
		for (int i = 0; i < agentEventListeners.size(); i++) {
			ICommunicationEventListener el = (ICommunicationEventListener) agentEventListeners.get(i);
			el.onClientErrorEvent(new CommunicationErrorEvent(sender, id, errCode, errMsg));
		}
	}



	protected final void sendOnOpenEvent(ICommunicationAgent sender) {
		onStart();

		for (int i = 0; i < agentEventListeners.size(); i++) {
			ICommunicationEventListener el = (ICommunicationEventListener) agentEventListeners.get(i);
			el.onOpenEvent(new CommunicationCommonEvent(sender));
		}
	}



	protected final void sendOnCloseEvent(ICommunicationAgent sender) {
		onStop();

		for (int i = 0; i < agentEventListeners.size(); i++) {
			ICommunicationEventListener el = (ICommunicationEventListener) agentEventListeners.get(i);
			el.onCloseEvent(new CommunicationCommonEvent(sender));
		}
	}



	private void onStart() {
		if (messageProcessCenter != null) {
			messageProcessCenter.start();
		}

		if (heartBeatHandler != null) {
			heartBeatHandler.start();
		}

	}



	private void onStop() {
		if (heartBeatHandler != null) {
			heartBeatHandler.stop();
		}

		if (messageProcessCenter != null) {
			messageProcessCenter.stop();
		}
	}



	protected final void sendOnErrorEvent(ICommunicationAgent sender, String id, String errCode, String errMsg) {
		for (int i = 0; i < agentEventListeners.size(); i++) {
			ICommunicationEventListener el = (ICommunicationEventListener) agentEventListeners.get(i);
			el.onErrorEvent(new CommunicationErrorEvent(sender, id, errCode, errMsg));
		}
	}

}
