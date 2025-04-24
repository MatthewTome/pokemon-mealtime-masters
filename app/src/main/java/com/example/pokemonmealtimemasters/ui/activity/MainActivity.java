package com.example.pokemonmealtimemasters.ui.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.pokemonmealtimemasters.R;
import com.example.pokemonmealtimemasters.model.LoggedMealModel;
import com.example.pokemonmealtimemasters.ui.adapter.LoggedMealsAdapter;
import com.example.pokemonmealtimemasters.ui.fragment.MealLoggingSheet;
import com.example.pokemonmealtimemasters.ui.fragment.RewardSheet;
import com.example.pokemonmealtimemasters.utils.RewardEngine;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import coil.Coil;
import coil.ImageLoader;
import coil.request.ImageRequest;

public class MainActivity extends AppCompatActivity {
    private static final String PREFS                = "prefs";
    private static final String KEY_LOGGED_MEALS     = "logged_meals";
    private static final String KEY_DAILY_CALORIES   = "daily_calories";
    private static final String KEY_DAILY_PROTEIN    = "daily_protein";
    private static final String KEY_DAILY_CARBS      = "daily_carbs";
    private static final String KEY_DAILY_VITAMINS   = "daily_vitamins";
    private static final String KEY_LAST_POKEMON     = "last_pokemon";

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // preferences & JSON helper
        prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        gson  = new Gson();

        // show the last earned Pokémon sprite
        ImageView sprite = findViewById(R.id.image_pokemon_sprite);
        String lastId = prefs.getString(KEY_LAST_POKEMON, null);
        if (lastId != null) {
            String url = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/"
                    + lastId + ".png";
            ImageLoader loader = Coil.imageLoader(this);
            ImageRequest req = new ImageRequest.Builder(this)
                    .data(url)
                    .placeholder(R.drawable.ic_placeholder)
                    .crossfade(true)
                    .target(sprite)
                    .build();
            loader.enqueue(req);
        }

        // load daily progress totals
        dailyCalories    = prefs.getFloat(KEY_DAILY_CALORIES, 0f);
        dailyProtein     = prefs.getFloat(KEY_DAILY_PROTEIN,  0f);
        dailyCarbs       = prefs.getFloat(KEY_DAILY_CARBS,    0f);
        dailyVitaminCount= prefs.getInt(KEY_DAILY_VITAMINS,  0);

        progCalories = findViewById(R.id.prog_calories);
        progProtein  = findViewById(R.id.prog_protein);
        progCarbs    = findViewById(R.id.prog_carbs);
        progVitamins = findViewById(R.id.prog_vitamins);
        updateProgressBars();

        // restore the list of today's logged meals
        String json = prefs.getString(KEY_LOGGED_MEALS, "");
        if (json.isEmpty()) {
            loggedMealModels = new ArrayList<>();
        } else {
            Type t = new TypeToken<List<LoggedMealModel>>(){}.getType();
            loggedMealModels = gson.fromJson(json, t);
        }

        RecyclerView rv = findViewById(R.id.logged_meals_recycler);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new LoggedMealsAdapter(loggedMealModels);
        rv.setAdapter(adapter);

        // FAB opens the meal-logging sheet
        FloatingActionButton fab = findViewById(R.id.fab_add_meal);
        fab.setOnClickListener(v ->
                new MealLoggingSheet()
                        .show(getSupportFragmentManager(), "MealLoggingSheet")
        );

        // listen for the result from the detail sheet
        getSupportFragmentManager().setFragmentResultListener(
                "meal_logged", this,
                (key, bundle) -> {
                    String name  = bundle.getString("name","Custom");
                    double cal   = bundle.getDouble("calories",0);
                    double prot  = bundle.getDouble("protein",0);
                    double carbs = bundle.getDouble("carbs",0);
                    int    vit   = (int)bundle.getDouble("vitamins",0);

                    // prepend and save the new meal
                    loggedMealModels.add(0,
                            new LoggedMealModel(name, cal, System.currentTimeMillis())
                    );
                    prefs.edit()
                            .putString(KEY_LOGGED_MEALS, gson.toJson(loggedMealModels))
                            .apply();
                    adapter.updateData(loggedMealModels);

                    // update totals and persist
                    dailyCalories    += cal;
                    dailyProtein     += prot;
                    dailyCarbs       += carbs;
                    dailyVitaminCount= Math.min(100, dailyVitaminCount + vit);
                    prefs.edit()
                            .putFloat(KEY_DAILY_CALORIES, (float)dailyCalories)
                            .putFloat(KEY_DAILY_PROTEIN,  (float)dailyProtein)
                            .putFloat(KEY_DAILY_CARBS,    (float)dailyCarbs)
                            .putInt  (KEY_DAILY_VITAMINS, dailyVitaminCount)
                            .apply();
                    updateProgressBars();

                    // compute and persist the new Pokémon reward
                    String pokedexId = RewardEngine.computeReward(cal, prot, carbs);
                    prefs.edit().putString(KEY_LAST_POKEMON, pokedexId).apply();

                    // immediately reload its sprite
                    String newUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/"
                            + pokedexId + ".png";
                    ImageRequest reloadReq = new ImageRequest.Builder(this)
                            .data(newUrl)
                            .placeholder(R.drawable.ic_placeholder)
                            .crossfade(true)
                            .target(sprite)
                            .build();
                    Coil.imageLoader(this).enqueue(reloadReq);

                    // show the celebration sheet
                    RewardSheet.newInstance(pokedexId)
                            .show(getSupportFragmentManager(), "RewardSheet");
                }
        );
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate your main_menu.xml so the pokédex icon appears at runtime
//        new MenuInflater(this).inflate(R.menu.main_menu, menu);
//        return true;
//    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // handle pokédex tap
//        if (item.getItemId() == R.id.action_pokedex) {
//            startActivity(new Intent(this, PokedexActivity.class));
//            return true;
//        }
//        return super.onOptionsItemSelected(item);
//    }

    private void updateProgressBars() {
        progCalories.setProgress((int)(dailyCalories/2000f*100), true);
        progProtein .setProgress((int)(dailyProtein/50f*100),   true);
        progCarbs   .setProgress((int)(dailyCarbs/30f*100),     true);
        progVitamins.setProgress(dailyVitaminCount,             true);
    }
}