package com.decibel.civilianc2.model.managers;

import com.decibel.civilianc2.model.dataaccess.LocationReports;
import com.decibel.civilianc2.model.entities.PositionReport;
import com.decibel.civilianc2.model.entities.Station;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by dburnett on 1/11/2018.
 */

public class PositionManager{
    public PositionManager(LocationReports datastore){
        this.datastore = datastore;
    }

    public interface IEventListener{
        void onAdded(String callSign, PositionReport report);
    }

    public void addPositionReport(Station station, PositionReport report){
        this.datastore.addLocationReport(station, report);
        for(IEventListener listener : listeners){
            listener.onAdded(station.getCallsign(), report);
        }
    }

    public List<PositionReport> getPositionReports(Station station){
        return this.datastore.getLocationReports(station);
    }

    public void addEventListener(IEventListener listener){
        listeners.add(listener);
    }

    public void removeEventListener(IEventListener listener){
        listeners.remove(listener);
    }

    private LocationReports datastore;
    private List<IEventListener> listeners = new ArrayList<>();
}
