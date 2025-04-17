package com.example.pokemonmealtimemasters.ui;

import android.os.Bundle;
import android.util.Log;
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
import com.example.pokemonmealtimemasters.network.ApiClient;
import com.example.pokemonmealtimemasters.network.FoodDataService;
import com.example.pokemonmealtimemasters.ui.adapter.MealAdapter;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MealLoggingSheet extends BottomSheetDialogFragment {

    private EditText searchInput;
    private Button searchButton;
    private RecyclerView resultsRecycler;
    private MealAdapter mealAdapter;

    public MealLoggingSheet() {
        setStyle(STYLE_NORMAL, R.style.Theme_MPM_BottomSheet);
    }

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.sheet_meal_logging, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle b) {
        MaterialToolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> dismiss());

        searchInput     = view.findViewById(R.id.search_input);
        searchButton    = view.findViewById(R.id.search_button);
        resultsRecycler = view.findViewById(R.id.search_results_recycler);

        resultsRecycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        mealAdapter = new MealAdapter(new ArrayList<>());
        resultsRecycler.setAdapter(mealAdapter);

        mealAdapter.setOnItemClickListener(item -> {
            Bundle result = new Bundle();
            result.putString("name", item.getDescription());
            result.putDouble("calories", extractCalories(item));
            result.putLong("timestamp", System.currentTimeMillis());
            getParentFragmentManager().setFragmentResult("meal_logged", result);
            dismiss();
        });

        searchButton.setOnClickListener(v -> {
            String q = searchInput.getText().toString().trim();
            if (!q.isEmpty()) performMealSearch(q);
            else Toast.makeText(requireContext(),
                    "Please enter a search query.",
                    Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        View bottom = getDialog()
                .findViewById(com.google.android.material.R.id.design_bottom_sheet);
        BottomSheetBehavior<View> bh = BottomSheetBehavior.from(bottom);
        // Allow the sheet to expand to full height
        bh.setFitToContents(false);
        // With no offset, expanded = all the way to the top
        bh.setExpandedOffset(0);
        // Finally, expand immediately
        bh.setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    private void performMealSearch(String query) {
        FoodDataService svc = ApiClient.getClient(requireContext())
                .create(FoodDataService.class);
        svc.searchFood(query, BuildConfig.FDC_API_KEY)
                .enqueue(new Callback<FoodSearchResponse>() {
                    @Override
                    public void onResponse(Call<FoodSearchResponse> call,
                                           Response<FoodSearchResponse> resp) {
                        if (resp.isSuccessful() && resp.body()!=null) {
                            mealAdapter.updateData(resp.body().getFoods());
                        } else {
                            String err = "";
                            try {
                                if (resp.errorBody()!=null)
                                    err = resp.errorBody().string();
                            } catch (Exception x){ x.printStackTrace(); }
                            Log.e("MealLoggingSheet",
                                    "API " + resp.code() + " " +
                                            resp.message() + " / " + err);
                            Toast.makeText(requireContext(),
                                    "API Error: " + resp.message(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onFailure(Call<FoodSearchResponse> call, Throwable t) {
                        Toast.makeText(requireContext(),
                                "Network Error: " + t.getMessage(),
                                Toast.LENGTH_SHORT).show();
                        t.printStackTrace();
                    }
                });
    }

    private double extractCalories(FoodSearchResponse.FoodItem item) {
        if (item.getFoodNutrients()==null) return 0.0;
        for (FoodSearchResponse.FoodItem.FoodNutrient n
                : item.getFoodNutrients()) {
            String nm = n.getNutrientName();
            if ("Energy".equalsIgnoreCase(nm) ||
                    "Calories".equalsIgnoreCase(nm)) {
                return n.getValue();
            }
        }
        return 0.0;
    }
}