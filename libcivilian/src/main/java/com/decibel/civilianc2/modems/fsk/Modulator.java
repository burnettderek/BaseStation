package com.decibel.civilianc2.modems.fsk;

import android.content.Context;
import android.media.AudioManager;

import com.decibel.civilianc2.model.entities.Symbol;
import com.decibel.civilianc2.protocols.ax25.AX25Packet;
import com.decibel.civilianc2.protocols.ax25.aprs.APRSMessage;
import com.decibel.civilianc2.radios.ISoftwareTransmitter;
import com.decibel.civilianc2.radios.ITransceiver;
import com.decibel.civilianc2.radios.ITransmitter;
import com.nogy.afu.soundmodem.APRSFrame;
import com.nogy.afu.soundmodem.Afsk;
import com.nogy.afu.soundmodem.Message;
import com.decibel.civilianc2.model.entities.Position;

import java.io.IOException;

/**
 * Created by dburnett on 2/26/2018.
 */

public class Modulator {
    public Modulator(ITransmitter transmitter){
        this.transmitter = transmitter;
    }

    public byte[] ackMessage(String source, String destination, String messageId){
        String data = APRSMessage.PacketTypeIndicator.TextMessage + destination + ":ack" + messageId;
        APRSFrame frame = new APRSFrame(source, destination, "WIDE1-1", data, frameLendthInMilliseconds);
        Message message = sendFrame(frame, transmitter);
        return message.data;
    }

    public byte[] sendMessage(AX25Packet packet){
        StringBuilder digipeters = new StringBuilder();
        if(packet.Header.DigipeaterAddresses.size() > 0) {
            for (int i = 0; i < packet.Header.DigipeaterAddresses.size(); i++){
                digipeters.append(packet.Header.DigipeaterAddresses.get(i));
                if(i != packet.Header.DigipeaterAddresses.size() - 1)
                    digipeters.append(",");
            }
        }
        APRSFrame frame = new APRSFrame(packet.Header.SourceAddress, packet.Header.DestinationAddress, digipeters.toString(), packet.Payload.toAX25Text(), frameLendthInMilliseconds);
        sendFrame(frame, transmitter);
        return frame.toAX25();
    }



    private Message sendFrame(APRSFrame frame, ITransmitter radio) {
        Message message = frame.getMessage();
        try {

            radio.getSoftwareTransmitter().setMode(ISoftwareTransmitter.Mode.TRANSMIT);
        } catch (IOException e){return null;}
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        afsk.setVolume(0.72f);
        afsk.sendMessage(message);
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        try {
            radio.getSoftwareTransmitter().setMode(ISoftwareTransmitter.Mode.RECEIVE);
        } catch (IOException e) {
            return null;
        }
        return message;
    }



    private Afsk afsk = new Afsk(AudioManager.STREAM_MUSIC, 22050);
    private int prefixInMilliseconds = 200;
    private int frameLendthInMilliseconds = prefixInMilliseconds * 1200/8/1000; //https://github.com/ge0rg/aprsdroid/blob/master/src/backend/AfskUploader.scala
    private ITransmitter transmitter;
}
