package com.tcb.authGateway.queue;

import java.util.List;
import java.util.LinkedList;


public class BlockQueue implements IQueue {

    private List list = new LinkedList();

    private Object gettingBlock = new Object();

    private Object puttingBlock = new Object();

    private int maxQueueSize = -1;

    public BlockQueue() {
    }



    public BlockQueue(int maxQueueSize) {
        this.maxQueueSize = maxQueueSize;
    }



    public Object get() {
        synchronized (gettingBlock) {
            while (list.isEmpty()) {
                try {
                    gettingBlock.wait();
                }
                catch (InterruptedException ex) {
                }
            }

            synchronized (puttingBlock) {
                Object obj = list.remove(0);
                puttingBlock.notifyAll();
                return obj;
            }
        }

    }



    public void put(Object obj) {
        synchronized (puttingBlock) {
            while (isFull()) {
                try {
                    puttingBlock.wait();
                }
                catch (InterruptedException ex) {
                }
            }
        }

        synchronized (gettingBlock) {
            list.add(obj);
            gettingBlock.notifyAll();
        }
    }



    private boolean isFull() {
        return maxQueueSize != -1 && list.size() >= maxQueueSize;
    }



    public int size() {
        return list.size();
    }



    public boolean isEmpty() {
        return list.isEmpty();
    }



    public Object first() {
        return list.get(0);
    }



    public Object last() {
        return list.get(list.size() - 1);
    }



    public void clear() {
        list.clear();
        synchronized(gettingBlock) {
            gettingBlock.notifyAll();
        }
        synchronized(puttingBlock) {
            puttingBlock.notifyAll();
        }
    }



    public void setMaxQueueSize(int maxQueueSize) {
        this.maxQueueSize = maxQueueSize;
    }

}