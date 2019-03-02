package com.decibel.civilianc2.protocols.ax25.aprs;

/**
 * Created by dburnett on 1/4/2018.
 */

public class ItemDefinition extends APRSPositionMessage {
    @Override
    public String toXML(String linePrefix) {
        StringBuilder builder = new StringBuilder();
        builder.append(linePrefix + "<ItemDefinition>");
        builder.append(linePrefix + "</ItemDefinition>");
        return builder.toString();
    }
}
