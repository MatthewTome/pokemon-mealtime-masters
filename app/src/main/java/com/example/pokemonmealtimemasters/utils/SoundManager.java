package com.example.pokemonmealtimemasters.utils;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.util.Log;
import com.example.pokemonmealtimemasters.R;

/**
 * SoundManager class for managing sound effects.
 */
public class SoundManager {

    public enum Sound {
        BUTTON_CLICK,
        MEAL_LOGGED,
        REWARD_RECEIVED,
        POPUP_OPEN,
    }

    private SoundPool soundPool;
    private final Context context;
    private boolean soundEnabled = true;
    private final SparseIntArray soundMap = new SparseIntArray();


    public SoundManager(Context context) {
        this.context = context.getApplicationContext();
        loadSounds();
    }

    private void loadSounds() {
        AudioAttributes attributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME) // Appropriate usage type
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        soundPool = new SoundPool.Builder()
                .setAudioAttributes(attributes)
                .setMaxStreams(5)
                .build();

        // Load sounds and store their IDs
        soundMap.put(Sound.BUTTON_CLICK.ordinal(), soundPool.load(context, R.raw.popup_open_sound, 1));
        soundMap.put(Sound.MEAL_LOGGED.ordinal(), soundPool.load(context, R.raw.meal_log_sound, 1));
        soundMap.put(Sound.REWARD_RECEIVED.ordinal(), soundPool.load(context, R.raw.reward_sound, 1));
        soundMap.put(Sound.POPUP_OPEN.ordinal(), soundPool.load(context, R.raw.popup_open_sound, 1));

        soundPool.setOnLoadCompleteListener((soundPool, sampleId, status) -> {
            if (status != 0) {
                Log.e("SoundManager", "Error loading sound ID: " + sampleId + ", Status: " + status);
            } else {
                Log.d("SoundManager", "Loaded sound ID: " + sampleId);
            }
        });
    }

    public void playSound(Sound sound) {
        if (!soundEnabled || soundPool == null) {
            return;
        }

        int soundId = soundMap.get(sound.ordinal(), -1); // Get sound ID using enum ordinal as key
        if (soundId != -1) {
            soundPool.play(soundId, 1.0f, 1.0f, 1, 0, 1.0f); // leftVolume, rightVolume, priority, loop, rate
        } else {
            Log.w("SoundManager", "Sound not found or loaded for: " + sound.name());
        }
    }

    public void setSoundEnabled(boolean enabled) {
        this.soundEnabled = enabled;
        if (!enabled) {
            soundPool.autoPause(); // Pause all streams if disabled
        } else {
            soundPool.autoResume(); // Resume streams if re-enabled
        }
    }

    // Release resources when activity/app is destroyed
    public void release() {
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
    }

    private static class SparseIntArray {
        private final android.util.SparseIntArray internalMap = new android.util.SparseIntArray();
        public void put(int key, int value) { internalMap.put(key, value); }
        public int get(int key, int defaultValue) { return internalMap.get(key, defaultValue); }
    }
}