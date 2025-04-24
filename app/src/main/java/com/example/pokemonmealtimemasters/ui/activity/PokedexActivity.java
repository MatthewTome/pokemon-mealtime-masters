package com.example.pokemonmealtimemasters.ui.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.pokemonmealtimemasters.R;
import com.example.pokemonmealtimemasters.model.PokemonModel;
import com.example.pokemonmealtimemasters.ui.adapter.PokemonAdapter;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Hosts the “My Pokédex” screen, showing all Pokémon the user
 * has earned so far. Pulls a JSON list of caught Pokémon from
 * SharedPreferences, displays them in a 2-column grid, and
 * lets the user tap back to return to MainActivity.
 */
public class PokedexActivity extends AppCompatActivity {
    private static final String PREFS_NAME           = "prefs";
    private static final String KEY_CAUGHT_POKEMON   = "caught_pokemon";

    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pokedex);

        MaterialToolbar toolbar = findViewById(R.id.top_app_bar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        List<PokemonModel> caughtList = loadCaughtPokemon();

        RecyclerView pokedexRecycler = findViewById(R.id.recycler_pokedex);
        pokedexRecycler.setLayoutManager(new GridLayoutManager(this, 2));
        PokemonAdapter adapter = new PokemonAdapter(caughtList);
        pokedexRecycler.setAdapter(adapter);
    }

    /**
     * Reads the JSON-serialized list of PokemonModel from prefs.
     * If none exists yet, returns an empty list.
     */
    private List<PokemonModel> loadCaughtPokemon() {
        String json = prefs.getString(KEY_CAUGHT_POKEMON, "");
        if (json.isEmpty()) {
            return new ArrayList<>();
        }
        Type type = new TypeToken<List<PokemonModel>>(){}.getType();
        return new Gson().fromJson(json, type);
    }
}