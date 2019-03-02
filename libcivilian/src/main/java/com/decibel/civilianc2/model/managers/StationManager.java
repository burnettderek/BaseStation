package com.decibel.civilianc2.model.managers;

import com.decibel.civilianc2.model.dataaccess.Stations;
import com.decibel.civilianc2.model.entities.Station;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by dburnett on 1/9/2018.
 */

public class StationManager{

    public StationManager(Stations dataStore){
        this.dataStore = dataStore;
    }


    public interface IEventListener{
        void onAdded(String callsign, Station station);
        void onUpdated(String callsign, Station station);
        void onRemoved(String callsign);
    }

    public boolean hasStation(String callsign){
        return dataStore.getStation(callsign) != null;
    }

    public void addStation(Station station){
        dataStore.addStation(station);
        for(IEventListener listener : listeners){
            listener.onAdded(station.getCallsign(), station);
        }
    }

    public void updateStation(Station station){
        if(!hasStation(station.getCallsign())) {
            addStation(station);
            return;
        }
        dataStore.updateStation(station);
        for(IEventListener listener : listeners){
            listener.onUpdated(station.getCallsign(), station);
        }
    }

    public void removeStation(Station station){
        dataStore.removeStation(station.getCallsign());
        for(IEventListener listener : listeners){
            listener.onRemoved(station.getCallsign());
        }
    }

    public List<Station> getAll(){
        List<Station> stations = dataStore.getAllStations();
        return stations;
    }

    public void updateStatus(String callSign, String status){
        Station station = dataStore.getStation(callSign);
        if(station == null){
            station = new Station(callSign);
            this.addStation(station);
        }
        dataStore.updateStatus(callSign, status);
        for(IEventListener listener : listeners){
            listener.onUpdated(callSign, station);
        }
    }

    public Date getLastUpdatedOn(Station station){
        return dataStore.getLastUpdatedOn(station.getCallsign());
    }

    public void addEventListener(IEventListener listener){
        listeners.add(listener);
    }

    public void removeEventListener(IEventListener listener){
        listeners.remove(listener);
    }

    private Stations dataStore;
    private List<IEventListener> listeners = new ArrayList<>();
}
