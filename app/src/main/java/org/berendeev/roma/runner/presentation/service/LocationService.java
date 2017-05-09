package org.berendeev.roma.runner.presentation.service;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import org.berendeev.roma.runner.data.LocationApiRepository;
import org.berendeev.roma.runner.domain.LocationHistoryRepository;
import org.berendeev.roma.runner.presentation.App;
import org.berendeev.roma.runner.presentation.fragment.ServiceControlFragment;

import io.reactivex.disposables.CompositeDisposable;

import static org.berendeev.roma.runner.presentation.fragment.ServiceControlFragment.PENDING_INTENT;


public class LocationService extends Service {

    private LocationApiRepository locationApiRepository;
    private LocationHistoryRepository historyRepository;

    MyBinder binder = new MyBinder();
    private CompositeDisposable disposable;
    private Intent intent;

    public LocationService() {
    }

    final String LOG_TAG = "myTag";
    public static final String COMMAND = "command";
    public static final String RESULT = "result";
    public static final String DATA = "data";
    public static final int START = 0;
    public static final int RESTART = 1;
    public static final int NEED_RESOLUTION = 1;
    public static final int NEED_PERMISSIONS = 2;
    public static final int LOCATION = 3;


    public void onCreate() {
        super.onCreate();
        Log.d(LOG_TAG, "MyService onCreate");
        locationApiRepository = App.getInstance().getMainComponent().provideLocationApiRepository();
        historyRepository = App.getInstance().getMainComponent().provideLocationHistoryRepository();
        disposable = new CompositeDisposable();
    }

    public IBinder onBind(Intent intent) {
        Log.d(LOG_TAG, "MyService onBind");
        return binder;
    }

    public void onRebind(Intent intent) {
        super.onRebind(intent);
        Log.d(LOG_TAG, "MyService onRebind");
    }

    public boolean onUnbind(Intent intent) {
        Log.d(LOG_TAG, "MyService onUnbind");
        return super.onUnbind(intent);
    }

    @Override public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(LOG_TAG, "MyService startCommand, flags: " + flags + ", startId: " + startId);
//        this.stopSelf(startId);
//        return super.onStartCommand(intent, flags, startId);

        this.intent = new Intent(ServiceControlFragment.BROADCAST_ACTION);

        int command = intent.getIntExtra(COMMAND, -1);
        switch (command){
            case START:{
                if(locationApiRepository.isConnected()){
                    locationApiRepository.startLocationUpdates();
                }else {
                    locationApiRepository.connect();
                    subscribeOnRequests();
                }
                subscribeOnLocation();
                break;
            }
        }

        return START_NOT_STICKY;
    }

    private void sendResult(int resultCode) {
        intent.putExtra(RESULT, resultCode);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void sendResult(int resultCode, PendingIntent resolution) {
        intent.putExtra(RESULT, resultCode);
        intent.putExtra(DATA, resolution);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void sendResult(int resultCode, Location location) {
        intent.putExtra(RESULT, resultCode);
        intent.putExtra(DATA, location);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void subscribeOnLocation(){
        disposable.add(locationApiRepository
                .getLocationObservable()
                .subscribe(location -> {
                    historyRepository.saveLocation(location).subscribe();
                    sendResult(LOCATION, location);
                }));
    }

    private void subscribeOnRequests(){
        disposable.add(locationApiRepository
                .getLocationStateObservable()
                .subscribe(locationState -> {
                    switch (locationState.state()){
                        case requestPermissions:{
                            sendResult(NEED_PERMISSIONS);
//                            pendingIntent.send(21);
                            break;
                        }
                        case requestResolution:{
                            sendResult(NEED_RESOLUTION, (PendingIntent) locationState.data().getParcelableExtra(PENDING_INTENT));
//                            pendingIntent.send(this, 22, locationState.data());
                            break;
                        }
                        case notAvailable:{
                            break;
                        }
                        case permissionsRejected:{
                            break;
                        }
                        case connected:{
                            locationApiRepository.startLocationUpdates();
                            break;
                        }
                        case ok:{
                            break;
                        }
                    }

                }));
    }

    public void onDestroy() {
        super.onDestroy();
        Log.d(LOG_TAG, "MyService onDestroy");
        locationApiRepository.disconnect();
        disposable.clear();
    }

    public void onPermissionsResult(int requestCode, int resultCode){
        locationApiRepository.onRequestResult(requestCode, resultCode);
    }

    public void schedule() {
        Log.d(LOG_TAG, "Schedule");
    }

    public class MyBinder extends Binder {
        public LocationService getService() {
            return LocationService.this;
        }
    }

}
