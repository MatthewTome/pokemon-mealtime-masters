package com.example.pokemonmealtimemasters.utils;

/**
 * Simple rule-based engine that assigns a Pokémon “buddy” key
 * based on the calorie, protein, and carb content of a meal.
 */
public class RewardEngine {
    /**
     * @param calories total kcal of the meal
     * @param protein  total grams of protein
     * @param carbs    total grams of carbs
     * @return a string key for which buddy to award
     */
    public static String computeReward(double calories, double protein, double carbs) {
        // a small set of example rules:
        if (calories <= 300 && protein >= 15 && carbs <= 40) {
            return "pikachu";
        } else if (protein >= 20) {
            return "charmander";
        } else if (carbs <= 30) {
            return "squirtle";
        } else {
            return "bulbasaur";
        }
    }
}