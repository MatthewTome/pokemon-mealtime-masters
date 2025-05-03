package com.example.pokemonmealtimemasters.model;

/**
 * Represents a single Badge that the user has earned.
 * Stores the id, title, description and date of earning.
 *
 * @param awardedAt -1 → not earned yet
 */
public record BadgeModel(String id, String title, String description, long awardedAt) {
    public boolean isEarned() {
        return awardedAt >= 0;
    }
}