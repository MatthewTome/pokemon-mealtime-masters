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
import com.example.pokemonmealtimemasters.model.FoodSearchResponseModel;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import java.util.Objects;

/**
 * Bottom sheet for displaying and editing the nutritional details of a selected food item.
 * Supports pre-populating values from the FoodData Central API or leaving fields blank
 * for custom input. On submission, returns the computed nutrient totals back to MainActivity.
 */
public class NutritionDetailSheet extends BottomSheetDialogFragment {
    private static final String ARG_ITEM = "item";
    private FoodSearchResponseModel.FoodItem item;

    /**
     * Factory method to create a new instance of this sheet with an optional FoodItem.
     * @param item The FoodItem to display (null for custom entry)
     * @return Configured NutritionDetailSheet instance
     */
    public static NutritionDetailSheet newInstance(
            @Nullable FoodSearchResponseModel.FoodItem item
    ) {
        NutritionDetailSheet sheet = new NutritionDetailSheet();
        Bundle args = new Bundle();
        args.putSerializable(ARG_ITEM, item);
        sheet.setArguments(args);
        return sheet;
    }

    // Inflates the layout and retrieves the passed FoodItem argument if present.
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        if (getArguments() != null) {
            item = (FoodSearchResponseModel.FoodItem)
                    getArguments().getSerializable(ARG_ITEM);
        }
        return inflater.inflate(
                R.layout.sheet_nutrition_detail, container, false
        );
    }

    /**
     * Sets up the toolbar, input fields, and "Add Meal" button.
     * Pre-fills fields if an item was passed; otherwise leaves them editable.
     */
    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MaterialToolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> dismiss());

        TextInputEditText editServings = view.findViewById(R.id.edit_servings);
        TextInputEditText editCalories = view.findViewById(R.id.edit_calories);
        TextInputEditText editProtein  = view.findViewById(R.id.edit_protein);
        TextInputEditText editCarbs    = view.findViewById(R.id.edit_carbs);
        MaterialButton   btnAdd        = view.findViewById(R.id.button_add_meal);

        // Prefill with one serving if a FoodItem is provided
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

        // Handle the Add Meal click: calculate totals and return result
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
                double carbs= Double.parseDouble(
                        Objects.requireNonNull(editCarbs.getText()).toString()
                ) * servings;

                Bundle result = new Bundle();
                result.putString(
                        "name",
                        item != null ? item.getDescription() : "Custom"
                );
                result.putDouble("calories", cal);
                result.putDouble("protein",  prot);
                result.putDouble("carbs",    carbs);
                result.putLong(  "timestamp", System.currentTimeMillis());

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

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            FrameLayout sheet = dialog.findViewById(
                    com.google.android.material.R.id.design_bottom_sheet
            );
            if (sheet != null) {
                BottomSheetBehavior<FrameLayout> behavior =
                        BottomSheetBehavior.from(sheet);
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        }
    }

    /**
     * Helper to extract nutrient values by name from the FoodItem.
     * @param it The FoodItem
     * @param key Nutrient name to look up (e.g., "Energy")
     * @return Nutrient value or 0 if not found
     */
    private double extract(
            FoodSearchResponseModel.FoodItem it, String key
    ) {
        if (it.getFoodNutrients() == null) return 0;
        for (FoodSearchResponseModel.FoodItem.FoodNutrient n :
                it.getFoodNutrients()) {
            if (n.getNutrientName().equalsIgnoreCase(key)) {
                return n.getValue();
            }
        }
        return 0;
    }
}