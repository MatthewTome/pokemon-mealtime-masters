package com.example.pokemonmealtimemasters.network;

import com.example.pokemonmealtimemasters.model.FoodSearchResponseModel;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Defines the Retrofit interface for querying the
 * USDA FoodData Central API's search endpoint.
 */
public interface FoodDataService {

    @GET("foods/search")
    Call<FoodSearchResponseModel> searchFood(
            @Query("query") String query,
            @Query("api_key") String apiKey
    );
}