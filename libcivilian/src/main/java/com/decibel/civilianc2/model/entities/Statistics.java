package com.decibel.civilianc2.model.entities;

import java.util.Date;

/**
 * Created by dburnett on 3/21/2018.
 */

public class Statistics {
    public Statistics(String callSign){
        this.callSign = callSign;
    }

    public String getCallSign(){ return this.callSign; }

    public Date getLastHeard() { return this.lastHeard; }

    public void setLastHeard(Date lastHeard){
        this.lastHeard = lastHeard;
    }


    private String callSign;
    private Date lastHeard;
}
