package com.decibel.civilianc2.model.managers;

import android.content.Context;
import android.database.DatabaseUtils;
import android.os.Environment;
import android.util.Pair;

import com.decibel.civilianc2.model.dataaccess.AX25Messages;
import com.decibel.civilianc2.model.dataaccess.Channels;
import com.decibel.civilianc2.model.dataaccess.LocationReports;
import com.decibel.civilianc2.model.dataaccess.Messages;
import com.decibel.civilianc2.model.dataaccess.Stations;
import com.decibel.civilianc2.model.dataaccess.UserSettings;
import com.decibel.civilianc2.model.entities.Position;
import com.decibel.civilianc2.model.entities.Station;
import com.decibel.civilianc2.model.entities.Statistics;
import com.decibel.civilianc2.modems.fsk.Demodulator;
import com.decibel.civilianc2.modems.fsk.Modulator;
import com.decibel.civilianc2.protocols.ax25.AX25Packet;
import com.decibel.civilianc2.protocols.ax25.aprs.APRSMessage;
import com.decibel.civilianc2.protocols.ax25.aprs.ItemDefinition;
import com.decibel.civilianc2.protocols.ax25.aprs.ObjectDefinition;
import com.decibel.civilianc2.protocols.ax25.aprs.PositionReport;
import com.decibel.civilianc2.protocols.ax25.aprs.StatusMessage;
import com.decibel.civilianc2.protocols.ax25.aprs.TextMessage;
import com.decibel.civilianc2.radios.ITransceiver;
import com.decibel.civilianc2.tools.Database;
import com.decibel.civilianc2.tools.Logger;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Created by dburnett on 1/11/2018.
 */

public class Model implements StationManager.IEventListener, MessageInterpreter.IMessageListener{
    protected Model(Context context){
        this.context = context;
        database = new Database(context, context.getDatabasePath("CivilianC2.db").getPath(), 1);
        Logger.setDatabase(database, true);
        stationManager = new StationManager(new Stations(database));
        positionManager = new PositionManager(new LocationReports(database));
        messageManager = new MessageManager(new Messages(database));
        userSettings = new UserSettings(database);
        ax25MessageManager = new AX25MessageManager(new AX25Messages(database));

        channels = new Channels(database);

        stationManager.addEventListener(this);
        List<Station> stations = stationManager.getAll();
        for(Station station : stations){
            this.statistics.put(station.getCallsign(), new Statistics(station.getCallsign()));
        }
    }

    public static void onCreate(Context context){
        instance = new Model(context);
    }

    public static Model getInstance(){
        return instance;
    }

    public StationManager getStationManager(){return this.stationManager;}

    public PositionManager getPositionManager() {return this.positionManager;}

    public MessageManager getMessageManager() {return this.messageManager;}

    public MessageInterpreter getMessageInterpreter() {return this.messageInterpreter;}

    public AX25MessageManager getAx25MessageManager() {return this.ax25MessageManager;}

    public void setTransceiver(ITransceiver transceiver){
        this.transceiver = transceiver;
        messageInterpreter = new MessageInterpreter(Demodulator.getInstance(), stationManager, positionManager, messageManager, ax25MessageManager, userSettings, transceiver);
        messageInterpreter.addEventListener(this);
        modulator = new Modulator(transceiver);
    }

    public UserSettings getUserSettings() {return this.userSettings;}

    public Channels getChannels() { return this.channels;}

    public HashMap<String, Statistics> getStatistics() { return this.statistics; }

    public void setPositionBeacon(PositionBeacon beacon){
        this.positionBeacon = beacon;
    }

    public PositionBeacon getPositionBeacon(){
        return this.positionBeacon;
    }

    public Modulator getModulator() { return this.modulator; }

    @Override
    public void onAdded(String callsign, Station station) {
        synchronized (this.statistics) {
            Statistics stats = new Statistics(callsign);
            stats.setLastHeard(new Date());
            this.statistics.put(callsign, stats);
        }
    }

    @Override
    public void onUpdated(String callsign, Station station) {
        synchronized (this.statistics) {
            Statistics stat = this.statistics.get(callsign);
            stat.setLastHeard(new Date());
        }
    }

    @Override
    public void onRemoved(String callsign) {

    }

    public AX25Packet getLastMessage(){
        AX25Packet safeRef = lastMessage;
        return safeRef;
    }

    @Override
    public void onPositionReport(AX25Packet packet, PositionReport positionReport) {
        lastMessage = packet;
    }

    @Override
    public void onObjectDefinition(AX25Packet packet, ObjectDefinition objectDefinition) {
        lastMessage = packet;

    }

    @Override
    public void onItemDefinition(AX25Packet packet, ItemDefinition definition) {
        lastMessage = packet;
    }

    @Override
    public void onStatusMessage(AX25Packet packet, StatusMessage message) {
        lastMessage = packet;
    }

    @Override
    public void onTextMessage(AX25Packet packet, TextMessage message) {
        lastMessage = packet;
    }

    private Context context;
    private Database database;
    private StationManager stationManager;
    private PositionManager positionManager;
    private MessageManager messageManager;
    private MessageInterpreter messageInterpreter;
    private AX25MessageManager ax25MessageManager;
    private UserSettings userSettings;
    private Channels channels;
    private HashMap<String, Statistics> statistics = new HashMap<>();
    private AX25Packet lastMessage;
    private PositionBeacon positionBeacon; //this may be null. Especially on base station model.
    private Modulator modulator;
    private ITransceiver transceiver;

    private static Model instance;
}
