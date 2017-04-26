package org.berendeev.roma.runner.data;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.location.OnNmeaMessageListener;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.util.List;

public class LocationRepository {

    private Context context;
    private LocationManager locationManager;

    public LocationRepository(Context context) {
        this.context = context;
        locationManager = getLocationManager();
    }

    private static final String NETWORK = LocationManager.NETWORK_PROVIDER;
    private static final String GPS = LocationManager.GPS_PROVIDER;

    private LocationManager getLocationManager(){
        return (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    }

    private LocationProvider getGpsProvider(){
        return getLocationManager().getProvider(GPS);
    }

    private LocationProvider getNetworkProvider(){
        return getLocationManager().getProvider(NETWORK);
    }

    private LocationProvider getProvider(){
        if (getLocationManager().isProviderEnabled(GPS)) {
            return getGpsProvider();
        }else if (getLocationManager().isProviderEnabled(NETWORK)){
            return getNetworkProvider();
        }else {
            return null;
        }
    }

    private boolean hasGps(){
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS);
    }

    //check if location service enabled in settings
    public boolean isGpsEnabled() {
        if (getLocationManager().isProviderEnabled(GPS)) {
            return true;
        }else {
            return false;
        }
    }

    public void test(){
        addNmeaListener();
    }

    public static boolean isPermissionsGranted(Context context){
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    public boolean isPermissionsGranted(){
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void addNmeaListener() throws SecurityException{
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            locationManager.addNmeaListener(new MyOnNmeaMessageListener());
        }else {
            locationManager.addNmeaListener(new MyGpsNmeaListener());
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private class MyOnNmeaMessageListener implements OnNmeaMessageListener{
        @Override public void onNmeaMessage(String message, long timestamp) {
            Log.d("myTag", message);
        }
    }

    private class MyGpsNmeaListener implements GpsStatus.NmeaListener{
        @Override public void onNmeaReceived(long timestamp, String nmea) {
            Log.d("myTag", nmea);
        }
    }

}
