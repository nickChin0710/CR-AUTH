package com.tcb.authGateway.agent;

import java.util.Properties;
import com.tcb.authGateway.utils.RCFunc;
//import com.sage.roger.RCFunc;
import java.text.MessageFormat;


public class CommonAgentEventListener implements ICommunicationEventListener, IMessageGatewayNeeded {

    private final static String LINE_SEPARATOR = System.getProperty("line.separator");

    private IMessageGateway owner;

    private String lastErrorCode = "";

    private String agentName;

    private String prefixAgentName;

    private String encoding = "Big5";

    /**
     * This table is used to store error codes of error message which are not shown when duplicate error occurred.
     */
    private Properties duplicatedErrMsgTable = new Properties();

    private void initErrMsgTable() {
        duplicatedErrMsgTable.setProperty(ErrorMessageHandler.emForwardMessageFailed, "1");
        duplicatedErrMsgTable.setProperty(ErrorMessageHandler.emConnectionNotOpenYet, "1");
        duplicatedErrMsgTable.setProperty(ErrorMessageHandler.emUndefinedConnection, "1");
        duplicatedErrMsgTable.setProperty(ErrorMessageHandler.emAddNewConnectionFailed, "1");
    }



    public CommonAgentEventListener() {
        initErrMsgTable();
    }



    protected IMessageGateway getMessageGateway() {
        return this.owner;
    }



    public void setMessageGateway(IMessageGateway messageGateway) {
        this.owner = messageGateway;
    }



    protected void showMessage(String msg) {
        owner.getLogger().logDebug(prefixAgentName + msg);
    }



    private void showData(String data) {
        if (owner.getLogger().isLogVerbose()) {
            owner.getLogger().logVerbose(1, prefixAgentName + LINE_SEPARATOR + data);
        }
    }



    private void showData(byte[] data) {
        if (owner.getLogger().isLogVerbose()) {
            try {
                showData(RCFunc.getHexTable(data, encoding));
            }
            catch (Exception ex) {
            }
        }
    }



    public final void onClientConnectEvent(CommunicationCommonEvent ev) {
        if (agentName == null) {
            setAgentNames(ev);
        }
        showMessage(ev.getClientID() + " is on.");
        onClientConnectEventImpl(ev);
    }



    protected void onClientConnectEventImpl(CommunicationCommonEvent ev) {

    }



    private void setAgentNames(CommunicationCommonEvent ev) {
        agentName = ev.getCommunicationAgent().getName();
        prefixAgentName = RCFunc.fillSpace("<" + ev.getCommunicationAgent().getName() + ">", 10, false);
    }



    public final void onClientDisconnectEvent(CommunicationCommonEvent ev) {
        if (agentName == null) {
            setAgentNames(ev);
        }

        showMessage(ev.getClientID() + " is off.");

        onClientDisconnectEventImpl(ev);
    }



    protected void onClientDisconnectEventImpl(CommunicationCommonEvent ev) {

    }



    public void onClientReadEvent(CommunicationReadWriteEvent ev) {
        if (agentName == null) {
            setAgentNames(ev);
        }

        showMessage(MessageFormat.format("{0}[{1}]   ---->   MGC ({2} bytes)", new Object[] {agentName, ev.getClientID(),
            new Integer(ev.getData().length)}));
        showData(ev.getData());
    }



    public void onClientWriteEvent(CommunicationReadWriteEvent ev) {
        if (agentName == null) {
            setAgentNames(ev);
        }

        showMessage(MessageFormat.format("MGC   ---->   {0}[{1}] ({2} bytes)", new Object[] {agentName, ev.getClientID(),
            new Integer(ev.getData().length)}));
        showData(ev.getData());

    }



    public void onClientErrorEvent(CommunicationErrorEvent ev) {
        if (agentName == null) {
            setAgentNames(ev);
        }

        boolean isDuplicatedErrMsgForNotShown = duplicatedErrMsgTable.containsKey(ev.getErrorCode());

        if (!isDuplicatedErrMsgForNotShown || (isDuplicatedErrMsgForNotShown && !lastErrorCode.equals(ev.getErrorCode()))) {
            showMessage(MessageFormat.format("[Client Error] {0} {1}", new Object[] {ev.getErrorCode(), ev.getErrorMessage()}));
        }
        lastErrorCode = ev.getErrorCode();
    }



    public void onErrorEvent(CommunicationErrorEvent ev) {
        if (agentName == null) {
            setAgentNames(ev);
        }

        boolean isDuplicatedErrMsgForNotShown = duplicatedErrMsgTable.containsKey(ev.getErrorCode());

        if (!isDuplicatedErrMsgForNotShown || (isDuplicatedErrMsgForNotShown && !lastErrorCode.equals(ev.getErrorCode())))
            showMessage(MessageFormat.format("[Error] {0} {1}", new Object[] {ev.getErrorCode(), ev.getErrorMessage()}));

        lastErrorCode = ev.getErrorCode();
    }



    public void onOpenEvent(CommunicationCommonEvent ev) {
        if (agentName == null) {
            setAgentNames(ev);
        }

        showMessage("All connections are opened.");
    }



    public void onCloseEvent(CommunicationCommonEvent ev) {
        if (agentName == null) {
            setAgentNames(ev);
        }

        showMessage("All connections are closed.");
    }

}
