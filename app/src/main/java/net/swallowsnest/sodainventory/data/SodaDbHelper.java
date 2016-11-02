package net.swallowsnest.sodainventory.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import net.swallowsnest.sodainventory.data.SodaContract.SodaEntry;

/**
 * Created by marshas on 10/27/16.
 */

public class SodaDbHelper extends SQLiteOpenHelper {

    public static final String LOG_TAG = SodaDbHelper.class.getName();

    //database name
    public static final String DATABASE_NAME = "sodas.db";

    //database version
    public static final int DATABASE_VERSION = 8;

    public SodaDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //string for the SQL create table statement
        String SQL_CREATE_SODAS_TABLE = "CREATE TABLE " + SodaEntry.TABLE_NAME + "("
                + SodaEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + SodaEntry.COLUMN_NAME + " TEXT NOT NULL, "
                + SodaEntry.COLUMN_QUANTITY + " INTEGER, "
                + SodaEntry.COLUMN_SOLD + " INTEGER, "
                + SodaEntry.COLUMN_PRICE + " INTEGER NOT NULL DEFAULT 0);";

        //execute db
        db.execSQL(SQL_CREATE_SODAS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists " + SodaEntry.TABLE_NAME);
        onCreate(db);
    }
}
