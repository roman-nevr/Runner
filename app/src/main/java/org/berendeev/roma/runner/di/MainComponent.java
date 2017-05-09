package org.berendeev.roma.runner.di;

import org.berendeev.roma.runner.data.LocationApiRepository;
import org.berendeev.roma.runner.domain.LocationHistoryRepository;

import javax.inject.Singleton;

import dagger.Component;

@Component(modules = MainModule.class)
@Singleton
public interface MainComponent {

    LocationApiRepository provideLocationApiRepository();

    LocationHistoryRepository provideLocationHistoryRepository();
}
