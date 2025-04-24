package com.example.pokemonmealtimemasters.model;

import java.io.Serializable;

/**
 * Represents a single Pokémon the user has earned.
 * Stores its name and the URL of its sprite image.
 */
public class PokemonModel implements Serializable {
    private final String name;
    private final String spriteUrl;

    public PokemonModel(String name, String spriteUrl) {
        this.name = name;
        this.spriteUrl = spriteUrl;
    }

    public String getName() {
        return name;
    }

    public String getSpriteUrl() {
        return spriteUrl;
    }
}