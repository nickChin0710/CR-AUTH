package com.tcb.authGateway.sg;

import java.io.*;
import java.text.*;
import java.util.*;

 
/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class SGLogger {

    public static int LEVEL_CRITICAL = 1;

    public static int LEVEL_WARNING = 2;

    public static int LEVEL_NORMAL = 3;

    public static int LEVEL_DEBUG = 4;

    private static final SGLogger logger = new SGLogger();

    private static Map logPool = new Hashtable();

    private static SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS ");

    private static SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd");

    private static SimpleDateFormat sdf3 = new SimpleDateFormat("yyyy-MM-dd-HH");

    private static NumberFormat nf = new DecimalFormat("000");

    private OutputStream screenOut = System.out;

    private OutputStream logOut;

    private String logDateStr;

    private int currLoggingLevel = 5;

    private boolean isDisplay = true;

    private boolean isLog = false;

    private String fileName = "LOG.log";

    private String path = "";

    private int logIndex;

    private int maxLogSize = 104857600;

    private long curLogSize;

    private String encoding = "Big5";

    private boolean logByTime = false;

    private boolean logBySize = true;

    private File logFile;

    private SGLogger() {
    }



    public static SGLogger getLogger() {
        return logger;
    }



    public static SGLogger getLogger(String loggerName) {
        if (logPool.containsKey(loggerName)) {
            return (SGLogger)logPool.get(loggerName);
        }

        SGLogger newLogger = new SGLogger();
        newLogger.fileName = loggerName + ".Log";
        logPool.put(loggerName, newLogger);
        return newLogger;
    }



    public synchronized void init() throws FileNotFoundException {
        
    		String logDateStr = (logByTime ? sdf3 : sdf2).format(new Date());
        boolean isChangeDate = logDateStr.equals(this.logDateStr);
         
        this.logDateStr = logDateStr;
        if(!isChangeDate){
        	logFile = null;
        	logIndex = 0;
        }

        
        logFile = generateLogFile(path, fileName);
        curLogSize = logFile.length();

        if (isLog) {
            if (logOut != null) {
                try {
                    close();
                }
                catch (IOException ex) {
                }
            }

            logOut = new BufferedOutputStream(new FileOutputStream(logFile, true));
        }
    }



    private File generateLogFile(String path, String fileName) {
        if (fileName.indexOf(".") > -1) {
            fileName = fileName.substring(0, fileName.lastIndexOf("."));
        }

        while (logFile == null || ((logFile.exists() && (logFile.length() > maxLogSize || curLogSize > maxLogSize)) )) {
            logFile = new File(getFullPath(path, fileName, logIndex++));
        }
        return logFile;
    }



    private String getFullPath(String path, String fileName, int index) {
        StringBuffer sb = new StringBuffer(path);
        sb.append(logDateStr);
        sb.append('-');
        sb.append(fileName);
        if (index > 0) {
            sb.append('-');
            sb.append(index <= 999 ? nf.format(index) : String.valueOf(index));
        }
        sb.append(".log");
        return sb.toString();
    }



    public void close() throws IOException {
        logOut.close();
    }



    public void setLogFileName(String filename) throws Exception {
        this.fileName = filename;
        if (logOut != null) {
            close();
        }
        logFile = null;
        logIndex = 0;
    }



    public boolean isLogDebug() {
        return currLoggingLevel >= this.LEVEL_DEBUG;
    }



    public boolean isLogVerbose() {
        return currLoggingLevel >= this.LEVEL_DEBUG + 1;
    }



    public boolean isLogNormal() {
        return currLoggingLevel >= this.LEVEL_NORMAL;
    }



    public void setLogPath(String path) {
        this.path = path;
    }



    public int getCurrentLoggingLevel() {
        return currLoggingLevel;
    }



    public String getLogPath() {
        return this.path;
    }



    public void setOutputStream(OutputStream os) {
        this.screenOut = os;
    }



    public OutputStream getOutputStream() {
        return this.logOut;
    }



    public void setLogDisplay(boolean state) {
        isDisplay = state;
    }



    public void setLogToFile(boolean state) {
        isLog = state;
    }



    public void setCurrentLoggingLevel(int level) {
        currLoggingLevel = level;
    }



    public void setMaxLogSize(int size) {
        this.maxLogSize = size;
    }


    
    public int getMaxLogSize() {
    	return this.maxLogSize;
    }
    
    

    public void setLogEncoding(String encoding) {
        this.encoding = encoding;
    }



    public void setLogBySize(boolean enabled) {
        this.logBySize = enabled;
    }



    public void setLogByTime(boolean enabled) {
        this.logByTime = enabled;
    }



    public void logWarning(String className, String msg) {
        if (currLoggingLevel >= LEVEL_WARNING)
            log(className, msg, "W");
    }



    public void logWarning(String msg) {
        logWarning(null, msg);
    }



    public void logCritical(String msg) {
        logCritical(null, msg);
    }



    public void logError(String msg) {
        logError(null, msg);
    }



    public void logDebug(String msg) {
        logDebug(null, msg);
    }



    public void logNormal(String msg) {
        logNormal(null, msg);
    }



    public void logVerbose(int verboseLevel, String msg) {
        logVerbose(verboseLevel, null, msg);
    }



    public void logNormal(String className, String msg) {
        if (currLoggingLevel >= LEVEL_NORMAL)
            log(className, msg, "N");
    }



    public void logDebug(String className, String msg) {
        if (currLoggingLevel >= LEVEL_DEBUG)
            log(className, msg, "D");
    }



    public void logDebug(String className, Throwable t) {
        logDebug(className, getStackTraceString(t, ""));
    }



    public void logDebug(Throwable t) {
        logDebug(getStackTraceString(t, ""));
    }



    public void logVerbose(int verboseLevel, String className, String msg) {
        if (verboseLevel < 1)
            verboseLevel = 1;
        if (currLoggingLevel >= LEVEL_DEBUG + verboseLevel)
            log(className, msg, "V" + verboseLevel);
    }



    public void logVerbose(int verboseLevel, Throwable t) {
        logVerbose(verboseLevel, null, getStackTraceString(t, ""));
    }



    public void logError(String className, String msg) {
        if (currLoggingLevel >= LEVEL_CRITICAL)
            log(className, msg, "E");
    }



    public void logError(Throwable t) {
        logError(getStackTraceString(t, ""));
    }



    public void logCritical(String className, String msg) {
        if (currLoggingLevel >= LEVEL_CRITICAL)
            log(className, msg, "C");
    }



    private String getStackTraceString(Throwable t, String msg) {
        if (t.getCause() != null) {
            msg = "Caused by : " + getStackTraceString(t.getCause(), msg);
        }

        StringBuffer errMsg = new StringBuffer();
        StackTraceElement[] trace = t.getStackTrace();
        errMsg.append(t.getMessage() + "\r\n");
        for (int i = 0; i < trace.length; i++) {
            if (i > 9) {
                errMsg.append("\t ....More");
                break;
            }
            errMsg.append("\t at " + trace[i].toString() + "\r\n");
        }
        return errMsg.toString() + "\r\n" + msg;

    }



    private void log(String className, String msg, String identifier) {
        if (!isDisplay && !isLog) {
            return;
        }

        Date now = new Date();
        String ts1 = sdf1.format(now);
        StringBuffer sb = new StringBuffer();
        sb.append(ts1);
        sb.append('[');
        sb.append(identifier);
        sb.append(']');
        sb.append(' ');
        sb.append(msg);
        sb.append("\r\n");

        log(sb.toString());
    }



    public void log(String log) {
        if (isDisplay) {
            System.out.print(log);
        }

        if (isLog) {
            if (needChangeFileName()) {
                try {
                    init();
                }
                catch (Exception ex) {
                }
            }
            saveLog(log);
        }
    }



    private boolean needChangeFileName() {
        if (logBySize && curLogSize > maxLogSize) {
            return true;
        }
        
        String curLogDateStr = (logByTime ? sdf3 : sdf2).format(new Date());
        if (!curLogDateStr.equals(logDateStr)) {
            logIndex = 0;
            return true;
        }
        return false;
    }



    private synchronized void saveLog(String msg) {
        try {
            if (logOut != null) {
                byte[] log = msg.getBytes(encoding);
                logOut.write(log);
                logOut.flush();
                curLogSize += log.length;
            }
            else {
                System.out.println("Logger not initialized.");
                System.out.println(msg);
            }
        }
        catch (Exception ex) {
            System.out.println("[SGLogger] Write log error! " + ex.getMessage());
        }

    }

}
