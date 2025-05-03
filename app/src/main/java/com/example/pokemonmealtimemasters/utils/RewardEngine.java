package com.example.pokemonmealtimemasters.utils;

import android.content.Context;
import com.example.pokemonmealtimemasters.network.PokemonTypeRepository;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Simple rule-based engine that assigns a Pokémon id
 * based on the calorie and protein content of a meal.
 * <p>
 * Type-aware: pulls current Fighting/Psychic rosters from PokeAPI.
 * (cached for the session in {@link PokemonTypeRepository}).
 */
public class RewardEngine {
    private static final List<String> BALANCED_REWARDS = Arrays.asList("1", "4", "7", "10", "25");

    /**
     * Computes the Pokédex-ID (as a String) of the reward the user should receive.
     *
     * @param context  any Android context (used only for cached PokeAPI networking)
     * @param calories meal calories
     * @param protein  meal protein (g)
     * @param caught   already-owned Pokémon IDs so we don’t repeat rewards
     *
     * @return Pokédex ID of the reward, or <code>null</code> if no reward
     */
    public static String computeReward(
            Context context,
            double calories,
            double protein,
            Set<String> caught) {

        // Basic nutrition gate: Needs at least 100 calories and some protein
        if (calories < 100 || protein < 5) {
            return null;
        }

        List<String> pool = new ArrayList<>();
        PokemonTypeRepository repo = PokemonTypeRepository.getInstance(context);

        // Rule: High protein increases chance of Fighting type
        if (protein >= 20) {
            pool.addAll(repo.getIdsForType("fighting"));
        }

        // Rule: Moderate protein and calories give chance for starters/balanced
        if (protein >= 10 && calories >= 200) {
            pool.addAll(BALANCED_REWARDS);
            for (int i = 1; i <= 50; i++) {
                pool.add(String.valueOf(i));
            }
        }

        // Base pool
        if (pool.isEmpty()) {
            for (int i = 1; i <= 151; i++) {
                pool.add(String.valueOf(i));
            }
        }

        // Remove duplicates
        pool = new ArrayList<>(new HashSet<>(pool));
        pool.removeAll(caught);

        if (pool.isEmpty()) {
            return null;
        }

        Collections.shuffle(pool);
        return pool.get(0);
    }
}