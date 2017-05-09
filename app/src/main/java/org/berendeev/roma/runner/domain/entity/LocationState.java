package org.berendeev.roma.runner.domain.entity;

import android.app.PendingIntent;
import android.content.Intent;

import com.google.auto.value.AutoValue;

import org.berendeev.roma.runner.data.LocationApiRepository;

import static org.berendeev.roma.runner.data.LocationApiRepository.State.notAvailable;

@AutoValue
public abstract class LocationState {

    public static LocationState DEFAULT = create(notAvailable, new Intent());

    public abstract LocationApiRepository.State state();

    public abstract Intent data();

    public static LocationState create(LocationApiRepository.State state, Intent data) {
        return builder()
                .state(state)
                .data(data)
                .build();
    }

    public abstract Builder toBuilder();

    public static Builder builder() {
        return new AutoValue_LocationState.Builder();
    }


    @AutoValue.Builder public abstract static class Builder {
        public abstract Builder state(LocationApiRepository.State state);

        public abstract Builder data(Intent data);

        public abstract LocationState build();
    }
}
