package com.example.pokemonmealtimemasters.network;

import android.content.Context;
import java.io.File;
import okhttp3.Cache;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Provides a singleton Retrofit client pre-configured
 * to access the USDA FoodData Central API. Retrofit handles HTTP
 * requests and JSON deserialization (via GsonConverterFactory).
 * OkHttpClient performs the network calls and
 * applies a 10MB disk cache to improve performance and reduce
 * redundant network traffic.
 */
public class ApiClient {
    private static final String BASE_URL = "https://api.nal.usda.gov/fdc/v1/";

    private static Retrofit retrofit = null;

    public static Retrofit getClient(Context context) {
        if (retrofit == null) {
            File httpCacheDirectory = new File(
                    context.getCacheDir(),
                    "responses"
            );
            Cache cache = new Cache(httpCacheDirectory, 10 * 1024 * 1024);

            OkHttpClient client = new OkHttpClient.Builder()
                    .cache(cache)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}