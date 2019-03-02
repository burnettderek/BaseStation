package com.decibel.civilianc2.protocols.ax25;

import com.decibel.civilianc2.protocols.ByteGobbler;
import com.decibel.civilianc2.protocols.ax25.aprs.APRSDecoder;

/**
 * Created by dburnett on 1/2/2018.
 */

public class AX25Decoder {
    public AX25Packet Decode(byte[] bytes){
        AX25Packet packet = new AX25Packet();
        ByteGobbler gobbler = new ByteGobbler(bytes);

        packet.Header.DestinationAddress = decodeCallsign(gobbler.munch(AX25AddressLength));
        packet.Header.SourceAddress = decodeCallsign(gobbler.munch(AX25AddressLength));
        byte[] digis = gobbler.munch(AX25Packet.UIFrame);
        if(digis.length > 0) {
            ByteGobbler digiGobbler = new ByteGobbler(digis);
            for (int i = 0; i < digis.length / AX25AddressLength; i++) {
                packet.Header.DigipeaterAddresses.add(decodeCallsign(digiGobbler.munch(AX25AddressLength)));
            }
        }
        if(gobbler.munch() == AX25Packet.UIFrame) {
            packet.RawMessage = gobbler.devour(); //devour doesn't remove any bytes
            byte protocolId = gobbler.munch();
            packet.Header.ProtocolId = String.format("0x%02x", protocolId).toUpperCase();

            if(protocolId == AX25Packet.ProtocolID_NoLayer3){
                //send the rest to the APRS decoder
                APRSDecoder decoder = new APRSDecoder();
                packet.Payload = decoder.decode(gobbler.devour(), packet.Header.DestinationAddress);
            }

        }
        return packet;
    }

    private char getCallsignASCII(byte val){
        if(val > 0) {
            return (char) (val >> 1);
        }
        return (char)((val + 256) >> 1);
    }

    private String decodeCallsign(byte[] bytes){
        StringBuilder callsign = new StringBuilder();
        for(int i = 0; i < bytes.length - 1; i++){
            char ascii = getCallsignASCII(bytes[i]);
            if(ascii != ' ')
                callsign.append(ascii);
        }
        char ssidField = getCallsignASCII(bytes[bytes.length - 1]);
        int ssid = ssidField & 0x0f;
        if(ssid != 0)
            callsign.append("-" + ssid);
        return callsign.toString();
    }

    private int AX25AddressLength = 7;
}
