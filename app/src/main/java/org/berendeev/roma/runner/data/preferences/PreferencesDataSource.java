package org.berendeev.roma.runner.data.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.Location;

import static org.berendeev.roma.runner.data.sqlite.DatabaseOpenHelper.ACCURACY;
import static org.berendeev.roma.runner.data.sqlite.DatabaseOpenHelper.BEARING;
import static org.berendeev.roma.runner.data.sqlite.DatabaseOpenHelper.LATITUDE;
import static org.berendeev.roma.runner.data.sqlite.DatabaseOpenHelper.LONGITUDE;
import static org.berendeev.roma.runner.data.sqlite.DatabaseOpenHelper.SPEED;
import static org.berendeev.roma.runner.data.sqlite.DatabaseOpenHelper.TIME;
import static org.berendeev.roma.runner.data.sqlite.LocationDataSource.FUSED;

public class PreferencesDataSource {

    public static final String DISTANCE = "distance";
    private final SharedPreferences distancePreferences;

    public PreferencesDataSource(Context context) {
        this.distancePreferences = context.getSharedPreferences(DISTANCE, Context.MODE_PRIVATE);
    }

    public Location getLastLocation(){
        Location location = new Location(FUSED);

        location.setTime(distancePreferences.getLong(TIME, 0));
        location.setLatitude(getDouble(distancePreferences, LATITUDE, 0));
        location.setLongitude(getDouble(distancePreferences, LONGITUDE, 0));
        location.setSpeed(distancePreferences.getFloat(SPEED, 0f));
        location.setBearing(distancePreferences.getFloat(BEARING, 0f));
        location.setAccuracy(distancePreferences.getFloat(ACCURACY, 0f));
        return location;
    }

    public void saveLastLocation(Location location){
        Editor editor = distancePreferences.edit()
                .putLong(TIME, location.getTime())
                .putFloat(ACCURACY, location.getAccuracy())
                .putFloat(SPEED, location.getSpeed())
                .putFloat(BEARING, location.getBearing());
        putDouble(editor, LATITUDE, location.getLatitude());
        putDouble(editor, LONGITUDE, location.getLongitude());
        editor.apply();
    }

    public void saveDistance(float distance){
        distancePreferences.edit()
                .putFloat(DISTANCE, distance)
                .apply();
    }

    public float getDistance(){
        return distancePreferences.getFloat(DISTANCE, 100f);
    }

    Editor putDouble(final Editor edit, final String key, final double value) {
        return edit.putLong(key, Double.doubleToRawLongBits(value));
    }

    double getDouble(final SharedPreferences prefs, final String key, final double defaultValue) {
        if ( !prefs.contains(key))
            return defaultValue;

        return Double.longBitsToDouble(prefs.getLong(key, 0));
    }

}
