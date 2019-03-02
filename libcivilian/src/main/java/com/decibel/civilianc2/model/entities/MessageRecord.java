package com.decibel.civilianc2.model.entities;

import java.util.Date;

/**
 * Created by dburnett on 3/26/2018.
 */

public class MessageRecord {
    public MessageRecord(Message message, Date receivedOn){
        this.message = message;
        this.receivedOn = receivedOn;
    }
    public Message getMessage() {
        return message;
    }

    public Date getReceivedOn() {
        return receivedOn;
    }

    private Message message;
    private Date receivedOn;
}
