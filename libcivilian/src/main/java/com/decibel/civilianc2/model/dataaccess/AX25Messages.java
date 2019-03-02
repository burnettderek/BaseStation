package com.decibel.civilianc2.model.dataaccess;

import com.decibel.civilianc2.model.entities.AX25MessageRecord;
import com.decibel.civilianc2.protocols.ax25.AX25Packet;
import com.decibel.civilianc2.tools.Database;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by dburnett on 4/16/2018.
 */

public class AX25Messages {
    public AX25Messages(Database database){
        this.database = database;
        Database.Table table = new Database.Table(TableName);
        table.addColumn(SourceColumn, Database.Table.ColumnType.STRING);
        table.addColumn(DestinationColumn, Database.Table.ColumnType.STRING);
        table.addColumn(DigipeatersColumn, Database.Table.ColumnType.STRING);
        table.addColumn(PayloadColumn, Database.Table.ColumnType.STRING);
        table.addColumn(SupportedColumn, Database.Table.ColumnType.BOOLEAN);
        table.addColumn(ReceivedOnColumn, Database.Table.ColumnType.DATETIME);
        this.database.enforceSchema(table);
    }

    public void addMessage(AX25Packet packet, boolean supported){
        Database.Insert insert = new Database.Insert(TableName);
        insert.addColumn(SourceColumn, packet.Header.SourceAddress);
        insert.addColumn(DestinationColumn, packet.Header.DestinationAddress);
        insert.addColumn(DigipeatersColumn, fromMany2One(packet.Header.DigipeaterAddresses));
        try {
            String strValue = new String(packet.RawMessage, "US-ASCII");
            insert.addColumn(PayloadColumn, strValue);
        } catch (UnsupportedEncodingException e){/*wont happen, don't care*/}
        insert.addColumn(SupportedColumn, supported);
        insert.addColumn(ReceivedOnColumn, new Date());
        database.execute(insert);
    }

    public AX25MessageRecord getLastMessage(String callSign){
        final List<AX25MessageRecord> result = new ArrayList<>();
        final String sql = "SELECT " + PayloadColumn + ", " + SupportedColumn + ", " + ReceivedOnColumn + " FROM " + TableName + " WHERE " + SourceColumn + " = '" + callSign + "' ORDER BY " + ReceivedOnColumn + " DESC LIMIT 1;";
        database.execute(sql, new Database.IDataReader() {
            @Override
            public void readRecord(Database.DataAdapter adapter) {
                result.add(fromDataAdapater(adapter));
            }
        });
        return result.size() == 0 ? null : result.get(0);
    }

    public List<AX25MessageRecord> getAllMessagesForStation(String callSign){
        final List<AX25MessageRecord> result = new ArrayList<>();
        final String sql = "SELECT " + PayloadColumn + ", " + SupportedColumn + ", " + ReceivedOnColumn + " FROM " + TableName + " WHERE " + SourceColumn + " = '" + callSign + "' ORDER BY " + ReceivedOnColumn + " DESC;";
        database.execute(sql, new Database.IDataReader() {
            @Override
            public void readRecord(Database.DataAdapter adapter) {
                result.add(fromDataAdapater(adapter));
            }
        });
        return result;
    }

    private AX25MessageRecord fromDataAdapater(Database.DataAdapter adapter){
        String payload = adapter.readString(PayloadColumn);
        Boolean decoded = adapter.readBoolean(SupportedColumn);
        Date receivedOn = adapter.readDate(ReceivedOnColumn);
        return new AX25MessageRecord(payload, decoded, receivedOn);
    }

    private String fromMany2One(List<String> stringList){
        StringBuilder builder = new StringBuilder();
        for(String string : stringList){
            builder.append(string);
            if(string != stringList.get(stringList.size() - 1)){
                builder.append(", ");
            }
        }
        return builder.toString();
    }

    private Database database;
    private final static String TableName = "AX25MessageLog";
    private final static String SourceColumn = "Source";
    private final static String DestinationColumn = "Destination";
    private final static String DigipeatersColumn = "Digipeaters";
    private final static String PayloadColumn = "Payload";
    private final static String SupportedColumn = "Supported";
    private final static String ReceivedOnColumn = "ReceivedOn";
}
