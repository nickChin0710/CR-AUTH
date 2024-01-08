package com.tcb.authGateway.sg;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import com.tcb.authGateway.SystemPathConfig;
import com.tcb.authGateway.agent.CommunicationAgentConfig;

//import com.sage.document.DocumentConfig;
//import com.sage.document.ISGDocument;
//import com.sage.document.SGXMLCoder;
//import com.sage.gateway.CommonConfigFormat;
//import com.sage.gateway.SystemPathConfig;

public class MGCConfig extends CommonConfigFormat {

	private String configFilePath;

	public static final String WORKING_CONFIG_PATH = "mgc-config.xml";

	public static final String DEFAULT_CONFIG_PATH = "mgc-config-default.xml";



	public MGCConfig(String configFilePath) {
		super(configFilePath);
		this.configFilePath = configFilePath;
	}



	public static void generateWorkingConfig() throws FileNotFoundException {
		MGCConfig defaultConfig = new MGCConfig(DEFAULT_CONFIG_PATH);
		defaultConfig.saveAs(WORKING_CONFIG_PATH);
	}



	public String getConfigFilePath() {
		return configFilePath;
	}



	protected String getDefaultConfigName() {
		return WORKING_CONFIG_PATH;
	}



	protected String getGlobalConfigPath(SystemPathConfig config) {
		return config.getCommunicationConfigPath();
	}



	public boolean hasDBConfig() {
		return configDoc.containsField("db_config_path");
	}



	public String getDBConfigPath() {
		return configDoc.getStringFieldValue("db_config_path");
	}



	public boolean hasExtendedConfigFilePath() {
		return configDoc.containsField("extended_config");
	}



	public String getExtendedConfigFilePath() {
		return configDoc.getStringFieldValue("extended_config");
	}



	public void setExtendedConfigFilePath(String filePath) {
		configDoc.setFieldValue("extended_config", filePath);
	}



	public String getDescription() {
		if (!configDoc.containsField("description")) {
			return null;
		}
		return configDoc.getStringFieldValue("description");
	}



	public String getName() {
		if (!configDoc.containsField("name")) {
			return "CommGateway";
		}
		return configDoc.getStringFieldValue("name");
	}



	public String getType() {
		if (!configDoc.containsField("type")) {
			return "MGC";
		}
		return configDoc.getStringFieldValue("type");
	}



	public LoggerConfig getLoggerConfig() {
		return new LoggerConfig(configDoc.getSubDocument("logger"));
	}



	public void setLoggerConfig(LoggerConfig loggerConfig) {
		configDoc.setSubDocument("logger", loggerConfig.getDocument());
	}



	public ThreadPoolConfig getThreadPoolConfig() {
		return new ThreadPoolConfig(configDoc.getSubDocument("thread_pool"));
	}



	public CommandServerConfig getCommandServerConfig() {
		if (!configDoc.containsField("command_server")) {
			return null;
		}
		return new CommandServerConfig(configDoc.getSubDocument("command_server"));
	}



	public CommunicationAgentConfig[] getCommunicationAgentConfigs() {
		if (!configDoc.containsField("communication_agents")) {
			return new CommunicationAgentConfig[0];
		}

		ISGDocument[] configDocs = configDoc.getSubDocuments("communication_agents.agent");
		CommunicationAgentConfig[] configs = new CommunicationAgentConfig[configDocs.length];
		for (int i = 0; i < configs.length; i++) {
			configs[i] = new CommunicationAgentConfig(configDocs[i]);
		}
		return configs;
	}



	public String toXML() {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		new SGXMLCoder().encode(configDoc, out, "mgc_config", "big5");
		return out.toString();
	}



	public void save() throws FileNotFoundException {
		saveAs(configFilePath != null ? configFilePath : getDefaultConfigName());
	}



	public void saveAs(String filePath) throws FileNotFoundException {
		try(FileOutputStream out = new FileOutputStream(filePath);){
			new SGXMLCoder().encode(configDoc, out, "mgc_config", "big5");
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}



class CommandServerConfig extends DocumentConfig {

	CommandServerConfig(ISGDocument configDoc) {
		super(configDoc);
	}



	public int getPort() {
		return super.getIntField("port");
	}

}
