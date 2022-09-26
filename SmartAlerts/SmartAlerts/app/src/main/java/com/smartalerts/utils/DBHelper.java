package com.smartalerts.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.SimpleCursorAdapter;
import com.smartalerts.R;
import com.smartalerts.ui.Notification;

public class DBHelper extends SQLiteOpenHelper {
    Context context;

    public DBHelper(Context context) {
        super(context, "LocationAlert.db", null, 1);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("create Table Alerts(_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, title TEXT NOT NULL, description TEXT, eventtype TEXT NOT NULL, locationdetail TEXT NOT NULL,phonenumber TEXT , message TEXT ,latitude DOUBLE NOT NULL, longitude DOUBLE NOT NULL)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("drop table if exists Alerts");
    }

    public boolean  insertAlertData(String title, String description, String eventType, String location, String phoneNumber, String message,double lat, double lon) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("title", title);
        contentValues.put("description", description);
        contentValues.put("eventtype", eventType);
        contentValues.put("locationdetail", location);
        contentValues.put("phonenumber",phoneNumber);
        contentValues.put("message",message);
        contentValues.put("latitude",lat);
        contentValues.put("longitude",lon);
        long result = db.insert("Alerts", null, contentValues);
        if (result == -1) {
            return false;
        }
        return true;
    }

    public boolean updateAlertData(String title, String description, String eventType, String location, String phoneNumber, String message, String lat, String lon) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("title", title);
        contentValues.put("description", description);
        contentValues.put("eventtype", eventType);
        contentValues.put("locationdetail", location);
        contentValues.put("phonenumber",phoneNumber);
        contentValues.put("message",message);
        contentValues.put("latitude",lat);
        contentValues.put("longitude",lon);
        long result = db.insert("Alerts", null, contentValues);
        if (result == -1) {
            return false;
        }
        return true;
    }

    public SimpleCursorAdapter populateListFromDB() {
        String[] columns = {"_id", "title", "description", "eventtype", "locationdetail", "phonenumber" ,"message","latitude" , "longitude"};
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query("Alerts", columns, null, null, null, null, null);
        int[] viewIds = {R.id.list_id, R.id.list_title};
        return new SimpleCursorAdapter(context, R.layout.single_item, cursor, columns, viewIds);

    }

    public Notification getNotificationDetails(int id) {
        String[] columns = {"_id", "title", "description", "eventtype", "locationdetail"};
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query("Alerts", columns,  "_id =?", new String[] { String.valueOf(id) }, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();

        Notification notification = new Notification(cursor.getInt(0),
                cursor.getString(1), cursor.getString(2) , cursor.getString(3),cursor.getString(4) );
        // return contact
        return notification;

    }

    public void deleteItem(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("Alerts",  "_id = ?",
                new String[] { String.valueOf(id) });
        db.close();
    }
}
