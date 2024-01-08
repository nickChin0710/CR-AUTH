package com.tcb.authGateway.thread;

import java.io.*;


public interface IThreadQueue {

    void init(int corePoolSize, int maxPoolSize, long keepAliveTime, IThreadFactory threadFactory);



    void init(int corePoolSize, int maxPoolSize, long keepAliveTime, IThreadFactory threadFactory, long maxBusyTime);



    void stop();



    boolean putTask(IThreadTask task);



    IThreadTask getTask();



    int getMaxTaskSize();



    int getTaskCount();



    void clear();



    void list(PrintWriter out);



    void listStatusTitle(PrintWriter out);



    void listStatusContent(PrintWriter out);

}