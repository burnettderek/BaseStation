package com.decibel.civilianc2.protocols.ax25.aprs;

import com.decibel.civilianc2.protocols.ax25.AX25Packet;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dburnett on 1/2/2018.
 */

public class APRSMessage implements AX25Packet.Payload{

    public APRSMessage(byte dataType){
        this.dataType = dataType;
    }

    public APRSMessage(){

    }

    public static final String GenericDigipeterAddress = "CVLN00";

    public static class PacketTypeIndicator{
        public static final char PositionNoMessaging = '!';
        public static final char PositionWithMessaging = '=';
        public static final char PositionWithTimestampNoMessage = '/';
        public static final char PositionWithTimestampWithMessage = '@';
        public static final char PositionCurrentMicEData = '`';
        public static final char PositionOldMicEData = '\'';
        public static final char SpecUnusedPositionReport = '\\';
        public static final char PositionRawGPS = '$';
        public static final char ObjectDefinition = ';';
        public static final char ItemDefinition = ')';
        public static final char TextMessage = ':';
        public static final char Status = '>';
    }

    @Override
    public String toXML(String linePrefix) {
        StringBuilder builder = new StringBuilder();
        builder.append(linePrefix + "<UnsupportedAPRSMessage>\n");
        builder.append(linePrefix + "\t<DataType>" + String.format("0x%02x", dataType).toUpperCase()
                                                        + "('" + (char)dataType + "')</DataType>\n");
        builder.append(linePrefix + "</UnsupportedAPRSMessage>\n");
        return builder.toString();
    }

    @Override
    public String toAX25Text(){
        return "AX25 for this message not implemented.";
    }

    private byte dataType;
}
