package com.tcb.authGateway.agent;

import java.io.Serializable;


public class Message implements Serializable {

    private String id;

    private byte[] data;

    private String correlationId;

    public Message(String id, byte[] data) {
        this.id = id;
        this.data = data;
    }



    public Message(String id, byte[] data, String correlationId) {
        this(id, data);
        this.correlationId = correlationId;
    }



    public byte[] getData() {
        return data;
    }



    public String getID() {
        return id;
    }



    public void setCorrelationId(String id) {
        this.correlationId = id;
    }



    public String getCorrelationId() {
        return this.correlationId;
    }



    public String toString() {
        StringBuffer out = new StringBuffer();
        out.append("[Message:");
        out.append("Id=");
        out.append(id);
        out.append(", CorrId=");
        out.append(correlationId);
        out.append(", len=");
        out.append(data != null ? data.length : 0);
        out.append("]");
        return out.toString();
    }

}