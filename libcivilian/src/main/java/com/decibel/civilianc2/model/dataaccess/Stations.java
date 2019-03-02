package com.decibel.civilianc2.model.dataaccess;

import com.decibel.civilianc2.model.entities.Station;
import com.decibel.civilianc2.model.entities.Symbol;
import com.decibel.civilianc2.tools.Database;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by dburnett on 12/28/2017.
 */

public class Stations {

    public Stations(Database database){
        this.database = database;
        //this.database.execute("DROP TABLE Stations; DROP TABLE Positions; DROP TABLE Symbols; DROP TABLE StationStatus;");
        //removeStation("KE0OVY");
        database.enforceSchema(StationSchema);
        database.enforceSchema(SymbolsSchema);
        database.enforceSchema(StatusSchema);
    }

    public void addStation(Station station){
        Station currentCopy = getStation(station.getCallsign());
        if(currentCopy != null) {
            updateStation(station);
            return;
        }
        Database.Insert insert = new Database.Insert("Stations");
        insert.addColumn("CallSign", station.getCallsign());
        insert.addColumn("LastUpdated", new Date());
        this.database.execute(insert);

        if(station.getSymbol() != null){
            addSymbol(station.getCallsign(), station.getSymbol());
        }
    }

    private void addSymbol(String callsign, Symbol symbol) {
        Database.Insert insert;
        insert = new Database.Insert("Symbols");
        insert.addColumn("CallSign", callsign);
        insert.addColumn("SymbolTable", symbol.getSymbolTable().toString());
        insert.addColumn("SymbolIndex", symbol.getSymbolIndex());
        insert.addColumn("LastUpdated", new Date());
        this.database.execute(insert);
    }

    public boolean hasSymbol(String callSign){
        String sql = "SELECT COUNT(*) FROM Symbols WHERE CallSign = '" + callSign + "';";
        return database.readInt(sql, "COUNT(*)") > 0;
    }

    public void updateStation(Station station){
        Date updatedOn = new Date();
        if(station.getSymbol() != null) {
            int symbolIndex = station.getSymbol().getSymbolIndex() >= 0 ? station.getSymbol().getSymbolIndex() : 1;
            if (hasSymbol(station.getCallsign())) {
                Database.Update update = new Database.Update("Symbols");
                update.addColumn("SymbolTable", station.getSymbol().getSymbolTable().toString());
                update.addColumn("SymbolIndex", symbolIndex);
                update.addColumn("LastUpdated", updatedOn);
                update.where("CallSign", station.getCallsign());
                database.execute(update);
            } else {
                addSymbol(station.getCallsign(), station.getSymbol());
            }
        } else {
            String sql = "DELETE FROM Symbols WHERE CallSign = '" + station.getCallsign() + "'";
            database.execute(sql);
        }
        Database.Update update = new Database.Update("Stations");
        update.addColumn("LastUpdated", updatedOn);
        update.where("CallSign", station.getCallsign());
        database.execute(update);
    }

    public void removeStation(String callSign){
        String sql = "DELETE FROM Symbols WHERE CallSign = '" + callSign + "';";
        database.execute(sql);
        sql = "DELETE FROM Positions WHERE CallSign = '" + callSign + "';";
        database.execute(sql);
        sql = "DELETE FROM Stations WHERE CallSign = '" + callSign + "';";
        database.execute(sql);
    }


    public List<Station> getAllStations(){
        final List<String> callSigns = new ArrayList<>();
        List<Station> stations = new ArrayList<>();
        String sql = "SELECT CallSign FROM Stations;";
        database.execute(sql, new Database.IDataReader() {
            @Override
            public void readRecord(Database.DataAdapter adapter) {
                String callsign = adapter.readString("CallSign");
                callSigns.add(callsign);
            }
        });
        for(String callSign : callSigns){
            stations.add(getStation(callSign));
        }
        return stations;
    }

    public Station getStation(final String callSign){
        final ArrayList<Station> results = new ArrayList<>();
        String sql = "SELECT SymbolTable, SymbolIndex FROM Stations " +
                     "LEFT JOIN Symbols ON Stations.CallSign = Symbols.CallSign " +
                     "WHERE Stations.CallSign = '" + callSign + "'";
        database.execute(sql, new Database.IDataReader() {
            @Override
            public void readRecord(Database.DataAdapter adapter) {
                Station station = new Station(callSign);
                String symbolTable = adapter.readString("SymbolTable");
                Integer symbolIndex = adapter.readInteger("SymbolIndex");

                if(symbolTable != null && symbolIndex != null) {
                    Symbol symbol = new Symbol(symbolTable, symbolIndex);
                    station.setSymbol(symbol);
                }
                results.add(station);
            }
        });
        return results.size() > 0 ? results.get(0) : null;
    }

    public String getStatus(String callSign){
        String sql = "SELECT Status FROM StationStatus WHERE CallSign = '" + callSign + "'";
        String status = database.readString(sql, "Status");
        return status;
    }

    public void updateStatus(String callSign, String status){
        if(getStatus(callSign) == null){
            Database.Insert insert = new Database.Insert("StationStatus");
            insert.addColumn("Status", status);
            insert.addColumn("CallSign", callSign);
            database.execute(insert);
        } else {
            Database.Update update = new Database.Update("StationStatus");
            update.addColumn("Status", status);
            update.where("CallSign", callSign);
            database.execute(update);
        }
    }

    public Date getLastUpdatedOn(String callSign){
        final String sql = "SELECT LastUpdated FROM Stations WHERE CallSign = '" + callSign + "';";
        return database.readDate(sql, "LastUpdated");
    }

    private Database database;
    private final String StationSchema = "CREATE TABLE IF NOT EXISTS Stations (CallSign TEXT PRIMARY KEY NOT NULL UNIQUE, LastUpdated TEXT);";

    private final String SymbolsSchema = "CREATE TABLE IF NOT EXISTS Symbols (CallSign TEXT UNIQUE, SymbolTable TEXT, SymbolIndex INTEGER, LastUpdated TEXT);";

    private final String StatusSchema = "CREATE TABLE IF NOT EXISTS StationStatus (CallSign TEXT PRIMARY KEY NOT NULL UNIQUE, LastUpdated TEXT, Status TEXT);";
}
