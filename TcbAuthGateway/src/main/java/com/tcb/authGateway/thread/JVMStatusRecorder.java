package com.tcb.authGateway.thread;

//import com.sage.log.*;
//import com.sage.roger.*;
import java.text.*;
import com.tcb.authGateway.sg.SGLogger;
import com.tcb.authGateway.utils.RCFunc;


public class JVMStatusRecorder extends AbstractThread {

    public static final String LOGGER = "StatusRecorder";

    private SGLogger logger = SGLogger.getLogger(LOGGER);

    private NumberFormat nf = new DecimalFormat("###,### Kb");

    private NumberFormat nf1 = new DecimalFormat("+###,### Kb ; -###,### Kb");

    private double lastUsed;

    public JVMStatusRecorder() {
    }



    public JVMStatusRecorder(int interval) {
        super(interval);
    }



    protected SGLogger getLogger() {
        return logger;
    }



    protected void logStatus() {
        double max = Runtime.getRuntime().maxMemory() / 1024;
        double total = Runtime.getRuntime().totalMemory() / 1024;
        double used = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024;
        double free = Runtime.getRuntime().freeMemory() / 1024;

        StringBuffer info = new StringBuffer();
        info.append("Memory:");
        info.append(RCFunc.alignField("Max: " + nf.format(max), 20, (byte)0x20, RCFunc.ALIGN_RIGHT));
        info.append("     ");
        info.append(RCFunc.alignField("Total: " + nf.format(total), 20, (byte)0x20, RCFunc.ALIGN_RIGHT));
        info.append("     ");
        info.append(RCFunc.alignField("Used: " + nf.format(used), 20, (byte)0x20, RCFunc.ALIGN_RIGHT));
        info.append("     ");
        info.append(RCFunc.alignField("Free: " + nf.format(free), 20, (byte)0x20, RCFunc.ALIGN_RIGHT));
        info.append("     ");
        info.append(RCFunc.alignField("Vary: " + nf1.format(used - lastUsed), 20, (byte)0x20, RCFunc.ALIGN_RIGHT));
        logger.logCritical(info.toString());
        lastUsed = used;
    }



    protected String getCurrentMaxMemory() {
        return nf.format(Runtime.getRuntime().maxMemory() / 1024);
    }



    protected String getCurrentTotalMemory() {
        return nf.format(Runtime.getRuntime().totalMemory() / 1024);
    }



    protected String getCurrentUsedMemory() {
        return nf.format( (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024);
    }



    protected String getCurrentFreeMemory() {
        return nf.format(Runtime.getRuntime().freeMemory() / 1024);
    }



    protected void doMainProcess() {
        try {
            logStatus();
        }
        catch (Exception ex) {
        //System.out.println(ex.getMessage());
        }
    }
}