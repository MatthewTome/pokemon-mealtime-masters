package com.example.pokemonmealtimemasters.model;

public class LoggedMeal
{
    private String name;
    private double calories;
    private long timestamp;

    public LoggedMeal(String name, double calories, long timestamp)
    {
        this.name      = name;
        this.calories  = calories;
        this.timestamp = timestamp;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public double getCalories()
    {
        return calories;
    }

    public void setCalories(double calories)
    {
        this.calories = calories;
    }

    public long getTimestamp()
    {
        return timestamp;
    }

    public void setTimestamp(long timestamp)
    {
        this.timestamp = timestamp;
    }
}