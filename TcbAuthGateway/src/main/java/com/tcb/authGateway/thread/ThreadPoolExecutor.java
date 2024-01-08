package com.tcb.authGateway.thread;

import java.io.*;


public class ThreadPoolExecutor {

    private IThreadFactory threadFactory;

    private IThreadQueue queue;

    private int corePoolSize;

    private int maxPoolSize;

    private long keepAliveTime;

    private long maxBusyTime;

    private IRejectExecutionHandler reHandler;

    public ThreadPoolExecutor(int corePoolSize, int maxPoolSize, long keepAliveTime, IThreadQueue queue, IThreadFactory threadFactory,
        IRejectExecutionHandler reHandler) {
        this(corePoolSize, maxPoolSize, keepAliveTime);
        this.queue = queue;
        this.threadFactory = threadFactory;
        this.reHandler = reHandler;
    }



    public ThreadPoolExecutor(int corePoolSize, int maxPoolSize, long keepAliveTime, long maxBusyTime, IThreadQueue queue,
        IThreadFactory threadFactory, IRejectExecutionHandler reHandler) {
        this(corePoolSize, maxPoolSize, keepAliveTime, queue, threadFactory, reHandler);
        this.maxBusyTime = maxBusyTime;
    }



    public ThreadPoolExecutor(int corePoolSize, int maxPoolSize, long keepAliveTime) {
        this.corePoolSize = corePoolSize;
        this.maxPoolSize = maxPoolSize;
        this.keepAliveTime = keepAliveTime;
        this.queue = new DefaultThreadQueue();
        this.threadFactory = new DefaultThreadFactory();
    }



    public ThreadPoolExecutor(int corePoolSize, int maxPoolSize, long keepAliveTime, long maxBusyTime) {
        this(corePoolSize, maxPoolSize, keepAliveTime);
        this.maxBusyTime = maxBusyTime;
    }



    public ThreadPoolExecutor() {
        this.corePoolSize = 2;
        this.maxPoolSize = 10;
        this.keepAliveTime = 5 * 60000;
        this.queue = new DefaultThreadQueue();
        this.threadFactory = new DefaultThreadFactory();
    }



    public void execute(IThreadTask task) {
        if (!queue.putTask(task)) {
            if (reHandler != null) {
                reHandler.rejectExecution(task, this);
            }
            else {
                throw new ThreadPoolExecutionException("Thread pool is full.");
            }
        }
    }



    public void init() {
        queue.init(corePoolSize, maxPoolSize, keepAliveTime, threadFactory, maxBusyTime);
    }



    public void stop() {
        queue.stop();
    }



    public void list(PrintWriter out) {
        out.print("\tThread Pool (MinThreads=");
        out.print(corePoolSize);
        out.print(", MaxThreads=");
        out.print(maxPoolSize);
        out.print(", AliveTime=");
        out.print(keepAliveTime);
        out.println(")");

        queue.list(out);
        threadFactory.list(out);
        out.flush();
    }



    public void listStatusTitle(PrintWriter out) {
        out.print("\tMin\tMax\tAlive");

        queue.listStatusTitle(out);
        threadFactory.listStatusTitle(out);
        out.flush();
    }



    public void listStatusContent(PrintWriter out) {
        out.print("\t");
        out.print(corePoolSize);
        out.print("\t");
        out.print(maxPoolSize);
        out.print("\t");
        out.print(keepAliveTime);

        queue.listStatusContent(out);
        threadFactory.listStatusContent(out);
        out.flush();
    }



    public IThreadFactory getThreadFactory() {
        return threadFactory;
    }

}