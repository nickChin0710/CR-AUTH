package com.tcb.authGateway.queue;

public interface IQueue {

    Object get();



    void put(Object obj);



    int size();



    boolean isEmpty();



    Object first();



    Object last();



    void clear();

}