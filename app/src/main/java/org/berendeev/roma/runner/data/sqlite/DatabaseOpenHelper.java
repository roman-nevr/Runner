package org.berendeev.roma.runner.data.sqlite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;


public class DatabaseOpenHelper extends SQLiteOpenHelper implements BaseColumns {

    private static final String DATABASE_NAME = "location.db";
    private static final int DATABASE_VERSION = 1;

    //ID | TIME | LAT | LONG | SPEED | BEARING | ACC
    public static final String HISTORY_TABLE = "history";
    public static final String TIME = "time";
    public static final String LATITUDE = "latitude";
    public static final String LONGITUDE = "longitude";
    public static final String SPEED = "speed";
    public static final String BEARING = "bearing";
    public static final String ACCURACY = "accuracy";

    public DatabaseOpenHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override public void onCreate(SQLiteDatabase db) {
        String script = "create table " + HISTORY_TABLE + " (" +
                BaseColumns._ID + " integer primary key autoincrement, " +
                TIME + " integer, " +
                LATITUDE + " real not null, " +
                LONGITUDE + " real not null, " +
                SPEED + " real, " +
                BEARING + " real, " +
                ACCURACY + " real);";
        db.execSQL(script);
    }

    @Override public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + HISTORY_TABLE);
        onCreate(db);
    }

    @Override public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        super.onDowngrade(db, oldVersion, newVersion);
        db.execSQL("DROP TABLE IF EXISTS " + HISTORY_TABLE);
        onCreate(db);
    }
}
