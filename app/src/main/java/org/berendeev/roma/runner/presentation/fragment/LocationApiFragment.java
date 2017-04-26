package org.berendeev.roma.runner.presentation.fragment;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

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

import org.berendeev.roma.runner.R;
import org.berendeev.roma.runner.data.LocationRepository;
import org.berendeev.roma.runner.utils.PermissionUtils;

import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LocationApiFragment extends Fragment implements
        ConnectionCallbacks,
        OnConnectionFailedListener,
        LocationListener
//        OnRequestPermissionsResultCallback
{

    private static final int REQUEST_CHECK_SETTINGS = 42;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final int RESOLUTION_REQUEST_ID = 3;
    @BindView(R.id.latitude) TextView latitude;
    @BindView(R.id.longitude) TextView longitude;
    @BindView(R.id.gps) TextView gps;

//    private LocationRepository repository;
    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;
    private LocationSettingsRequest.Builder locationSettingsBuilder;

    private static final String TAG = "myTag";

    @Nullable @Override public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_main, container, false);
        ButterKnife.bind(this, view);
//        repository = new LocationRepository(getApplicationContext());


        return view;
    }

    @Override public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Create an instance of GoogleAPIClient.
        Context context = getActivity().getApplicationContext();
        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(context)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }

    public void onStart() {
        googleApiClient.connect();
        super.onStart();
    }

    public void onStop() {
        if(googleApiClient.isConnected()){
            googleApiClient.disconnect();
        }
        super.onStop();
    }

    @Override
    public void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    protected void stopLocationUpdates() {
        if(googleApiClient.isConnected()){
            LocationServices.FusedLocationApi.removeLocationUpdates(
                    googleApiClient, this);
        }
    }

    @Override public void onConnected (@Nullable Bundle bundle) throws SecurityException {
        Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(
                googleApiClient);
        if (lastLocation != null) {
            showLocation(lastLocation);
        }else {
            LocationAvailability locationAvailability = LocationServices.FusedLocationApi.getLocationAvailability(googleApiClient);
            Toast.makeText(getActivity(), "location availability is " + locationAvailability.isLocationAvailable(), Toast.LENGTH_LONG).show();
        }

        createLocationRequest();
        if(LocationRepository.isPermissionsGranted(getAppContext())){
            checkSettings();
        }else {
            requestLocationPermissions();
        }
    }

    private void requestLocationPermissions() {
        Log.d(TAG, "request permissions");
//        PermissionUtils.requestPermission(getActivity(), LOCATION_PERMISSION_REQUEST_CODE,
//                Manifest.permission.ACCESS_FINE_LOCATION, true);
        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
    }

    private void showLocation(Location location){
        latitude.setText(String.valueOf(location.getLatitude()));
        longitude.setText(String.valueOf(location.getLongitude()));
        gps.setText(locationToString(location));
    }

    @Override public void onConnectionSuspended(int i) {

    }

    @Override public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private void checkSettings(){
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

                        requestUpdates();
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied, but this can be fixed
                        // by showing the user a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            Log.d(TAG, "RESOLUTION_REQUIRED");
                            PendingIntent resolution = status.getResolution();

                            if(status.hasResolution()) {
                                startIntentSenderForResult(resolution.getIntentSender(), RESOLUTION_REQUEST_ID, null, 0, 0, 0, new Bundle());
                            }

//                            status.startResolutionForResult(
//                                    getActivity(),
//                                    REQUEST_CHECK_SETTINGS);
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

    @Override public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        int a = 0;
        Log.d(TAG, "" + permissions[0] + "\npermissions granted: " + LocationRepository.isPermissionsGranted(getAppContext()));
//        for (int permissionIndex = 0; permissionIndex < permissions.length; permissionIndex++) {
//            if(permissions[permissionIndex].eq)
//        }

        if(LocationRepository.isPermissionsGranted(getAppContext())){
            checkSettings();
        }
    }

    private Context getAppContext(){
        return getActivity().getApplicationContext();
    }

    @Override public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(!isLocationAvailable()){
            Toast.makeText(getActivity(), "location is not available", Toast.LENGTH_LONG).show();
        }else {
            Toast.makeText(getActivity(), "location is available now", Toast.LENGTH_LONG).show();
        }
    }

    private boolean isLocationAvailable(){
        boolean result = false;
        try {
            result = LocationServices.FusedLocationApi.getLocationAvailability(googleApiClient).isLocationAvailable();
        }catch (SecurityException e){

        }
        return result;
    }
}
