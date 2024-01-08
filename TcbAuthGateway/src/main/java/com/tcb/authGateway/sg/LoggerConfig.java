package com.tcb.authGateway.sg;

import com.tcb.authGateway.SystemPathConfig;

//import com.sage.document.ISGDocument;
//import com.sage.gateway.SystemPathConfig;
//import com.sage.document.DocumentConfig;


public class LoggerConfig extends DocumentConfig {
    public LoggerConfig(ISGDocument loggerConfigDoc) {
        super(loggerConfigDoc);
    }



    public boolean isDisplay() {
        return super.isTrue("display");
    }



    public boolean isLogToFile() {
        return super.isTrue("log_to_file");
    }



    public String getLogPath() {
        String logPath = super.getField("log_path");
        if (logPath.startsWith("%") && logPath.endsWith("%")) {
            logPath = new SystemPathConfig().getProperty(logPath.substring(1, logPath.length() - 1));
        }
        return logPath;
    }



    public String getLogFilename() {
        return super.getField("log_filename");
    }



    public boolean isLogBySize() {
        return super.isTrue("log_by_size");
    }



    public boolean isLogByTime() {
        return super.isTrue("log_by_time");
    }



    public int getLogLevel() {
        return super.getIntField("log_level");
    }



    public int getMaxLogSize() {
        return super.getIntField("max_log_size");
    }
}
