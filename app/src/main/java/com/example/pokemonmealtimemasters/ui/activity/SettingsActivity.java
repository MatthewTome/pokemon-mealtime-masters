package com.example.pokemonmealtimemasters.ui.activity;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.Button;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.example.pokemonmealtimemasters.R;
import com.google.android.material.appbar.MaterialToolbar;
import java.util.Objects;

/**
 * Settings screen where the user can reset all logged meals and daily nutrient totals.
 * Provides a toolbar with a back navigation icon and a single "Reset Today" button.
 */
public class SettingsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Configure toolbar with back navigation and black-colored icons/text
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        int black = ContextCompat.getColor(this, android.R.color.black);
        toolbar.setTitleTextColor(black);
        Drawable overflow = toolbar.getOverflowIcon();
        if (overflow != null) overflow.setTint(black);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        // Reset button clears shared prefs values for today and returns to MainActivity
        Button resetButton = findViewById(R.id.button_reset_today);
        resetButton.setOnClickListener(v -> {
            getSharedPreferences("prefs", MODE_PRIVATE)
                    .edit()
                    .putString("logged_meals", "")
                    .putFloat("daily_calories", 0f)
                    .putFloat("daily_protein",  0f)
                    .putFloat("daily_carbs",    0f)
                    .putInt(  "daily_vitamins", 0)
                    .apply();
            finish();
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}