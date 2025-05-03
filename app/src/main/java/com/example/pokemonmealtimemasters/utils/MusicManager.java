package com.example.pokemonmealtimemasters.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.util.Log;
import com.example.pokemonmealtimemasters.R;

/**
 * Manages music player throughout the app.
 */
public class MusicManager {

    private static final String TAG = "MusicManager";
    private static final String PREFS_NAME = "app_prefs";
    private static final String KEY_MUSIC_ENABLED = "music_enabled";

    private static volatile MusicManager instance;
    private MediaPlayer mediaPlayer;
    private boolean isPrepared = false;
    private boolean isMusicEnabled = true;
    private Context appContext;

    private MusicManager() { }

    public static MusicManager getInstance() {
        if (instance == null) {
            synchronized (MusicManager.class) {
                if (instance == null) {
                    instance = new MusicManager();
                }
            }
        }
        return instance;
    }

    // Initialize with Application Context and load preference
    public void initialize(Context context) {
        if (this.appContext == null) {
            this.appContext = context.getApplicationContext();
            loadMusicPreference();
        }
    }

    // Load the music enabled/disabled preference
    private void loadMusicPreference() {
        if (appContext == null) return;
        SharedPreferences prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        isMusicEnabled = prefs.getBoolean(KEY_MUSIC_ENABLED, true);
    }

    // Save the music enabled/disabled preference
    public void setMusicEnabled(boolean enabled) {
        if (appContext == null) {
            return;
        }
        isMusicEnabled = enabled;
        SharedPreferences prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_MUSIC_ENABLED, enabled).apply();

        if (enabled) {
            startMusic();
        } else {
            pauseMusic();
        }
    }

    public boolean isMusicEnabled() {
        return isMusicEnabled;
    }

    public void startMusic() {
        if (appContext == null) {
            return;
        }
        if (!isMusicEnabled) {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                pauseMusicInternal();
            }
            return;
        }

        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            return;
        }

        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(appContext, R.raw.background_music); // Use appContext
            if (mediaPlayer == null) {
                return;
            }

            mediaPlayer.setLooping(true); // Loop the background music
            mediaPlayer.setVolume(0.5f, 0.5f); // Set initial volume (optional)

            mediaPlayer.setOnPreparedListener(mp -> {
                isPrepared = true;
                if (isMusicEnabled) { // Double-check preference before starting
                    mp.start();
                }
            });

            mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                isPrepared = false;
                release();
                return true;
            });

            if (mediaPlayer != null && isMusicEnabled) {
                try {
                    mediaPlayer.start();
                    isPrepared = true;
                } catch (IllegalStateException e) {
                    Log.e(TAG, "Error starting MediaPlayer immediately after create.", e);
                }
            }

        } else if (isMusicEnabled && !mediaPlayer.isPlaying()) {
            try {
                if (isPrepared) {
                    mediaPlayer.start();
                }
            } catch (IllegalStateException e) {
                release();
            }
        }
    }

    public void pauseMusic() {
        pauseMusicInternal();
    }

    // Internal pause to avoid checks/logging loops if called from setMusicEnabled
    private void pauseMusicInternal() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            try {
                mediaPlayer.pause();
            } catch (IllegalStateException e) {
                release();
            }
        }
    }

    public void stopAndRelease() {
        release();
    }

    private void release() {
        if (mediaPlayer != null) {
            try {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
                mediaPlayer.reset();
                mediaPlayer.release();
            } catch (Exception e) {
                Log.e(TAG, "Error during MediaPlayer release.", e);
            } finally {
                mediaPlayer = null;
                isPrepared = false;
            }
        }
    }
}