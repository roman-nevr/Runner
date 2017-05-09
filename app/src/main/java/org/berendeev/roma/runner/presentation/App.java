package org.berendeev.roma.runner.presentation;

import android.app.Application;

import com.facebook.stetho.Stetho;

import org.berendeev.roma.runner.BuildConfig;
import org.berendeev.roma.runner.di.DaggerMainComponent;
import org.berendeev.roma.runner.di.MainComponent;
import org.berendeev.roma.runner.di.MainModule;

import timber.log.Timber;


public class App extends Application {

    private MainComponent mainComponent;

    private static App instance;

    public static App getInstance() {
        return instance;
    }

    @Override public void onCreate() {
        super.onCreate();
        instance = this;
        initDi();
        initTimber();
        initStetho();
    }

    private void initDi() {
        mainComponent = DaggerMainComponent.builder().mainModule(new MainModule(getApplicationContext())).build();
//        mainComponent.provideLocationApiRepository().connect();
    }

    private void initTimber(){
        if (BuildConfig.DEBUG) Timber.plant(new Timber.DebugTree());
    }

    public MainComponent getMainComponent() {
        return mainComponent;
    }

    private void initStetho(){
        if(!BuildConfig.DEBUG){
            return;
        }
        // Create an InitializerBuilder
        Stetho.InitializerBuilder initializerBuilder =
                Stetho.newInitializerBuilder(this);

        // Enable Chrome DevTools
        initializerBuilder.enableWebKitInspector(
                Stetho.defaultInspectorModulesProvider(this)
        );

        // Enable command line interface
        initializerBuilder.enableDumpapp(
                Stetho.defaultDumperPluginsProvider(this)
        );

        // Use the InitializerBuilder to generate an Initializer
        Stetho.Initializer initializer = initializerBuilder.build();

        // Initialize Stetho with the Initializer
        Stetho.initialize(initializer);
    }
}
