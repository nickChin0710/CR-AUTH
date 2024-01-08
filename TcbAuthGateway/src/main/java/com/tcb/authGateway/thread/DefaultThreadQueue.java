package com.tcb.authGateway.thread;

//import com.sage.util.*;
import java.io.*;
import java.io.PrintWriter;
import com.tcb.authGateway.queue.Queue;


public class DefaultThreadQueue implements IThreadQueue {

    private Queue queue = new Queue(100);

    private IThreadFactory threadFactory;

    private int coreThreadCount;

    private int maxThreadCount;

    private int maxQueueSize = 5000;

    private long keepAliveTime;

    private long maxBusyTime;

    private DefaultThreadQueue self = this;

    private boolean isBlocked = false;

    private Object blockObj = new Object();

    private IdleThreadCheckingThread idleCheckThread;

    public void init(int coreThreadCount, int maxThreadCount, long keepAliveTime, IThreadFactory threadFactory) {
        this.coreThreadCount = coreThreadCount;
        this.maxThreadCount = maxThreadCount;
        this.threadFactory = threadFactory;
        this.keepAliveTime = keepAliveTime;

        isBlocked = false;

        for (int i = 0; i < coreThreadCount; i++) {
            threadFactory.createNewThread(this).start();
        }

        idleCheckThread = new IdleThreadCheckingThread();
        idleCheckThread.setDaemon(true);
        idleCheckThread.start();
    }



    public void init(int corePoolSize, int maxPoolSize, long keepAliveTime, IThreadFactory threadFactory, long maxBusyTime) {
        this.maxBusyTime = maxBusyTime;
        init(corePoolSize, maxPoolSize, keepAliveTime, threadFactory);
    }



    public synchronized boolean putTask(IThreadTask task) {
        if (isBlocked || queue.getSize() >= maxQueueSize) {
            return false;
        }

        queue.put(task);
        notifyAll();
        return true;
    }



    public synchronized IThreadTask getTask() {
        while (queue.isEmpty()) {
            try {
                synchronized (blockObj) {
                    blockObj.notifyAll();
                }
                wait();
            }
            catch (InterruptedException ex) {
                return null;
            }
        }
        return (IThreadTask)queue.get();
    }



    public int getMaxTaskSize() {
        return maxQueueSize;
    }



    public int getTaskCount() {
        return queue.getSize();
    }



    public void stop() {
        isBlocked = true;
        idleCheckThread.stopThread();

        synchronized (blockObj) {
            while (!queue.isEmpty()) {
                try {
                    blockObj.wait();
                }
                catch (InterruptedException ex) {
                }
            }
        }

        threadFactory.stop();
    }



    public void clear() {
        queue.clear();
    }



    public void list(PrintWriter out) {
        out.print("\tWaiting tasks: ");
        out.print(queue.getSize());
        out.flush();
    }



    public void listStatusTitle(PrintWriter out) {
        out.print("\tTasks");
    }



    public void listStatusContent(PrintWriter out) {
        out.print("\t");
        out.print(queue.getSize());
    }



    class IdleThreadCheckingThread extends AbstractThread {
        IdleThreadCheckingThread() {
            super(1000);
        }



        protected void doMainProcess() {
            if (threadFactory.getActiveThreadCount() > coreThreadCount) {
                threadFactory.destroyIdleThreads(keepAliveTime, coreThreadCount);
            }

            if (threadFactory.getActiveThreadCount() < coreThreadCount) {
                for (int i = 0; i < coreThreadCount - threadFactory.getActiveThreadCount(); i++) {
                    threadFactory.createNewThread(self).start();
                }
            }

            if (queue.getSize() > threadFactory.getActiveThreadCount() && threadFactory.getActiveThreadCount() < maxThreadCount) {
                threadFactory.createNewThread(self).start();
            }

            if (maxBusyTime > 0) {
                threadFactory.destroyNonRespondingThreads(maxBusyTime);
            }

        }
    }

}