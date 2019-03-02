package com.decibel.civilianc2.model.entities;

import java.util.Date;

/**
 * Created by dburnett on 4/3/2018.
 */

public class MessageAck {
    public MessageAck(String callSign, int messageId, Date timeStamp){
        this.callSign = callSign;
        this.messageId = messageId;
    }

    public String getCallSign() {
        return callSign;
    }

    public int getMessageId() {
        return messageId;
    }

    public Date getTimeStamp(){
        return timeStamp;
    }

    private String callSign;
    private int messageId;
    private Date timeStamp;
}
