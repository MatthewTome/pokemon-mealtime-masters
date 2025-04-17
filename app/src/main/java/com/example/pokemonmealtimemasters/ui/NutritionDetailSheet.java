package com.example.pokemonmealtimemasters.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.pokemonmealtimemasters.R;
import com.example.pokemonmealtimemasters.model.FoodSearchResponse;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

public class NutritionDetailSheet extends DialogFragment {
    private static final String ARG_ITEM = "item";
    private FoodSearchResponse.FoodItem item;

    public static NutritionDetailSheet newInstance(FoodSearchResponse.FoodItem item) {
        NutritionDetailSheet sheet = new NutritionDetailSheet();
        Bundle args = new Bundle();
        args.putSerializable(ARG_ITEM, item);
        sheet.setArguments(args);
        return sheet;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NORMAL, R.style.FullScreenDialog);
        if (getArguments() != null) {
            item = (FoodSearchResponse.FoodItem) getArguments().getSerializable(ARG_ITEM);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inf, @Nullable ViewGroup parent, @Nullable Bundle s) {
        return inf.inflate(R.layout.dialog_nutrition_detail, parent, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle s) {
        MaterialToolbar tb = v.findViewById(R.id.toolbar);
        tb.setNavigationOnClickListener(x -> dismiss());

        TextView tCal    = v.findViewById(R.id.text_calories);
        TextView tProt   = v.findViewById(R.id.text_protein);
        TextView tSugar  = v.findViewById(R.id.text_sugar);
        TextView tVits   = v.findViewById(R.id.text_vitamins);
        MaterialButton btn = v.findViewById(R.id.button_add_meal);

        double cal  = extractCalories(item);
        double prot = extractProtein(item);
        double sugar= extractSugar(item);
        boolean hasVit = hasVitamins(item);

        tCal.setText("Calories: " + (int)cal);
        tProt.setText("Protein: " + (int)prot + "g");
        tSugar.setText("Sugar: " + (int)sugar + "g");
        tVits.setText("Vitamins: " + (hasVit? "Yes":"No"));

        btn.setOnClickListener(x -> {
            Bundle res = new Bundle();
            res.putString("name", item.getDescription());
            res.putDouble("calories", cal);
            res.putDouble("protein", prot);
            res.putDouble("sugar", sugar);
            res.putBoolean("vitamins", hasVit);
            res.putLong("timestamp", System.currentTimeMillis());
            getParentFragmentManager().setFragmentResult("meal_logged", res);
            dismiss();
        });
    }

    private double extractCalories(FoodSearchResponse.FoodItem it) {
        if (it.getFoodNutrients() == null) return 0;
        for (FoodSearchResponse.FoodItem.FoodNutrient n : it.getFoodNutrients()) {
            String name = n.getNutrientName();
            if ("Energy".equalsIgnoreCase(name) || "Calories".equalsIgnoreCase(name)) {
                return n.getValue();
            }
        }
        return 0;
    }

    private double extractProtein(FoodSearchResponse.FoodItem it) {
        if (it.getFoodNutrients() == null) return 0;
        for (FoodSearchResponse.FoodItem.FoodNutrient n : it.getFoodNutrients()) {
            if ("Protein".equalsIgnoreCase(n.getNutrientName())) {
                return n.getValue();
            }
        }
        return 0;
    }

    private double extractSugar(FoodSearchResponse.FoodItem it) {
        if (it.getFoodNutrients() == null) return 0;
        for (FoodSearchResponse.FoodItem.FoodNutrient n : it.getFoodNutrients()) {
            if (n.getNutrientName().toLowerCase().contains("sugar")) {
                return n.getValue();
            }
        }
        return 0;
    }

    private boolean hasVitamins(FoodSearchResponse.FoodItem it) {
        if (it.getFoodNutrients() == null) return false;
        for (FoodSearchResponse.FoodItem.FoodNutrient n : it.getFoodNutrients()) {
            if (n.getNutrientName().toLowerCase().contains("vitamin")) {
                return true;
            }
        }
        return false;
    }
}