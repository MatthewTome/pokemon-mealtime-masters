package com.example.pokemonmealtimemasters.ui.activity;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.pokemonmealtimemasters.R;
import com.example.pokemonmealtimemasters.ui.adapter.PokemonAdapter;
import com.google.android.material.appbar.MaterialToolbar;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Hosts the “My Pokédex” screen, showing all Pokémon the user
 * has earned so far. Pulls a JSON list of caught Pokémon from
 * SharedPreferences, displays them in a 2-column grid, and
 * lets the user tap back to return to MainActivity.
 */
public class PokedexActivity extends AppCompatActivity {

    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pokedex);

        prefs = getSharedPreferences("prefs", MODE_PRIVATE);

        // Toolbar setup for back navigation
        MaterialToolbar toolbar = findViewById(R.id.pokedex_toolbar);
        toolbar.setTitle("My Pokédex");
        toolbar.setTitleTextColor(Color.BLACK);
        toolbar.setNavigationOnClickListener(v -> finish());
        toolbar.setNavigationOnClickListener(v -> finish()); // returns to MainActivity

        RecyclerView recyclerView = findViewById(R.id.pokedexRecycler);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshPokedex();
    }

    private void refreshPokedex() {
        RecyclerView recyclerView = findViewById(R.id.pokedexRecycler);

        Set<String> caughtPokemonIds = prefs.getStringSet("caught_pokemon_ids", new HashSet<>());
        List<String> allPokemonIds = getAllPokemonIds();

        PokemonAdapter adapter = new PokemonAdapter(allPokemonIds, caughtPokemonIds, this);
        recyclerView.setAdapter(adapter);
    }

    private List<String> getAllPokemonIds() {
        List<String> ids = new ArrayList<>();
        for (int i = 1; i <= 151; i++) {
            ids.add(String.valueOf(i));
        }
        return ids;
    }
}