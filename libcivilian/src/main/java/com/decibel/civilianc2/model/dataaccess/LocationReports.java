package com.decibel.civilianc2.model.dataaccess;


import com.decibel.civilianc2.model.entities.Position;
import com.decibel.civilianc2.model.entities.PositionReport;
import com.decibel.civilianc2.model.entities.Station;
import com.decibel.civilianc2.tools.Database;

import java.util.Date;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by dburnett on 12/29/2017.
 */

public class LocationReports {
    public LocationReports(Database database){
        this.database = database;
        this.database.enforceSchema(LocationsSchema);
    }

    public void addLocationReport(Station station, PositionReport report){
        Database.Insert insert = new Database.Insert("Positions");
        insert.addColumn("CallSign", station.getCallsign());
        insert.addColumn("Longitude", report.getPosition().getLongitude());
        insert.addColumn("Latitude", report.getPosition().getLatitude());
        insert.addColumn("ReceivedOn", report.getDate());
        database.execute(insert);
    }

    public List<PositionReport> getLocationReports(Station station){
        final ArrayList<PositionReport> reports = new ArrayList<PositionReport>();
        String sql = "SELECT Longitude, Latitude, Altitude, ReceivedOn FROM Positions WHERE CallSign = '" + station.getCallsign() + "';";
        this.database.execute(sql, new Database.IDataReader() {
            @Override
            public void readRecord(Database.DataAdapter adapter) {
                double longitude = adapter.readDouble("Longitude");
                double latitude = adapter.readDouble("Latitude");
                Double altitude = adapter.readDouble("Altitude");
                Date receivedOn = adapter.readDate("ReceivedOn");
                if(altitude != null)
                    reports.add(new PositionReport(new Position(longitude, latitude, altitude), receivedOn));
                else
                    reports.add(new PositionReport(new Position(longitude, latitude), receivedOn));
            }
        });
        return reports;
    }

    private Database database;
    private final String LocationsSchema = "CREATE TABLE IF NOT EXISTS Positions (CallSign TEXT REFERENCES Stations (CallSign), Longitude REAL NOT NULL, Latitude REAL NOT NULL, Altitude REAL, ReceivedOn TEXT);";
}
