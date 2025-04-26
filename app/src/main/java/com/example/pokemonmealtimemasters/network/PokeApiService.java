package com.example.pokemonmealtimemasters.network;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * Defines the Retrofit interface for querying the
 * PokeAPI's search endpoint.
 */
public interface PokeApiService
{
    @GET("pokemon/{idOrName}")
    Call<Pokemon> getPokemon(@Path("idOrName") String idOrName);

    class Pokemon
    {
        public int id;
        public String name;
        public List<TypeSlot> types;

        public static class TypeSlot
        {
            public NamedResource type;
        }

        public static class NamedResource
        {
            public String name;
            public String url;
        }
    }

    @GET("type/{typeName}")
    Call<TypeResponse> getType(@Path("typeName") String typeName);

    class TypeResponse
    {
        public int id;
        public String name;
        public List<PokemonEntry> pokemon;

        public static class PokemonEntry
        {
            public PokemonResource pokemon;
        }

        public static class PokemonResource
        {
            public String name;
            public String url;    // “.../pokemon/68/”
        }
    }
}