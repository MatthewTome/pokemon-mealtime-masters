package com.example.pokemonmealtimemasters.utils;

import android.content.SharedPreferences;
import com.example.pokemonmealtimemasters.model.BadgeModel;
import java.util.*;

/**
 * Awards and persists badge progress.
 *  • Pokémon-catch badges – first catch and every 10 caught.
 *  • Meal-logging streak badges – 3, 5, 7 days straight.
 */
public class BadgeUtils
{
    private static final String PREF_BADGES = "earned_badges";

    // Ensure prefs contain every badge the player now qualifies for
    public static void updateEarnedBadges(SharedPreferences prefs)
    {
        Set<String> raw   = new HashSet<>(prefs.getStringSet(PREF_BADGES, new HashSet<>()));
        long now          = System.currentTimeMillis();

        Set<String> caught   = prefs.getStringSet("caught_pokemon_ids", new HashSet<>());
        int          caughtN = caught.size();
        int          streak  = prefs.getInt("meal_streak", 0);

        // Award Pokémon-catch badges
        if (caughtN >= 1)         raw.add("FIRST_POKEMON|" + now);
        for (int i = 10; i <= caughtN; i += 10)
        {
            raw.add("CAUGHT_" + i + "|" + now);
        }

        // Award meal-streak badges
        if (streak >= 3)          raw.add("STREAK_3|" + now);
        if (streak >= 5)          raw.add("STREAK_5|" + now);
        if (streak >= 7)          raw.add("STREAK_7|" + now);

        prefs.edit().putStringSet(PREF_BADGES, raw).apply();
    }

    public static Map<String, Long> getEarnedBadgeMap(SharedPreferences prefs)
    {
        Map<String, Long> map   = new HashMap<>();
        for (String entry : prefs.getStringSet(PREF_BADGES, new HashSet<>()))
        {
            String[] parts = entry.split("\\|");
            if (parts.length == 2)
            {
                map.put(parts[0], Long.parseLong(parts[1]));
            }
        }
        return map;
    }

    public static List<BadgeModel> getAllBadges(Map<String, Long> earned)
    {
        List<BadgeModel> list = new ArrayList<>();

        // First catch
        list.add(make("FIRST_POKEMON", "First Catch",
                "Catch your first Pokémon.", earned));

        // Every 10 caught (up to original 151)
        for (int i = 10; i <= 150; i += 10)
        {
            list.add(make("CAUGHT_" + i,
                    i + " Pokémon!",
                    "Catch " + i + " Pokémon.", earned));
        }

        // Meal-logging streaks
        list.add(make("STREAK_3", "3-Day Meal Streak",
                "Log meals 3 days in a row.", earned));
        list.add(make("STREAK_5", "5-Day Meal Streak",
                "Log meals 5 days in a row.", earned));
        list.add(make("STREAK_7", "7-Day Meal Streak",
                "Log meals 7 days in a row.", earned));

        return list;
    }

    private static BadgeModel make(String id, String title, String desc, Map<String, Long> earned)
    {
        return new BadgeModel(id, title, desc,
                earned.containsKey(id) ? earned.get(id) : -1);
    }
}