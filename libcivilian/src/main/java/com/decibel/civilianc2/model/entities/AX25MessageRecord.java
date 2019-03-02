package com.decibel.civilianc2.model.entities;

import com.decibel.civilianc2.protocols.ax25.AX25Packet;

import java.util.Date;

/**
 * Created by dburnett on 4/16/2018.
 */

public class AX25MessageRecord {
    public AX25MessageRecord(String payload, Boolean decoded, Date receivedOn){
        this.payload = payload;
        this.decoded = decoded;
        this.receivedOn = receivedOn;
    }

    public String getPayload() {
        return payload;
    }

    public Boolean getSupported() {
        return decoded;
    }

    public Date getReceivedOn(){ return this.receivedOn; }

    private String payload;
    private Boolean decoded;
    private Date receivedOn;
}
