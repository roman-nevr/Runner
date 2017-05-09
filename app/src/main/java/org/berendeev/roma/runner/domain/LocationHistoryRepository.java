package org.berendeev.roma.runner.domain;

import android.location.Location;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Observable;

public interface LocationHistoryRepository {
    Completable saveLocation(Location location);

    Observable<List<Location>> getLocations();

    Completable clearHistory();

    Completable setMaxDistance(float distance);
}
