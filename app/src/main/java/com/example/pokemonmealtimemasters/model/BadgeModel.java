package com.example.pokemonmealtimemasters.model;

/**
 * Represents a single Badge that the user has earned.
 * Stores the id, title, description and date of earning.
 */
public class BadgeModel
{
    private final String id;
    private final String title;
    private final String description;
    private final long awardedAt;   // -1 → not earned yet

    public BadgeModel(String id, String title, String description, long awardedAt)
    {
        this.id = id;
        this.title = title;
        this.description = description;
        this.awardedAt = awardedAt;
    }

    public String  getId()         { return id; }
    public String  getTitle()      { return title; }
    public String  getDescription(){ return description; }
    public long    getAwardedAt()  { return awardedAt; }
    public boolean isEarned()      { return awardedAt >= 0; }
}