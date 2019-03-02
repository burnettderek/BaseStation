package com.decibel.civilianc2.model.dataaccess;

import com.decibel.civilianc2.tools.Database;

/**
 * Created by dburnett on 4/17/2018.
 */

public class Items {
    public Items(Database database){
        this.database = database;

        Database.Table table = new Database.Table(TableName);
    }

    private Database database;
    private final static String TableName = "Items";
    private final static String NameColumn = "Name";
}
