package org.berendeev.roma.runner.presentation.fragment;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.berendeev.roma.runner.R;
import org.berendeev.roma.runner.data.LocationApiRepository;
import org.berendeev.roma.runner.domain.LocationHistoryRepository;
import org.berendeev.roma.runner.presentation.App;
import org.berendeev.roma.runner.presentation.presenter.LocationPresenter;
import org.berendeev.roma.runner.presentation.service.LocationService;

import java.util.Date;
import java.util.Locale;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static org.berendeev.roma.runner.presentation.service.LocationService.COMMAND;
import static org.berendeev.roma.runner.presentation.service.LocationService.DATA;
import static org.berendeev.roma.runner.presentation.service.LocationService.LOCATION;
import static org.berendeev.roma.runner.presentation.service.LocationService.NEED_PERMISSIONS;
import static org.berendeev.roma.runner.presentation.service.LocationService.NEED_RESOLUTION;
import static org.berendeev.roma.runner.presentation.service.LocationService.START;


public class ServiceControlFragment extends Fragment {

    private static final String LOG_TAG = "myTag";
    public static final int REQUEST_CODE = 42;
    public static final String PENDING_INTENT = "pending_intent";
    public static final String BROADCAST_ACTION = "org.berendeev.roma.runner";
    public static final String MY_TAG = "myTag";
    @BindView(R.id.btn_send) Button btnSend;
    @BindView(R.id.btn_bind) Button btnBind;
    @BindView(R.id.btn_start) Button btnStart;
    @BindView(R.id.btn_stop) Button btnStop;
    @BindView(R.id.btn_clear) Button btnClear;
    @BindView(R.id.progress_bind) ProgressBar progressBind;
    @BindView(R.id.location) TextView tvLocation;
    @BindView(R.id.test_text) TextView tvTest;

    @BindView(R.id.distance) EditText etDistance;
    private Intent intent;
    private boolean bound;
    private ServiceConnection serviceConnection;
    private LocationService locationService;
    private LocationHistoryRepository locationHistoryRepository;
    private BroadcastReceiver broadcastReceiver;

    @Inject LocationPresenter presenter;


    @Nullable @Override public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.service_control, container, false);
        ButterKnife.bind(this, view);

        initUi();
        initService();
        initDi();
        registerReceiver();

        Location location = new Location("23");
        location.setLongitude(0.0123d);

        Bundle bundle = new Bundle();
        bundle.putParcelable("re", location);
        System.out.println(bundle);

        bundle.getParcelable("re");
        return view;
    }

    private void registerReceiver() {
        broadcastReceiver = new BroadcastReceiver() {
            @Override public void onReceive(Context context, Intent intent) {
                int result = intent.getIntExtra(LocationService.RESULT, -1);

                switch (result){
                    case NEED_RESOLUTION:{
                        PendingIntent resolution = intent.getParcelableExtra(DATA);
                        try {
                            LocationApiRepository.requestResolution(ServiceControlFragment.this, resolution);
                        } catch (IntentSender.SendIntentException e) {
                            e.printStackTrace();
                            //бяда
                        }
                        break;
                    }
                    case NEED_PERMISSIONS:{
                        LocationApiRepository.requestLocationPermissions(ServiceControlFragment.this);
                        break;
                    }
                    case LOCATION:{
                        Location location = intent.getParcelableExtra(DATA);
                        tvLocation.setText(locationToString(location));
                    }
                }
            }
        };


//        AsyncTaskLoader<String> loader;
//        LoaderManager manager = getActivity().getSupportLoaderManager();
        IntentFilter intFilt = new IntentFilter(BROADCAST_ACTION);
//        // регистрируем (включаем) BroadcastReceiver
//        getActivity().registerReceiver(broadcastReceiver, intFilt);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(broadcastReceiver, intFilt);
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

        builder.append(formatTime(location.getTime()));
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

    private String formatTime(long time) {
        return String.format(Locale.getDefault(), "time: %1$tF %1$tT", new Date(time));
    }

    private void unregisterReceiver(){
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(broadcastReceiver);
    }

    private void initDi() {
        App.getInstance().getMainComponent().inject(this);
        locationHistoryRepository = App.getInstance().getMainComponent().provideLocationHistoryRepository();
        tvTest.setText(formatTime(presenter.time));
    }

    private void initService() {
        createIntent();
        serviceConnection = new ServiceConnection() {
            public void onServiceConnected(ComponentName name, IBinder binder) {
                Log.d(LOG_TAG, "MainActivity onServiceConnected");
                bound = true;
                locationService = ((LocationService.MyBinder) binder).getService();
                check();
                btnBind.setText(R.string.unbind_label);
                hideBindProgress();
                showStop();
            }

            public void onServiceDisconnected(ComponentName name) {
                Log.d(LOG_TAG, "MainActivity onServiceDisconnected");
                bound = false;
                check();
                locationService = null;
                showStart();
            }
        };
//        bindService(intent, serviceConnection, BIND_AUTO_CREATE);
    }

    private void createIntent() {
        intent = new Intent(getActivity(), LocationService.class);
//        Intent nullIntent = new Intent();
//        PendingIntent pendingResult = getActivity().createPendingResult(REQUEST_CODE, nullIntent, 0);
//        intent.putExtra(PENDING_INTENT, pendingResult);
    }

    @Override public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(locationService != null){
            locationService.onPermissionsResult(requestCode, resultCode);
        }
    }



    private void initUi() {
        btnBind.setOnClickListener(v -> {
            if(locationService == null){//onBind
//                btnBind.setText(R.string.unbind_label);
                showBindProgress();
                getActivity().bindService(intent, serviceConnection, 0);
                showStart();
//                startService(intent);
            }else {
                btnBind.setText(R.string.bind_label);
                getActivity().unbindService(serviceConnection);
                locationService = null;

            }
        });
        btnStart.setOnClickListener(v -> {
            intent.putExtra(COMMAND, START);
            getActivity().startService(intent);
            showStop();
        });
        btnStop.setOnClickListener(v -> {
            getActivity().stopService(intent);
            showStart();
        });
        btnClear.setOnClickListener(v -> {
            locationHistoryRepository.clearHistory().subscribe();
        });
        check();
    }

    private void check(){
//        ActivityManager am = (ActivityManager) getActivity().getSystemService(ACTIVITY_SERVICE);
//        List<ActivityManager.RunningServiceInfo> rs = am.getRunningServices(50);
//
//        StringBuilder builder = new StringBuilder();
//        for (int i = 0; i < rs.size(); i++) {
//            ActivityManager.RunningServiceInfo rsi = rs.get(i);
//            String string = "Process " + rsi.process + " with component "
//                    + rsi.service.getClassName() + "/n";
//            Log.i("Service", string);
//            builder.append("Process " + rsi.process);
//        }
//        etDistance.setText(builder.toString());
    }

    public void showBindProgress(){
        progressBind.setVisibility(VISIBLE);
        btnBind.setVisibility(GONE);
    }

    public void hideBindProgress(){
        progressBind.setVisibility(GONE);
        btnBind.setVisibility(VISIBLE);
    }

    public void showStop(){
        btnStart.setVisibility(GONE);
        btnStop.setVisibility(VISIBLE);
    }

    public void showStart(){
        btnStart.setVisibility(VISIBLE);
        btnStop.setVisibility(GONE);
    }

    @Override public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(LocationApiRepository.isPermissionsGranted(getActivity().getApplicationContext())){
            intent.putExtra(COMMAND, START);
            getActivity().startService(intent);
        }
    }

    @Override public void onPause() {
        super.onPause();

        if (this.isRemoving()){
            Log.d(MY_TAG, "Removing");
        }

        if(this.isDetached()){
            Log.d(MY_TAG, "Detached");
        }

        Log.d(MY_TAG, "finishing: "+ getActivity().isFinishing() );

        Log.d(MY_TAG, "ChangingConfigurations: " + getActivity().isChangingConfigurations());

//        if (getActivity().is)
//        if(!isStoped()){
//            presenter.setView(this);
//            presenter.setText();
//        }
        if(isStoped()){
            App.getInstance().clearMainComponent();
        }

    }

    @Override public void onStop() {
        super.onStop();
        unregisterReceiver();
        if(locationService != null){
            getActivity().unbindService(serviceConnection);
            locationService = null;
        }
    }

    private boolean isStoped(){
        return getActivity().isFinishing();
    }


    public void setText(String text){
        tvTest.setText(text);
    }

}
