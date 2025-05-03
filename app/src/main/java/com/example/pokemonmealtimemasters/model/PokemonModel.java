package com.example.pokemonmealtimemasters.model;

import java.io.Serializable;

/**
 * Represents a single Pokémon the user has earned.
 * Stores its name and the URL of its sprite image.
 */
public record PokemonModel(String name, String spriteUrl) implements Serializable {
}