package com.example.pokemonmealtimemasters.ui;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentResultListener;
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
    private static final String PREFS_NAME        = "com.example.pokemonmealtimemasters.PREFS";
    private static final String KEY_LOGGED_MEALS  = "logged_meals";
    private static final String KEY_DAILY_CAL     = "daily_calories";
    private static final String KEY_DAILY_PROT    = "daily_protein";
    private static final String KEY_DAILY_SUGAR   = "daily_sugar";
    private static final String KEY_DAILY_VITAMIN = "daily_vitamins";

    private SharedPreferences prefs;
    private Gson gson;

    private List<LoggedMeal> loggedMeals;
    private LoggedMealsAdapter adapter;

    private double dailyCalories;
    private double dailyProtein;
    private double dailySugar;
    private int    dailyVitaminCount;

    private LinearProgressIndicator progCalories;
    private LinearProgressIndicator progProtein;
    private LinearProgressIndicator progSugar;
    private LinearProgressIndicator progVitamins;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs  = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        gson   = new Gson();

        // Load persisted meals & totals
        loggedMeals       = loadMeals();
        dailyCalories     = prefs.getFloat(KEY_DAILY_CAL, 0f);
        dailyProtein      = prefs.getFloat(KEY_DAILY_PROT, 0f);
        dailySugar        = prefs.getFloat(KEY_DAILY_SUGAR, 0f);
        dailyVitaminCount = prefs.getInt(KEY_DAILY_VITAMIN, 0);

        // Setup progress bars
        progCalories = findViewById(R.id.prog_calories);
        progProtein  = findViewById(R.id.prog_protein);
        progSugar    = findViewById(R.id.prog_sugar);
        progVitamins = findViewById(R.id.prog_vitamins);
        updateProgressBars();

        // Setup logged meals list
        RecyclerView recycler = findViewById(R.id.logged_meals_recycler);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new LoggedMealsAdapter(loggedMeals);
        recycler.setAdapter(adapter);

        // FAB to launch logging sheet
        FloatingActionButton fab = findViewById(R.id.fab_add_meal);
        fab.setOnClickListener(v ->
                new MealLoggingSheet().show(getSupportFragmentManager(), "MealLoggingSheet")
        );

        // Listen for meals logged from the sheet
        getSupportFragmentManager().setFragmentResultListener(
                "meal_logged", this,
                (requestKey, bundle) -> {
                    // Extract data
                    String name     = bundle.getString("name", "Unknown");
                    double cal      = bundle.getDouble("calories", 0.0);
                    double prot     = bundle.getDouble("protein",  0.0);
                    double sugar    = bundle.getDouble("sugar",    0.0);
                    boolean vits    = bundle.getBoolean("vitamins", false);
                    long timestamp  = bundle.getLong("timestamp", System.currentTimeMillis());

                    // Add to list & persist
                    loggedMeals.add(0, new LoggedMeal(name, cal, timestamp));
                    adapter.updateData(loggedMeals);
                    saveMeals(loggedMeals);

                    // Update and persist daily totals
                    dailyCalories     += cal;
                    dailyProtein      += prot;
                    dailySugar        += sugar;
                    if (vits) dailyVitaminCount++;
                    saveDailyTotals();

                    // Refresh UI
                    updateProgressBars();
                }
        );
    }

    private List<LoggedMeal> loadMeals() {
        String json = prefs.getString(KEY_LOGGED_MEALS, "");
        if (json.isEmpty()) return new ArrayList<>();
        Type listType = new TypeToken<List<LoggedMeal>>(){}.getType();
        return gson.fromJson(json, listType);
    }

    private void saveMeals(List<LoggedMeal> meals) {
        prefs.edit()
                .putString(KEY_LOGGED_MEALS, gson.toJson(meals))
                .apply();
    }

    private void saveDailyTotals() {
        prefs.edit()
                .putFloat(KEY_DAILY_CAL,     (float)dailyCalories)
                .putFloat(KEY_DAILY_PROT,    (float)dailyProtein)
                .putFloat(KEY_DAILY_SUGAR,   (float)dailySugar)
                .putInt(KEY_DAILY_VITAMIN,   dailyVitaminCount)
                .apply();
    }

    private void updateProgressBars() {
        // Example goals: 2000 cal, 50 g protein, 30 g sugar
        int calPct   = (int)(dailyCalories / 2000f * 100);
        int protPct  = (int)(dailyProtein  /   50f * 100);
        int sugarPct = (int)(dailySugar    /   30f * 100);
        int vitPct   = dailyVitaminCount > 0 ? 100 : 0;

        progCalories.setProgress(calPct,    /*animated=*/true);
        progProtein .setProgress(protPct,   true);
        progSugar   .setProgress(sugarPct,  true);
        progVitamins.setProgress(vitPct,    true);
    }
}