package com.tcb.authGateway.queue;

import com.tcb.authGateway.sg.DocumentConfig;

//import com.sage.util.IQueue;
//import com.sage.document.ISGDocument;
//import com.sage.document.DocumentConfig;


public interface IQueueFactory {
    IQueue createQueue(DocumentConfig configDoc);
}