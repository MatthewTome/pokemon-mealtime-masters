package com.example.pokemonmealtimemasters.ui;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.fragment.app.FragmentResultListener;

import com.example.pokemonmealtimemasters.R;
import com.example.pokemonmealtimemasters.model.LoggedMeal;
import com.example.pokemonmealtimemasters.ui.adapter.LoggedMealsAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String PREFS_NAME        = "com.example.pokemonmealtimemasters.PREFS";
    private static final String KEY_LOGGED_MEALS  = "logged_meals";

    private SharedPreferences prefs;
    private Gson gson;
    private List<LoggedMeal> loggedMeals;
    private LoggedMealsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs      = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        gson       = new Gson();
        loggedMeals = loadMeals();

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
                new FragmentResultListener() {
                    @Override
                    public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle bundle) {
                        String name      = bundle.getString("name", "Unknown");
                        double calories  = bundle.getDouble("calories", 0.0);
                        long timestamp   = bundle.getLong("timestamp", System.currentTimeMillis());

                        LoggedMeal newMeal = new LoggedMeal(name, calories, timestamp);
                        loggedMeals.add(0, newMeal);
                        adapter.updateData(loggedMeals);
                        saveMeals(loggedMeals);
                    }
                }
        );
    }

    private List<LoggedMeal> loadMeals() {
        String json = prefs.getString(KEY_LOGGED_MEALS, "");
        if (json.isEmpty()) {
            return new ArrayList<>();
        }
        Type listType = new TypeToken<List<LoggedMeal>>(){}.getType();
        return gson.fromJson(json, listType);
    }

    private void saveMeals(List<LoggedMeal> meals) {
        String json = gson.toJson(meals);
        prefs.edit()
                .putString(KEY_LOGGED_MEALS, json)
                .apply();
    }
}