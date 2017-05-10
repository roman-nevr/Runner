package org.berendeev.roma.runner.di;

import org.berendeev.roma.runner.data.LocationApiRepository;
import org.berendeev.roma.runner.domain.LocationHistoryRepository;
import org.berendeev.roma.runner.presentation.fragment.ServiceControlFragment;

import javax.inject.Singleton;

import dagger.Component;

@Component(modules = MainModule.class)
@MyScope
public interface MainComponent {

    LocationApiRepository provideLocationApiRepository();

    LocationHistoryRepository provideLocationHistoryRepository();

    void inject(ServiceControlFragment fragment);
}
