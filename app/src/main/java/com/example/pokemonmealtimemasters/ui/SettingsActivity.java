package com.example.pokemonmealtimemasters.ui;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.pokemonmealtimemasters.R;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.Objects;

public class SettingsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_settings);

        MaterialToolbar settingsBar = findViewById(R.id.toolbar);
        setSupportActionBar(settingsBar);
        settingsBar.setTitleTextColor(getResources().getColor(android.R.color.black));
        Drawable ov = settingsBar.getOverflowIcon();
        if (ov != null) ov.setTint(getResources().getColor(android.R.color.black));
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);


        Button btnReset = findViewById(R.id.button_reset_today);
        btnReset.setOnClickListener(v -> {
            // clear today’s meals & totals
            getSharedPreferences("prefs", MODE_PRIVATE)
                    .edit()
                    .putString("logged_meals", "")
                    .putFloat("daily_calories", 0f)
                    .putFloat("daily_protein",  0f)
                    .putFloat("daily_carbs",    0f)
                    .putInt(  "daily_vitamins", 0)
                    .apply();
            finish(); // return to MainActivity
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}