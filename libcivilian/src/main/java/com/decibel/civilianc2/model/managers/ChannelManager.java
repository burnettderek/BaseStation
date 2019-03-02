package com.decibel.civilianc2.model.managers;

import com.decibel.civilianc2.model.dataaccess.Channels;
import com.decibel.civilianc2.radios.Channel;

import java.util.ArrayList;
import java.util.List;

public class ChannelManager {
    public interface EventListener{
        void onChannelAdded(Channel channel);
        void onChannelRemoved(Channel channel);
        void onChannelChanged(Channel channel);
    }

    public ChannelManager(Channels data){
        this.channels = data;
    }

    public void addEventListener(EventListener listener){
        this.eventListeners.add(listener);
    }

    public void removeEventListener(EventListener listener){
        this.eventListeners.remove(listener);
    }

    public List<Channel> getAll(){
        return this.channels.getAll();
    }

    public boolean addChannel(Channel channel){
        if(validate(channel)) {
            this.channels.add(channel);
            for (EventListener eventListener : eventListeners) {
                eventListener.onChannelAdded(channel);
            }
            return true;
        }
        return false;
    }

    private boolean validate(Channel channel){
        if(channel.getName() == null || channel.getName().length() == 0)return false;
        return true;
    }

    public void removeChannel(Channel channel){
        this.channels.remove(channel);
        for(EventListener eventListener : eventListeners){
            eventListener.onChannelRemoved(channel);
        }
    }

    public void changeChannel(Channel channel){
        this.channels.update(channel);
        for(EventListener eventListener : eventListeners){
            eventListener.onChannelChanged(channel);
        }
    }

    private Channels channels;
    private List<EventListener> eventListeners = new ArrayList<>();
}
