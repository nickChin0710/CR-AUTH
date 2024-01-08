package com.tcb.authGateway.agent;

//import com.sage.util.AbstractThread;
import java.util.*;
//import com.sage.log.SGLogger;
//import com.sage.roger.RCFunc;
import com.tcb.authGateway.sg.SGLogger;
import com.tcb.authGateway.thread.AbstractThread;
import com.tcb.authGateway.utils.RCFunc;

public class HeartBeatHandler {

	private SGLogger logger = SGLogger.getLogger();

	private byte[] heartBeatMessage;

	private int sendFrequence;

	private int receiveTimeout;

	private AbstractCommunicationAgent agent;

	private Map lastReceiveTimeMap = new HashMap();

	private Map lastSendTimeMap = new HashMap();

	private HeartBeatThread heartBeatThread = new HeartBeatThread(1000);

	private String logPrefix;



	public HeartBeatHandler(AbstractCommunicationAgent agent, byte[] heartBeatMessage, int sendFrequence,
			int receiveTimeout) {
		this.agent = agent;
		this.heartBeatMessage = heartBeatMessage;
		this.sendFrequence = sendFrequence;
		this.receiveTimeout = receiveTimeout;
		this.logPrefix = "[" + agent.getName() + "] ";
		heartBeatThread.start();
		heartBeatThread.suspendThread();
	}



	public void start() {
		lastReceiveTimeMap.clear();
		lastSendTimeMap.clear();
		heartBeatThread.resumeThread();

		logger.logCritical(logPrefix + "HeartBeatHandler started. " + getConfigInfo());
	}



	private String getConfigInfo() {
		StringBuffer out = new StringBuffer();
		out.append("(HB-MSG={");
		for (int i = 0; i < heartBeatMessage.length; i++) {
			out.append(Integer.toHexString(heartBeatMessage[i]));
			if (i < heartBeatMessage.length - 1) {
				out.append(" ");
			}
		}
		out.append("}, SEND_FRQ=");
		out.append(String.valueOf(sendFrequence));
		out.append("ms, RECV_TMO=");
		out.append(String.valueOf(receiveTimeout));
		out.append("ms)");
		return out.toString();
	}



	public void stop() {
		heartBeatThread.suspendThread();

		logger.logCritical(logPrefix + "HeartBeatHandler stopped.");
	}



	/**
	 * 註冊HeartBeat訊息，回傳已過濾掉HeartBeat訊息的一般訊息
	 * 
	 * @param msg
	 *            包含HeartBeat訊息的訊息
	 * @return
	 */
	public byte[] registerHeartBeat(String sessionId, byte[] msg) {
		if (msg.length < heartBeatMessage.length || !isBeginWithHeartBeatMessage(msg)) {
			return msg;
		}

		while ((msg != null) && (msg.length >= heartBeatMessage.length) && (isBeginWithHeartBeatMessage(msg))) {
			msg = (msg.length > heartBeatMessage.length) ? RCFunc.copyBytes(msg, heartBeatMessage.length, msg.length)
					: null;
		}

		lastReceiveTimeMap.put(sessionId, new Long(System.currentTimeMillis()));
		if (logger.isLogDebug()) {
			logger.logDebug(logPrefix + "Received heartbeat from " + sessionId);
		}
		return msg;
	}



	/**
	 * 註冊收到一般訊息
	 * 
	 * @param sessionId
	 */
	public void registerMessageReceived(String sessionId) {
		lastReceiveTimeMap.put(sessionId, new Long(System.currentTimeMillis()));
	}



	public void registerConnection(String sessionId) {
		Long now = new Long(System.currentTimeMillis());
		lastReceiveTimeMap.put(sessionId, now);
		lastSendTimeMap.put(sessionId, now);
	}



	public void unregisterConnection(String sessionId) {
		lastReceiveTimeMap.remove(sessionId);
		lastSendTimeMap.remove(sessionId);
	}



	/**
	 * 檢查訊息是否以HeartBeat訊息開始
	 * 
	 * @return
	 */
	public boolean isBeginWithHeartBeatMessage(byte[] msg) {
		if (msg == null || msg.length < heartBeatMessage.length) {
			return false;
		}

		for (int i = 0; i < heartBeatMessage.length; i++) {
			if (msg[i] != heartBeatMessage[i]) {
				return false;
			}
		}
		return true;
	}



	private boolean isSendTimeout(String sessionId) {
		return isTimeout(sessionId, lastSendTimeMap, sendFrequence);
	}



	private boolean isRecvTimeout(String sessionId) {
		return isTimeout(sessionId, lastReceiveTimeMap, receiveTimeout);
	}



	private boolean isTimeout(String sessionId, Map timeMap, int timeout) {
		if (!timeMap.containsKey(sessionId)) {
			return true;
		}

		long lastTime = ((Long) timeMap.get(sessionId)).longValue();
		return (System.currentTimeMillis() - lastTime) >= timeout;
	}



	public void setLogger(SGLogger logger) {
		this.logger = logger;
	}



	class HeartBeatThread extends AbstractThread {

		HeartBeatThread(int sendInterval) {
			super(sendInterval);
		}



		public void doMainProcess() {
			checkReceiveHeartBeats();
			sendHeartBeatToAllConnections();
		}



		private void checkReceiveHeartBeats() {
			for (Iterator iter = agent.getConnectionIDs(); iter.hasNext();) {
				String sessionId = (String) iter.next();

				if (agent.isClientConnected(sessionId) && isRecvTimeout(sessionId)) {
					try {
						agent.close(sessionId);

						if (logger.isLogDebug()) {
							logger.logDebug(logPrefix + "Closed client connection(" + sessionId
									+ ") due to missing heartbeat.");
						}
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			}

		}



		private void sendHeartBeatToAllConnections() {
			for (Iterator iter = agent.getConnectionIDs(); iter.hasNext();) {
				String sessionId = (String) iter.next();

				if (agent.isClientConnected(sessionId) && isSendTimeout(sessionId)) {
					sendMessage(sessionId);
				}
			}
		}



		private void sendMessage(String sessionId) {
			try {
				agent.send(sessionId, heartBeatMessage);
				lastSendTimeMap.put(sessionId, new Long(System.currentTimeMillis()));

				if (logger.isLogDebug()) {
					logger.logDebug(logPrefix + "Sent Heartbeat message to " + sessionId);
				}
			} catch (Exception ex) {
				try {
					agent.close(sessionId);
				} catch (Exception ex1) {
					ex1.printStackTrace();
				}
			}
		}
	}

}