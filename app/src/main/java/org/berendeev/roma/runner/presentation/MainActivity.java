package org.berendeev.roma.runner.presentation;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import org.berendeev.roma.runner.R;
import org.berendeev.roma.runner.data.LocationRepository;
import org.berendeev.roma.runner.utils.PermissionUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.gps) TextView gps;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private LocationRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        repository = new LocationRepository(getApplicationContext());
        gps.setText("" + repository.isGpsEnabled());
        repository.test();
    }

    @Override protected void onStart() {
        super.onStart();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
        }else {
            repository.test();
        }
    }
}
