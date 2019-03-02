package com.decibel.civilianc2.model.managers;

import com.decibel.civilianc2.model.dataaccess.AX25Messages;
import com.decibel.civilianc2.model.entities.AX25MessageRecord;
import com.decibel.civilianc2.protocols.ax25.AX25Packet;

import java.util.List;

/**
 * Created by dburnett on 4/16/2018.
 */

public class AX25MessageManager {
    public AX25MessageManager(AX25Messages datastore){
        messagesDatastore = datastore;
    }

    public void addMessage(AX25Packet packet, boolean decoded) {
        messagesDatastore.addMessage(packet, decoded);
    }

    public AX25MessageRecord getLastMessage(String callSign){
        return messagesDatastore.getLastMessage(callSign);
    }

    public List<AX25MessageRecord> getAllMessagesForStation(String callSign){
        return messagesDatastore.getAllMessagesForStation(callSign);
    }

    private AX25Messages messagesDatastore;
}
