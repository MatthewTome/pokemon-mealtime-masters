package com.example.pokemonmealtimemasters.network;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * Minimal Retrofit interface to fetch a Pokémon’s data by its name.
 */
public interface PokeApiService {
    @GET("pokemon/{idOrName}")
    Call<Pokemon> getPokemon(@Path("idOrName") String idOrName);

    class Pokemon {
        public String name;
    }
}