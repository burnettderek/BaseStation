package com.decibel.civilianc2.model.dataaccess;

import com.decibel.civilianc2.model.entities.Message;
import com.decibel.civilianc2.model.entities.MessageAck;
import com.decibel.civilianc2.model.entities.MessageRecord;
import com.decibel.civilianc2.tools.Database;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by dburnett on 3/26/2018.
 */

public class Messages {
    public Messages(Database database){
        this.database = database;
        this.messageTable = new Database.Table("Messages");
        this.messageTable.addColumn("Source", Database.Table.ColumnType.STRING, Database.Table.NOT_NULL);
        this.messageTable.addColumn("Destination", Database.Table.ColumnType.STRING);
        this.messageTable.addColumn("Body", Database.Table.ColumnType.STRING);
        this.messageTable.addColumn("RequestAck", Database.Table.ColumnType.BOOLEAN, Database.Table.NOT_NULL);
        this.messageTable.addColumn("SourceId", Database.Table.ColumnType.STRING);
        this.messageTable.addColumn("ReceivedOn", Database.Table.ColumnType.DATETIME);
        this.database.enforceSchema(messageTable);

        this.ackTable = new Database.Table("MessageAcks");
        this.ackTable.addColumn("Source", Database.Table.ColumnType.STRING);
        this.ackTable.addColumn("MessageId", Database.Table.ColumnType.INTEGER, Database.Table.NOT_NULL);
        this.ackTable.addColumn("TimeStamp", Database.Table.ColumnType.DATETIME, Database.Table.NOT_NULL);
        this.database.enforceSchema(ackTable);
    }

    public MessageRecord getMessage(final long iD){
        final List<MessageRecord> records = new ArrayList<>();
        String sql = "SELECT Source, Destination, Body, RequestAck, ReceivedOn, SourceId FROM Messages WHERE rowid = " + iD + ";";
        database.execute(sql, new Database.IDataReader() {
            @Override
            public void readRecord(Database.DataAdapter adapter) {
                String source = adapter.readString("Source");
                String destination = adapter.readString("Destination");
                String body = adapter.readString("Body");
                boolean requestAck = adapter.readBoolean("RequestAck");
                String sourceId = adapter.readString("SourceId");
                Date receivedOn = adapter.readDate("ReceivedOn");
                records.add(new MessageRecord(new Message(iD, source, destination, body, requestAck, sourceId), receivedOn));
            }
        });
        if(records.size() == 0)return null;
        return records.get(0);
    }

    public List<MessageRecord> getMessageRecords(final String source, final String destination){
        final List<MessageRecord> records = new ArrayList<>();
        String sql = "SELECT rowid, Body, RequestAck, ReceivedOn FROM Messages WHERE source = '" + source + "' AND Destination = '" + destination + "';";
        database.execute(sql, new Database.IDataReader() {
            @Override
            public void readRecord(Database.DataAdapter adapter) {
                int id = adapter.readInteger("rowid");
                String body = adapter.readString("Body");
                boolean requestAck = adapter.readBoolean("RequestAck");
                String sourceId = adapter.readString("SourceId");
                Date receivedOn = adapter.readDate("ReceivedOn");
                records.add(new MessageRecord(new Message(id, source, destination, body, requestAck, sourceId), receivedOn));
            }
        });
        return records;
    }

    public List<String> getMessageContacts(String destination){
        final List<String> records = new ArrayList<>();
        String sql = "SELECT source FROM Messages WHERE Destination = '" + destination + "';";
        database.execute(sql, new Database.IDataReader() {
            @Override
            public void readRecord(Database.DataAdapter adapter) {
                String contact = adapter.readString("Source");
                records.add(contact);
            }
        });
        return records;
    }

    public long addMessage(String source, String destination, String body, boolean requestAck, String sourceId, Date receivedOn){
        Database.Insert insert = new Database.Insert(messageTable.getName());
        insert.addColumn("Source", source);
        insert.addColumn("Destination", destination);
        insert.addColumn("Body", body);
        insert.addColumn("RequestAck", requestAck);
        insert.addColumn("SourceId", sourceId);
        insert.addColumn("ReceivedOn", receivedOn);
        return database.execute(insert);
    }

    public void addAck(MessageAck ack){
        Database.Insert insert = new Database.Insert(ackTable.getName());
        insert.addColumn("Source", ack.getCallSign());
        insert.addColumn("MessageId", ack.getMessageId());
        insert.addColumn("TimeStamp", ack.getTimeStamp());
        database.execute(insert);
    }

    private Database.Table messageTable;
    private Database.Table ackTable;
    private Database database;
}
