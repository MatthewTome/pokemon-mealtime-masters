package com.example.pokemonmealtimemasters.ui.adapter;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import com.example.pokemonmealtimemasters.model.LoggedMealModel;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

/**
 * Tests for the LoggedMealsAdapter.
 * Verifies formatting, item count, and data updates.
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE, sdk = 28)
public class LoggedMealsAdapterTest {

    private LoggedMealsAdapter adapterInstanceForTesting;

    @Before
    public void setUp() {
        adapterInstanceForTesting = new LoggedMealsAdapter(Collections.emptyList());
    }

    private String invokeFormatTime(long timestamp) throws Exception {
        Method method = LoggedMealsAdapter.class.getDeclaredMethod("formatTime", long.class);
        method.setAccessible(true);
        return (String) method.invoke(adapterInstanceForTesting, timestamp);
    }

    /**
     * Test 1: Verifies formatTime correctly formats a timestamp.
     */
    @Test
    public void formatTime_correctlyFormatsTimestamp() throws Exception {
        // Arrange
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 9);
        calendar.set(Calendar.MINUTE, 5);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long timestampAM_Local = calendar.getTimeInMillis();
        String expectedFormat = "09:05 AM";

        // Act
        String formattedTimeAM = invokeFormatTime(timestampAM_Local);

        // Assert
        assertEquals("Expected " + expectedFormat, expectedFormat, formattedTimeAM);
    }

    /**
     * Test 2: Verifies updateData changes the adapter's data and getItemCount reflects it.
     */
    @Test
    public void updateData_updatesInternalDataAndItemCount() {
        // Arrange
        List<LoggedMealModel> initialData = Arrays.asList(
                new LoggedMealModel("Meal A", 100, 10, 5, System.currentTimeMillis()),
                new LoggedMealModel("Meal B", 200, 20, 10, System.currentTimeMillis())
        );
        LoggedMealsAdapter adapter = new LoggedMealsAdapter(initialData);

        // Assert
        assertEquals("Initial item count should be 2", 2, adapter.getItemCount());

        // Arrange
        List<LoggedMealModel> newData = Collections.singletonList(
                new LoggedMealModel("Meal C", 300, 30, 15, System.currentTimeMillis())
        );

        // Act
        adapter.updateData(newData);

        // Assert
        assertEquals("Item count should be 1 after updateData", 1, adapter.getItemCount());

        // Arrange
        adapter.updateData(null);

        // Assert
        assertEquals("Item count should be 0 after updating data to null", 0, adapter.getItemCount());
    }

    /**
     * Test 3: Verifies getItemCount returns 0 when adapter data is null.
     */
    @Test
    public void getItemCount_whenDataIsNull_returnsZero() {
        // Arrange
        LoggedMealsAdapter adapter = new LoggedMealsAdapter(null);

        // Act
        int count = adapter.getItemCount();

        // Assert
        assertEquals("Item count should be 0 for null data", 0, count);
    }

    /**
     * Test 4: Verifies getItemCount returns 0 when adapter data is an empty list.
     */
    @Test
    public void getItemCount_whenDataIsEmptyList_returnsZero() {
        // Arrange
        LoggedMealsAdapter adapter = new LoggedMealsAdapter(Collections.emptyList());

        // Act
        int count = adapter.getItemCount();

        // Assert
        assertEquals("Item count should be 0 for empty list data", 0, count);
    }

    /**
     * Test 5: Verifies getItemCount returns the correct size for a list with items.
     */
    @Test
    public void getItemCount_whenDataHasItems_returnsCorrectSize() {
        // Arrange
        List<LoggedMealModel> data = Arrays.asList(
                new LoggedMealModel("Apple", 95, 0.5, 19, System.currentTimeMillis()),
                new LoggedMealModel("Chicken", 335, 30, 0, System.currentTimeMillis())
        );
        LoggedMealsAdapter adapter = new LoggedMealsAdapter(data);

        // Act
        int count = adapter.getItemCount();

        // Assert
        assertEquals("Item count should match data size", 2, count);
    }
}