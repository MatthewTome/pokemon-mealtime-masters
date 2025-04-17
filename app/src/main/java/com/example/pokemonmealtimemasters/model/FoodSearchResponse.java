package com.example.pokemonmealtimemasters.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class FoodSearchResponse {
    @SerializedName("totalHits")
    private int totalHits;

    @SerializedName("foods")
    private List<FoodItem> foods;

    public int getTotalHits() {
        return totalHits;
    }

    public void setTotalHits(int totalHits) {
        this.totalHits = totalHits;
    }

    public List<FoodItem> getFoods() {
        return foods;
    }

    public void setFoods(List<FoodItem> foods) {
        this.foods = foods;
    }

    public static class FoodItem {
        @SerializedName("fdcId")
        private String fdcId;

        @SerializedName("description")
        private String description;

        @SerializedName("foodNutrients")
        private List<FoodNutrient> foodNutrients;

        public String getFdcId() {
            return fdcId;
        }

        public void setFdcId(String fdcId) {
            this.fdcId = fdcId;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public List<FoodNutrient> getFoodNutrients() {
            return foodNutrients;
        }

        public void setFoodNutrients(List<FoodNutrient> foodNutrients) {
            this.foodNutrients = foodNutrients;
        }

        public static class FoodNutrient {
            @SerializedName("nutrientName")
            private String nutrientName;

            @SerializedName("value")
            private double value;

            public String getNutrientName() {
                return nutrientName;
            }

            public void setNutrientName(String nutrientName) {
                this.nutrientName = nutrientName;
            }

            public double getValue() {
                return value;
            }

            public void setValue(double value) {
                this.value = value;
            }
        }
    }
}