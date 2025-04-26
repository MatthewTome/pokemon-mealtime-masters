package com.example.pokemonmealtimemasters.ui.activity;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.pokemonmealtimemasters.R;
import com.example.pokemonmealtimemasters.model.BadgeModel;
import com.example.pokemonmealtimemasters.ui.adapter.BadgesAdapter;
import com.example.pokemonmealtimemasters.utils.BadgeUtils;
import com.google.android.material.appbar.MaterialToolbar;
import java.util.List;
import java.util.Map;

/**
 * Hosts the “My Badges” screen, showing all Pokémon the user
 * has earned so far. Pulls a JSON list of caught Pokémon from
 * SharedPreferences, displays them in a 2-column grid, and
 * lets the user tap back to return to MainActivity.
 */
public class BadgesActivity extends AppCompatActivity
{
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle state)
    {
        super.onCreate(state);
        setContentView(R.layout.activity_badges);

        prefs = getSharedPreferences("prefs", MODE_PRIVATE);

        MaterialToolbar bar = findViewById(R.id.badges_toolbar);
        bar.setTitle(R.string.my_badges);
        bar.setTitleTextColor(Color.BLACK);
        bar.setNavigationOnClickListener(v -> finish());

        RecyclerView rv = findViewById(R.id.badgesRecycler);
        rv.setLayoutManager(new GridLayoutManager(this, 3));
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        refreshBadges();
    }

    private void refreshBadges()
    {
        // Give the player any new badges they now qualify for.
        BadgeUtils.updateEarnedBadges(prefs);

        Map<String, Long> earned = BadgeUtils.getEarnedBadgeMap(prefs);
        List<BadgeModel> all     = BadgeUtils.getAllBadges(earned);

        RecyclerView rv = findViewById(R.id.badgesRecycler);
        rv.setAdapter(new BadgesAdapter(all, this));
    }
}