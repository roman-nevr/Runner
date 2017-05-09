package org.berendeev.roma.runner.presentation.fragment;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import org.berendeev.roma.runner.R;
import org.berendeev.roma.runner.data.LocationApiRepository;
import org.berendeev.roma.runner.domain.LocationHistoryRepository;
import org.berendeev.roma.runner.presentation.App;
import org.berendeev.roma.runner.presentation.service.LocationService;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static org.berendeev.roma.runner.presentation.service.LocationService.COMMAND;
import static org.berendeev.roma.runner.presentation.service.LocationService.START_UPDATE;


public class ServiceControlFragment extends Fragment {

    private static final String LOG_TAG = "myTag";
    public static final int REQUEST_CODE = 42;
    public static final String PENDING_INTENT = "pending_intent";
    @BindView(R.id.btn_send) Button btnSend;
    @BindView(R.id.btn_bind) Button btnBind;
    @BindView(R.id.btn_start) Button btnStart;
    @BindView(R.id.btn_stop) Button btnStop;
    @BindView(R.id.btn_clear) Button btnClear;
    @BindView(R.id.progress_bind) ProgressBar progressBind;

    @BindView(R.id.distance) EditText etDistance;
    private Intent intent;
    private boolean bound;
    private ServiceConnection serviceConnection;
    private LocationService locationService;
    private LocationHistoryRepository locationHistoryRepository;


    @Nullable @Override public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.service_control, container, false);
        ButterKnife.bind(this, view);

        initUi();
        initService();
        initDi();

        return view;
    }

    private void initDi() {
        locationHistoryRepository = App.getInstance().getMainComponent().provideLocationHistoryRepository();
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
        Intent nullIntent = new Intent();
        PendingIntent pendingResult = getActivity().createPendingResult(REQUEST_CODE, nullIntent, 0);
        intent.putExtra(PENDING_INTENT, pendingResult);
    }

    @Override public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_CODE && resultCode == 22){
            try {
                LocationApiRepository.requestResolution(this, intent.getParcelableExtra(PENDING_INTENT));
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        }
        if (requestCode == REQUEST_CODE && resultCode == 21){
            LocationApiRepository.requestLocationPermissions(this);
        }
        if(requestCode == REQUEST_CODE && requestCode == 20){
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
            intent.putExtra(COMMAND, START_UPDATE);
            getActivity().getApplicationContext().startService(intent);
            showStop();
        });
        btnStop.setOnClickListener(v -> {
            getActivity().getApplicationContext().stopService(intent);
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
            intent.putExtra(COMMAND, START_UPDATE);
            getActivity().startService(intent);
        }
    }
}
