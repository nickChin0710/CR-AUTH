package com.tcb.authGateway.agent;

import com.tcb.authGateway.queue.IQueue;
import com.tcb.authGateway.thread.AbstractThread;
import com.tcb.authGateway.thread.IThreadTask;
import com.tcb.authGateway.thread.ThreadPoolExecutor;

//import com.sage.util.AbstractThread;
//import com.sage.util.IQueue;
//import com.sage.util.concurrent.IThreadTask;
//import com.sage.util.concurrent.ThreadPoolExecutor;

public class MessageProcessCenter {

	private IMessageHandler messageHandler;

	private IQueue processQueue;

	private ThreadPoolExecutor threadPool;

	private AbstractCommunicationAgent owner;

	private MessageFetchThread fetchThread;

	private IMessageResendAgent resendAgent;

	private String logPrefix;



	public MessageProcessCenter(AbstractCommunicationAgent owner, IMessageHandler messageHandler,
			ThreadPoolExecutor threadPool) {
		this.owner = owner;
		this.messageHandler = messageHandler;
		this.processQueue = owner.getProcessQueue();
		this.threadPool = threadPool;
		this.logPrefix = "[" + owner.getName() + "] ";
	}



	public MessageProcessCenter(AbstractCommunicationAgent owner, IMessageHandler messageHandler,
			ThreadPoolExecutor threadPool, IMessageResendAgent resendAgent) {
		this(owner, messageHandler, threadPool);
		this.resendAgent = resendAgent;
	}



	public void start() {
		messageHandler.start();

		initFetchThread();
		initResendAgent();

		owner.getLogger().logCritical(logPrefix + "MessageProcessCenter started.");
	}



	private void initResendAgent() {
		if (resendAgent == null) {
			return;
		}
		resendAgent.start();
	}



	private void initFetchThread() {
		if (fetchThread == null) {
			fetchThread = new MessageFetchThread();
			fetchThread.start();
			owner.getLogger().logCritical(logPrefix + "MessageFetchThread started.");
		}
	}



	public void stop() {
		messageHandler.stop();
		stopFetchThread();
		stopResendAgent();

		owner.getLogger().logCritical(logPrefix + "MessageProcessCenter stopped.");
	}



	private void stopResendAgent() {
		if (resendAgent == null) {
			return;
		}

		resendAgent.stop();
	}



	private void stopFetchThread() {
		if (fetchThread == null) {
			return;
		}

		fetchThread.stopThread();
		processQueue.clear();
		fetchThread = null;
		owner.getLogger().logCritical(logPrefix + "MessageFetchThread stopped.");
	}



	public IMessageHandler getMessageHandler() {
		return this.messageHandler;
	}



	class MessageFetchThread extends AbstractThread {

		private MessageFetchThread() {
			super(5);
		}



		protected void doMainProcess() {
			Object message = processQueue.get();
			if (owner.getLogger().isLogDebug()) {
				owner.getLogger().logDebug(logPrefix + "Got message from ProcessQueue.");
			}

			threadPool.execute(new MessageProcessTask((Message) message));
		}
	}



	class MessageProcessTask implements IThreadTask {

		private Message message;



		private MessageProcessTask(Message message) {
			this.message = message;
		}



		public void run() {
			String fromId = message.getID();
			if (messageHandler != null) {
				try {
					message = messageHandler.doMessageHandle(message, owner, owner.getTargetCommunicationAgents());
				} catch (Exception ex) {
					owner.getLogger().logError(ex);
					owner.sendOnClientErrorEvent(	owner,
													message.getID(),
													ErrorMessageHandler.emMessageHandlerError,
													ErrorMessageHandler.getErrorMessage(ErrorMessageHandler.emMessageHandlerError)
															+ ex.getMessage());
					message = null;
				}
			}
			try {
				ICommunicationAgent[] subscribers = owner.getTargetCommunicationAgents();
				if (subscribers != null && message != null) {
					for (int i = 0; i < subscribers.length; i++) {
						sendMessage(subscribers[i], fromId);
					}
				}
			} catch (Exception ex) {
				owner.getLogger().logError(ex);
				owner.sendOnClientErrorEvent(	owner,
												message.getID(),
												ErrorMessageHandler.emForwardMessageFailed,
												ErrorMessageHandler.getErrorMessage(ErrorMessageHandler.emForwardMessageFailed));
			}

		}



		private void sendMessage(ICommunicationAgent subscriber, String fromId) throws Exception {
			try {
				subscriber.send(message.getID(), message.getData());
			} catch (Exception ex) {
				owner.getLogger().logError(ex);

				if (owner.isUndeliverableMessageQueueable() && resendAgent != null) {
					resendAgent.addMessage(new ResendMessage(subscriber.getName(), message, fromId));
					owner.getLogger().logCritical("Put message into ResendQueue due to sending failure. MsgId="
							+ message.getID());
				} else {
					owner.close(fromId);
				}
			}
		}

	}

}