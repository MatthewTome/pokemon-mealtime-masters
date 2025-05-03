package com.example.pokemonmealtimemasters.model;

/**
 * Data Model representing a meal logged by the user, storing its name, calorie count,
 * and timestamp. Utilizes Shared Preferences for local storage on the device.
 */
public record LoggedMealModel(String name, double calories, double protein, double totalSugars,
                              long timestamp) {
}