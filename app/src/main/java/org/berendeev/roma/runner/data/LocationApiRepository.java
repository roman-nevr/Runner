package org.berendeev.roma.runner.data;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

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

import org.berendeev.roma.runner.domain.entity.LocationInfo;

import io.reactivex.subjects.BehaviorSubject;

import static org.berendeev.roma.runner.domain.entity.LocationInfo.LocationStatus.disconnected;
import static org.berendeev.roma.runner.domain.entity.LocationInfo.LocationStatus.notAvailable;
import static org.berendeev.roma.runner.domain.entity.LocationInfo.LocationStatus.notAvailableAlways;
import static org.berendeev.roma.runner.domain.entity.LocationInfo.LocationStatus.ok;
import static org.berendeev.roma.runner.domain.entity.LocationInfo.LocationStatus.requestPermissions;
import static org.berendeev.roma.runner.domain.entity.LocationInfo.LocationStatus.requestResolution;

public class LocationApiRepository implements ConnectionCallbacks, OnConnectionFailedListener, LocationListener {

    private Context context;
    private GoogleApiClient googleApiClient;
    private BehaviorSubject<LocationInfo> locationInfoSubject;

    private Location lastLocation;
    private LocationRequest locationRequest;
    private LocationSettingsRequest.Builder locationSettingsBuilder;

    public LocationApiRepository(Context context) {
        this.context = context;

        // Create an instance of GoogleAPIClient.
        googleApiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        locationInfoSubject.createDefault(LocationInfo.DEFAULT);
        createLocationRequest();
    }

    @Override public void onConnected(@Nullable Bundle bundle) {
        try {
            Location lastLocation = getLastLocation();
            if (lastLocation == null) {
                LocationAvailability locationAvailability = LocationServices.FusedLocationApi.getLocationAvailability(googleApiClient);
                locationInfoSubject.onNext(LocationInfo.create(null, notAvailable, null));
            }
            startLocationUpdates();
        }catch (SecurityException e){
            //TODO
            locationInfoSubject.onNext(LocationInfo.create(null, disconnected, null));
        }
    }

    private void requestLocationPermissions() {
        locationInfoSubject.onNext(LocationInfo.create(getLastLocation(), requestPermissions, null));
    }

    private Location getLastLocation() throws SecurityException{
        return LocationServices.FusedLocationApi
                .getLastLocation(googleApiClient);
    }

    public void connect() {
        if (googleApiClient != null && !googleApiClient.isConnected()){
            googleApiClient.connect();
        }
    }

    public void disconnect() {
        if (googleApiClient != null && googleApiClient.isConnected()){
            googleApiClient.disconnect();
        }
    }

    public void startLocationUpdates(){
        if (googleApiClient != null && googleApiClient.isConnected()) {
            if(LocationRepository.isPermissionsGranted(context)){
                checkSettings();
            }else {
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

    private boolean availability() throws SecurityException{
        LocationAvailability locationAvailability = LocationServices.FusedLocationApi.getLocationAvailability(googleApiClient);
        return locationAvailability.isLocationAvailable();
    }

    private void checkSettings() {
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
                        locationInfoSubject.onNext(LocationInfo.create(null, requestResolution, status.getResolution()));
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way
                        // to fix the settings so we won't show the dialog.
                        locationInfoSubject.onNext(LocationInfo.create(null, notAvailableAlways, null));
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

    }

    @Override public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override public void onLocationChanged(Location location) {
        locationInfoSubject.onNext(LocationInfo.create(location, ok, null));
    }

    public BehaviorSubject<LocationInfo> getLocationInfoObservable() {
        return locationInfoSubject;
    }
}
