package com.example.pokemonmealtimemasters.utils; // Or your appropriate package

import androidx.annotation.NonNull;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;

/**
 * Lifecycle Observer class, used for managing music lifecycle.
 */
public class AppLifecycleObserver implements DefaultLifecycleObserver {

    private final MusicManager musicManager;

    public AppLifecycleObserver() {
        this.musicManager = MusicManager.getInstance();
    }

    @Override
    public void onResume(@NonNull LifecycleOwner owner) {
        musicManager.startMusic(); // Attempt to start/resume music
    }

    @Override
    public void onPause(@NonNull LifecycleOwner owner) {
        musicManager.pauseMusic(); // Pause music
    }

    @Override
    public void onDestroy(@NonNull LifecycleOwner owner) {
        musicManager.stopAndRelease();
        DefaultLifecycleObserver.super.onDestroy(owner);
    }
}