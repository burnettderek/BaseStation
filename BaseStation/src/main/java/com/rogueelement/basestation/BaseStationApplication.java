package com.rogueelement.basestation;

import android.app.Application;

import com.decibel.civilianc2.fragments.RadioControlFragment;
import com.decibel.civilianc2.model.dataaccess.Channels;
import com.decibel.civilianc2.model.managers.ChannelManager;
import com.decibel.civilianc2.radios.ITransceiver;
import com.decibel.civilianc2.radios.RadioDetection;
import com.decibel.civilianc2.tools.Database;

import java.util.List;

public class BaseStationApplication extends Application{
    @Override
    public void onCreate(){
        super.onCreate();
        if(database == null) {
            database = new Database(this, this.getDatabasePath(DatabaseName).getPath(), 1);
            Channels channels = new Channels(database);
            channelManager = new ChannelManager(channels);
        }

    }

    public static ChannelManager getChannels(){
        return channelManager;
    }

    public static ITransceiver getRadio(){ return radio; }

    public static void setRadio(ITransceiver transceiver){ radio = transceiver; }

    private static Database database;
    private final String DatabaseName = "BaseStation.db";
    private static ChannelManager channelManager;
    private static ITransceiver radio;
}
