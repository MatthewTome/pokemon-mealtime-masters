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
import com.example.pokemonmealtimemasters.model.LoggedMealModel;
import com.example.pokemonmealtimemasters.ui.adapter.LoggedMealsAdapter;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * MainActivity hosts the home screen where users see their daily progress
 * bars (calories, protein, carbs, vitamins) and a scrollable list of
 * meals logged for the current day. It also provides a FAB to add new meals
 * via the MealLoggingSheet.
 */
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

    private List<LoggedMealModel> loggedMealModels;
    private LoggedMealsAdapter adapter;

    /**
     * Initializes the toolbar, loads persisted data (daily totals and
     * logged meals), sets up RecyclerView and FAB listener, and registers
     * a FragmentResultListener to receive new meal entries.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Toolbar setup
        MaterialToolbar topBar = findViewById(R.id.top_app_bar);
        setSupportActionBar(topBar);

        // Preferences and JSON helper
        prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        gson  = new Gson();

        // Load the daily totals (or default to 0)
        dailyCalories    = prefs.getFloat(KEY_DAILY_CALORIES, 0f);
        dailyProtein     = prefs.getFloat(KEY_DAILY_PROTEIN,  0f);
        dailyCarbs       = prefs.getFloat(KEY_DAILY_SUGAR,    0f);
        dailyVitaminCount= prefs.getInt(KEY_DAILY_VITAMIN,    0);

        // Wire up progress bars
        progCalories = findViewById(R.id.prog_calories);
        progProtein  = findViewById(R.id.prog_protein);
        progCarbs    = findViewById(R.id.prog_carbs);
        progVitamins = findViewById(R.id.prog_vitamins);
        updateProgressBars();

        // Restore logged meals list
        String json = prefs.getString(KEY_LOGGED_MEALS, "");
        if (json.isEmpty()) {
            loggedMealModels = new ArrayList<>();
        } else {
            Type type = new TypeToken<List<LoggedMealModel>>(){}.getType();
            loggedMealModels = gson.fromJson(json, type);
        }

        // RecyclerView for logged meals
        RecyclerView rv = findViewById(R.id.logged_meals_recycler);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new LoggedMealsAdapter(loggedMealModels);
        rv.setAdapter(adapter);

        // Floating action button opens the meal logging sheet
        FloatingActionButton fab = findViewById(R.id.fab_add_meal);
        fab.setOnClickListener(v ->
                new MealLoggingSheet()
                        .show(getSupportFragmentManager(), "MealLoggingSheet")
        );

        // Receive results from NutritionDetailSheet when a meal is added
        getSupportFragmentManager().setFragmentResultListener(
                "meal_logged", this,
                (requestKey, bundle) -> {
                    String name  = bundle.getString("name","Custom");
                    double cal   = bundle.getDouble("calories",0);
                    double prot  = bundle.getDouble("protein",0);
                    double carbs = bundle.getDouble("carbs",0);
                    int    vit   = (int)bundle.getDouble("vitamins",0);

                    // Prepend and persist the new logged meal
                    loggedMealModels.add(0, new LoggedMealModel(name, cal, System.currentTimeMillis()));
                    prefs.edit()
                            .putString(KEY_LOGGED_MEALS, gson.toJson(loggedMealModels))
                            .apply();
                    adapter.updateData(loggedMealModels);

                    // Update and persist daily totals
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

    /**
     * Updates each of the four circular/linear progress bars on screen
     * by converting absolute totals to percent-of-goal values.
     */
    private void updateProgressBars() {
        progCalories.setProgress((int)(dailyCalories/2000f*100), true);
        progProtein .setProgress((int)(dailyProtein/50f*100),   true);
        progCarbs   .setProgress((int)(dailyCarbs/30f*100),     true);
        progVitamins.setProgress(dailyVitaminCount,             true);
    }
}