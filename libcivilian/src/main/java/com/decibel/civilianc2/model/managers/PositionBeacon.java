package com.decibel.civilianc2.model.managers;

import android.Manifest;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import com.decibel.civilianc2.model.dataaccess.UserSettings;
import com.decibel.civilianc2.model.entities.Position;
import com.decibel.civilianc2.model.entities.PositionReport;
import com.decibel.civilianc2.model.entities.Station;
import com.decibel.civilianc2.model.entities.Symbol;
import com.decibel.civilianc2.modems.fsk.Modulator;
import com.decibel.civilianc2.protocols.ax25.AX25Packet;
import com.decibel.civilianc2.protocols.ax25.aprs.APRSMessage;
import com.decibel.civilianc2.tools.Logger;

import java.util.Date;

/**
 * Created by dburnett on 4/2/2018.
 */

public class PositionBeacon implements LocationListener{
    public PositionBeacon(LocationManager locationManager, UserSettings userSettings, StationManager stationManager, PositionManager manager, Modulator modulator){
        this.locationManager = locationManager;
        this.userSettings = userSettings;
        this.positionManager = manager;
        this.stationManager = stationManager;
        this.modulator = modulator;
    }

    public void start(){
        isActive = true;
        try {
            Position lastKnown = userSettings.getPosition(UserSettings.Location);
            if(lastKnown != null)
                onPositionChanged(lastKnown);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000 * 60 * 10, 50, this);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000 * 60 * 10, 50, this);

        }
        catch (SecurityException e){
            isActive = false;
            logger.log(Logger.Level.ERROR, e.getMessage());
        }
    }

    public void stop(){
        isActive = false;
        locationManager.removeUpdates(this);
    }

    @Override
    public void onLocationChanged(Location location) {
        Position position = new Position(location.getLatitude(), location.getLongitude());
        onPositionChanged(position);
    }

    private void onPositionChanged(Position position) {
        String stationCallSign = userSettings.getSetting(UserSettings.CallSign);
        if(stationCallSign != null && !stationCallSign.isEmpty()){

            userSettings.setSetting(UserSettings.Location, position);
            Station station = new Station(stationCallSign);
            Symbol symbol = new Symbol(Symbol.PRIMARY_SYMBOL_TABLE, Symbol.getSymbolIndex('-'));
            station.setSymbol(symbol);
            /*if(Model.getInstance().getStationManager().hasStation(stationCallSign))
                Model.getInstance().getPositionManager().addPositionReport(station, new PositionReport(position, new Date()));
            else {
                Model.getInstance().getStationManager().addStation(station);
                Model.getInstance().getPositionManager().addPositionReport(station, new PositionReport(position, new Date()));
            }*/
            //positionManager.addPositionReport(station, new PositionReport(position, new Date()));
            if(this.modulator != null){
                AX25Packet packet = new AX25Packet();
                packet.Header.SourceAddress = station.getCallsign();
                packet.Header.DestinationAddress = APRSMessage.GenericDigipeterAddress;
                com.decibel.civilianc2.protocols.ax25.aprs.PositionReport positionReport = new com.decibel.civilianc2.protocols.ax25.aprs.PositionReport();
                positionReport.Position = new Position(position.getLatitude(), position.getLongitude());
                positionReport.Symbol = symbol;
                packet.Payload = positionReport;
                byte[] bytes = this.modulator.sendMessage(packet);
                Model.getInstance().getMessageInterpreter().onPacketReceived(bytes); //feed it back into our own system
            }
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    public boolean isActive() { return this.isActive; }

    private Logger<PositionBeacon> logger = new Logger<>(this);
    private LocationManager locationManager;
    private UserSettings userSettings;
    private PositionManager positionManager;
    private StationManager stationManager;
    private Modulator modulator;
    private boolean isActive = false;
}
