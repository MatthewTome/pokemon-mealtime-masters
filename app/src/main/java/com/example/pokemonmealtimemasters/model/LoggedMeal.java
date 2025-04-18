package com.example.pokemonmealtimemasters.model;

/**
 * Represents a meal logged by the user, storing its name, calorie count, and timestamp.
 */
public class LoggedMeal {
    private final String name;
    private final double calories;
    private final long timestamp;

    public LoggedMeal(String name, double calories, long timestamp) {
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