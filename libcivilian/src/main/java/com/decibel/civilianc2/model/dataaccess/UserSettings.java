package com.decibel.civilianc2.model.dataaccess;

import com.decibel.civilianc2.model.entities.Position;
import com.decibel.civilianc2.tools.Database;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dburnett on 1/16/2018.
 */

public class UserSettings {
    public UserSettings(Database database){
        this.database = database;
        this.database.enforceSchema(SettingsSchema);
    }

    public int getInt(String name, int defaultValue){
        String setting = getSetting(name);
        if(setting == null)return defaultValue;
        return Integer.parseInt(setting);
    }

    public double getDouble(String name, double defaultValue){
        String setting = getSetting(name);
        if(setting == null)return defaultValue;
        return Double.parseDouble(setting);
    }

    public Position getPosition(String name){
        String posString = getSetting(name);
        if(posString == null || posString.isEmpty()){
            return null;
        }
        String[] latlon = posString.split(",");
        double latitude = Double.parseDouble(latlon[0]);
        double longitude = Double.parseDouble(latlon[1]);
        return new Position(latitude, longitude);
    }

    public String getSetting(String name){
        return database.readString("SELECT Setting FROM Settings WHERE Name = '" + name + "'", "Setting");
    }

    public void setSetting(String name, int setting){
        setSetting(name, Integer.toString(setting));
    }

    public void setSetting(String name, double setting){
        setSetting(name, String.format("%06.4f", setting));
    }

    public void setSetting(String name, Position position){
        setSetting(name, position.getLatitude() + ", " + position.getLongitude());
    }

    public void setSetting(String name, String setting){
        if(!hasSetting(name)){
            Database.Insert insert = new Database.Insert("Settings");
            insert.addColumn("Name", name);
            insert.addColumn("Setting", setting);
            database.execute(insert);
        } else {
            Database.Update update = new Database.Update("Settings");
            update.addColumn("Setting", setting);
            update.where("Name", name);
            database.execute(update);
        }
    }

    public boolean hasSetting(String name){
        final List<String> values = new ArrayList<>();
        String sql = "SELECT Setting FROM Settings WHERE Name = '" + name + "';";
        database.execute(sql, new Database.IDataReader() {
            @Override
            public void readRecord(Database.DataAdapter adapter) {
                values.add(adapter.readString("Setting"));
            }
        });
        return values.size() > 0;
    }

    public final static String CallSign = "Device.CallSign";
    public final static String SSID = "Device.SSID";
    public final static String Location = "Device.Location";
    public final static String APRSCommentField = "Device.APRS.Comment";

    private Database database;

    private static final String SettingsSchema = "CREATE TABLE IF NOT EXISTS Settings (Name TEXT PRIMARY KEY NOT NULL UNIQUE, Setting TEXT);";
}
