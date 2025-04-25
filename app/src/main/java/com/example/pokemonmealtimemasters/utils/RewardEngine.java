package com.example.pokemonmealtimemasters.utils;

import android.content.Context;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Simple rule-based engine that assigns a Pokémon id
 * based on the calorie, protein, carb, and vitamin content of a meal.
 * <p>
 * Type-aware: pulls current Fighting/Psychic rosters from PokeAPI at runtime
 * (cached for the session in {@link com.example.pokemonmealtimemasters.network.PokemonTypeRepository}).
 */
public class RewardEngine
{
    // Starters + Pikachu stay hard-coded as the “balanced” reward pool
    private static final List<String> BALANCED_REWARDS = Arrays.asList("1", "4", "7", "10", "25");

    /**
     * Computes the Pokédex-ID (as a String) of the reward the user should receive.
     *
     * @param context  any Android context (used only for cached PokeAPI networking)
     * @param calories meal calories
     * @param protein  meal protein (g)
     * @param carbs    meal carbohydrates (g)
     * @param vitamins vitamin “score”
     * @param caught   already-owned Pokémon IDs so we don’t repeat rewards
     *
     * @return Pokédex ID of the reward, or <code>null</code> if no reward
     */
    public static String computeReward(
            Context context,
            double calories,
            double protein,
            double carbs,
            double vitamins,
            Set<String> caught)
    {
        if (!isNutritious(calories, protein, carbs, vitamins))
        {
            return null;     // No reward if meal fails nutrition gate
        }

        List<String> pool = new ArrayList<>();
        var repo = com.example.pokemonmealtimemasters.network.PokemonTypeRepository
                .getInstance(context);

        if (protein >= 20)
        {
            pool.addAll(repo.getIdsForType("fighting"));
        }

        if (vitamins >= 1)
        {
            pool.addAll(repo.getIdsForType("psychic"));
        }

        if (protein >= 15 && carbs >= 15)
        {
            pool.addAll(BALANCED_REWARDS);
            for (int i = 1; i <= 151; i++)
            {
                pool.add(String.valueOf(i));
            }
        }

        // Avoid duplicates + randomise
        pool.removeAll(caught);

        if (pool.isEmpty())
        {
            return null;
        }

        Collections.shuffle(pool);
        return pool.get(0);
    }

    private static boolean isNutritious(
            double calories,
            double protein,
            double carbs,
            double vitamins)
    {
        return calories >= 100 && (protein > 5 || carbs > 5 || vitamins > 5);
    }
}