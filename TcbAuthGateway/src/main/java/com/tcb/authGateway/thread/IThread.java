package com.tcb.authGateway.thread;

public interface IThread {

    void resume();



    void suspend();



    void stop();



    void start();



    void kill();



    String getName();



    ThreadStatus getThreadStatus();



    void setThreadStatus(ThreadStatus status);



    long getIdleTime();



    long getBusyTime();
}