package com.tcb.authGateway.agent;

import com.tcb.authGateway.sg.DocumentConfig;
import com.tcb.authGateway.sg.ISGDocument;
import com.tcb.authGateway.utils.RCFunc;

//import com.sage.document.*;
//import com.sage.roger.RCFunc;


public class CommunicationAgentConfig extends DocumentConfig {
    public CommunicationAgentConfig(ISGDocument configDoc) {
        super(configDoc);
    }



    public int getPort() {
        return super.getIntField("port");
    }



    public String getIP() {
        return super.getField("ip");
    }



    public String getAlias() {
        return super.getField("name");
    }



    public int getMaxConnections() {
        return super.getIntField("max_connections");
    }



    public String getImplementClassName() {
        return super.getField("class");
    }



    public String[] getTargetAgentNames() {
        if (!super.containsField("target")) {
            return null;
        }
        return RCFunc.splitString(super.getField("target"), ",");
    }



    public DocumentConfig getAdvancedConfig() {
        return super.getDocumentConfig("advanced_parameters");
    }



    public boolean hasAdvancedConfig() {
        return super.containsField("advanced_parameters");
    }



    public boolean hasHeartBeatHandlerConfig() {
        return super.containsField("heart_beat_handler");
    }



    public DocumentConfig getHeartBeatHandlerConfig() {
        return super.getDocumentConfig("heart_beat_handler");
    }



    public String getProcessQueueFactoryClassName() {
        return getProcessQueueFactoryConfig().getField("factory");
    }



    public DocumentConfig getProcessQueueFactoryConfig() {
        if (super.containsField("process_queue")) {
            return super.getDocumentConfig("process_queue");
        }
        return super.getDocumentConfig("queue");
    }



    public String getResendQueueFactoryClassName() {
        return getResendQueueFactoryConfig().getField("factory");
    }



    public DocumentConfig getResendQueueFactoryConfig() {
        return super.getDocumentConfig("resend_queue");
    }



    public boolean hasProcessQueueFactoryConfig() {
        return getProcessQueueFactoryConfig() != null;
    }



    public boolean hasResendQueueFactoryConfig() {
        return getResendQueueFactoryConfig() != null;
    }



    public String[] getEventListenersClassNames() {
        DocumentConfig[] configs = this.getEventListenerConfig();
        String[] classes = new String[configs.length];
        for (int i = 0; i < configs.length; i++) {
            classes[i] = configs[i].getField("class");
        }
        return classes;
    }



    public DocumentConfig[] getEventListenerConfig() {
        return super.getDocumentConfigs("event_listeners.listener");
    }



    public boolean hasEventListenerConfig() {
        return getEventListenerConfig() != null;
    }



    public String getMessageHandlerClassName() {
        return getMessageHandlerConfig().getField("class");
    }



    public DocumentConfig getMessageHandlerConfig() {
        return super.getDocumentConfig("message_handler");
    }



    public boolean hasMessageHandlerConfig() {
        return getMessageHandlerConfig() != null;
    }



    public DocumentConfig getMessageResendAgentConfig() {
        return super.getDocumentConfig("resend_agent");
    }



    public String getMessageResendAgentClassName() {
        return getMessageResendAgentConfig().getField("class");
    }



    public boolean hasMessageResendAgentConfig() {
        return getMessageResendAgentConfig() != null;
    }
    
    public boolean isHideSensitivityLog(){
      if (!super.containsField("hide_sensitivity_log")) {
        return true;
      }
    	return new Boolean(super.getField("hide_sensitivity_log"));
    }
    
}
