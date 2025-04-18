package com.example.pokemonmealtimemasters.ui;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.pokemonmealtimemasters.R;
import com.example.pokemonmealtimemasters.model.FoodSearchResponse;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Objects;

public class NutritionDetailSheet extends BottomSheetDialogFragment {
    private static final String ARG_ITEM = "item";
    private FoodSearchResponse.FoodItem item;

    public static NutritionDetailSheet newInstance(
            @Nullable FoodSearchResponse.FoodItem item
    ) {
        NutritionDetailSheet sheet = new NutritionDetailSheet();
        Bundle args = new Bundle();
        args.putSerializable(ARG_ITEM, item);
        sheet.setArguments(args);
        return sheet;
    }

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        if (getArguments() != null) {
            item = (FoodSearchResponse.FoodItem)
                    getArguments().getSerializable(ARG_ITEM);
        }
        return inflater.inflate(
                R.layout.sheet_nutrition_detail, container, false
        );
    }

    @Override public void onViewCreated(@NonNull View view,
                                        @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MaterialToolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> dismiss());

        TextInputEditText editServings = view.findViewById(R.id.edit_servings);
        TextInputEditText editCalories = view.findViewById(R.id.edit_calories);
        TextInputEditText editProtein  = view.findViewById(R.id.edit_protein);
        TextInputEditText editCarbs    = view.findViewById(R.id.edit_carbs);
        MaterialButton   btnAdd        = view.findViewById(R.id.button_add_meal);

        if (item != null) {
            editServings.setText("1");
            editCalories.setText(
                    String.valueOf((int) extract(item, "Energy"))
            );
            editProtein.setText(
                    String.valueOf((int) extract(item, "Protein"))
            );
            editCarbs.setText(
                    String.valueOf((int) extract(item, "Carbs"))
            );
        }

        btnAdd.setOnClickListener(v -> {
            try {
                double servings = Double.parseDouble(
                        Objects.requireNonNull(editServings.getText()).toString()
                );
                double cal  = Double.parseDouble(
                        Objects.requireNonNull(editCalories.getText()).toString()
                ) * servings;
                double prot = Double.parseDouble(
                        Objects.requireNonNull(editProtein.getText()).toString()
                ) * servings;
                double sug  = Double.parseDouble(
                        Objects.requireNonNull(editCarbs.getText()).toString()
                ) * servings;

                Bundle result = new Bundle();
                result.putString(
                        "name",
                        item != null ? item.getDescription() : "Custom"
                );
                result.putDouble("calories", cal);
                result.putDouble("protein",  prot);
                result.putDouble("carbs",    sug);
                result.putLong("timestamp",
                        System.currentTimeMillis()
                );

                getParentFragmentManager()
                        .setFragmentResult("meal_logged", result);
                dismiss();
            } catch (NumberFormatException e) {
                Toast.makeText(requireContext(),
                        "Enter valid numbers", Toast.LENGTH_SHORT
                ).show();
            }
        });
    }

    @Override public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            FrameLayout sheet = dialog.findViewById(
                    com.google.android.material.R.id.design_bottom_sheet
            );
            if (sheet != null) {
                BottomSheetBehavior<FrameLayout> behavior =
                        BottomSheetBehavior.from(sheet);
                behavior.setState(
                        BottomSheetBehavior.STATE_EXPANDED
                );
            }
        }
    }

    private double extract(
            FoodSearchResponse.FoodItem it, String key
    ) {
        if (it.getFoodNutrients() == null) return 0;
        for (FoodSearchResponse.FoodItem.FoodNutrient n :
                it.getFoodNutrients()) {
            if (n.getNutrientName().equalsIgnoreCase(key)) {
                return n.getValue();
            }
        }
        return 0;
    }
}
