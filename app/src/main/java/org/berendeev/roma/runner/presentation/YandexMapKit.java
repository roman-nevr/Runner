package org.berendeev.roma.runner.presentation;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import org.berendeev.roma.runner.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import ru.yandex.yandexmapkit.MapView;

public class YandexMapKit extends AppCompatActivity {

    @BindView(R.id.map) MapView mapView;

    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map);
        ButterKnife.bind(this);
        mapView.showFindMeButton(true);
    }
}
