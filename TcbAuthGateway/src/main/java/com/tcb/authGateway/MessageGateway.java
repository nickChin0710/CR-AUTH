package com.tcb.authGateway;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import com.tcb.authGateway.agent.AbstractCommunicationAgent;
import com.tcb.authGateway.agent.AbstractMessageGateway;
import com.tcb.authGateway.agent.CommunicationAgentConfig;
import com.tcb.authGateway.agent.ICommunicationAgent;
import com.tcb.authGateway.agent.ICommunicationEventListener;
import com.tcb.authGateway.agent.IMessageGatewayNeeded;
import com.tcb.authGateway.agent.IMessageHandler;
import com.tcb.authGateway.agent.IMessageResendAgent;
import com.tcb.authGateway.agent.MessageProcessCenter;
import com.tcb.authGateway.queue.IQueue;
import com.tcb.authGateway.queue.IQueueFactory;
import com.tcb.authGateway.sg.DocumentConfig;
import com.tcb.authGateway.sg.IDocumentConfigurable;
import com.tcb.authGateway.sg.LoggerConfig;
import com.tcb.authGateway.sg.MGCConfig;
import com.tcb.authGateway.sg.SGLogger;
import com.tcb.authGateway.sg.ThreadPoolConfig;
import com.tcb.authGateway.thread.JVMStatusRecorder;
import com.tcb.authGateway.thread.ThreadPoolExecutor;
import com.tcb.authGateway.utils.Utils;

//import com.sage.db.DatabaseAccessManager;
//import com.sage.document.DocumentConfig;
//import com.sage.document.ISGDocument;
//import com.sage.document.SGXMLCoder;
//import com.sage.log.SGLogger;
//import com.sage.util.IQueue;
//import com.sage.util.JVMStatusRecorder;
//import com.sage.util.concurrent.ThreadPoolExecutor;

public class MessageGateway extends AbstractMessageGateway {

    public static final String VERSION = "";

    public static final String COPYRIGHT = "";

    private String gatewayId = "1";

    private String configFileName;

    private static final int GW_MSG_LEN = 1595;

    private String name;

    private String type;

    private JVMStatusRecorder status;

    private ThreadPoolExecutor threadPool;
//by Kevin remove    
//    private CommandServer commandServer;

    private List<Runnable> shutdownHooks;

    public MessageGateway() {
    }

    public MessageGateway(String configFileName) {
        this.configFileName = configFileName;
    }

    public CommunicationAgentConfig loadCommunicationAgentConfig(String agentName) {
        MGCConfig config = loadConfig();

        for (int i = 0; i < config.getCommunicationAgentConfigs().length; i++) {
            CommunicationAgentConfig agentConfig = config.getCommunicationAgentConfigs()[i];
            if (agentConfig.getAlias() != null && agentConfig.getAlias().equals(agentName)) {
                return agentConfig;
            }
        }
        return null;
    }

    public void addShutdownHook(Runnable runnable) {
        shutdownHooks.add(runnable);
    }

    private void executeShutdownHooks() {
        for (Runnable hook : shutdownHooks) {
            try {
                hook.run();
            } catch (Exception e) {
                super.getLogger().logWarning("Fail to execute shutdown hook. " + e.getMessage());
            }
        }
    }

    public MGCConfig loadConfig() {
        try {
            File file = new File(configFileName != null ? configFileName : MGCConfig.WORKING_CONFIG_PATH);
            if (!file.exists()) {
                MGCConfig.generateWorkingConfig();
            }

            MGCConfig config = new MGCConfig(configFileName);
            if (config.hasExtendedConfigFilePath()) {
                try {
                    config = new MGCConfig(config.getExtendedConfigFilePath());
                } catch (Exception ex) {
                    super.getLogger().logError(ex);
                    super.showMessage("Loading specified config("
                            + config.getExtendedConfigFilePath()
                            + ") failed. Using default config instead.");
                }
            }
            return config;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public void saveConfigAs(String fileName, String configXML) throws FileNotFoundException, IOException {
        try(FileOutputStream fileOut = new FileOutputStream(fileName);){
            fileOut.write(configXML.getBytes());
            fileOut.close();
        }
        MGCConfig config = new MGCConfig(configFileName);
        config.setExtendedConfigFilePath(fileName);
        config.save();
    }
//by Kevin remove
//    public CommandServer getCommandServer() {
//        return this.commandServer;
//    }

    public void reload() throws Exception {
        super.stopGateway();
        super.init();
        super.startGateway();
    }
//by Kevin remove
//    protected void startImpl() {
//        if (DatabaseAccessManager.getInstance().isInit() && !DatabaseAccessManager.getInstance().isConnected()) {
//            DatabaseAccessManager.getInstance().start();
//        }
//    }
//
//    protected void stopImpl() {
//        if (DatabaseAccessManager.getInstance().isInit()) {
//            DatabaseAccessManager.getInstance().stop();
//        }
//
//        if (commandServer != null) {
//            commandServer.stop();
//        }
//
//        executeShutdownHooks();
//    }

    protected void initImpl() {
        shutdownHooks = new ArrayList<Runnable>();

        MGCConfig config = loadConfig();
        name = config.getName();
        type = config.getType();
        initLogger(config.getLoggerConfig());

        String loadedConfigFileName = (config.getConfigFilePath() == null ? config.WORKING_CONFIG_PATH
                : config.getConfigFilePath());
        getLogger().logCritical("Config file loaded. (" + loadedConfigFileName + ")");
//by Kevin remove
//        initDatabaseAccessManager(config);

//by Kevin remove
//      initCommandServer(config.getCommandServerConfig());
        initThreadPool(config.getThreadPoolConfig());
        initCommunicationAgents(config.getCommunicationAgentConfigs());
        initCommunicationAgentTargets(config.getCommunicationAgentConfigs());

        super.showMessage(VERSION + (config.getDescription() != null ? "(" + config.getDescription() + ")" : ""));
        super.showMessage(String.format(COPYRIGHT, new Date()));
        super.showMessage("MessageGateway started successfully. (" + name + ")");
    }
//by Kevin remove
//    protected void initCommandServer(CommandServerConfig config) {
//        if (config == null || commandServer != null) {
//            return;
//        }
//
//        commandServer = new CommandServer(config.getPort(), this, getLogger());
//        commandServer.applyConfig(config);
//        commandServer.start();
//    }
//by Kevin remove
//    private void initDatabaseAccessManager(MGCConfig config) {
//        if (!config.hasDBConfig()) {
//            return;
//        }
//
//        ISGDocument doc = new SGXMLCoder().decode(config.getDBConfigPath());
//        if (!DatabaseAccessManager.getInstance().isInit()) {
//            DatabaseAccessManager.getInstance().init(new DocumentConfig(doc));
//        }
//
//        if (!DatabaseAccessManager.getInstance().isConnected()) {
//            DatabaseAccessManager.getInstance().start();
//        }
//    }

    private void initCommunicationAgentTargets(CommunicationAgentConfig[] configs) {
        for (int i = 0; i < configs.length; i++) {
            ICommunicationAgent agent = super.getCommunicationAgent(configs[i].getAlias());
            if (agent != null) {
                agent.setTargetCommunicationAgents(getTargetAgents(configs[i]));
            }
        }
    }

    private ICommunicationAgent[] getTargetAgents(CommunicationAgentConfig config) {
        String[] targetNames = config.getTargetAgentNames();
        if (targetNames == null) {
            return null;
        }
        List list = new ArrayList();

        for (int i = 0; i < targetNames.length; i++) {
            ICommunicationAgent target = super.getCommunicationAgent(targetNames[i]);
            if (target != null) {
                list.add(target);
            }
        }

        ICommunicationAgent[] targetAgents = new ICommunicationAgent[list.size()];
        list.toArray(targetAgents);
        return targetAgents;
    }

    private void initCommunicationAgents(CommunicationAgentConfig[] configs) {
        for (int i = 0; i < configs.length; i++) {
            String caName = configs[i].getAlias();
            try {
                initCommunicationAgent(configs[i]);
            } catch (Exception ex) {
                getLogger().logError(MessageFormat.format("Initializing CommunicationAgent({0}) failed. {1}",
                                                          new Object[] { caName, ex.getMessage() }));
                getLogger().logDebug(ex);
            }
        }
    }

    private void initCommunicationAgent(CommunicationAgentConfig config) {
        getLogger().logNormal(MessageFormat.format("Initializing CommunicationAgent({0})....",
                                                   new Object[] { config.getAlias() }));
        AbstractCommunicationAgent agent = (AbstractCommunicationAgent) createObject(config.getImplementClassName());

        agent.setMessageGateway(this);
        agent.setName(config.getAlias());
        agent.setLogger(getLogger());
        agent.applyConfig(config);
        agent.setHideSensitivityLog(config.isHideSensitivityLog());

        initCommunicationAgentListeners(config, agent);
//by Kevin remove
        // agent.setCorrelationTable(getCorrelationTable(null));
        agent.setProcessQueue(getProcessQueue(config));
        agent.setResendQueue(getResendQueue(config));
        agent.setMessageProcessCenter(new MessageProcessCenter(agent, getMessageHandler(config, agent), threadPool,
                getMessageResendAgent(config, agent)));

        super.addCommunicationAgent(agent);
        getLogger().logCritical(MessageFormat.format("Initialized CommunicationAgent. ({0})",
                                                     new Object[] { agent.getName() }));
    }

    private void initCommunicationAgentListeners(CommunicationAgentConfig config, AbstractCommunicationAgent agent) {
        if (!config.hasEventListenerConfig()) {
            if (config.containsField("event_listener")) {
                agent.addCommunicationEventListener(getCommunicationEventListener(config.getDocumentConfig("event_listener")));
            } else {
                getLogger().logWarning("No CommunicationEventListener configured. Please check config file.");
            }
            return;
        }

        DocumentConfig[] eventListenerConfigs = config.getEventListenerConfig();
        for (int i = 0; i < eventListenerConfigs.length; i++) {
            agent.addCommunicationEventListener(getCommunicationEventListener(eventListenerConfigs[i]));
        }
    }

    private ICommunicationEventListener getCommunicationEventListener(DocumentConfig config) {
        ICommunicationEventListener eventListener = (ICommunicationEventListener) createObject(config.getField("class"));

        assigntMessageGatewayIfNeeded(eventListener);
        applyAdvancedConfigIfNeeded(eventListener, config);

        getLogger().logCritical(MessageFormat.format("Initialized CommunicationEventListener. ({0})",
                                                     new Object[] { eventListener.getClass().getName() }));
        return eventListener;
    }

    private void applyAdvancedConfigIfNeeded(Object object, DocumentConfig config) {
        if (object instanceof IDocumentConfigurable) {
            ((IDocumentConfigurable) object).applyConfig(config);

            getLogger().logCritical(MessageFormat.format("Applied config for ({0}).", new Object[] { object.getClass()
                                                                                                           .getName() }));
        }
    }

    private void assigntMessageGatewayIfNeeded(Object object) {
        if (object instanceof IMessageGatewayNeeded) {
            ((IMessageGatewayNeeded) object).setMessageGateway(this);
        }
    }

    private Hashtable getCorrelationTable(String className) {
        return null;
    }

    private IQueue getProcessQueue(CommunicationAgentConfig config) {
        if (!config.hasProcessQueueFactoryConfig()) {
            throw new RuntimeException("No config found for queue.");
        }

        IQueueFactory queueFactory = (IQueueFactory) createObject(config.getProcessQueueFactoryClassName());
        IQueue queue = queueFactory.createQueue(config.getProcessQueueFactoryConfig());

        getLogger().logCritical(MessageFormat.format("Initialized ProcessQueue. ({0})",
                                                     new Object[] { queue.getClass().getName() }));
        return queue;
    }

    private IQueue getResendQueue(CommunicationAgentConfig config) {
        if (!config.hasResendQueueFactoryConfig()) {
            return null;
        }

        IQueueFactory queueFactory = (IQueueFactory) createObject(config.getResendQueueFactoryClassName());
        IQueue queue = queueFactory.createQueue(config.getResendQueueFactoryConfig());

        getLogger().logCritical(MessageFormat.format("Initialized ResendQueue. ({0})", new Object[] { queue.getClass()
                                                                                                           .getName() }));
        return queue;
    }

    private IMessageHandler getMessageHandler(CommunicationAgentConfig config, ICommunicationAgent agent) {
        if (!config.hasMessageHandlerConfig()) {
            getLogger().logNormal("No MessageHandler configured.");
            return null;
        }

        IMessageHandler messageHandler = (IMessageHandler) createObject(config.getMessageHandlerClassName());

        messageHandler.setCommunicationAgent(agent);
        assigntMessageGatewayIfNeeded(messageHandler);
        applyAdvancedConfigIfNeeded(messageHandler, config.getMessageHandlerConfig());

        getLogger().logCritical(MessageFormat.format("Initialized MessageHandler. ({0})",
                                                     new Object[] { messageHandler.getClass().getName() }));
        return messageHandler;
    }

    private IMessageResendAgent getMessageResendAgent(CommunicationAgentConfig config, AbstractCommunicationAgent agent) {
        if (!config.hasMessageResendAgentConfig()) {
            getLogger().logNormal("No MessageResendAgent configured.");
            return null;
        }

        IMessageResendAgent resendAgent = (IMessageResendAgent) createObject(config.getMessageResendAgentClassName());

        resendAgent.setQueue(agent.getResendQueue());
        resendAgent.setCommunicationAgent(agent);

        assigntMessageGatewayIfNeeded(resendAgent);
        applyAdvancedConfigIfNeeded(resendAgent, config.getMessageResendAgentConfig());

        getLogger().logCritical(MessageFormat.format("Initialized MessageResendAgent. ({0})",
                                                     new Object[] { resendAgent.getClass().getName() }));
        return resendAgent;
    }

    private Object createObject(String className) {
        return Utils.createObject(className);
    }

    protected void initLogger(LoggerConfig loggerConfig) {
        try {
            applyLoggerConfig(loggerConfig);

            initStatusRecorder(loggerConfig.getLogPath());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public void applyLoggerConfig(LoggerConfig loggerConfig) throws Exception {
        getLogger().setLogDisplay(loggerConfig.isDisplay());
        getLogger().setCurrentLoggingLevel(loggerConfig.getLogLevel());
        getLogger().setLogBySize(loggerConfig.isLogBySize());
        getLogger().setLogByTime(loggerConfig.isLogByTime());
        getLogger().setLogToFile(loggerConfig.isLogToFile());
        getLogger().setLogPath(loggerConfig.getLogPath());
        getLogger().setLogFileName(loggerConfig.getLogFilename());
        getLogger().setMaxLogSize(loggerConfig.getMaxLogSize());
        getLogger().init();
        getLogger().logCritical("Logger initialized.");
    }

    private void initStatusRecorder(String logPath) {
        try {
            SGLogger.getLogger(status.LOGGER).setLogPath(logPath);
            SGLogger.getLogger(status.LOGGER).setLogFileName("status.log");
            SGLogger.getLogger(status.LOGGER).setLogDisplay(false);
            SGLogger.getLogger(status.LOGGER).setLogToFile(true);
            SGLogger.getLogger(status.LOGGER).init();
            if (status == null) {
                status = new JVMStatusRecorder(10000);
                status.start();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    protected void initThreadPool(ThreadPoolConfig config) {
        if (threadPool != null) {
            threadPool.stop();
        }

        try {
            int minThreads = config.getMinimumThreadCount();
            int maxThreads = config.getMaximumThreadCount();
            int aliveTime = config.getThreadAliveTime();
            int maxBusyTime = config.getThreadMaximumBusyTime();

            threadPool = new ThreadPoolExecutor(minThreads, maxThreads, aliveTime, maxBusyTime);
            getLogger().logCritical("ThreadPool initialized. (min="
                    + minThreads
                    + ", max="
                    + maxThreads
                    + ", alive="
                    + aliveTime
                    + ", busy="
                    + maxBusyTime
                    + ")");
        } catch (Exception ex) {
            threadPool = new ThreadPoolExecutor();
            getLogger().logWarning("Loading system config failed. Initializing ThreadPool with default parameters.");
        }

        threadPool.init();
    }

    protected ThreadPoolExecutor getThreadPool() {
        return threadPool;
    }

    public void shutdown() {
        new Timer().schedule(new TimerTask() {

            public void run() {
                System.exit(0);
            }
        }

        , 2000);
    }

    public static void main(String[] args) {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        String configFileName = null;
        if (args.length > 0) {
            configFileName = args[0];
        }
        final MessageGateway mg = new MessageGateway(configFileName);
        mg.init();
        
        Runtime.getRuntime().addShutdownHook(new Thread() {

            public void run() {
                try {
                    mg.stopGateway();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        try {
            mg.startGateway();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

}