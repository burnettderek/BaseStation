package com.decibel.civilianc2.radios;

/**
 * Created by dburnett on 2/13/2018.
 */

public class Channel {
    public Channel(String name, String description, int tx, int rx, Integer txctcss, Integer rxctcss){
        this.txFreq = tx;
        this.rxFreq = rx;
        this.name = name;
        this.description = description;
        this.txCTCSS = txctcss;
        this.rxCTCSS = rxctcss;
    }

    public int getTxFreq() {
        return txFreq;
    }

    public int getRxFreq() {
        return rxFreq;
    }

    public Integer getRxCTCSS() { return rxCTCSS; }

    public Integer getTxCTCSS() { return txCTCSS; }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    private int txFreq;
    private int rxFreq;
    private Integer txCTCSS;
    private Integer rxCTCSS;
    private String name;
    private String description;


}
