package com.example.pokemonmealtimemasters.network;

import android.content.Context;
import java.io.File;
import okhttp3.Cache;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Provides a singleton Retrofit client pre-configured to access the PokeAPI.
 * Uses a small on-disk cache so repeated type look-ups stay instant.
 */
public final class PokeApiClient
{
    private static final String BASE_URL = "https://pokeapi.co/api/v2/";
    private static Retrofit retrofit;

    private PokeApiClient()
    {
    }

    public static synchronized Retrofit getClient(Context ctx)
    {
        if (retrofit == null)
        {
            File cacheDir = new File(ctx.getCacheDir(), "pokeapi_cache");
            Cache cache = new Cache(cacheDir, 5 * 1024 * 1024); // 5 MB

            OkHttpClient ok = new OkHttpClient.Builder()
                    .cache(cache)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(ok)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}