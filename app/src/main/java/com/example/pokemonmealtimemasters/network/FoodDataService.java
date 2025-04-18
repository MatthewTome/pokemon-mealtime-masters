package com.example.pokemonmealtimemasters.network;

import com.example.pokemonmealtimemasters.model.FoodSearchResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Defines the Retrofit interface for querying the
 * USDA FoodData Central API's search endpoint.
 */
public interface FoodDataService {

    @GET("foods/search")
    Call<FoodSearchResponse> searchFood(
            @Query("query") String query,
            @Query("api_key") String apiKey
    );
}