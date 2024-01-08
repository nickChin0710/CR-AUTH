package com.tcb.authGateway.thread;

import java.util.*;
import com.tcb.authGateway.sg.SGLogger;
import java.text.*;
import java.io.*;
//import com.sage.log.SGLogger;


public class DefaultThreadFactory implements IThreadFactory {

    private static int serialNo;

    private ThreadGroup group = new ThreadGroup("WorkerThreads");

    private NumberFormat nf = new DecimalFormat("0000");

    private HashMap pool = new HashMap(50);

    private SGLogger logger = SGLogger.getLogger();

    public IThread createNewThread(IThreadQueue queue) {
        IThread thread = new WorkerThread(group, "Thread-" + getSerialNo(), queue);
        pool.put(thread.getName(), thread);
        logger.logCritical(MessageFormat.format("Thread({0}) has been created.", new Object[] {thread.getName()}));
        return thread;
    }



    public void stop() {
        for (Iterator iter = pool.values().iterator(); iter.hasNext(); ) {
            IThread thread = (IThread)iter.next();
            thread.stop();
        }

        pool.clear();
    }



    private synchronized String getSerialNo() {
        return nf.format(++serialNo);
    }



    public int getActiveThreadCount() {
        return pool.size();
    }



    public void destroyIdleThreads(long idleTime, int maxThreadCount) {
        if (pool.size() <= maxThreadCount) {
            return;
        }

        IThread[] threads = new IThread[pool.size()];
        pool.values().toArray(threads);

        for (int i = 0; i < threads.length; i++) {
            if (pool.size() > maxThreadCount && threads[i].getThreadStatus() == ThreadStatus.IDLE && threads[i].getIdleTime() > idleTime) {

                threads[i].stop();
                pool.remove(threads[i].getName());
                logger.logCritical(MessageFormat.format("Thread({0}) has been removed due to idle for a period of time.",
                    new Object[] {threads[i].getName()}));
                threads[i] = null;
//                System.gc();
            }

        }

    }



    public void destroyNonRespondingThreads(long maxBusyTime) {
        if (pool.size() <= 0) {
            return;
        }

        IThread[] threads = new IThread[pool.size()];
        pool.values().toArray(threads);

        for (int i = 0; i < threads.length; i++) {
            if (threads[i].getThreadStatus() == ThreadStatus.BUSY && threads[i].getBusyTime() > maxBusyTime) {
                pool.remove(threads[i].getName());
                threads[i].kill();
                logger.logCritical(MessageFormat.format("Thread({0}) has been killed due to out of control.", new Object[] {threads[i].getName()}));

                threads[i] = null;
//                System.gc();
            }

        }

    }



    public void list() {
        System.out.println("Thread Status");
        list(new PrintWriter(System.out));
    }



    public void list(PrintWriter out) {
        out.print(", Active threads: ");
        out.print(pool.size());
        IThread[] threads = new IThread[pool.size()];
        pool.values().toArray(threads);

        int idle = 0;
        int busy = 0;

        for (int i = 0; i < threads.length; i++) {
            if (threads[i].getThreadStatus() == ThreadStatus.BUSY) {
                busy++;
            }
            else if (threads[i].getThreadStatus() == ThreadStatus.IDLE) {
                idle++;
            }
        }
        out.print(", Idle:");
        out.print(idle);
        out.print(", Busy:");
        out.print(busy);
        out.flush();
    }



    public void listStatusTitle(PrintWriter out) {
        out.print("\tActive\tIdle\tBusy");
    }



    public void listStatusContent(PrintWriter out) {
        out.print("\t");
        out.print(pool.size());

        IThread[] threads = new IThread[pool.size()];
        pool.values().toArray(threads);

        int idle = 0;
        int busy = 0;

        for (int i = 0; i < threads.length; i++) {
            if (threads[i].getThreadStatus() == ThreadStatus.BUSY) {
                busy++;
            }
            else if (threads[i].getThreadStatus() == ThreadStatus.IDLE) {
                idle++;
            }
        }
        out.print("\t");
        out.print(idle);
        out.print("\t");
        out.print(busy);

    }

}