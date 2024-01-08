package com.tcb.authGateway.thread;

//import com.sage.util.*;


public class WorkerThread implements IThread {

    private ThreadStatus status = ThreadStatus.INIT;

    private InnerWorkerThread thread;

    private IThreadQueue queue;

    private long lastIdleBeginTime;

    private long lastBusyBeginTime;

    public WorkerThread(ThreadGroup group, String name, IThreadQueue queue) {
        thread = new InnerWorkerThread(group, name);
        //thread.setPriority(Thread.NORM_PRIORITY + (Thread.MAX_PRIORITY - Thread.NORM_PRIORITY) / 2);
        this.queue = queue;
    }



    public void resume() {
        thread.resumeThread();
    }



    public void suspend() {
        thread.suspendThread();
        status = ThreadStatus.SUSPEND;
    }



    public void stop() {
        thread.interrupt();
        thread.stopThread();
        status = ThreadStatus.STOPPED;
    }



    public void kill() {
        stop();
//        thread.stop();
    }



    public void start() {
        thread.start();
    }



    public String getName() {
        return thread.getName();
    }



    public ThreadStatus getThreadStatus() {
        return status;
    }



    public void setThreadStatus(ThreadStatus status) {
        this.status = status;
    }



    public long getIdleTime() {
        return System.currentTimeMillis() - lastIdleBeginTime;
    }



    public long getBusyTime() {
        return System.currentTimeMillis() - lastBusyBeginTime;
    }



    public String toString() {
        StringBuffer out = new StringBuffer();
        out.append(thread.getName());
        out.append(" [");
        out.append("Status:");
        out.append(status.toString());
        if (status == ThreadStatus.IDLE || status == ThreadStatus.BUSY) {
            out.append(" for ");
            out.append( (status == ThreadStatus.IDLE ? getIdleTime() : getBusyTime()) / 1000d);
            out.append("s");
        }
        out.append("]");
        return out.toString();
    }



    class InnerWorkerThread extends AbstractThread {
        InnerWorkerThread(ThreadGroup group, String name) {
            super(group, name, 1);
        }



        protected void doMainProcess() {
            status = ThreadStatus.IDLE;
            lastIdleBeginTime = System.currentTimeMillis();

            IThreadTask task = queue.getTask();
            if (task != null) {
                status = ThreadStatus.BUSY;
                lastBusyBeginTime = System.currentTimeMillis();
                task.run();
            }
        }

    }
}