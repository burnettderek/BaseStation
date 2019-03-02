package com.decibel.civilianc2.protocols.ax25;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dburnett on 1/3/2018.
 */

public class AX25Packet {
    public static class Header{
        public String SourceAddress;
        public String DestinationAddress;
        public String ProtocolId;
        public List<String> DigipeaterAddresses = new ArrayList<>();
    }

    public Header Header = new Header();
    public Payload Payload;
    public byte[] RawMessage;

    public static final byte UIFrame = 0x03;
    public static final byte ProtocolID_NoLayer3 = (byte)0xf0;

    public interface Payload{
        String toXML(String linePrefix);
        String toAX25Text();
    }

    @Override
    public String toString(){
        String message = "<AX25>\n";
        if(Header.DestinationAddress != null)
            message += "\t<Destination>" + Header.DestinationAddress + "</Destination>\n";
        if(Header.SourceAddress != null)
            message += "\t<Source>" + Header.SourceAddress + "</Source>\n";
        if(Header.ProtocolId != null)
            message += "\t<ProtocolId>" + Header.ProtocolId + "</ProtocolId>\n";
        message += "\t<Digipeaters>\n";
        for(String digipeater : Header.DigipeaterAddresses){
            message += "\t\t<Digipeater>" + digipeater + "</Digipeater>\n";
        }
        message += "\t</Digipeaters>\n";
        if(Payload != null)
            message += Payload.toXML("\t");
        message += "</AX25>\n";
        return message;
    }
}
