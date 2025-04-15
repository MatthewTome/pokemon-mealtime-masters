package com.example.pokemonmealtimemasters.ui;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pokemonmealtimemasters.BuildConfig;
import com.example.pokemonmealtimemasters.R;
import com.example.pokemonmealtimemasters.model.FoodSearchResponse;
import com.example.pokemonmealtimemasters.network.ApiClient;
import com.example.pokemonmealtimemasters.network.FoodDataService;
import com.example.pokemonmealtimemasters.ui.adapter.MealAdapter;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;

public class MealLoggingActivity extends AppCompatActivity {
    private EditText searchInput;
    private MealAdapter mealAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meal_logging);

        searchInput = findViewById(R.id.search_input);
        Button searchButton = findViewById(R.id.search_button);
        EditText manualInput = findViewById(R.id.manual_input);
        RecyclerView resultsRecycler = findViewById(R.id.search_results_recycler);

        // RecyclerView with a simple adapter and vertical layout manager.
        resultsRecycler.setLayoutManager(new LinearLayoutManager(this));
        mealAdapter = new MealAdapter(new ArrayList<>());
        resultsRecycler.setAdapter(mealAdapter);

        // Set the search button listener to trigger the API call
        searchButton.setOnClickListener(v -> {
            String query = searchInput.getText().toString().trim();
            if (!query.isEmpty()) {
                performMealSearch(query);
            } else {
                Toast.makeText(MealLoggingActivity.this, "Please enter a search query.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void performMealSearch(String query) {
        final String apiKey = BuildConfig.FDC_API_KEY;

        FoodDataService service = ApiClient.getClient(this).create(FoodDataService.class);
        Call<FoodSearchResponse> call = service.searchFood(query, apiKey);

        call.enqueue(new retrofit2.Callback<>() {
            @Override
            public void onResponse(@NonNull Call<FoodSearchResponse> call, @NonNull retrofit2.Response<FoodSearchResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<FoodSearchResponse.FoodItem> foodItems = response.body().getFoods();
                    mealAdapter.updateData(foodItems);
                } else {
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "No error body";
                        int errorCode = response.code();
                        String errorMessage = response.message();
                        android.util.Log.e("MealLoggingActivity",
                                "API error code: " + errorCode +
                                        "\nMessage: " + errorMessage +
                                        "\nError Body: " + errorBody);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(MealLoggingActivity.this, "API Error: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<FoodSearchResponse> call, @NonNull Throwable t) {
                Toast.makeText(MealLoggingActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                t.printStackTrace();
            }
        });
    }
}