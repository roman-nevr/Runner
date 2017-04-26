package org.berendeev.roma.runner.presentation.overlay;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;

import org.berendeev.roma.runner.R;

import ru.yandex.yandexmapkit.MapController;
import ru.yandex.yandexmapkit.MapView;
import ru.yandex.yandexmapkit.overlay.Overlay;
import ru.yandex.yandexmapkit.overlay.OverlayItem;
import ru.yandex.yandexmapkit.utils.ScreenPoint;


public class MyPathOverLay extends Overlay {
    MapView mMmapView;

    public MyPathOverLay(MapController controller, MapView mapView) {
        super(controller);
        mMmapView = mapView;
        this.setIRender(new TrackOverlayRender(this));
    }

    @Override
    public int compareTo(Object arg0) {
        return 0;
    }

    @Override
    public boolean onLongPress(float x, float y) {
        OverlayItem m = new OverlayItem(
                this.e.getGeoPoint(new ScreenPoint(x, y)),
                getDrawable());
        m.setOffsetY(-23);
        this.addOverlayItem(m);
        this.e.setPositionNoAnimationTo(this.e
                .getGeoPoint(new ScreenPoint(x, y)));
        return true;
    }

    private Drawable getDrawable() {
//        BitmapFactory.decodeResource(
//                this.e.getContext().getResources(),
//                R.drawable.ic_bookmark_black);
        return ContextCompat.getDrawable(this.e.getContext(), R.drawable.ic_bookmark_black);
    }
}
