package org.berendeev.roma.runner.presentation;

import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import org.berendeev.roma.runner.R;
import org.berendeev.roma.runner.data.LocationRepository;

import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;


public class LocationApiActivity extends AppCompatActivity implements ConnectionCallbacks, OnConnectionFailedListener, LocationListener {

    private static final int REQUEST_CHECK_SETTINGS = 42;
    @BindView(R.id.latitude) TextView latitude;
    @BindView(R.id.longitude) TextView longitude;
    @BindView(R.id.gps) TextView gps;

    private LocationRepository repository;
    private GoogleApiClient googleApiClient;
    private Location lastLocation;
    private LocationRequest locationRequest;
    private LocationSettingsRequest.Builder locationSettingsBuilder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        repository = new LocationRepository(getApplicationContext());

        // Create an instance of GoogleAPIClient.
        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }

    protected void onStart() {
        googleApiClient.connect();
        super.onStart();
    }

    protected void onStop() {
        googleApiClient.disconnect();
        super.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                googleApiClient, this);
    }

    @Override public void onConnected (@Nullable Bundle bundle) throws SecurityException {
        lastLocation = LocationServices.FusedLocationApi.getLastLocation(
                googleApiClient);
        if (lastLocation != null) {
            showLocation(lastLocation);
        }

        locationSettingsBuilder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);

        createLocationRequest();
        checkSettings();
        requestUpdates();
    }

    private void showLocation(Location location){
        latitude.setText(String.valueOf(lastLocation.getLatitude()));
        longitude.setText(String.valueOf(lastLocation.getLongitude()));
        gps.setText(locationToString(lastLocation));
    }

    @Override public void onConnectionSuspended(int i) {

    }

    @Override public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private void checkSettings(){
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
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied, but this can be fixed
                        // by showing the user a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(
                                    LocationApiActivity.this,
                                    REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way
                        // to fix the settings so we won't show the dialog.
                        break;
                }
            }
        });
    }

    private void requestUpdates() throws SecurityException{
        LocationServices.FusedLocationApi.requestLocationUpdates(
                googleApiClient, locationRequest, this);
    }

    protected void createLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private String locationToString(Location location){
        StringBuilder builder = new StringBuilder();
        builder.append("latitude: ");
        builder.append(location.getLatitude());
        builder.append("\n");

        builder.append("longitude: ");
        builder.append(location.getLongitude());
        builder.append("\n");

        builder.append("accuracy: ");
        builder.append(location.getAccuracy());
        builder.append("\n");

        builder.append(String.format(Locale.getDefault(), "time: %1$tF %1$tT", new Date(location.getTime())));
        builder.append("\n");

        builder.append("speed: ");
        builder.append(location.getSpeed());
        builder.append("\n");

        builder.append("bearing: ");
        builder.append(location.getBearing());
        builder.append("\n");

        builder.append("test: ");
        builder.append(location.toString());
        builder.append("\n");

        return builder.toString();
    }

    @Override public void onLocationChanged(Location location) {
        showLocation(location);
    }
}
