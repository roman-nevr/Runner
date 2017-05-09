package org.berendeev.roma.runner.di;

import android.content.Context;

import org.berendeev.roma.runner.data.LocationApiRepository;
import org.berendeev.roma.runner.data.preferences.PreferencesDataSource;
import org.berendeev.roma.runner.data.sqlite.DatabaseOpenHelper;
import org.berendeev.roma.runner.data.history.LocationHistoryRepositoryImpl;
import org.berendeev.roma.runner.data.sqlite.LocationDataSource;
import org.berendeev.roma.runner.domain.LocationHistoryRepository;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class MainModule {

    private Context context;

    public MainModule(Context context) {
        this.context = context;
    }

    @Provides
    @Singleton
    public Context provideContext(){
        return context;
    }

    @Provides
    @Singleton
    public LocationApiRepository provideLocationApiRepository(Context context){
        return new LocationApiRepository(context);
    }

    @Provides
    @Singleton
    public DatabaseOpenHelper provideDatabaseOpenHelper(Context context){
        return new DatabaseOpenHelper(context);
    }

    @Provides
    @Singleton
    public LocationHistoryRepository provideLocationHistoryRepository(LocationDataSource locationDataSource, PreferencesDataSource preferencesDataSource){
        return new LocationHistoryRepositoryImpl(locationDataSource, preferencesDataSource);
    }

    @Provides
    @Singleton
    public LocationDataSource provideLocationDataSource(DatabaseOpenHelper openHelper){
        return new LocationDataSource(openHelper);
    }

    @Provides
    @Singleton
    public PreferencesDataSource providePreferencesDataSource(Context context){
        return new PreferencesDataSource(context);
    }
}
