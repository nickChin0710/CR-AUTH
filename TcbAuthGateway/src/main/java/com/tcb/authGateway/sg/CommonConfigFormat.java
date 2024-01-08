package com.tcb.authGateway.sg;

import java.io.File;
import com.tcb.authGateway.SystemPathConfig;

//import com.sage.document.*;


public abstract class CommonConfigFormat {

    protected ISGDocument configDoc;

    private SystemPathConfig sysConfig;

    public CommonConfigFormat(String configFileName) {
        init(configFileName);
    }



    protected abstract String getDefaultConfigName();



    protected abstract String getGlobalConfigPath(SystemPathConfig config);



    private void init(String configFileName) {
        SGXMLCoder coder = new SGXMLCoder();

        try {
            configDoc = coder.decode(getAbsolouteConfigFileName(configFileName));
        }
        catch (Exception ex) {
            throw new RuntimeException("Loading config failed.", ex);
        }
    }



    private String getAbsolouteConfigFileName(String configFileName) {
        if (configFileName == null) {
            return this.getDefaultConfigName();
        }
        else if (configFileName.indexOf("/") > -1 || configFileName.indexOf("\\") > -1) {
            return configFileName;
        }
        else {
            try {
                sysConfig = new SystemPathConfig();
                return getGlobalConfigPath(sysConfig) + File.separator + configFileName;
            }
            catch (Exception ex) {
                return configFileName;
            }
        }

    }



    public boolean isLog() {
        String log = (String)configDoc.getFieldValue("log");
        return new Boolean(log).booleanValue();
    }



    public boolean isTrace() {
        String trace = (String)configDoc.getFieldValue("trace");
        return new Boolean(trace).booleanValue();
    }



    public boolean isDisplay() {
        String display = (String)configDoc.getFieldValue("display");
        return new Boolean(display).booleanValue();
    }



    public String getLogPath() {
        String logPath = configDoc.getStringFieldValue("log-path");
        if (logPath.startsWith("%") && logPath.endsWith("%")) {
            logPath = sysConfig.getProperty(logPath.substring(1, logPath.length() - 1));
        }
        return logPath;
    }



    public String getLogFileName() {
        String logFileName = configDoc.getStringFieldValue("log-filename");
        return logFileName;
    }

}
