package com.decibel.civilianc2.tools;

/**
 * Created by dburnett on 12/28/2017.
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.util.Pair;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Database {

    public Database(Context context, String path, int version){
        this.context = context;
        this.path = path;
        checkVersion(version);
    }

    public void enforceSchema(String schema){
        execute(schema);
    }

    public void enforceSchema(Table table){
        execute(table.toString());
    }



    public void execute(String sql){
        SQLiteDatabase connection = getConnection();
        try {

            connection.execSQL(sql);
        }
        finally {
            connection.close();
        }

    }

    public long execute(Insert insert){
        long id = -1;
        SQLiteDatabase connection = getConnection();
        try {
            id = connection.insert(insert.tableName, null, insert.values);
        }
        finally {
            connection.close();
        }
        return id;
    }

    public void execute(Update update) { execute(update.toString()); }

    public interface IDataReader {
        void readRecord(DataAdapter adapter);
    }

    public class DataAdapter{
        protected DataAdapter(Cursor cursor){
            this.cursor = cursor;
        }

        protected Cursor getCursor(){return this.cursor; }

        public Integer readInteger(String columnName){
            int columnIndex = cursor.getColumnIndex(columnName);
            if(!cursor.isNull(columnIndex))return cursor.getInt(columnIndex);
            return null;
        }

        public Double readDouble(String columnName){
            int columnIndex = cursor.getColumnIndex(columnName);
            if(cursor.isNull(columnIndex))return null;
            return cursor.getDouble(columnIndex);
        }

        public String readString(String columnName){
            int columnIndex = cursor.getColumnIndex(columnName);
            if(cursor.isNull(columnIndex))return null;
            return cursor.getString(columnIndex);
        }

        public Boolean readBoolean(String columnName){
            Integer value = readInteger(columnName);
            if(value == null) return null;
            return value == 1 ? true : false;
        }

        public boolean isNull(String columnName){
            return cursor.isNull(cursor.getColumnIndex(columnName));
        }

        public Date readDate(String columnName){
            String dateString = readString(columnName);
            try{
                Date date = toDate(dateString);
                return date;
            }
            catch (ParseException e) {
                return null;
            }
            catch (NullPointerException e){
                return null;
            }
        }

        private Cursor cursor;
    }

    public void execute(String sql, IDataReader reader){
        SQLiteDatabase connection = getConnection();
        Cursor cursor = connection.rawQuery(sql, null);
        DataAdapter adapter = new DataAdapter(cursor);
        try {
            if (cursor.moveToFirst()) {
                do {
                    reader.readRecord(adapter);
                } while (cursor.moveToNext());
            }

        }
        finally {
            cursor.close();
            connection.close();
        }
    }

    public String readString(String sql, final String column){
        final List<String> results = new ArrayList<>();
        execute(sql, new IDataReader() {
            @Override
            public void readRecord(DataAdapter adapter) {
                results.add(adapter.readString(column));
            }
        });
        if(results.size() == 1)
            return results.get(0);
        return null;
    }

    public Date readDate(String sql, final String column){
        try {
            return toDate(readString(sql, column));
        } catch (ParseException e) {
            return null;
        }
    }

    public Integer readInt(String sql, final String column){
        final List<Integer> results = new ArrayList<>();
        execute(sql, new IDataReader() {
            @Override
            public void readRecord(DataAdapter adapter) {
                results.add(adapter.readInteger(column));
            }
        });
        if(results.size() == 1)
            return results.get(0);
        return null;
    }

    public Boolean readBool(String sql, final String column){
        final List<Boolean> results = new ArrayList<>();
        execute(sql, new IDataReader() {
            @Override
            public void readRecord(DataAdapter adapter) {
                results.add(adapter.readBoolean(column));
            }
        });
        if(results.size() == 1)
            return results.get(0);
        return null;
    }

    public static class Insert{
        public Insert(String tableName){
            this.tableName = tableName;
        }

        public void addColumn(String columnName, int value){
            values.put(columnName, value);
        }

        public void addColumn(String columnName, double value){
            values.put(columnName, value);
        }

        public void addColumn(String columnName, float value){
            values.put(columnName, value);
        }

        public void addColumn(String name, boolean value){
            values.put(name, value ? 1 : 0);
        }

        public void addColumn(String name, String value){
            values.put(name, value);
        }

        public void addColumn(String name, Date time){
            addColumn(name, toDateString(time));
        }

        @Override
        public String toString(){
            return "INSERT INTO " + tableName + " (" + toSqlList(values.keySet()) + ") " +
                    "VALUES (" + toSqlList(values.valueSet()) + ");";
        }

        private String tableName;
        private ContentValues values = new ContentValues();
    }

    public static class Update{
        public Update(String tableName){
            this.tableName = tableName;
        }

        public void addColumn(String columnName, Integer value){
            columns.put(columnName, value);
        }

        public void addColumn(String columnName, Double value){
            columns.put(columnName, value);
        }

        public void addColumn(String columnName, Float value){
            columns.put(columnName, value);
        }

        public void addColumn(String name, Boolean value){
            columns.put(name, value ? 1 : 0);
        }

        public void addColumn(String name, String value){
            if(value != null)
                columns.put(name, DatabaseUtils.sqlEscapeString(value));
            else
                columns.put(name, null);
        }

        public void addColumn(String name, Date time){
            addColumn(name, toDateString(time));
        }

        public void where(String columnName, String value){
            whereClause = new Pair<>(columnName, DatabaseUtils.sqlEscapeString(value));
        }

        public void where(String columnName, int value){
            whereClause = new Pair<>(columnName, Integer.toString(value));
        }

        @Override
        public String toString(){
            if(whereClause != null)
                return "UPDATE " + tableName + " SET " + toSqlSet(columns) + " WHERE " + whereClause.first + " = " + whereClause.second + ";";
            return "UPDATE " + tableName;
        }

        Pair<String, String> whereClause;
        private String tableName;
        private HashMap<String, Object> columns = new HashMap<>();
    }

    public static class Table{
        public Table(String name){
            this.name = name;
        }

        public String getName() { return this.name; }

        public enum ColumnType{
            INTEGER,
            STRING,
            DATETIME,
            BOOLEAN
        }

        public static final int PRIMARY_KEY = 1;
        public static final int NOT_NULL = 2;
        public static final int UNIQUE = 4;

        public void addColumn(String name, ColumnType type, int flags){
            columns.put(name, new Pair<ColumnType, Integer>(type, flags));
        }

        public void addColumn(String name, ColumnType type){
            columns.put(name, new Pair<ColumnType, Integer>(type, null));
        }

        protected String getFlagString(Integer value){
            if(value == null){
                return "";
            }
            StringBuilder stringBuilder = new StringBuilder();
            if((value & PRIMARY_KEY) == PRIMARY_KEY){
                stringBuilder.append(" PRIVATE KEY");
            }
            if((value & NOT_NULL) == NOT_NULL){
                stringBuilder.append(" NOT NULL");
            }
            if((value & UNIQUE) == UNIQUE){
                stringBuilder.append(" UNIQUE");
            }
            return stringBuilder.toString();
        }

        protected String getTypeString(ColumnType type){
            switch (type){
                case INTEGER:
                    return "INTEGER";
                case STRING:
                    return "TEXT";
                case DATETIME:
                    return "DATETIME";
                case BOOLEAN:
                    return "INTEGER";
            }
            throw new IllegalArgumentException(type.toString());
        }

        @Override
        public String toString(){
            StringBuilder builder = new StringBuilder();
            builder.append("CREATE TABLE IF NOT EXISTS " + name + " (");
            for(Iterator iterator = columns.entrySet().iterator(); iterator.hasNext();){
                Map.Entry<String, Pair<ColumnType, Integer>> item = (Map.Entry<String, Pair<ColumnType, Integer>>)iterator.next();
                builder.append(item.getKey() + " " + getTypeString(item.getValue().first) + getFlagString(item.getValue().second));
                if(iterator.hasNext()){
                    builder.append(", ");
                }
            }
            builder.append(");");
            return builder.toString();
        }
        private String name;
        private HashMap<String, Pair<ColumnType, Integer>> columns = new HashMap<>();
    }



    private static Date toDate(String dateString) throws ParseException {
        return iso8601Format.parse(dateString);
    }

    private static String toDateString(Date date){
        return iso8601Format.format(date);
    }

    private SQLiteDatabase getConnection(){

        String internalFilePath = path;
        SQLiteDatabase connection = context.openOrCreateDatabase(internalFilePath, Context.MODE_PRIVATE, null);

        return connection;
    }

    private static <T> String toSqlList(Collection<T> collection){
        List<T> list = new ArrayList<T>();
        list.addAll(collection);
        StringBuilder builder = new StringBuilder();
        for(T item : list){
            if(item == null)
                builder.append("NULL");
            else
                builder.append(item);
            if(item != list.get(collection.size() - 1))
                builder.append(", ");
        }
        return builder.toString();
    }

    private static String toSqlSet(HashMap<String, Object> columns){
        StringBuilder builder = new StringBuilder();
        Object[] keys = columns.keySet().toArray();
        for (Object key : columns.keySet()){
            builder.append(key + " = " + (columns.get(key) == null ? "null" : columns.get(key)));
            if(key != keys[keys.length - 1])
                builder.append(", ");
        }
        return builder.toString();
    }

    private void checkVersion(int version){
        SQLiteDatabase connection = getConnection();
        if(connection.getVersion() != version){
            final List<String> tableNames = new ArrayList<>();
            String sql = "SELECT Name FROM sqlite_master WHERE type='table'";
            Cursor cursor = connection.rawQuery(sql, null);
            while(cursor.moveToNext()){
                tableNames.add(cursor.getString(0));
            }
            cursor.close();
            for(String tableName : tableNames){
                sql = "DROP TABLE IF EXISTS " + tableName + ";";
                execute(sql);
            }
            connection.setVersion(version);
        }

    }

    public void limitTableRecords(String tableName, int recordLimit){
        String sql = "SELECT COUNT(*) FROM " + tableName + ";";
        int count = readInt(sql, "COUNT(*)");
        if (count > recordLimit)
        {
            int numberToDelete = count - recordLimit;
            if (numberToDelete > 0)
            {
                sql = "DELETE FROM " + tableName + " WHERE RowId IN (SELECT RowId FROM " + tableName + " ORDER BY RowId ASC LIMIT " + recordLimit + ");";
                execute(sql);
            }
        }
    }

    private String path;
    private static final DateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private final Context context;
}