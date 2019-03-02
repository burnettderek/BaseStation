package com.decibel.civilianc2.model.entities;

import java.util.Date;

/**
 * Created by dburnett on 12/29/2017.
 */

public class PositionReport {
    public PositionReport(Position position, Date date){
        this.position = position;
        this.date = date;
    }

    public Position getPosition() {
        return position;
    }

    public Date getDate() {
        return date;
    }

    private Position position;
    private Date date;

}
