package org.berendeev.roma.runner.domain.entity;

import android.app.PendingIntent;
import android.location.Location;

import com.google.auto.value.AutoValue;

import org.berendeev.roma.runner.data.LocationApiRepository;

import static org.berendeev.roma.runner.domain.entity.LocationInfo.LocationStatus.notAvailable;

@AutoValue
public abstract class LocationInfo {

    public static LocationInfo DEFAULT = create(null, notAvailable, null);

    public abstract Location location();
    public abstract LocationStatus status();

    public abstract PendingIntent resolution();

    public static LocationInfo create(Location location, LocationStatus status, PendingIntent resolution) {
        return builder()
                .location(location)
                .status(status)
                .resolution(resolution)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_LocationInfo.Builder();
    }

    public enum LocationStatus {
        ok, notAvailable, requestPermissions, requestResolution, notAvailableAlways, disconnected
    }


    @AutoValue.Builder public abstract static class Builder {
        public abstract Builder location(Location location);

        public abstract Builder status(LocationStatus status);

        public abstract Builder resolution(PendingIntent resolution);

        public abstract LocationInfo build();
    }
}
