package org.berendeev.roma.runner.presentation.fragment;

import android.Manifest;
import android.content.Intent;
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

import org.berendeev.roma.runner.R;
import org.berendeev.roma.runner.data.LocationApiRepository;
import org.berendeev.roma.runner.domain.LocationHistoryRepository;
import org.berendeev.roma.runner.presentation.App;

import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.disposables.CompositeDisposable;

import static org.berendeev.roma.runner.data.LocationApiRepository.LOCATION_PERMISSION_REQUEST_ID;

public class LocationRepositoryFragment extends Fragment {

    @BindView(R.id.latitude) TextView latitude;
    @BindView(R.id.longitude) TextView longitude;
    @BindView(R.id.gps) TextView gps;

    private LocationApiRepository locationApiRepository;
    private LocationHistoryRepository historyRepository;
    private CompositeDisposable disposable;

    private static final String TAG = "myTag";
    private Snackbar snackbar;

    @Override public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        locationApiRepository = App.getInstance().getMainComponent().provideLocationApiRepository();
        historyRepository = App.getInstance().getMainComponent().provideLocationHistoryRepository();

        disposable = new CompositeDisposable();
    }

    @Nullable @Override public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_main, container, false);
        ButterKnife.bind(this, view);

        return view;
    }

    @Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        snackbar = Snackbar
                .make(latitude, "Location not available, check settings", Snackbar.LENGTH_INDEFINITE)
                .setAction("Check", v -> locationApiRepository.openLocationSettings(this));
    }

    public void onStart() {
        super.onStart();
        subscribeOnLocation();
        subscribeOnRequests();
        locationApiRepository.connect();
    }

    public void onStop() {
        super.onStop();
        disposable.clear();
        locationApiRepository.disconnect();
    }

    private void subscribeOnLocation(){
        disposable.add(locationApiRepository
                .getLocationObservable()
                .subscribe(location -> {
                    showLocation(location);
                    historyRepository.saveLocation(location).subscribe();
                }));
    }

    private void subscribeOnRequests(){
        disposable.add(locationApiRepository
                .getStateObservable()
                .subscribe(state -> {
                    switch (state){
                        case requestPermissions:{
                            locationApiRepository.requestLocationPermissions(this);
                            break;
                        }
                        case requestResolution:{
                            locationApiRepository.requestResolution(this);
                            break;
                        }
                        case notAvailable:{
                            if(!snackbar.isShown()){
                                snackbar.show();
                            }
                            break;
                        }
                        case permissionsRejected:{
                            if(!snackbar.isShown()){
                                snackbar.show();
                            }
                            break;
                        }
                        case ok:{
                            if(snackbar.isShown()){
                                snackbar.dismiss();
                            }
                            break;
                        }
                    }
//                    if (state == requestPermissions){
//                        locationApiRepository.requestLocationPermissions(this);
//                    }
//                    if (state == requestResolution){
//                        locationApiRepository.requestResolution(this);
//                    }
                }));
    }

    @Override public void onResume() {
        super.onResume();
        locationApiRepository.startLocationUpdates();
    }

    @Override
    public void onPause() {
        super.onPause();
        locationApiRepository.stopLocationUpdates();
    }

    private void requestLocationPermissions() {
        Log.d(TAG, "request permissions");
//        PermissionUtils.requestPermission(getActivity(), LOCATION_PERMISSION_REQUEST_ID,
//                Manifest.permission.ACCESS_FINE_LOCATION, true);
        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_ID);
    }

    private void showLocation(Location location){
        latitude.setText(String.valueOf(location.getLatitude()));
        longitude.setText(String.valueOf(location.getLongitude()));
        gps.setText(locationToString(location));
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

    @Override public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        int a = 0;
        Log.d(TAG, "" + permissions[0] + "\npermissions granted: " + locationApiRepository.isPermissionsGranted());

        if(locationApiRepository.isPermissionsGranted()){
            locationApiRepository.startLocationUpdates();
        }
    }

    @Override public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(!locationApiRepository.isLocationAvailable()){
            Toast.makeText(getActivity(), "location is not available", Toast.LENGTH_LONG).show();
        }else {
            Toast.makeText(getActivity(), "location is available now", Toast.LENGTH_LONG).show();
        }
        locationApiRepository.onRequestResult(requestCode, resultCode);
    }
}
