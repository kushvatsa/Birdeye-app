package com.kushvatsa.birdeye.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class CustDatabaseHelper extends SQLiteOpenHelper {

    // Database Version
    public static final int DATABASE_VERSION = 1;

    // Database name
    static final String DATABASE_NAME = "customers.db";

    public CustDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        //creating table
        final String SQL_CREATE_CUST_TABLE = "CREATE TABLE " + CustContract.CustDetailsEntry.TABLE_NAME + " (" +
                CustContract.CustDetailsEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                CustContract.CustDetailsEntry.COLUMN_EMAIL + " TEXT NOT NULL UNIQUE ON CONFLICT REPLACE, " +
                CustContract.CustDetailsEntry.COLUMN_F_NAME+ " TEXT NOT NULL, " +
                CustContract.CustDetailsEntry.COLUMN_L_NAME+ " TEXT NOT NULL, " +
                CustContract.CustDetailsEntry.COLUMN_PHONE + " TEXT NOT NULL, " +
                CustContract.CustDetailsEntry.COLUMN_CUST_NUMBER + " TEXT NOT NULL, " +
                CustContract.CustDetailsEntry.COLUMN_DATE + " TEXT NOT NULL UNIQUE ON CONFLICT REPLACE "
                + " );";


        db.execSQL(SQL_CREATE_CUST_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL("DROP TABLE IF EXISTS " + CustContract.CustDetailsEntry.TABLE_NAME);
        onCreate(db);
    }
}