package com.decibel.civilianc2.model.entities;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dburnett on 12/28/2017.
 */

public class Station {
    public Station(String callsign){
        this.callsign = callsign;
    }

    public void setSymbol(Symbol symbol){
        this.symbol = symbol;
    }

    public Symbol getSymbol(){ return this.symbol; }

    public String getCallsign() { return this.callsign;}

    private String callsign;
    private Symbol symbol;
}
