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
    }
}