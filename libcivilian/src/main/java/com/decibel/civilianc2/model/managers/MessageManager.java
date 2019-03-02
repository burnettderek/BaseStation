package com.decibel.civilianc2.model.managers;

import com.decibel.civilianc2.model.dataaccess.Messages;
import com.decibel.civilianc2.model.entities.Message;
import com.decibel.civilianc2.model.entities.MessageRecord;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by dburnett on 3/26/2018.
 */

public class MessageManager {
    public MessageManager(Messages messages){
        this.messages = messages;
    }

    public interface IEventListener {
        void onMessageReceived(MessageRecord messageRecord);
    }

    public void addEventListener(IEventListener listener){
        this.listeners.add(listener);
    }

    public void removeEventListener(IEventListener listener){
        this.listeners.remove(listener);
    }

    public void addMessage(String source, String destination, String body, boolean requestAck, String sourceId){
        long id = messages.addMessage(source, destination, body, requestAck,  sourceId, new Date());
        MessageRecord record = messages.getMessage(id);
        for (IEventListener listener:listeners) {
            listener.onMessageReceived(record);
        }
    }

    public List<MessageRecord> getMessageRecords(String source, String destination){
        return messages.getMessageRecords(source, destination);
    }

    public List<String> getMessageContacts(String destination){
        return messages.getMessageContacts(destination);
    }


    private Messages messages;
    private List<IEventListener> listeners = new ArrayList<>();
}
