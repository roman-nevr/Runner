package org.berendeev.roma.runner.data;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import org.berendeev.roma.runner.domain.entity.LocationState;

import io.reactivex.subjects.BehaviorSubject;

import static org.berendeev.roma.runner.data.LocationApiRepository.State.connected;
import static org.berendeev.roma.runner.data.LocationApiRepository.State.disconnected;
import static org.berendeev.roma.runner.data.LocationApiRepository.State.notAvailable;
import static org.berendeev.roma.runner.data.LocationApiRepository.State.ok;
import static org.berendeev.roma.runner.data.LocationApiRepository.State.permissionsRejected;
import static org.berendeev.roma.runner.data.LocationApiRepository.State.requestPermissions;
import static org.berendeev.roma.runner.data.LocationApiRepository.State.requestResolution;
import static org.berendeev.roma.runner.domain.entity.LocationState.DEFAULT;
import static org.berendeev.roma.runner.presentation.fragment.ServiceControlFragment.PENDING_INTENT;

public class LocationApiRepository implements ConnectionCallbacks, OnConnectionFailedListener, LocationListener {

    public static final int REQUEST_CHECK_SETTINGS = 42;
    public static final int LOCATION_PERMISSION_REQUEST_ID = 43;
    public static final int RESOLUTION_REQUEST_ID = 44;

    private Context context;

    private GoogleApiClient googleApiClient;
    private BehaviorSubject<Location> locationSubject;
    private BehaviorSubject<State> stateSubject;
    private BehaviorSubject<LocationState> locationStateSubject;
    private Location lastLocation;

    private LocationRequest locationRequest;
    private LocationSettingsRequest.Builder locationSettingsBuilder;

    private PendingIntent pendingIntent;

    public LocationApiRepository(Context context) {
        this.context = context;


        // Create an instance of GoogleAPIClient.
        googleApiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        locationSubject = BehaviorSubject.create();
        stateSubject = BehaviorSubject.create();
        locationStateSubject = BehaviorSubject.createDefault(DEFAULT);
        createLocationRequest();
    }

    @Override public void onConnected(@Nullable Bundle bundle) {
        try {
            locationStateSubject.onNext(DEFAULT.toBuilder().state(connected).build());
            Location lastLocation = getLastLocation();
            if (lastLocation == null) {
                LocationAvailability locationAvailability = LocationServices.FusedLocationApi.getLocationAvailability(googleApiClient);
                if (!locationAvailability.isLocationAvailable()) {
                    stateSubject.onNext(notAvailable);
                    locationStateSubject.onNext(DEFAULT);
                }
            }
//            startLocationUpdates();
        } catch (SecurityException e) {
            //TODO
            stateSubject.onNext(disconnected);
            locationStateSubject.onNext(DEFAULT.toBuilder().state(disconnected).build());
        }
    }

    private void requestLocationPermissions() {
        stateSubject.onNext(requestPermissions);
        locationStateSubject.onNext(DEFAULT.toBuilder().state(requestPermissions).build());
    }

    private Location getLastLocation() {
        try {
            return LocationServices.FusedLocationApi
                    .getLastLocation(googleApiClient);
        } catch (SecurityException e) {
            return null;
        }

    }

    public void connect() {
        if (googleApiClient != null && !googleApiClient.isConnected()) {
            googleApiClient.connect();
        }
    }

    public void disconnect() {
        if (googleApiClient != null && googleApiClient.isConnected()) {
            googleApiClient.disconnect();
            stateSubject.onNext(disconnected);
            locationStateSubject.onNext(DEFAULT.toBuilder().state(disconnected).build());
        }
    }

    public void startLocationUpdates() {
        if (googleApiClient != null && googleApiClient.isConnected()) {
            if (LocationRepository.isPermissionsGranted(context)) {
                checkSettings();
            } else {
                requestLocationPermissions();
            }
        }
    }

    public void stopLocationUpdates() {
        if (googleApiClient != null && googleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(
                    googleApiClient, this);
        }

    }

    private boolean isAvailable() throws SecurityException {
        LocationAvailability locationAvailability = LocationServices.FusedLocationApi.getLocationAvailability(googleApiClient);
        return locationAvailability.isLocationAvailable();
    }

    private void checkSettings() {
        locationSettingsBuilder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(googleApiClient,
                        locationSettingsBuilder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override public void onResult(@NonNull LocationSettingsResult result) {
                final Status status = result.getStatus();
                final LocationSettingsStates states = result.getLocationSettingsStates();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        // All location settings are satisfied. The client can
                        // initialize location requests here.
                        createLocationRequest();
                        requestUpdates();
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied, but this can be fixed
                        // by showing the user a dialog.
                        /*try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(
                                    LocationApiActivity.this,
                                    REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        }*/
                        if(status.hasResolution() && stateSubject.getValue() != permissionsRejected){
                            pendingIntent = status.getResolution();
                            stateSubject.onNext(requestResolution);
                            Intent intent = new Intent();
                            intent.putExtra(PENDING_INTENT, pendingIntent);
                            locationStateSubject.onNext(DEFAULT.toBuilder().state(requestResolution).data(intent).build());
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way
                        // to fix the settings so we won't show the dialog.
                        stateSubject.onNext(permissionsRejected);
                        locationStateSubject.onNext(DEFAULT.toBuilder().state(permissionsRejected).build());
                        break;
                }
            }
        });
    }

    private void requestUpdates() throws SecurityException {
        LocationServices.FusedLocationApi.requestLocationUpdates(
                googleApiClient, locationRequest, this);
    }

    protected void createLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override public void onConnectionSuspended(int i) {
        int a =0;
    }

    @Override public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        int a =0;
    }

    @Override public void onLocationChanged(Location location) {
        locationSubject.onNext(location);
        stateSubject.onNext(ok);
        locationStateSubject.onNext(DEFAULT.toBuilder().state(ok).build());
    }

    public BehaviorSubject<Location> getLocationObservable() {
        return locationSubject;
    }

    public BehaviorSubject<State> getStateObservable() {
        return stateSubject;
    }

    public BehaviorSubject<LocationState> getLocationStateObservable() {
        return locationStateSubject;
    }

    public static boolean isPermissionsGranted(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    public boolean isPermissionsGranted() {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    public boolean isLocationAvailable() {
        try {
            return LocationServices.FusedLocationApi.getLocationAvailability(googleApiClient).isLocationAvailable();
        } catch (SecurityException e) {
            return false;
        }
    }

    public void onRequestResult(int requestCode, int resultCode) {
        if(requestCode == RESOLUTION_REQUEST_ID){
            if (resultCode == 0){
                stateSubject.onNext(permissionsRejected);
                locationStateSubject.onNext(DEFAULT.toBuilder().state(permissionsRejected).build());
            }else {
                startLocationUpdates();
            }
        }
    }

    public enum State {
        ok, connected, notAvailable, requestPermissions, requestResolution, permissionsRejected, disconnected
    }

    public static void requestResolution(Fragment fragment, PendingIntent pendingIntent) throws IntentSender.SendIntentException {
        if(pendingIntent != null) {
            fragment.startIntentSenderForResult(pendingIntent.getIntentSender(), RESOLUTION_REQUEST_ID, null, 0, 0, 0, new Bundle());
        }
    }

    public void requestResolution(Fragment fragment) throws IntentSender.SendIntentException {
        if(pendingIntent != null) {
            fragment.startIntentSenderForResult(pendingIntent.getIntentSender(), RESOLUTION_REQUEST_ID, null, 0, 0, 0, new Bundle());
        }
    }

    public static void requestResolution(Activity activity, PendingIntent pendingIntent) throws IntentSender.SendIntentException {
        if(pendingIntent != null) {
            ActivityCompat.startIntentSenderForResult(activity, pendingIntent.getIntentSender(), RESOLUTION_REQUEST_ID, null, 0, 0, 0, new Bundle());
        }
    }

    public static void requestLocationPermissions(Fragment fragment) {
        fragment.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_ID);
    }

    public static void requestLocationPermissions(Activity activity) {
        ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_ID);
    }

    public void openLocationSettings(Fragment fragment){
        fragment.startActivity(new Intent(
                android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
    }

    public boolean isConnected(){
        if(googleApiClient != null){
            return googleApiClient.isConnected();
        }else {
            return false;
        }
    }

    //ToDo complete
    private class Listener{
        private Activity activity;
        private Fragment fragment;

        public void registerObject(Activity activity){
            this.activity = activity;
        }

        public void requestResolution(){

        }
    }

}
