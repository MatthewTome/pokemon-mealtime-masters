package com.example.pokemonmealtimemasters.model;

/**
 * Data Model representing a meal logged by the user, storing its name, calorie count,
 * and timestamp. Utilizes Shared Preferences for local storage on the device.
 */
public class LoggedMealModel {
    private final String name;
    private final double calories;
    private final long timestamp;

    public LoggedMealModel(String name, double calories, long timestamp) {
        this.name = name;
        this.calories = calories;
        this.timestamp = timestamp;
    }

    public String getName() {
        return name;
    }

    public double getCalories() {
        return calories;
    }

    public long getTimestamp() {
        return timestamp;
    }
}