package org.berendeev.roma.runner.presentation.service;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import org.berendeev.roma.runner.data.LocationApiRepository;
import org.berendeev.roma.runner.domain.LocationHistoryRepository;
import org.berendeev.roma.runner.presentation.App;

import io.reactivex.disposables.CompositeDisposable;

import static org.berendeev.roma.runner.presentation.fragment.ServiceControlFragment.PENDING_INTENT;


public class LocationService extends Service {

    private LocationApiRepository locationApiRepository;
    private LocationHistoryRepository historyRepository;

    MyBinder binder = new MyBinder();
    private CompositeDisposable disposable;
    private PendingIntent pendingIntent;

    public LocationService() {
    }

    final String LOG_TAG = "myTag";
    public static final String COMMAND = "command";
    public static final int START = 0;
    public static final int CONNECT = 1;
    public static final int START_UPDATE = 2;

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
        int command = intent.getIntExtra(COMMAND, -1);
        switch (command){
            case START:{

                break;
            }
            case CONNECT:{
                locationApiRepository.connect();
                subscribeOnRequests();
                break;
            }
            case START_UPDATE:{
                if(locationApiRepository.isConnected()){
                    locationApiRepository.startLocationUpdates();
                }else {
                    locationApiRepository.connect();
                    subscribeOnRequests();
                }
                subscribeOnLocation();
            }
        }
        if (intent.hasExtra(PENDING_INTENT)){
            pendingIntent = intent.getParcelableExtra(PENDING_INTENT);
        }

        return START_NOT_STICKY;
    }

    private void subscribeOnLocation(){
        disposable.add(locationApiRepository
                .getLocationObservable()
                .subscribe(location -> {
                    historyRepository.saveLocation(location).subscribe();
                }));
    }

    private void subscribeOnRequests(){
        disposable.add(locationApiRepository
                .getLocationStateObservable()
                .subscribe(locationState -> {
                    switch (locationState.state()){
                        case requestPermissions:{
                            pendingIntent.send(21);
                            break;
                        }
                        case requestResolution:{
                            pendingIntent.send(this, 22, locationState.data());
                            break;
                        }
                        case notAvailable:{

                        }
                        case permissionsRejected:{

                        }
                        case ok:{

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

    public void schedule() {
        Log.d(LOG_TAG, "Schedule");
    }

    public class MyBinder extends Binder {
        public LocationService getService() {
            return LocationService.this;
        }
    }

}
