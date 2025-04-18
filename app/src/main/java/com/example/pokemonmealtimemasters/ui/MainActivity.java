package com.example.pokemonmealtimemasters.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pokemonmealtimemasters.R;
import com.example.pokemonmealtimemasters.model.LoggedMeal;
import com.example.pokemonmealtimemasters.ui.adapter.LoggedMealsAdapter;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String PREFS              = "prefs";
    private static final String KEY_LOGGED_MEALS   = "logged_meals";
    private static final String KEY_DAILY_CALORIES = "daily_calories";
    private static final String KEY_DAILY_PROTEIN  = "daily_protein";
    private static final String KEY_DAILY_SUGAR    = "daily_carbs";
    private static final String KEY_DAILY_VITAMIN  = "daily_vitamins";

    private SharedPreferences prefs;
    private Gson gson;

    private double dailyCalories;
    private double dailyProtein;
    private double dailyCarbs;
    private int    dailyVitaminCount;

    private LinearProgressIndicator progCalories;
    private LinearProgressIndicator progProtein;
    private LinearProgressIndicator progCarbs;
    private LinearProgressIndicator progVitamins;

    private List<LoggedMeal> loggedMeals;
    private LoggedMealsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MaterialToolbar topBar = findViewById(R.id.top_app_bar);
        setSupportActionBar(topBar);

        prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        gson  = new Gson();

        // load daily totals
        dailyCalories    = prefs.getFloat(KEY_DAILY_CALORIES, 0f);
        dailyProtein     = prefs.getFloat(KEY_DAILY_PROTEIN,  0f);
        dailyCarbs       = prefs.getFloat(KEY_DAILY_SUGAR,    0f);
        dailyVitaminCount= prefs.getInt(KEY_DAILY_VITAMIN,    0);

        progCalories = findViewById(R.id.prog_calories);
        progProtein  = findViewById(R.id.prog_protein);
        progCarbs    = findViewById(R.id.prog_carbs);
        progVitamins = findViewById(R.id.prog_vitamins);
        updateProgressBars();

        // load logged meals
        String json = prefs.getString(KEY_LOGGED_MEALS, "");
        if (json.isEmpty()) {
            loggedMeals = new ArrayList<>();
        } else {
            Type type = new TypeToken<List<LoggedMeal>>(){}.getType();
            loggedMeals = gson.fromJson(json, type);
        }

        RecyclerView rv = findViewById(R.id.logged_meals_recycler);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new LoggedMealsAdapter(loggedMeals);
        rv.setAdapter(adapter);

        FloatingActionButton fab = findViewById(R.id.fab_add_meal);
        fab.setOnClickListener(v ->
                new MealLoggingSheet()
                        .show(getSupportFragmentManager(), "MealLoggingSheet")
        );

        // Listen for the fragment result from NutritionDetailSheet
        getSupportFragmentManager().setFragmentResultListener(
                "meal_logged", this,
                (requestKey, bundle) -> {
                    String name  = bundle.getString("name","Custom");
                    double cal   = bundle.getDouble("calories",0);
                    double prot  = bundle.getDouble("protein",0);
                    double carbs = bundle.getDouble("carbs",0);
                    int    vit   = (int)bundle.getDouble("vitamins",0);

                    // add & persist
                    loggedMeals.add(0, new LoggedMeal(name, cal, System.currentTimeMillis()));
                    prefs.edit()
                            .putString(KEY_LOGGED_MEALS, gson.toJson(loggedMeals))
                            .apply();
                    adapter.updateData(loggedMeals);

                    // update totals & persist
                    dailyCalories    += cal;
                    dailyProtein     += prot;
                    dailyCarbs       += carbs;
                    dailyVitaminCount= Math.min(100, dailyVitaminCount + vit);
                    prefs.edit()
                            .putFloat(KEY_DAILY_CALORIES, (float)dailyCalories)
                            .putFloat(KEY_DAILY_PROTEIN,  (float)dailyProtein)
                            .putFloat(KEY_DAILY_SUGAR,    (float)dailyCarbs)
                            .putInt  (KEY_DAILY_VITAMIN,   dailyVitaminCount)
                            .apply();
                    updateProgressBars();
                }
        );
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        new MenuInflater(this).inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateProgressBars() {
        progCalories.setProgress((int)(dailyCalories/2000f*100), true);
        progProtein .setProgress((int)(dailyProtein/50f*100),   true);
        progCarbs   .setProgress((int)(dailyCarbs/30f*100),     true);
        progVitamins.setProgress(dailyVitaminCount,             true);
    }
}