package com.decibel.civilianc2.model.entities;

/**
 * Created by dburnett on 3/26/2018.
 */

public class Message {
    public Message(long iD, String source, String destination, String messageBody, boolean requestAck, String sourceId){
        this.iD = iD;
        this.source = source;
        this.destination = destination;
        this.messageBody = messageBody;
        this.requestAck = requestAck;
        this.sourceId = sourceId;
    }

    public long getiD(){
        return iD;
    }

    public String getSource() {
        return source;
    }

    public String getDestination() {
        return destination;
    }

    public String getMessageBody() {
        return messageBody;
    }

    public boolean getRequestAck() {
        return requestAck;
    }

    public String getSourceId() { return sourceId; }

    private long iD;
    private String source;
    private String destination;
    private String messageBody;
    private boolean requestAck;
    private String sourceId;
}
