package org.berendeev.roma.runner.data.history;

import android.location.Location;

import org.berendeev.roma.runner.data.preferences.PreferencesDataSource;
import org.berendeev.roma.runner.data.sqlite.LocationDataSource;
import org.berendeev.roma.runner.domain.LocationHistoryRepository;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Observable;

public class LocationHistoryRepositoryImpl implements LocationHistoryRepository {

    private LocationDataSource locationDataSource;
    private PreferencesDataSource preferencesDataSource;
    private Location lastLocation;
    private float distance;

    public LocationHistoryRepositoryImpl(LocationDataSource locationDataSource, PreferencesDataSource preferencesDataSource) {
        this.locationDataSource = locationDataSource;
        this.preferencesDataSource = preferencesDataSource;
        lastLocation = preferencesDataSource.getLastLocation();
        distance = preferencesDataSource.getDistance();
    }

    @Override public Completable saveLocation(Location location) {
        return Completable.fromAction(() -> {
            if(location.distanceTo(lastLocation) > distance){
                locationDataSource.saveLocation(location);
                lastLocation = location;
            }
        });
    }

    @Override public Observable<List<Location>> getLocations() {
        return Observable.fromCallable(() -> locationDataSource.getLocations());
    }

    @Override public Completable clearHistory() {
        return Completable.fromAction(() -> {
            locationDataSource.clearHistory();
        });
    }

    @Override public Completable setMaxDistance(float distance) {
        return Completable.fromAction(() -> {
            this.distance = distance;
            preferencesDataSource.saveDistance(distance);
        });
    }
}
