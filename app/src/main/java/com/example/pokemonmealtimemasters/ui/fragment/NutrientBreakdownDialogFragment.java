package com.example.pokemonmealtimemasters.ui.fragment;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import com.example.pokemonmealtimemasters.R;
import com.example.pokemonmealtimemasters.ui.activity.MainActivity; // Needed for SoundManager access
import com.example.pokemonmealtimemasters.utils.AnimationUtils;
import com.example.pokemonmealtimemasters.utils.SoundManager;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * Popup to educate users on their nutritional intake.
 * Takes Logged meals, breaks them up into an infographic,
 * and gives tips based on nutrition.
 */
public class NutrientBreakdownDialogFragment extends DialogFragment {

    private static final String ARG_TITLE = "arg_title";
    private static final String ARG_TOTAL_VALUE = "arg_total_value";
    private static final String ARG_GOAL_VALUE = "arg_goal_value";
    private static final String ARG_NUTRIENT_TYPE = "arg_nutrient_type";
    private static final String ARG_CONTRIBUTIONS = "arg_contributions";

    public enum NutrientType {CALORIES, PROTEIN, TOTAL_SUGARS}

    public static class MealContribution implements Serializable {
        String mealName;
        double value;

        public MealContribution(String mealName, double value) {
            this.mealName = mealName;
            this.value = value;
        }

        public String getMealName() {
            return mealName;
        }

        public double getValue() {
            return value;
        }
    }

    private SoundManager soundManager;

    // Factory method for creating instance with arguments
    public static NutrientBreakdownDialogFragment newInstance(String title, double totalValue, double goalValue, NutrientType type, List<MealContribution> contributions) {
        NutrientBreakdownDialogFragment fragment = new NutrientBreakdownDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        args.putDouble(ARG_TOTAL_VALUE, totalValue);
        args.putDouble(ARG_GOAL_VALUE, goalValue);
        args.putSerializable(ARG_NUTRIENT_TYPE, type);
        args.putSerializable(ARG_CONTRIBUTIONS, (Serializable) contributions);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof MainActivity) {
            soundManager = ((MainActivity) context).getSoundManager();
        } else {
            Log.w("NutrientBreakdownDialog", "Dialog not attached to MainActivity, sound effects might not work.");
            soundManager = new SoundManager(context.getApplicationContext());
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity(), R.style.Theme_MPM_Dialog);
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.popup_nutrient_breakdown, null);
        builder.setView(dialogView);

        // Retrieve arguments
        Bundle args = getArguments();
        if (args == null) {
            return builder.create();
        }

        String title = args.getString(ARG_TITLE, getString(R.string.nutrient_breakdown_title)); // Default title
        double totalValue = args.getDouble(ARG_TOTAL_VALUE, 0);
        double goalValue = args.getDouble(ARG_GOAL_VALUE, 1);
        NutrientType type = (NutrientType) args.getSerializable(ARG_NUTRIENT_TYPE);
        List<MealContribution> contributions = (List<MealContribution>) args.getSerializable(ARG_CONTRIBUTIONS);
        if (contributions == null) {
            contributions = Collections.emptyList();
        }

        // Find Views
        TextView popupTitle = dialogView.findViewById(R.id.popup_title);
        TextView popupTotal = dialogView.findViewById(R.id.popup_total);
        LinearLayout segmentedBarContainer = dialogView.findViewById(R.id.segmented_bar_container);
        LinearLayout legendContainer = dialogView.findViewById(R.id.legend_container);
        ImageView tipIcon = dialogView.findViewById(R.id.popup_tip_icon);
        TextView tipText = dialogView.findViewById(R.id.popup_educational_tip);
        Button closeButton = dialogView.findViewById(R.id.button_close_popup);

        // Set Title and Total
        popupTitle.setText(getString(R.string.nutrient_sources_title, title));
        assert type != null;
        String unit = getUnitForType(type);
        popupTotal.setText(getString(R.string.nutrient_total_format, (int) totalValue, unit));

        // Build Segmented Bar and Legend
        segmentedBarContainer.removeAllViews();
        legendContainer.removeAllViews();

        int[] colors = {
                ContextCompat.getColor(requireContext(), R.color.segment_1),
                ContextCompat.getColor(requireContext(), R.color.segment_2),
                ContextCompat.getColor(requireContext(), R.color.segment_3),
                ContextCompat.getColor(requireContext(), R.color.segment_4),
                ContextCompat.getColor(requireContext(), R.color.segment_5),
                ContextCompat.getColor(requireContext(), R.color.segment_6)
        };
        int colorIndex = 0;

        if (totalValue <= 0 || contributions.isEmpty()) {
            handleEmptyState(legendContainer, segmentedBarContainer);
        } else {
            segmentedBarContainer.setBackground(null);
            for (MealContribution contribution : contributions) {
                if (contribution.getValue() <= 0) continue;

                float weight = (float) (contribution.getValue() / totalValue);
                if (weight <= 0.005f && totalValue > 0) continue;

                int color = colors[colorIndex % colors.length];

                // Create Segment View
                addSegmentView(segmentedBarContainer, weight, color);

                // Create Legend Item View
                addLegendItem(inflater, legendContainer, contribution, unit, weight, color);

                colorIndex++;
            }
            if (segmentedBarContainer.getChildCount() == 0 && totalValue > 0) {
                handleEmptyState(legendContainer, segmentedBarContainer); // Fallback if all segments were too small
            }
        }


        // Educational Tip
        setEducationalTip(tipIcon, tipText, type, totalValue, goalValue);

        // Close Button
        closeButton.setOnClickListener(v -> {
            if (soundManager != null) {
                soundManager.playSound(SoundManager.Sound.BUTTON_CLICK);
            }
            AnimationUtils.applyPressAnimation(v);
            dismiss();
        });

        // Create and return the Dialog
        AlertDialog dialog = builder.create();

        // Apply entrance animation
        dialog.setOnShowListener(dialogInterface -> AnimationUtils.applyDialogEntranceAnimation(dialogView));
        return dialog;
    }

    private void handleEmptyState(LinearLayout legendContainer, LinearLayout segmentedBarContainer) {
        TextView emptyText = new TextView(requireContext());
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        emptyText.setLayoutParams(textParams);
        emptyText.setText(getString(R.string.no_contribution_data));
        emptyText.setGravity(Gravity.CENTER);
        emptyText.setPadding(0, 16, 0, 16);
        emptyText.setTextAppearance(requireContext(), androidx.appcompat.R.style.TextAppearance_AppCompat_Body1);
        legendContainer.addView(emptyText);
        segmentedBarContainer.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.grey_light));
    }

    private void addSegmentView(LinearLayout container, float weight, int color) {
        View segment = new View(requireContext());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.MATCH_PARENT,
                weight
        );

        params.setMarginEnd(getResources().getDimensionPixelSize(R.dimen.segment_margin));
        segment.setLayoutParams(params);
        segment.setBackgroundColor(color);
        container.addView(segment);
    }

    private void addLegendItem(LayoutInflater inflater, LinearLayout container, MealContribution contribution, String unit, float weight, int color) {
        View legendItemView = inflater.inflate(R.layout.item_nutrient_legend, container, false);
        View swatch = legendItemView.findViewById(R.id.legend_color_swatch);
        TextView legendText = legendItemView.findViewById(R.id.legend_text);

        swatch.setBackgroundColor(color);

        String legendStr = getString(R.string.legend_item_format,
                contribution.getMealName(),
                (int) contribution.getValue(),
                unit,
                (int) (weight * 100));
        legendText.setText(legendStr);
        container.addView(legendItemView);
    }

    private String getUnitForType(NutrientType type) {
        return switch (type) {
            case CALORIES -> getString(R.string.unit_kcal);
            case PROTEIN, TOTAL_SUGARS -> getString(R.string.unit_gram);
        };
    }

    private void setEducationalTip(ImageView iconView, TextView textView, NutrientType type, double totalValue, double goalValue) {
        String tip = "";
        int iconResId = 0;
        double percentageOfGoal = (goalValue > 0) ? (totalValue / goalValue) * 100 : 0;

        switch (type) {
            case CALORIES:
                if (percentageOfGoal < 30) {
                    tip = getString(R.string.tip_calories_low);
                    iconResId = R.drawable.ic_tip_energy_low;
                } else if (percentageOfGoal > 110) {
                    tip = getString(R.string.tip_calories_high);
                    iconResId = R.drawable.ic_tip_energy_high;
                } else {
                    tip = getString(R.string.tip_calories_good);
                    iconResId = R.drawable.ic_tip_energy_good;
                }
                break;
            case PROTEIN:
                if (percentageOfGoal < 40) {
                    tip = getString(R.string.tip_protein_low);
                    iconResId = R.drawable.ic_tip_protein_low;
                } else if (percentageOfGoal > 150) {
                    tip = getString(R.string.tip_protein_very_high);
                    iconResId = R.drawable.ic_tip_protein_good;
                } else {
                    tip = getString(R.string.tip_protein_good);
                    iconResId = R.drawable.ic_tip_protein_good;
                }
                break;
            case TOTAL_SUGARS:
                if (percentageOfGoal < 50) {
                    tip = getString(R.string.tip_sugars_good);
                    iconResId = R.drawable.ic_tip_sugar_good;
                } else if (percentageOfGoal > 100) {
                    tip = getString(R.string.tip_sugars_high);
                    iconResId = R.drawable.ic_tip_sugar_high;
                } else {
                    tip = getString(R.string.tip_sugars_moderate);
                    iconResId = R.drawable.ic_tip_sugar_moderate;
                }
                break;
        }

        if (!tip.isEmpty()) {
            textView.setText(tip);
            textView.setVisibility(View.VISIBLE);
            if (iconResId != 0) {
                iconView.setImageResource(iconResId);
                iconView.setVisibility(View.VISIBLE);
            } else {
                iconView.setVisibility(View.GONE);
            }
        } else {
            textView.setVisibility(View.GONE);
            iconView.setVisibility(View.GONE);
        }
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}