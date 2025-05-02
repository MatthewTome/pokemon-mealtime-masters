package com.example.pokemonmealtimemasters.utils;

import static org.junit.Assert.*;
import android.content.Context;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLog;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Tests for the RewardEngine.
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE, sdk = 28)
public class RewardEngineTest {

    private Context context;
    private Set<String> caughtPokemon;

    private static final List<String> BALANCED_REWARDS_IN_ENGINE = Arrays.asList("1", "4", "7", "10", "25");

    @BeforeClass
    public static void setupLogging() {
        ShadowLog.stream = System.out;
    }

    @Before
    public void setUp() {
        context = RuntimeEnvironment.getApplication();
        caughtPokemon = new HashSet<>();
    }

    /**
     * Test 6: Verifies that meals below the minimum calorie
     * or protein thresholds receive no reward.
     */
    @Test
    public void computeReward_whenNutritionTooLow_returnsNull() {
        // Arrange Calories to be low
        double lowCalories = 50;
        double adequateProtein = 10;

        // Act
        String rewardLowCal = RewardEngine.computeReward(context, lowCalories, adequateProtein, caughtPokemon);

        // Assert
        assertNull("Reward should be null for calories < 100", rewardLowCal);

        // Arrange Protein to be low
        double adequateCalories = 200;
        double lowProtein = 3;

        // Act
        String rewardLowProtein = RewardEngine.computeReward(context, adequateCalories, lowProtein, caughtPokemon);

        // Assert
        assertNull("Reward should be null for protein < 5", rewardLowProtein);

        // Arrange both low
        double bothLowCalories = 90;
        double bothLowProtein = 4;

        // Act
        String rewardBothLow = RewardEngine.computeReward(context, bothLowCalories, bothLowProtein, caughtPokemon);

        // Assert
        assertNull("Reward should be null when both calories and protein are low", rewardBothLow);
    }

    /**
     * Test 7: Verifies that if the "balanced" reward rule conditions are met,
     * but all Pokémon added by that specific rule are already caught,
     * the result is null.
     */
    @Test
    public void computeReward_whenBalancedRuleAppliesAndAllRelevantCaught_returnsNull() {
        // Arrange
        double calories = 300;
        double protein = 15;

        Set<String> allBalancedAndGeneralCaught = new HashSet<>(BALANCED_REWARDS_IN_ENGINE);
        for (int i = 1; i <= 151; i++) {
            allBalancedAndGeneralCaught.add(String.valueOf(i));
        }

        // Act
        String reward = RewardEngine.computeReward(context, calories, protein, allBalancedAndGeneralCaught);

        // Assert
        assertNull("Reward should be null if balanced rule applies and all its rewards + base pool are caught", reward);
    }

    /**
     * Test 8: Verifies that when minimum nutrition is met, but no specific rules apply,
     * the base pool is used, resulting in a non-null reward.
     */
    @Test
    public void computeReward_usesBasePoolWhenNoSpecificRulesMet() {
        // Arrange
        double calories = 150;
        double protein = 8;
        Set<String> noPokemonCaught = new HashSet<>();

        // Act
        String reward = RewardEngine.computeReward(context, calories, protein, noPokemonCaught);

        // Assert
        assertNotNull("Reward should not be null when base pool is used and pokemon are available", reward);
    }

    /**
     * Test 9: Verifies Pokemon you've already caught are filtered out of the reward pool.
     */
    @Test
    public void computeReward_filtersCaughtPokemonFromBalancedPool() {
        // Arrange
        double calories = 250;
        double protein = 12;
        Set<String> almostAllBalancedCaught = new HashSet<>();

        for (int i = 1; i <= 24; i++) {
            almostAllBalancedCaught.add(String.valueOf(i));
        }

        // Act
        String reward;
        boolean caughtPokemonAwarded = false;
        int attempts = 50;
        Set<String> awardedRewards = new HashSet<>();

        for (int i = 0; i < attempts; i++) {
            reward = RewardEngine.computeReward(context, calories, protein, almostAllBalancedCaught);
            assertNotNull("Reward should not be null in this scenario", reward);
            awardedRewards.add(reward);

            if (almostAllBalancedCaught.contains(reward)) {
                caughtPokemonAwarded = true;
                System.err.println("Error: Awarded caught Pokemon: " + reward);
                break;
            }
        }

        // Assert
        assertFalse("Should not award a Pokémon that is in the 'caught' set", caughtPokemonAwarded);
        System.out.println("Filtering test passed: No explicitly caught Pokémon were awarded over " + attempts + " attempts. Awards: " + awardedRewards);
    }

    /**
     * Test 10: Verifies that when high protein rule is met, a non-null reward is returned.
     */
    @Test
    public void computeReward_givesNonNullRewardWhenHighProteinRuleApplies() {
        // Arrange
        double calories = 200;
        double protein = 25;
        Set<String> noPokemonCaught = new HashSet<>();

        // Act
        String reward = RewardEngine.computeReward(context, calories, protein, noPokemonCaught);

        // Assert
        assertNotNull("Reward should not be null when high protein rule applies and pokemon are available", reward);
    }

    /**
     * Test 11: Verifies that when balanced meal rule is met, a non-null reward is returned.
     */
    @Test
    public void computeReward_givesNonNullRewardWhenBalancedRuleApplies() {
        double calories = 300;
        double protein = 18;

        // Arrange
        Set<String> noPokemonCaught = new HashSet<>();

        // Act
        String reward = RewardEngine.computeReward(context, calories, protein, noPokemonCaught);

        // Assert
        assertNotNull("Reward should not be null when balanced rule applies and pokemon are available", reward);
    }
}