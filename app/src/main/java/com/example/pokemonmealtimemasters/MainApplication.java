package com.example.pokemonmealtimemasters; // Your base package

import android.app.Application;
import androidx.lifecycle.ProcessLifecycleOwner;
import com.example.pokemonmealtimemasters.utils.AppLifecycleObserver;
import com.example.pokemonmealtimemasters.utils.MusicManager;

public class MainApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize MusicManager with application context
        MusicManager.getInstance().initialize(this);

        // Setup lifecycle observer
        AppLifecycleObserver lifecycleObserver = new AppLifecycleObserver();
        ProcessLifecycleOwner.get().getLifecycle().addObserver(lifecycleObserver);
    }
}