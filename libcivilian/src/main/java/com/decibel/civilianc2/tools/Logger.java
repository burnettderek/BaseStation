package com.decibel.civilianc2.tools;

import android.util.Log;

import com.decibel.civilianc2.tools.Database;

import java.util.GregorianCalendar;

/**
 * Created by dburnett on 12/9/2016.
 */
public class Logger<T> {
    public enum Level{
        DEBUG,
        INFO,
        WARNING,
        ERROR
    }

    public Logger(T typeInstance){
        this.typeName = typeInstance.getClass().getSimpleName();
    }

    public static void setDatabase(Database db, boolean debug){
        database = db;
        DEBUG = debug;
        Database.Table schema = new Database.Table(TableName);
        schema.addColumn(LevelColumn, Database.Table.ColumnType.STRING, Database.Table.NOT_NULL);
        schema.addColumn(MessageColumn, Database.Table.ColumnType.STRING, Database.Table.NOT_NULL);
        schema.addColumn(AddedOnColumn, Database.Table.ColumnType.DATETIME, Database.Table.NOT_NULL);
        database.enforceSchema(schema);
    }

    public void log(Level level, String message){
        if(level != Level.DEBUG || DEBUG == true) { //filter out debug in production builds
            Database.Insert insert = new Database.Insert(TableName);
            insert.addColumn(LevelColumn, level.toString());
            String fullMessage = typeName + ": " + message;
            insert.addColumn(MessageColumn, fullMessage);
            insert.addColumn(AddedOnColumn, GregorianCalendar.getInstance().getTime());
            synchronized (lock) {
                database.execute(insert);
            }

            if (DEBUG) {
                switch (level) {
                    case DEBUG:
                        Log.d(typeName, message);
                        break;
                    case INFO:
                        Log.i(typeName, message);
                        break;
                    case WARNING:
                        Log.w(typeName, message);
                        break;
                    case ERROR:
                        Log.e(typeName, message);
                        break;
                }
            }
        }
    }

    public void log(Exception e){
        log(Level.ERROR, e.getClass().getSimpleName() + ": " + e.getMessage() + " @" + e.getStackTrace());
    }

    private String typeName;
    private static Database database;
    private static Object lock = new Object();
    //Database Columns
    private static final String TableName = "Logs";
    private static final String LevelColumn = "Level";
    private static final String MessageColumn = "Message";
    private static final String AddedOnColumn = "AddedOn";
    private static boolean DEBUG;
}
