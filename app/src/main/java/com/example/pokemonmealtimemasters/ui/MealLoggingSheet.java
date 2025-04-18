package com.example.pokemonmealtimemasters.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pokemonmealtimemasters.BuildConfig;
import com.example.pokemonmealtimemasters.R;
import com.example.pokemonmealtimemasters.model.FoodSearchResponse;
import com.example.pokemonmealtimemasters.model.LoggedMeal;
import com.example.pokemonmealtimemasters.network.ApiClient;
import com.example.pokemonmealtimemasters.network.FoodDataService;
import com.example.pokemonmealtimemasters.ui.adapter.LoggedMealsAdapter;
import com.example.pokemonmealtimemasters.ui.adapter.MealAdapter;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MealLoggingSheet extends BottomSheetDialogFragment {
    private EditText searchInput;
    private MealAdapter searchAdapter, presetAdapter;
    private FoodDataService service;
    private SharedPreferences prefs;
    private Gson gson;

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

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inf,
                             @Nullable ViewGroup parent,
                             @Nullable Bundle saved) {
        return inf.inflate(R.layout.sheet_meal_logging, parent, false);
    }

    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle saved) {
        MaterialToolbar tb = view.findViewById(R.id.toolbar);
        tb.setNavigationOnClickListener(b -> dismiss());

        service = ApiClient.getClient(requireContext())
                .create(FoodDataService.class);
        prefs   = requireContext()
                .getSharedPreferences(PREFS_NAME, 0);
        gson    = new Gson();

        // --- SEARCH SETUP ---
        searchInput = view.findViewById(R.id.search_input);
        Button searchButton = view.findViewById(R.id.search_button);
        RecyclerView searchRecycler = view.findViewById(R.id.search_results_recycler);
        searchRecycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        searchAdapter = new MealAdapter(new ArrayList<>());
        searchRecycler.setAdapter(searchAdapter);
        searchRecycler.setVisibility(View.GONE);
        searchAdapter.setOnItemClickListener(this::openDetail);

        searchButton.setOnClickListener(b -> {
            String q = searchInput.getText().toString().trim();
            if (q.isEmpty()) {
                Toast.makeText(requireContext(), "Enter a search term", Toast.LENGTH_SHORT).show();
                return;
            }
            service.searchFood(q, BuildConfig.FDC_API_KEY)
                    .enqueue(new Callback<>() {
                        @Override
                        public void onResponse(@NonNull Call<FoodSearchResponse> c, @NonNull Response<FoodSearchResponse> r) {
                            if (r.isSuccessful() && r.body() != null) {
                                searchAdapter.updateData(r.body().getFoods());
                                searchRecycler.setVisibility(View.VISIBLE);
                            } else {
                                Toast.makeText(requireContext(), "No results", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<FoodSearchResponse> c, @NonNull Throwable t) {
                            Toast.makeText(requireContext(), "Network error", Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        // --- CUSTOM FOOD ---
        Button custom = view.findViewById(R.id.button_custom_food);
        custom.setOnClickListener(b -> openDetail(null));

        // --- RECENT MEALS ---
        RecyclerView recent = view.findViewById(R.id.recycler_recent);
        recent.setLayoutManager(new LinearLayoutManager(requireContext()));
        LoggedMealsAdapter recentAdapter = new LoggedMealsAdapter(loadLoggedMeals());
        recent.setAdapter(recentAdapter);
        recentAdapter.setOnItemClickListener(m -> service.searchFood(m.getName(), BuildConfig.FDC_API_KEY)
                .enqueue(new Callback<>() {
                    @Override
                    public void onResponse(@NonNull Call<FoodSearchResponse> c, @NonNull Response<FoodSearchResponse> r) {
                        if (r.isSuccessful() && r.body() != null && !r.body().getFoods().isEmpty())
                            openDetail(r.body().getFoods().get(0));
                    }

                    @Override
                    public void onFailure(@NonNull Call<FoodSearchResponse> c, @NonNull Throwable t) {
                    }
                }));

        // --- PRESET OPTIONS ---
        RecyclerView preset = view.findViewById(R.id.recycler_preset);
        preset.setLayoutManager(new LinearLayoutManager(requireContext()));
        presetAdapter = new MealAdapter(new ArrayList<>());
        preset.setAdapter(presetAdapter);
        presetAdapter.setOnItemClickListener(this::openDetail);
        loadPresetOptions();
    }

    @Override public void onStart() {
        super.onStart();
        View sheet = Objects.requireNonNull(getDialog())
                .findViewById(com.google.android.material.R.id.design_bottom_sheet);
        BottomSheetBehavior.from(sheet).setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    private List<LoggedMeal> loadLoggedMeals() {
        String j = prefs.getString(KEY_LOGGED_MEALS, "");
        if (j.isEmpty()) return new ArrayList<>();
        Type t = new TypeToken<List<LoggedMeal>>(){}.getType();
        List<LoggedMeal> all = gson.fromJson(j, t);
        return all.size() <=5 ? all : all.subList(0,5);
    }

    private void loadPresetOptions() {
        for (String q : PRESETS) {
            service.searchFood(q, BuildConfig.FDC_API_KEY)
                    .enqueue(new Callback<>() {
                        @Override
                        public void onResponse(@NonNull Call<FoodSearchResponse> c, @NonNull Response<FoodSearchResponse> r) {
                            if (r.isSuccessful() && r.body() != null && !r.body().getFoods().isEmpty()) {
                                List<FoodSearchResponse.FoodItem> cur =
                                        new ArrayList<>(presetAdapter.getData());
                                cur.add(r.body().getFoods().get(0));
                                presetAdapter.updateData(cur);
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<FoodSearchResponse> c, @NonNull Throwable t) {
                        }
                    });
        }
    }

    private void openDetail(FoodSearchResponse.FoodItem item) {
        NutritionDetailSheet.newInstance(item)
                .show(getParentFragmentManager(), "NutritionDetail");
        dismiss();
    }
}