package com.decibel.civilianc2.model.dataaccess;

import com.decibel.civilianc2.radios.Channel;
import com.decibel.civilianc2.tools.Database;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dburnett on 2/13/2018.
 */

public class Channels {
    public Channels(Database database){
        this.database = database;
        //this.database.execute("DROP TABLE Channels;");
        database.enforceSchema(Schema);
    }

    public void add(Channel channel){
        if(getChannel(channel.getName()) != null)
            throw new IllegalArgumentException("A channel with that name already exists.");
        Database.Insert insert = new Database.Insert("Channels");
        insert.addColumn("Name", channel.getName());
        insert.addColumn("Description", channel.getDescription());
        insert.addColumn("TransmitFrequency", channel.getTxFreq());
        insert.addColumn("ReceiveFrequency", channel.getRxFreq());
        if(channel.getRxCTCSS() != null){
            insert.addColumn("RxCTCSS", channel.getRxCTCSS());
        }
        if(channel.getTxCTCSS() != null){
            insert.addColumn("TxCTCSS", channel.getTxCTCSS());
        }
        database.execute(insert);
    }

    public void update(Channel channel){
        if(getChannel(channel.getName()) == null)
            throw new IllegalArgumentException("No channel with that name exists.");
        Database.Update update = new Database.Update("Channels");
        update.addColumn("Description", channel.getDescription());
        update.addColumn("TransmitFrequency", channel.getTxFreq());
        update.addColumn("ReceiveFrequency", channel.getRxFreq());
        update.addColumn("RxCTCSS", channel.getRxCTCSS());
        update.addColumn("TxCTCSS", channel.getTxCTCSS());
        update.where("Name", channel.getName());
        database.execute(update);
    }

    public Channel getChannel(String name){
        final List<Channel> results = new ArrayList<>();
        final String sql = "SELECT Name, Description, TransmitFrequency, ReceiveFrequency, RxCTCSS, TxCTCSS FROM Channels WHERE Name ='" + name +"';";
        database.execute(sql, new Database.IDataReader() {
            @Override
            public void readRecord(Database.DataAdapter adapter) {
                results.add(readChannel(adapter));
            }
        });
        return results.size() > 0 ? results.get(0) : null;
    }

    public List<Channel> getAll(){
        final List<Channel> channels = new ArrayList<>();
        final String sql = "SELECT Name, Description, TransmitFrequency, ReceiveFrequency, RxCTCSS, TxCTCSS FROM Channels";
        database.execute(sql, new Database.IDataReader() {
            @Override
            public void readRecord(Database.DataAdapter adapter) {
                channels.add(readChannel(adapter));
            }
        });
        return channels;
    }

    private Channel readChannel(Database.DataAdapter adapter) {
        String name = adapter.readString("Name");
        String description = adapter.readString("Description");
        int txFreq = adapter.readInteger("TransmitFrequency");
        int rxFreq = adapter.readInteger("ReceiveFrequency");
        Integer rxCtcss = adapter.readInteger("RxCTCSS");
        Integer txCtcss = adapter.readInteger("TxCTCSS");
        return new Channel(name, description, txFreq, rxFreq, txCtcss, rxCtcss);
    }

    public void remove(Channel channel){
        final String sql = "DELETE FROM Channels WHERE Name = '" + channel.getName() + "';";
        database.execute(sql);
    }
    private Database database;
    private static final String Schema = "CREATE TABLE IF NOT EXISTS Channels (Name TEXT NOT NULL Unique, Description TEXT, TransmitFrequency INTEGER NOT NULL, ReceiveFrequency INTEGER NOT NULL, RxCTCSS INTEGER, TxCTCSS INTEGER);";
}
