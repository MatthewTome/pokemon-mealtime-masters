package com.example.pokemonmealtimemasters.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;

/**
 * Model for the FoodData Central search response.
 * Contains a list of matching FoodItem entries.
 */
public class FoodSearchResponse {
    @SerializedName("foods")
    private List<FoodItem> foods;

    public List<FoodItem> getFoods() {
        return foods;
    }

    public static class FoodItem implements Serializable {
        @SerializedName("fdcId")
        private String fdcId;

        @SerializedName("description")
        private String description;

        @SerializedName("foodNutrients")
        private List<FoodNutrient> foodNutrients;

        public String getFdcId() {
            return fdcId;
        }

        public String getDescription() {
            return description;
        }

        public List<FoodNutrient> getFoodNutrients() {
            return foodNutrients;
        }

        public static class FoodNutrient implements Serializable {
            @SerializedName("nutrientName")
            private String nutrientName;

            @SerializedName("value")
            private double value;

            public String getNutrientName() {
                return nutrientName;
            }

            public double getValue() {
                return value;
            }
        }
    }
}