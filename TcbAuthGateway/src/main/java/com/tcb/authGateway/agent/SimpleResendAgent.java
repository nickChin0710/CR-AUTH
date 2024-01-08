package com.tcb.authGateway.agent;

//import com.sage.mgc.IMessageResendAgent;
//import com.sage.mgc.ICommunicationAgent;
//import com.sage.mgc.ResendMessage;
//import com.sage.util.IQueue;
import java.text.MessageFormat;
//import com.sage.util.AbstractThread;
//import com.sage.mgc.IDocumentConfigurable;
//import com.sage.document.DocumentConfig;
import com.tcb.authGateway.queue.IQueue;
import com.tcb.authGateway.sg.DocumentConfig;
import com.tcb.authGateway.sg.IDocumentConfigurable;
import com.tcb.authGateway.thread.AbstractThread;


public class SimpleResendAgent implements IMessageResendAgent, IDocumentConfigurable {

    private ICommunicationAgent owner;

    private IQueue queue;

    private MessageResendThread resendThread;

    private String logPrefix;

    private int resendInterval = 5000;

    private int maxRetryCount = 3;

    public SimpleResendAgent() {
    }



    public void applyConfig(DocumentConfig config) {
        this.resendInterval = config.getIntField("resend_interval");
        this.maxRetryCount = config.getIntField("retry_limit");
    }



    public void addMessage(ResendMessage message) {
        queue.put(message);
    }



    public void start() {
        if (queue == null || !owner.isUndeliverableMessageQueueable()) {
            return;
        }

        if (resendThread == null) {
            resendThread = new MessageResendThread();
            resendThread.start();
            owner.getLogger().logCritical(logPrefix + "MessageResendAgent started.");
        }

    }



    public void stop() {
        if (resendThread == null) {
            return;
        }

        resendThread.stopThread();
        queue.clear();
        resendThread = null;
        owner.getLogger().logCritical(logPrefix + "MessageResendAgent stopped.");

    }



    class MessageResendThread extends AbstractThread {
        private MessageResendThread() {
            super(resendInterval);
        }



        protected void doMainProcess() {
            ResendMessage message = (ResendMessage)queue.get();
            if (owner.getLogger().isLogDebug()) {
                owner.getLogger().logDebug(logPrefix + "Got message from ResendQueue.");
            }

            ICommunicationAgent agent = getAgent(message.getAgentName());
            if (agent != null) {
                try {
                    agent.send(message.getMessage().getID(), message.getMessage().getData());
                }
                catch (Exception ex) {
                    message.resend();
                    owner.getLogger().logWarning(MessageFormat.format("Resend message failed. MsgId={0}, count={1}",
                        new Object[] {message.getMessage().getID(), new Integer(message.getResendCount())}));

                    if (message.getResendCount() >= maxRetryCount) {
                        try {
                            owner.close(message.getFromId());
                        }
                        catch (Exception ex1) {
                        }
                        owner.getLogger().logCritical("Abort message due to exceed resend count. MsgId=" + message.getMessage().getID());
                    }
                    else {
                        queue.put(message);
                    }
                }
            }
            else {
                owner.getLogger().logError("Failed to resend message due to non-exist CommunicationAgent:" + message.getAgentName());
            }
        }



        private ICommunicationAgent getAgent(String agentName) {
            ICommunicationAgent[] subscribers = owner.getTargetCommunicationAgents();
            for (int i = 0; i < subscribers.length; i++) {
                if (subscribers[i].getName().equals(agentName)) {
                    return subscribers[i];
                }
            }
            return null;
        }

    }



    public IQueue getQueue() {
        return queue;
    }



    public void setQueue(IQueue queue) {
        this.queue = queue;
    }



    public void setCommunicationAgent(ICommunicationAgent agent) {
        this.owner = agent;
        this.logPrefix = "[" + owner.getName() + "] ";
    }



    public ICommunicationAgent getCommunicationAgent() {
        return this.owner;
    }



    public void setMaxRetryCount(int maxRetryCount) {
        this.maxRetryCount = maxRetryCount;
    }



    public void setResendInterval(int resendInterval) {
        this.resendInterval = resendInterval;
    }

}