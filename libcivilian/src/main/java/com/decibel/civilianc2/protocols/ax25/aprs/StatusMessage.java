package com.decibel.civilianc2.protocols.ax25.aprs;

/**
 * Created by dburnett on 3/21/2018.
 */

public class StatusMessage extends APRSMessage {

    public String Status;

    @Override
    public String toXML(String lineprefix) {
        StringBuilder builder = new StringBuilder();
        builder.append(lineprefix + "<StatusMessage>\n");
        if (Status != null) {
            builder.append(lineprefix + "\t<Status>" + Status + "</Status>\n");
        }
        builder.append(lineprefix + "</StatusMessage>");
        return builder.toString();
    }
}