package com.tcb.authGateway.queue;

import com.tcb.authGateway.sg.DocumentConfig;

//import com.sage.util.IQueue;
//import com.sage.util.BlockQueue;
//import com.sage.document.DocumentConfig;


public class BlockQueueFactory implements IQueueFactory {
    public BlockQueueFactory() {
    }



    public IQueue createQueue(DocumentConfig configDoc) {
        if (configDoc.containsField("maxQueueSize")) {
            return new BlockQueue(configDoc.getIntField("maxQueueSize"));
        }
        return new BlockQueue();
    }

}