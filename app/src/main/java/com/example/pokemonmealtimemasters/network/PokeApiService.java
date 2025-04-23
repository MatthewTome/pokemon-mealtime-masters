package com.example.pokemonmealtimemasters.network;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * Minimal Retrofit interface to fetch a Pokémon’s data by its name.
 */
public interface PokeApiService {
    @GET("pokemon/{name}")
    Call<Pokemon> getPokemon(@Path("name") String name);

    class Pokemon {
        public String name;
        public Sprites sprites;

        public static class Sprites {
            public String front_default;
        }
    }
}