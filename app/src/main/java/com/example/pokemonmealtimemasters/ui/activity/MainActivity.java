package com.example.pokemonmealtimemasters.ui.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import coil.Coil;
import coil.request.ErrorResult;
import coil.request.ImageRequest;
import coil.request.SuccessResult;
import com.example.pokemonmealtimemasters.R;
import com.example.pokemonmealtimemasters.model.LoggedMealModel;
import com.example.pokemonmealtimemasters.ui.adapter.LoggedMealsAdapter;
import com.example.pokemonmealtimemasters.ui.fragment.MealLoggingSheet;
import com.example.pokemonmealtimemasters.ui.fragment.NutrientBreakdownDialogFragment;
import com.example.pokemonmealtimemasters.ui.fragment.RewardSheet;
import com.example.pokemonmealtimemasters.utils.AnimationUtils;
import com.example.pokemonmealtimemasters.utils.RewardEngine;
import com.example.pokemonmealtimemasters.utils.SoundManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Hosts the “Home” screen, displaying the user's meals for the day,
 * daily progress (Calories, Protein, Total Sugars), and vitamin status.
 * Allows logging meals and displays the user's current partner Pokémon.
 */
public class MainActivity extends AppCompatActivity {
    // SharedPreferences Keys
    private static final String PREFS = "prefs";
    private static final String KEY_LOGGED_MEALS = "logged_meals";
    private static final String KEY_DAILY_CALORIES = "daily_calories";
    private static final String KEY_DAILY_PROTEIN = "daily_protein";
    private static final String KEY_DAILY_SUGARS = "daily_sugars";
    private static final String KEY_MULTIVITAMIN_TAKEN = "vitamin_taken";
    private static final String KEY_LAST_CHECKED_DATE = "last_checked_date";
    private static final String KEY_LAST_POKEMON = "last_pokemon";
    private static final String KEY_CAUGHT_SET = "caught_pokemon_ids";

    // Goals
    private static final float GOAL_CALORIES = 2000f;
    private static final float GOAL_PROTEIN = 50f;
    private static final float GOAL_SUGARS = 90f;

    private SharedPreferences prefs;
    private Gson gson;
    private SoundManager soundManager;

    // Daily Tracking Variables
    private double dailyCalories;
    private double dailyProtein;
    private double dailyTotalSugars;
    private boolean vitaminTakenToday;

    // UI Elements
    private ImageView pokemonSpriteImage;
    private LinearProgressIndicator progCalories;
    private LinearProgressIndicator progProtein;
    private LinearProgressIndicator progTotalSugars;
    private TextView textCaloriesValue;
    private TextView textProteinValue;
    private TextView textSugarsValue;
    private CheckBox checkboxVitamin;

    private List<LoggedMealModel> loggedMealModels;
    private LoggedMealsAdapter adapter;
    private RecyclerView loggedMealsRecyclerView;
    private FloatingActionButton fabAddMeal;
    private com.google.android.material.appbar.MaterialToolbar toolbar;

    private final ExecutorService rewardExecutor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        gson = new Gson();
        soundManager = new SoundManager(this);

        findViews();
        setupToolbar();
        loadLastPokemon();
        loadLoggedMeals();
        checkAndResetDailyData();
        updateProgressBars();
        setupProgressBarClickListeners();
        setupRecyclerView();
        setupFab();
        setupMealResultListener();
        startPokemonIdleAnimation();
    }

    private void findViews() {
        pokemonSpriteImage = findViewById(R.id.image_pokemon_sprite);
        progCalories = findViewById(R.id.prog_calories);
        progProtein = findViewById(R.id.prog_protein);
        progTotalSugars = findViewById(R.id.prog_total_sugars);
        textCaloriesValue = findViewById(R.id.text_calories_value);
        textProteinValue = findViewById(R.id.text_protein_value);
        textSugarsValue = findViewById(R.id.text_sugars_value);
        checkboxVitamin = findViewById(R.id.checkbox_vitamin);
        loggedMealsRecyclerView = findViewById(R.id.logged_meals_recycler);
        fabAddMeal = findViewById(R.id.fab_add_meal);
        toolbar = findViewById(R.id.top_app_bar);
    }

    private void loadLastPokemon() {
        String lastId = prefs.getString(KEY_LAST_POKEMON, "25"); // Default to Pikachu if none caught
        String url = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/"
                + lastId + ".png";
        displayPokemonSprite(url);
    }

    private void displayPokemonSprite(String url) {
        Coil.imageLoader(this)
                .enqueue(new ImageRequest.Builder(this)
                        .data(url)
                        .placeholder(R.drawable.pokeball_silhouette)
                        .error(R.drawable.pokeball_silhouette)
                        .crossfade(true)
                        .target(pokemonSpriteImage)
                        .build());
    }

    private void checkAndResetDailyData() {
        String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        String lastCheckedDate = prefs.getString(KEY_LAST_CHECKED_DATE, "");

        // Reset Daily Totals
        if (!todayDate.equals(lastCheckedDate)) {
            vitaminTakenToday = false;
            dailyCalories = 0;
            dailyProtein = 0;
            dailyTotalSugars = 0;
            loggedMealModels.clear();

            prefs.edit()
                    .putString(KEY_LAST_CHECKED_DATE, todayDate)
                    .putBoolean(KEY_MULTIVITAMIN_TAKEN, false)
                    .putFloat(KEY_DAILY_CALORIES, 0f)
                    .putFloat(KEY_DAILY_PROTEIN, 0f)
                    .putFloat(KEY_DAILY_SUGARS, 0f)
                    .putString(KEY_LOGGED_MEALS, gson.toJson(loggedMealModels))
                    .apply();
        } else {
            // Same day, load the saved data
            vitaminTakenToday = prefs.getBoolean(KEY_MULTIVITAMIN_TAKEN, false);
            dailyCalories = prefs.getFloat(KEY_DAILY_CALORIES, 0f);
            dailyProtein = prefs.getFloat(KEY_DAILY_PROTEIN, 0f);
            dailyTotalSugars = prefs.getFloat(KEY_DAILY_SUGARS, 0f);
        }

        // Set checkbox state AFTER loading/resetting
        checkboxVitamin.setChecked(vitaminTakenToday);

        // Set up the listener for the checkbox AFTER potentially resetting it
        checkboxVitamin.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Only trigger sound/animation on direct user interaction
            if (buttonView.isPressed()) {
                soundManager.playSound(SoundManager.Sound.BUTTON_CLICK);
                if (isChecked) {
                    AnimationUtils.applyPopInAnimation(buttonView);
                } else {
                    AnimationUtils.applyPressAnimation(buttonView);
                }
            }
            vitaminTakenToday = isChecked;
            prefs.edit().putBoolean(KEY_MULTIVITAMIN_TAKEN, isChecked).apply();
        });
    }

    private void loadLoggedMeals() {
        String json = prefs.getString(KEY_LOGGED_MEALS, "");
        if (json.isEmpty()) {
            loggedMealModels = new ArrayList<>();
        } else {
            try {
                Type t = new TypeToken<List<LoggedMealModel>>() {}.getType();
                loggedMealModels = gson.fromJson(json, t);
                if (loggedMealModels == null) {
                    loggedMealModels = new ArrayList<>();
                } else {
                    loggedMealModels = new ArrayList<>(loggedMealModels);
                }
            } catch (Exception e) {
                loggedMealModels = new ArrayList<>();
                prefs.edit().remove(KEY_LOGGED_MEALS).apply();
            }
        }
    }

    private void setupRecyclerView() {
        loggedMealsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new LoggedMealsAdapter(loggedMealModels);
        loggedMealsRecyclerView.setAdapter(adapter);
    }

    private void setupToolbar() {
        toolbar.setTitle(getString(R.string.title));
        toolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.black));

        toolbar.setOnMenuItemClickListener(item -> {
            soundManager.playSound(SoundManager.Sound.BUTTON_CLICK);
            View itemView = toolbar.findViewById(item.getItemId());
            if (itemView != null) {
                AnimationUtils.applyPressAnimation(itemView);
            }

            int itemId = item.getItemId();

            if (itemId == R.id.action_pokedex) {
                startActivity(new Intent(MainActivity.this, PokedexActivity.class));
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                return true;
            } else if (itemId == R.id.action_badges) {
                startActivity(new Intent(this, BadgesActivity.class));
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                return true;
            }
            return false;
        });
    }

    private void setupFab() {
        fabAddMeal.setOnClickListener(v -> {
            soundManager.playSound(SoundManager.Sound.BUTTON_CLICK);
            AnimationUtils.applyPressAnimation(v);
            new MealLoggingSheet()
                    .show(getSupportFragmentManager(), "MealLoggingSheet");
        });
    }

    private void setupMealResultListener() {
        getSupportFragmentManager().setFragmentResultListener(
                "meal_logged", this,
                (key, bundle) ->
                {
                    soundManager.playSound(SoundManager.Sound.MEAL_LOGGED);

                    // Extract Meal data from result bundle
                    String name = bundle.getString("name", getString(R.string.custom_meal_name));
                    double cal = bundle.getDouble("calories", 0);
                    double prot = bundle.getDouble("protein", 0);
                    double sugars = bundle.getDouble("totalSugars", 0);

                    // Create new meal model
                    LoggedMealModel newMeal = new LoggedMealModel(name, cal, prot, sugars, System.currentTimeMillis());

                    // Update local list and adapter
                    loggedMealModels.add(0, newMeal);
                    adapter.updateData(loggedMealModels);
                    loggedMealsRecyclerView.scrollToPosition(0);

                    // Update daily progress totals
                    dailyCalories += cal;
                    dailyProtein += prot;
                    dailyTotalSugars += sugars;

                    saveDailyData();
                    updateProgressBars();
                    calculateReward(cal, prot);
                });
    }

    private void saveDailyData() {
        prefs.edit()
                .putFloat(KEY_DAILY_CALORIES, (float) dailyCalories)
                .putFloat(KEY_DAILY_PROTEIN, (float) dailyProtein)
                .putFloat(KEY_DAILY_SUGARS, (float) dailyTotalSugars)
                .putString(KEY_LOGGED_MEALS, gson.toJson(loggedMealModels))
                .apply();
    }

    private void calculateReward(double calories, double protein) {
        Set<String> caught = new HashSet<>(
                prefs.getStringSet(KEY_CAUGHT_SET, new HashSet<>()));

        // Compute rewards
        rewardExecutor.execute(() ->
        {
            String pokedexId = RewardEngine.computeReward(
                    MainActivity.this, calories, protein, caught);

            if (pokedexId == null) {
                Log.d("MainActivity", "No reward calculated for this meal.");
                return;
            }

            runOnUiThread(() -> {
                soundManager.playSound(SoundManager.Sound.REWARD_RECEIVED);

                caught.add(pokedexId);
                prefs.edit()
                        .putString(KEY_LAST_POKEMON, pokedexId)
                        .putStringSet(KEY_CAUGHT_SET, caught)
                        .apply();

                updatePokemonSpriteWithAnimation(pokedexId);

                RewardSheet.newInstance(pokedexId)
                        .show(getSupportFragmentManager(), "RewardSheet");
            });
        });
    }

    private void updatePokemonSpriteWithAnimation(String pokedexId) {
        String newUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/"
                + pokedexId + ".png";

        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(pokemonSpriteImage, "alpha", 1f, 0f);
        fadeOut.setDuration(300);
        fadeOut.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                // Load new image
                Coil.imageLoader(MainActivity.this)
                        .enqueue(new ImageRequest.Builder(MainActivity.this)
                                .data(newUrl)
                                .placeholder(R.drawable.pokeball_silhouette)
                                .error(R.drawable.pokeball_silhouette)
                                .crossfade(false)
                                .target(pokemonSpriteImage)
                                .listener(new ImageRequest.Listener() {
                                    @Override
                                    public void onSuccess(@NonNull ImageRequest request, @NonNull SuccessResult result) {
                                        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(pokemonSpriteImage, "alpha", 0f, 1f);
                                        fadeIn.setDuration(300);
                                        fadeIn.start();
                                        AnimationUtils.applyBounceAnimation(pokemonSpriteImage);
                                    }

                                    @Override
                                    public void onError(@NonNull ImageRequest request, @NonNull ErrorResult result) {
                                        Log.e("MainActivity", "Error loading new Pokemon sprite: " + result.getThrowable());
                                        pokemonSpriteImage.setImageResource(R.drawable.pokeball_silhouette);
                                        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(pokemonSpriteImage, "alpha", 0f, 1f);
                                        fadeIn.setDuration(300);
                                        fadeIn.start();
                                    }
                                })
                                .build());
            }
        });
        fadeOut.start();
    }

    private void startPokemonIdleAnimation() {
        ObjectAnimator bobbing = ObjectAnimator.ofFloat(pokemonSpriteImage, "translationY", 0f, -15f, 0f);
        bobbing.setDuration(1800);
        bobbing.setRepeatCount(ObjectAnimator.INFINITE);
        bobbing.setRepeatMode(ObjectAnimator.REVERSE);
        bobbing.setInterpolator(new AccelerateDecelerateInterpolator());
        bobbing.start();
    }

    private void updateProgressBars() {
        // Calculate percentages
        int caloriesPercent = (int) Math.min(100, (dailyCalories / GOAL_CALORIES * 100));
        int proteinPercent = (int) Math.min(100, (dailyProtein / GOAL_PROTEIN * 100));
        int sugarsPercent = (int) Math.min(100, (dailyTotalSugars / GOAL_SUGARS * 100)); // Use total sugars

        // Animate progress updates
        progCalories.setProgressCompat(caloriesPercent, true);
        progProtein.setProgressCompat(proteinPercent, true);
        progTotalSugars.setProgressCompat(sugarsPercent, true); // Update sugar progress

        // Set progress text
        textCaloriesValue.setText(String.format(Locale.getDefault(), "%d / %.0f kcal", (int) dailyCalories, GOAL_CALORIES));
        textProteinValue.setText(String.format(Locale.getDefault(), "%d / %.0f g", (int) dailyProtein, GOAL_PROTEIN));
        textSugarsValue.setText(String.format(Locale.getDefault(), "%d / %.0f g", (int) dailyTotalSugars, GOAL_SUGARS));
    }

    private void setupProgressBarClickListeners() {
        progCalories.setOnClickListener(v -> {
            AnimationUtils.applyPressAnimation(v);
            // Note: Play sound here BEFORE showing dialog for simplicity
            soundManager.playSound(SoundManager.Sound.POPUP_OPEN);
            List<NutrientBreakdownDialogFragment.MealContribution> contributions = getMealContributionsForToday(NutrientBreakdownDialogFragment.NutrientType.CALORIES);
            NutrientBreakdownDialogFragment.newInstance(
                    getString(R.string.calories_label),
                    dailyCalories,
                    GOAL_CALORIES, // Pass the goal
                    NutrientBreakdownDialogFragment.NutrientType.CALORIES,
                    contributions
            ).show(getSupportFragmentManager(), "NutrientBreakdownDialog_Calories");
        });

        progProtein.setOnClickListener(v -> {
            AnimationUtils.applyPressAnimation(v);
            soundManager.playSound(SoundManager.Sound.POPUP_OPEN);
            List<NutrientBreakdownDialogFragment.MealContribution> contributions = getMealContributionsForToday(NutrientBreakdownDialogFragment.NutrientType.PROTEIN);
            NutrientBreakdownDialogFragment.newInstance(
                    getString(R.string.protein_label),
                    dailyProtein,
                    GOAL_PROTEIN, // Pass the goal
                    NutrientBreakdownDialogFragment.NutrientType.PROTEIN,
                    contributions
            ).show(getSupportFragmentManager(), "NutrientBreakdownDialog_Protein");
        });

        progTotalSugars.setOnClickListener(v -> {
            AnimationUtils.applyPressAnimation(v);
            soundManager.playSound(SoundManager.Sound.POPUP_OPEN);
            List<NutrientBreakdownDialogFragment.MealContribution> contributions = getMealContributionsForToday(NutrientBreakdownDialogFragment.NutrientType.TOTAL_SUGARS);
            NutrientBreakdownDialogFragment.newInstance(
                    getString(R.string.total_sugars_label),
                    dailyTotalSugars,
                    GOAL_SUGARS, // Pass the goal
                    NutrientBreakdownDialogFragment.NutrientType.TOTAL_SUGARS,
                    contributions
            ).show(getSupportFragmentManager(), "NutrientBreakdownDialog_Sugars");
        });
    }

    // Gets contributions ONLY for the current day
    private List<NutrientBreakdownDialogFragment.MealContribution> getMealContributionsForToday(NutrientBreakdownDialogFragment.NutrientType type) {
        List<NutrientBreakdownDialogFragment.MealContribution> contributions = new ArrayList<>();
        Calendar calStart = Calendar.getInstance();
        calStart.set(Calendar.HOUR_OF_DAY, 0);
        calStart.set(Calendar.MINUTE, 0);
        calStart.set(Calendar.SECOND, 0);
        calStart.set(Calendar.MILLISECOND, 0);
        long todayStartTime = calStart.getTimeInMillis();

        // Iterate through the currently loaded list of meals
        for (LoggedMealModel meal : loggedMealModels) {
            if (meal.timestamp() >= todayStartTime) {
                double value = switch (type) {
                    case CALORIES -> meal.calories();
                    case PROTEIN -> meal.protein();
                    case TOTAL_SUGARS -> meal.totalSugars();
                };
                if (value > 0) {
                    contributions.add(new NutrientBreakdownDialogFragment.MealContribution(meal.name(), value));
                }
            }
        }
        contributions.sort(Comparator.comparingDouble(NutrientBreakdownDialogFragment.MealContribution::getValue).reversed());
        return contributions;
    }

    public SoundManager getSoundManager() {
        return soundManager;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (soundManager != null) {
            soundManager.release();
        }
        if (rewardExecutor != null && !rewardExecutor.isShutdown()) {
            rewardExecutor.shutdown();
        }
    }

    // Resume/Pause Handling for Animations/Sound
    @Override
    protected void onResume() {
        super.onResume();
        if (soundManager != null) {
            soundManager.resume();
        }
        // Restart animations if they were paused
        if (pokemonSpriteImage != null && pokemonSpriteImage.getAnimation() != null) {
            pokemonSpriteImage.getAnimation().start();
        }

        // Check if last checked date is different
        String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        String lastCheckedDate = prefs.getString(MainActivity.KEY_LAST_CHECKED_DATE, "");
        if (!todayDate.equals(lastCheckedDate)) {
            checkAndResetDailyData();
            updateProgressBars();
            adapter.updateData(loggedMealModels);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (soundManager != null) {
            soundManager.pause();
        }
        // Pause animations to save resources
        if (pokemonSpriteImage != null && pokemonSpriteImage.getAnimation() != null) {
            pokemonSpriteImage.clearAnimation();
        }
    }
}