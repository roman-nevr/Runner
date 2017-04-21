package org.berendeev.roma.runner.data;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.location.OnNmeaMessageListener;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;

import java.util.List;

public class LocationRepository {

    private Context context;

    public LocationRepository(Context context) {
        this.context = context;
    }

    private static final String NETWORK = LocationManager.NETWORK_PROVIDER;
    private static final String GPS = LocationManager.GPS_PROVIDER;

    public void getLocation() {
        LocationManager locationManager = getLocationManager();
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
//            return TODO;
//            return locationManager.getLastKnownLocation(GPS);
        }
    }

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

    public boolean isGpsEnabled() {
        if (getLocationManager().isProviderEnabled(GPS)) {
            return true;
        }else {
            return false;
        }
    }

    private void isGpsAviable(){

//        getLocationManager().addNmeaListener(new OnNmeaMessageListener() {
//            @Override public void onNmeaMessage(String message, long timestamp) {
//
//            }
//        })
    }

    private boolean isPermissionsGranted(){
        return ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED;
    }
}
