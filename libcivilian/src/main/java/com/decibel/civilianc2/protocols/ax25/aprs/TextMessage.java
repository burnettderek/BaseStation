package com.decibel.civilianc2.protocols.ax25.aprs;

/**
 * Created by dburnett on 1/5/2018.
 */

public class TextMessage extends APRSMessage {
    public String Message;
    public boolean Acknowledge;
    public String MessageId;

    @Override
    public String toXML(String lineprefix){
        StringBuilder builder = new StringBuilder();
        builder.append(lineprefix + "<TextMessage>\n");
        if(Message != null){
            builder.append(lineprefix + "\t<Message>" + Message + "</Message>\n");
        }
        builder.append(lineprefix + "<Acknowledge>" + (Acknowledge ? "true" : false) + "</Acknowledge>\n");
        return builder.toString();
    }

    @Override
    public String toAX25Text() {
        return PacketTypeIndicator.TextMessage + Message + (Acknowledge ? "{" + MessageId : "");
    }
}
