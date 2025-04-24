package com.example.pokemonmealtimemasters.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Simple rule-based engine that assigns a Pokémon “buddy” key
 * based on the calorie, protein, and carb content of a meal.
 */
public class RewardEngine {

    private static final List<String> LEGENDARY_IDS = Arrays.asList("144", "145", "146", "150", "151");

    public static String computeReward(double cal, double prot, double carbs, Set<String> caughtPokemonIds) {
        List<String> availablePokemon = new ArrayList<>();

        for (int i = 1; i <= 151; i++) {
            String id = String.valueOf(i);
            if (!caughtPokemonIds.contains(id)) {
                if (LEGENDARY_IDS.contains(id)) {
                    if (caughtPokemonIds.size() >= 50) { // Example threshold
                        availablePokemon.add(id); // Legendary unlocked after catching 50 Pokémon
                    }
                } else {
                    availablePokemon.add(id);
                }
            }
        }

        if (availablePokemon.isEmpty()) {
            return "1"; // default fallback Pokémon if all caught
        }

        Collections.shuffle(availablePokemon);
        return availablePokemon.get(0);
    }
}