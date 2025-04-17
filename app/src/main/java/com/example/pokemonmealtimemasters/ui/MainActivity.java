package com.example.pokemonmealtimemasters.ui;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pokemonmealtimemasters.R;
import com.example.pokemonmealtimemasters.model.LoggedMeal;
import com.example.pokemonmealtimemasters.ui.adapter.LoggedMealsAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String PREFS_NAME           = "com.example.pokemonmealtimemasters.PREFS";
    private static final String KEY_LOGGED_MEALS     = "logged_meals";
    private static final String KEY_DAILY_CALORIES   = "daily_calories";
    private static final String KEY_DAILY_PROTEIN    = "daily_protein";
    private static final String KEY_DAILY_FATS       = "daily_fats";
    private static final String KEY_DAILY_CARBS      = "daily_carbs";
    private static final String KEY_DAILY_VITAMINS   = "daily_vitamins";

    private SharedPreferences prefs;
    private Gson gson;

    private List<LoggedMeal> loggedMeals;
    private LoggedMealsAdapter adapter;

    private double dailyCalories;
    private double dailyProtein;
    private double dailyFats;
    private double dailyCarbs;
    private int    dailyVitaminCount;

    private LinearProgressIndicator progCalories;
    private LinearProgressIndicator progProtein;
    private LinearProgressIndicator progFats;
    private LinearProgressIndicator progCarbs;
    private LinearProgressIndicator progVitamins;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        gson  = new Gson();

        loggedMeals       = loadMeals();
        dailyCalories     = prefs.getFloat(KEY_DAILY_CALORIES, 0f);
        dailyProtein      = prefs.getFloat(KEY_DAILY_PROTEIN,  0f);
        dailyFats         = prefs.getFloat(KEY_DAILY_FATS,     0f);
        dailyCarbs        = prefs.getFloat(KEY_DAILY_CARBS,    0f);
        dailyVitaminCount = prefs.getInt(KEY_DAILY_VITAMINS,   0);

        progCalories = findViewById(R.id.prog_calories);
        progProtein  = findViewById(R.id.prog_protein);
        progFats     = findViewById(R.id.prog_fats);
        progCarbs    = findViewById(R.id.prog_carbs);
        progVitamins = findViewById(R.id.prog_vitamins);
        updateProgressBars();

        RecyclerView recycler = findViewById(R.id.logged_meals_recycler);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new LoggedMealsAdapter(loggedMeals);
        recycler.setAdapter(adapter);

        FloatingActionButton fab = findViewById(R.id.fab_add_meal);
        fab.setOnClickListener(v ->
                new MealLoggingSheet().show(getSupportFragmentManager(), "MealLoggingSheet")
        );

        getSupportFragmentManager().setFragmentResultListener(
                "meal_logged", this,
                (requestKey, bundle) -> {
                    String name    = bundle.getString("name", "Unknown");
                    double cal     = bundle.getDouble("calories", 0.0);
                    double prot    = bundle.getDouble("protein",  0.0);
                    double fats    = bundle.getDouble("fats",     0.0);
                    double carbs   = bundle.getDouble("carbs",    0.0);
                    boolean vits   = bundle.getBoolean("vitamins", false);
                    long timestamp = bundle.getLong("timestamp", System.currentTimeMillis());

                    loggedMeals.add(0, new LoggedMeal(name, cal, timestamp));
                    adapter.updateData(loggedMeals);
                    saveMeals(loggedMeals);

                    dailyCalories     += cal;
                    dailyProtein      += prot;
                    dailyFats         += fats;
                    dailyCarbs        += carbs;
                    if (vits) dailyVitaminCount++;
                    saveDailyTotals();

                    updateProgressBars();
                }
        );
    }

    private List<LoggedMeal> loadMeals() {
        String json = prefs.getString(KEY_LOGGED_MEALS, "");
        if (json.isEmpty()) {
            return new ArrayList<>();
        }
        Type type = new TypeToken<List<LoggedMeal>>(){}.getType();
        return gson.fromJson(json, type);
    }

    private void saveMeals(List<LoggedMeal> meals) {
        prefs.edit()
                .putString(KEY_LOGGED_MEALS, gson.toJson(meals))
                .apply();
    }

    private void saveDailyTotals() {
        prefs.edit()
                .putFloat(KEY_DAILY_CALORIES,   (float)dailyCalories)
                .putFloat(KEY_DAILY_PROTEIN,    (float)dailyProtein)
                .putFloat(KEY_DAILY_FATS,       (float)dailyFats)
                .putFloat(KEY_DAILY_CARBS,      (float)dailyCarbs)
                .putInt(KEY_DAILY_VITAMINS,     dailyVitaminCount)
                .apply();
    }

    private void updateProgressBars() {
        int calPct   = (int)(dailyCalories / 2000f * 100);
        int protPct  = (int)(dailyProtein  /   50f * 100);
        int fatsPct  = (int)(dailyFats     /   70f * 100);
        int carbsPct = (int)(dailyCarbs    /  300f * 100);
        int vitPct   = dailyVitaminCount > 0 ? 100 : 0;

        progCalories.setProgress(calPct,    true);
        progProtein .setProgress(protPct,   true);
        progFats    .setProgress(fatsPct,   true);
        progCarbs   .setProgress(carbsPct,  true);
        progVitamins.setProgress(vitPct,    true);
    }
}