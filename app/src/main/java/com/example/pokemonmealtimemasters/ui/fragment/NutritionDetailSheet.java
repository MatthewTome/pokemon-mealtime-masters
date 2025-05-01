package com.example.pokemonmealtimemasters.ui.fragment;

import android.app.Dialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.pokemonmealtimemasters.R;
import com.example.pokemonmealtimemasters.model.FoodSearchResponseModel;
import com.example.pokemonmealtimemasters.ui.activity.MainActivity; // Import MainActivity
import com.example.pokemonmealtimemasters.utils.AnimationUtils; // Import AnimationUtils
import com.example.pokemonmealtimemasters.utils.SoundManager; // Import SoundManager
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

/**
 * Bottom sheet for displaying and editing the nutritional details of a selected food item.
 * Allows editing servings, calories, protein, and total sugars before logging.
 */
public class NutritionDetailSheet extends BottomSheetDialogFragment {
    private static final String ARG_ITEM = "item";
    private FoodSearchResponseModel.FoodItem item; // The selected food item, null if custom

    private TextInputEditText editServings;
    private TextInputEditText editCalories;
    private TextInputEditText editProtein;
    private TextInputEditText editTotalSugars; // Added

    private SoundManager soundManager; // To play sounds

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
        args.putSerializable(ARG_ITEM, item); // FoodItem must be Serializable
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
            // Check if ARG_ITEM exists and is of the correct type
            if (getArguments().containsKey(ARG_ITEM) && getArguments().getSerializable(ARG_ITEM) instanceof FoodSearchResponseModel.FoodItem) {
                item = (FoodSearchResponseModel.FoodItem) getArguments().getSerializable(ARG_ITEM);
            }
        }
        // Initialize SoundManager from parent activity
        if (getActivity() instanceof MainActivity) {
            soundManager = ((MainActivity) getActivity()).getSoundManager();
        }

        return inflater.inflate(
                R.layout.sheet_nutrition_detail, container, false
        );
    }

    /**
     * Sets up the toolbar, input fields, and "Add Meal" button.
     * Pre-fills fields if a food item was passed; otherwise leaves them editable for custom entry.
     */
    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Find Views
        // UI Elements
        MaterialToolbar toolbar = view.findViewById(R.id.toolbar);
        editServings = view.findViewById(R.id.edit_servings);
        editCalories = view.findViewById(R.id.edit_calories);
        editProtein = view.findViewById(R.id.edit_protein);
        editTotalSugars = view.findViewById(R.id.edit_total_sugars); // Added find view
        MaterialButton btnAdd = view.findViewById(R.id.button_add_meal);

        // Setup Toolbar
        toolbar.setNavigationOnClickListener(v -> {
            if (soundManager != null) soundManager.playSound(SoundManager.Sound.BUTTON_CLICK);
            dismiss();
        });


        // Pre-fill data if a food item exists
        if (item != null && item.getDescription() != null) {
            toolbar.setTitle(item.getDescription()); // Set title to food name
            editServings.setText("1"); // Default to 1 serving

            // Extract Energy (kcal) - prioritize kcal, convert kJ if needed
            double rawEnergy = extractNutrientValue(item, "Energy", "kcal", true);
            editCalories.setText(String.valueOf(Math.round(rawEnergy))); // Round to nearest whole number

            // Extract Protein (g)
            double rawProt = extractNutrientValue(item, "Protein", "g", false);
            editProtein.setText(String.valueOf(Math.round(rawProt)));

            // Extract Total Sugars (g) - Check common names
            double rawSugars = extractNutrientValue(item, "Sugars, total including NLEA", "g", false);
            if (rawSugars == 0) { // Fallback 1
                rawSugars = extractNutrientValue(item, "Sugars, total", "g", false);
            }
            if (rawSugars == 0) { // Fallback 2
                rawSugars = extractNutrientValue(item, "Total Sugars", "g", false);
            }
            editTotalSugars.setText(String.valueOf(Math.round(rawSugars))); // Set sugars value

        } else {
            // Setup for custom entry
            toolbar.setTitle(getString(R.string.custom_meal_entry)); // Title for custom entry
            editServings.setText("1");
            // Leave other fields blank for user input
        }

        // Handle the Add Meal button click
        btnAdd.setOnClickListener(v -> {
            if (soundManager != null) soundManager.playSound(SoundManager.Sound.BUTTON_CLICK);
            AnimationUtils.applyPressAnimation(v); // Add button press animation
            logMeal(); // Call separate method to handle logging logic
        });
    }

    private void logMeal() {
        try {
            // Validate and parse servings
            double servings = parseDoubleOrDefault(editServings.getText());
            if (servings <= 0) {
                showToast(getString(R.string.error_positive_servings));
                AnimationUtils.applyShakeAnimation(editServings); // Shake animation for error
                return;
            }

            // Parse nutrient values (use safe defaults if empty)
            double cal = parseDoubleOrDefault(editCalories.getText()) * servings;
            double prot = parseDoubleOrDefault(editProtein.getText()) * servings;
            double sugars = parseDoubleOrDefault(editTotalSugars.getText()) * servings; // Get sugars

            // Basic validation: check if at least calories are entered
            if (cal <= 0 && prot <= 0 && sugars <= 0 && parseDoubleOrDefault(editCalories.getText()) <=0) {
                showToast(getString(R.string.error_enter_nutrition_details));
                AnimationUtils.applyShakeAnimation(editCalories); // Shake animation for error
                AnimationUtils.applyShakeAnimation(editProtein);
                AnimationUtils.applyShakeAnimation(editTotalSugars);
                AnimationUtils.applyShakeAnimation(editServings);
                return;
            }


            // Create result bundle
            Bundle result = new Bundle();
            result.putString("name", item != null && !TextUtils.isEmpty(item.getDescription()) ? item.getDescription() : getString(R.string.custom_meal_name)); // Use string resource for custom
            result.putDouble("calories", cal);
            result.putDouble("protein", prot);
            result.putDouble("totalSugars", sugars); // Add sugars to result
            result.putLong("timestamp", System.currentTimeMillis());

            // Send result back to the parent fragment/activity (MainActivity)
            getParentFragmentManager().setFragmentResult("meal_logged", result);
            dismiss(); // Close the bottom sheet

        } catch (NumberFormatException e) {
            // This catch might be less likely now with parseDoubleOrDefault, but keep as fallback
            showToast(getString(R.string.error_invalid_numbers));
        }
    }

    // Helper to parse Double, returning 0.0 if empty or invalid
    private double parseDoubleOrDefault(@Nullable Editable text) {
        if (text == null || TextUtils.isEmpty(text.toString().trim())) {
            return 0.0;
        }
        try {
            return Double.parseDouble(text.toString());
        } catch (NumberFormatException e) {
            Log.w("NutritionDetailSheet", "NumberFormatException parsing: " + text);
            return 0.0; // Return 0 if parsing fails
        }
    }

    // Helper to show Toast messages consistently
    private void showToast(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }

    // Make the BottomSheet expand fully on start
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setOnShowListener(dialogInterface -> {
            BottomSheetDialog bottomSheetDialog = (BottomSheetDialog) dialogInterface;
            FrameLayout bottomSheet = bottomSheetDialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                BottomSheetBehavior<FrameLayout> behavior = BottomSheetBehavior.from(bottomSheet);
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                behavior.setSkipCollapsed(true); // Prevent it from settling in collapsed state
            }
        });
        return dialog;
    }

    /**
     * Finds the named nutrient in the FoodItem. Handles unit conversion (kJ to kcal) if specified.
     * Checks for exact and partial matches in nutrient names.
     *
     * @param foodItem The food item data.
     * @param nutrientName The target nutrient name (e.g., "Energy", "Protein", "Sugars, total").
     * @param targetUnit The desired unit (e.g., "kcal", "g").
     * @param allowKJConversion If true, allows conversion from kJ to kcal for "Energy".
     * @return The nutrient value in the target unit, or 0.0 if not found or conversion fails.
     */
    private double extractNutrientValue(
            @Nullable FoodSearchResponseModel.FoodItem foodItem,
            @NonNull String nutrientName,
            @NonNull String targetUnit,
            boolean allowKJConversion) {

        if (foodItem == null || foodItem.getFoodNutrients() == null) {
            Log.d("NutrientExtract", "Food item or nutrients list is null for: " + nutrientName);
            return 0.0;
        }

        String nutrientNameLower = nutrientName.toLowerCase();
        FoodSearchResponseModel.FoodItem.FoodNutrient exactMatch = null;
        FoodSearchResponseModel.FoodItem.FoodNutrient partialMatch = null;

        // Iterate through nutrients to find the best match
        for (FoodSearchResponseModel.FoodItem.FoodNutrient nutrient : foodItem.getFoodNutrients()) {
            if (nutrient.getNutrientName() != null) {
                String currentNutrientNameLower = nutrient.getNutrientName().toLowerCase();

                // Check for exact match first
                if (currentNutrientNameLower.equals(nutrientNameLower)) {
                    exactMatch = nutrient;
                    break; // Found exact match, no need to continue
                }
                // Check for partial match (e.g., "Energy" matches "Energy, total")
                // Only store the first partial match found for now
                if (partialMatch == null && currentNutrientNameLower.contains(nutrientNameLower)) {
                    partialMatch = nutrient;
                }
            }
        }

        // Prioritize exact match, fall back to partial match
        FoodSearchResponseModel.FoodItem.FoodNutrient bestMatch = (exactMatch != null) ? exactMatch : partialMatch;

        if (bestMatch != null) {
            Log.d("NutrientExtract", "Found match for '" + nutrientName + "': '" + bestMatch.getNutrientName() + "' with value " + bestMatch.getValue() + " " + bestMatch.getUnitName());
            return convertNutrientValue(bestMatch.getValue(), bestMatch.getUnitName(), targetUnit, allowKJConversion && nutrientNameLower.contains("energy"));
        } else {
            Log.w("NutrientExtract", "Nutrient not found for: " + nutrientName + " in item: " + foodItem.getDescription());
            return 0.0; // Nutrient not found
        }
    }


    /**
     * Converts a nutrient value from its original unit to a target unit if possible.
     * Currently supports kJ to kcal, mg to g, and ug to g/mg.
     *
     * @param value The original nutrient value.
     * @param originalUnit The original unit name (e.g., "kJ", "G", "MG").
     * @param targetUnit The desired target unit (e.g., "kcal", "g").
     * @param allowKJConversion Specific flag to enable kJ -> kcal conversion.
     * @return The converted value, or the original value if units match or conversion is not supported.
     */
    private double convertNutrientValue(double value, @Nullable String originalUnit, @NonNull String targetUnit, boolean allowKJConversion) {
        if (TextUtils.isEmpty(originalUnit)) {
            Log.w("NutrientConvert", "Original unit is null or empty, cannot convert.");
            return value; // Cannot convert without original unit
        }

        String unitLower = originalUnit.toLowerCase();
        String targetUnitLower = targetUnit.toLowerCase();

        // Direct match (case-insensitive) - common case
        if (unitLower.equals(targetUnitLower)) {
            return value;
        }
        // Handle common variations like "g" vs "gram" if needed, though API seems consistent
        if (unitLower.equals("g") && targetUnitLower.equals("gram")) return value;
        if (unitLower.equals("gram") && targetUnitLower.equals("g")) return value;


        // Specific conversion: kJ to kcal (only if allowed and units match)
        if (allowKJConversion && unitLower.equals("kj") && targetUnitLower.equals("kcal")) {
            Log.d("NutrientConvert", "Converting " + value + " kJ to kcal.");
            return value / 4.184;
        }

        // Unit conversions: mass
        if (unitLower.equals("mg") && targetUnitLower.equals("g")) {
            Log.d("NutrientConvert", "Converting " + value + " mg to g.");
            return value / 1000.0;
        }
        if (unitLower.equals("ug") && targetUnitLower.equals("g")) {
            Log.d("NutrientConvert", "Converting " + value + " ug to g.");
            return value / 1000000.0;
        }
        if (unitLower.equals("mcg") && targetUnitLower.equals("g")) { // Also handle mcg
            Log.d("NutrientConvert", "Converting " + value + " mcg to g.");
            return value / 1000000.0;
        }
        if (unitLower.equals("ug") && targetUnitLower.equals("mg")) {
            Log.d("NutrientConvert", "Converting " + value + " ug to mg.");
            return value / 1000.0;
        }
        if (unitLower.equals("mcg") && targetUnitLower.equals("mg")) { // Also handle mcg
            Log.d("NutrientConvert", "Converting " + value + " mcg to mg.");
            return value / 1000.0;
        }


        // If no specific conversion rule is found
        Log.w("NutrientConvert", "Cannot convert unit '" + originalUnit + "' to '" + targetUnit + "'. Returning original value.");
        return value;
    }
}