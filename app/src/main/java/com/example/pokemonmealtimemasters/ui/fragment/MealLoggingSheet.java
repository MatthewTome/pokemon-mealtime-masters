package com.example.pokemonmealtimemasters.ui.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.pokemonmealtimemasters.BuildConfig;
import com.example.pokemonmealtimemasters.R;
import com.example.pokemonmealtimemasters.model.FoodSearchResponseModel;
import com.example.pokemonmealtimemasters.model.LoggedMealModel;
import com.example.pokemonmealtimemasters.network.ApiClient;
import com.example.pokemonmealtimemasters.network.FoodDataService;
import com.example.pokemonmealtimemasters.ui.adapter.LoggedMealsAdapter;
import com.example.pokemonmealtimemasters.ui.adapter.FoodSearchResponseAdapter;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Bottom sheet fragment that allows users to log a meal via three
 * modes: search by text, select from recent/preset lists, or custom entry.
 * It retrieves nutritional data from the FoodData Central API and
 * navigates to the NutritionDetailSheet for final entry.
 */
public class MealLoggingSheet extends BottomSheetDialogFragment {
    private EditText searchInput;
    private FoodSearchResponseAdapter searchAdapter;
    private FoodSearchResponseAdapter presetAdapter;
    private FoodDataService service;
    SharedPreferences prefs;
    Gson gson;

    private static final String PREFS_NAME       = "com.example.pokemonmealtimemasters.PREFS";
    private static final String KEY_LOGGED_MEALS = "logged_meals";

    private static final String[] PRESETS = {
            "McDONALD'S, QUARTER POUNDER with Cheese",
            "TACO BELL, Original Taco with beef, cheese and lettuce",
            "CRACKER BARREL, grilled sirloin steak",
            "Ice creams, BREYERS, 98% Fat Free Vanilla",
            "Chicken breast, rotisserie, skin not eaten"
    };

    public MealLoggingSheet() {
        setStyle(STYLE_NORMAL, R.style.Theme_MPM_BottomSheet);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.sheet_meal_logging, container, false);
    }

    /**
     * Initialize UI components, network service, and shared prefs.
     * Sets up search input, recent and preset lists, and custom button.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        MaterialToolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> dismiss());

        service = ApiClient.getClient(requireContext()).create(FoodDataService.class);
        prefs   = requireContext().getSharedPreferences(PREFS_NAME, 0);
        gson    = new Gson();

        // Nutrition tips
        String[] tips = {
                "Tip: High-protein meals may attract Fighting-type Pokémon!",
                "Tip: Vitamin-rich meals may attract Psychic-type Pokémon!",
                "Tip: Balanced meals might reward you with rare Pokémon!",
                "Tip: Junk food won’t earn rewards. Eat nutritious foods!"
        };
        TextView tipText = view.findViewById(R.id.text_nutrition_tip);
        tipText.setText(tips[new Random().nextInt(tips.length)]);

        // Search section
        searchInput   = view.findViewById(R.id.search_input);
        Button searchButton = view.findViewById(R.id.search_button);
        RecyclerView searchRecycler = view.findViewById(R.id.search_results_recycler);
        searchRecycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        searchAdapter = new FoodSearchResponseAdapter(new ArrayList<>());
        searchRecycler.setAdapter(searchAdapter);
        searchRecycler.setVisibility(View.GONE);
        searchAdapter.setOnItemClickListener(this::openDetail);

        searchButton.setOnClickListener(v -> {
            String query = searchInput.getText().toString().trim();
            if (query.isEmpty()) {
                Toast.makeText(requireContext(), "Enter a search term", Toast.LENGTH_SHORT).show();
                return;
            }
            service.searchFood(query, BuildConfig.FDC_API_KEY)
                    .enqueue(new Callback<>() {
                        @Override
                        public void onResponse(@NonNull Call<FoodSearchResponseModel> call,
                                               @NonNull Response<FoodSearchResponseModel> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                searchAdapter.updateData(response.body().getFoods());
                                searchRecycler.setVisibility(View.VISIBLE);
                            } else {
                                Toast.makeText(requireContext(), "No results", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<FoodSearchResponseModel> call, @NonNull Throwable t) {
                            Toast.makeText(requireContext(), "Network error", Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        // Custom entry button
        Button customBtn = view.findViewById(R.id.button_custom_food);
        customBtn.setOnClickListener(v -> openDetail(null));

        // Recent meals list
        RecyclerView recentRecycler = view.findViewById(R.id.recycler_recent);
        recentRecycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        LoggedMealsAdapter recentAdapter = new LoggedMealsAdapter(loadLoggedMeals());
        recentRecycler.setAdapter(recentAdapter);
        recentAdapter.setOnItemClickListener(meal ->
                service.searchFood(meal.name(), BuildConfig.FDC_API_KEY)
                        .enqueue(new Callback<>() {
                            @Override
                            public void onResponse(@NonNull Call<FoodSearchResponseModel> call,
                                                   @NonNull Response<FoodSearchResponseModel> response) {
                                if (response.isSuccessful() && response.body() != null
                                        && !response.body().getFoods().isEmpty()) {
                                    openDetail(response.body().getFoods().get(0));
                                }
                            }

                            @Override
                            public void onFailure(@NonNull Call<FoodSearchResponseModel> call, @NonNull Throwable t) {
                                // no-op
                            }
                        })
        );

        // Preset options list
        RecyclerView presetRecycler = view.findViewById(R.id.recycler_preset);
        presetRecycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        presetAdapter = new FoodSearchResponseAdapter(new ArrayList<>());
        presetRecycler.setAdapter(presetAdapter);
        presetAdapter.setOnItemClickListener(this::openDetail);
        loadPresetOptions();
    }

    @Override
    public void onStart() {
        super.onStart();
        View sheet = Objects.requireNonNull(getDialog())
                .findViewById(com.google.android.material.R.id.design_bottom_sheet);
        BottomSheetBehavior.from(sheet)
                .setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    private List<LoggedMealModel> loadLoggedMeals() {
        String json = prefs.getString(KEY_LOGGED_MEALS, "");
        if (json.isEmpty()) return new ArrayList<>();
        Type type = new TypeToken<List<LoggedMealModel>>(){}.getType();
        List<LoggedMealModel> all = gson.fromJson(json, type);
        return all.size() <= 5 ? all : all.subList(0, 5);
    }

    private void loadPresetOptions() {
        for (String query : PRESETS) {
            service.searchFood(query, BuildConfig.FDC_API_KEY)
                    .enqueue(new Callback<>() {
                        @Override
                        public void onResponse(@NonNull Call<FoodSearchResponseModel> call,
                                               @NonNull Response<FoodSearchResponseModel> response) {
                            if (response.isSuccessful() && response.body() != null
                                    && !response.body().getFoods().isEmpty()) {
                                List<FoodSearchResponseModel.FoodItem> current =
                                        new ArrayList<>(presetAdapter.getData());
                                current.add(response.body().getFoods().get(0));
                                presetAdapter.updateData(current);
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<FoodSearchResponseModel> call, @NonNull Throwable t) {
                        }
                    });
        }
    }

    // Open the NutritionDetailSheet for the chosen item (or null = custom).
    private void openDetail(FoodSearchResponseModel.FoodItem item) {
        NutritionDetailSheet.newInstance(item)
                .show(getParentFragmentManager(), "NutritionDetail");
        dismiss();
    }
}