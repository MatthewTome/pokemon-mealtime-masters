package com.example.pokemonmealtimemasters.utils;

import android.content.Context;
import android.util.Log; // Import Log

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
 * Type-aware: pulls current Fighting/Psychic rosters from PokeAPI at runtime
 * (cached for the session in {@link PokemonTypeRepository}).
 */
public class RewardEngine {
    // Starters + Pikachu stay hard-coded as the “balanced” reward pool
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
            Log.d("RewardEngine", "Meal too low in calories or protein for reward.");
            return null;
        }

        List<String> pool = new ArrayList<>();
        PokemonTypeRepository repo = PokemonTypeRepository.getInstance(context);

        // Rule: High protein increases chance of Fighting type
        if (protein >= 20) {
            Log.d("RewardEngine", "High protein detected, adding Fighting types.");
            try {
                pool.addAll(repo.getIdsForType("fighting"));
            } catch (Exception e) {
                Log.e("RewardEngine", "Error getting Fighting type IDs", e);
            }
        }

        // Rule: Moderate protein and calories give chance for starters/balanced
        if (protein >= 10 && calories >= 200) {
            Log.d("RewardEngine", "Balanced meal detected, adding starters and general pool.");
            pool.addAll(BALANCED_REWARDS);
            // Add a wider range of common Pokémon for variety
            for (int i = 1; i <= 50; i++) { // Example: first 50 Pokemon
                pool.add(String.valueOf(i));
            }
        }

        // Base pool
        if (pool.isEmpty()) {
            Log.d("RewardEngine", "No specific rules met, adding general pool.");
            for (int i = 1; i <= 151; i++) {
                pool.add(String.valueOf(i));
            }
        }

        // Remove duplicates
        pool = new ArrayList<>(new HashSet<>(pool));
        pool.removeAll(caught);

        if (pool.isEmpty()) {
            Log.w("RewardEngine", "No available Pokémon left to award from the pool (all caught or pool empty).");
            return null;
        }

        Collections.shuffle(pool);
        Log.i("RewardEngine", "Awarding Pokémon ID: " + pool.get(0) + " from pool of size " + pool.size());
        return pool.get(0);
    }
}