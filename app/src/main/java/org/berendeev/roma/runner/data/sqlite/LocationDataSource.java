package org.berendeev.roma.runner.data.sqlite;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;

import static org.berendeev.roma.runner.data.sqlite.DatabaseOpenHelper.ACCURACY;
import static org.berendeev.roma.runner.data.sqlite.DatabaseOpenHelper.BEARING;
import static org.berendeev.roma.runner.data.sqlite.DatabaseOpenHelper.HISTORY_TABLE;
import static org.berendeev.roma.runner.data.sqlite.DatabaseOpenHelper.LATITUDE;
import static org.berendeev.roma.runner.data.sqlite.DatabaseOpenHelper.LONGITUDE;
import static org.berendeev.roma.runner.data.sqlite.DatabaseOpenHelper.SPEED;
import static org.berendeev.roma.runner.data.sqlite.DatabaseOpenHelper.TIME;

public class LocationDataSource {

    public static final String FUSED = "fused";
    private SQLiteDatabase database;
    private final ContentValues contentValues;

    public LocationDataSource(DatabaseOpenHelper openHelper) {
        this.database = openHelper.getWritableDatabase();
        contentValues = new ContentValues();
    }

    public void saveLocation(Location location){
        fillContentValues(location);
        database.insert(HISTORY_TABLE, null, contentValues);
    }

    public List<Location> getLocations() {
        List<Location> locations = new ArrayList<Location>();
        Cursor cursor = database.query(HISTORY_TABLE, null, null, null, null, null, null, null);
        while (cursor.moveToNext()){
            locations.add(getLocationFromCursor(cursor));
        }
        return locations;
    }

    public void clearHistory() {
//        String selection = String.format("%1s = ? AND %2s = ? AND %3s = ? AND %4s = ? AND %5s = ? AND %6s = ?",
//                WORD, TRANSLATION, LANGUAGE_FROM, LANGUAGE_TO, IS_IN_HISTORY, IS_IN_FAVOURITES);
//        String[] selectionArgs = {word.word(), word.translation(), word.languageFrom(), word.languageTo(), TRUE, FALSE};
//        database.beginTransaction();
        database.delete(HISTORY_TABLE, null, null);
    }

    private void fillContentValues(Location location){
        contentValues.clear();
        contentValues.put(TIME, location.getTime());
        contentValues.put(LATITUDE, location.getLatitude());
        contentValues.put(LONGITUDE, location.getLongitude());
        contentValues.put(SPEED, location.getSpeed());
        contentValues.put(BEARING, location.getBearing());
        contentValues.put(ACCURACY, location.getAccuracy());
    }

    private Location getLocationFromCursor(Cursor cursor) {
        int timeIndex = cursor.getColumnIndex(TIME);
        int latitudeIndex = cursor.getColumnIndex(LATITUDE);
        int longitudeIndex = cursor.getColumnIndex(LONGITUDE);
        int speedIndex = cursor.getColumnIndex(SPEED);
        int bearingIndex = cursor.getColumnIndex(BEARING);
        int accuracyIndex = cursor.getColumnIndex(ACCURACY);

        Location location = new Location(FUSED);

        location.setTime(cursor.getLong(timeIndex));
        location.setLatitude(cursor.getDouble(latitudeIndex));
        location.setLongitude(cursor.getDouble(longitudeIndex));
        location.setSpeed(cursor.getFloat(speedIndex));
        location.setBearing(cursor.getFloat(bearingIndex));
        location.setAccuracy(cursor.getFloat(accuracyIndex));

        return location;
    }
}
