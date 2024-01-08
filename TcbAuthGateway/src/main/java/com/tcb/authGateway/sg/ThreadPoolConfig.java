package com.tcb.authGateway.sg;

//import com.sage.document.DocumentConfig;
//import com.sage.document.ISGDocument;


public class ThreadPoolConfig extends DocumentConfig {
    public ThreadPoolConfig(ISGDocument configDoc) {
        super(configDoc);
    }



    public int getMaximumThreadCount() {
        return super.getIntField("maximum_threads");
    }



    public int getMinimumThreadCount() {
        return super.getIntField("minimum_threads");
    }



    public int getThreadAliveTime() {
        return super.getIntField("thread_alive_time");
    }



    public int getThreadMaximumBusyTime() {
        return super.getIntField("thread_max_busy_time");
    }

}
