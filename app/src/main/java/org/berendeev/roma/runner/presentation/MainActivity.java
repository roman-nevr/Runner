package org.berendeev.roma.runner.presentation;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import org.berendeev.roma.runner.R;
import org.berendeev.roma.runner.data.LocationRepository;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.gps) TextView gps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        LocationRepository repository = new LocationRepository(getApplicationContext());
        gps.setText("" + repository.isGpsEnabled());
    }
}
