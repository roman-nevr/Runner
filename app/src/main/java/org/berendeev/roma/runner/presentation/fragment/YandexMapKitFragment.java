package org.berendeev.roma.runner.presentation.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.berendeev.roma.runner.R;
import org.berendeev.roma.runner.presentation.overlay.MyPathOverLay;

import butterknife.BindView;
import butterknife.ButterKnife;
import ru.yandex.yandexmapkit.MapController;
import ru.yandex.yandexmapkit.MapView;
import ru.yandex.yandexmapkit.OverlayManager;


public class YandexMapKitFragment extends Fragment {
    @BindView(R.id.map) MapView mapView;

    @Nullable @Override public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.map, container, false);
        ButterKnife.bind(this, view);
        initUI();
        return view;
    }

    private void initUI() {
        mapView.showFindMeButton(true);

        MapController controller = mapView.getMapController();
        OverlayManager overlayManager = controller.getOverlayManager();
        overlayManager.addOverlay(new MyPathOverLay(controller, mapView));
        mapView.showBuiltInScreenButtons(true);
//        overlayManager.getMyLocation().setEnabled(false);
    }
}
