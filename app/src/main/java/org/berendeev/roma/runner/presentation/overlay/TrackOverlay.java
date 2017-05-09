package org.berendeev.roma.runner.presentation.overlay;

import android.graphics.drawable.Drawable;
import android.location.Location;
import android.support.v4.content.ContextCompat;

import org.berendeev.roma.runner.R;

import java.util.List;

import ru.yandex.yandexmapkit.MapController;
import ru.yandex.yandexmapkit.MapView;
import ru.yandex.yandexmapkit.overlay.Overlay;
import ru.yandex.yandexmapkit.overlay.OverlayItem;
import ru.yandex.yandexmapkit.utils.GeoPoint;
import ru.yandex.yandexmapkit.utils.ScreenPoint;

public class TrackOverlay extends Overlay {
    MapView mMmapView;

    public TrackOverlay(MapController controller, MapView mapView, List<Location> locations) {
        super(controller);
        mMmapView = mapView;
        this.setIRender(new TrackOverlayRender(this));
        for (Location location : locations) {
            this.addOverlayItem(getOverlayItem(location));
        }
    }

    private OverlayItem getOverlayItem(Location location){
        return new OverlayItem(getGeoPoint(location), getDrawable());
    }

    private GeoPoint getGeoPoint(Location location) {
        return new GeoPoint(location.getLatitude(), location.getLongitude());
    }

    private Drawable getDrawable() {
        return ContextCompat.getDrawable(this.e.getContext(), R.drawable.ic_bookmark_black);
    }

//    @Override
//    public boolean onLongPress(float x, float y) {
//        OverlayItem m = new OverlayItem(
//                this.e.getGeoPoint(new ScreenPoint(x, y)),
//                getDrawable());
//        m.setOffsetY(-23);
//        this.addOverlayItem(m);
//        this.e.setPositionNoAnimationTo(this.e
//                .getGeoPoint(new ScreenPoint(x, y)));
//        return true;
//    }
}
