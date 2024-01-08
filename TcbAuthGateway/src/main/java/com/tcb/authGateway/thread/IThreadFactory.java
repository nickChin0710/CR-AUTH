package com.tcb.authGateway.thread;

//import com.sage.util.*;
import java.io.*;


public interface IThreadFactory {
    IThread createNewThread(IThreadQueue queue);



    int getActiveThreadCount();



    void destroyIdleThreads(long idleTime, int maxThreadCount);



    void destroyNonRespondingThreads(long maxBusyTime);



    void list();



    void stop();



    void list(PrintWriter out);



    void listStatusTitle(PrintWriter out);



    void listStatusContent(PrintWriter out);

}