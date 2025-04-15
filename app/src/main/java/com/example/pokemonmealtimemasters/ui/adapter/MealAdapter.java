package com.example.pokemonmealtimemasters.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.pokemonmealtimemasters.R;
import com.example.pokemonmealtimemasters.model.FoodSearchResponse;
import java.util.ArrayList;
import java.util.List;

public class MealAdapter extends RecyclerView.Adapter<MealAdapter.ViewHolder> {

    // List to hold our food items. Using the FoodItem model from FoodSearchResponse.
    private List<FoodSearchResponse.FoodItem> foodItems;

    // Constructor for the adapter. It receives a list of food items.
    public MealAdapter(List<FoodSearchResponse.FoodItem> foodItems) {
        this.foodItems = (foodItems != null) ? foodItems : new ArrayList<>();
    }

    // ViewHolder class manages individual item views.
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView textDescription;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textDescription = itemView.findViewById(R.id.text_description);
        }
    }

    @NonNull
    @Override
    public MealAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout for individual list items.
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_food, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MealAdapter.ViewHolder holder, int position) {
        // Get the food item for the current position.
        FoodSearchResponse.FoodItem foodItem = foodItems.get(position);
        // Set the text description.
        holder.textDescription.setText(foodItem.getDescription());
    }

    @Override
    public int getItemCount() {
        return foodItems.size();
    }

    // Method to update the data in the adapter and refresh the list.
    public void updateData(List<FoodSearchResponse.FoodItem> newItems) {
        foodItems.clear();
        if (newItems != null) {
            foodItems.addAll(newItems);
        }
        notifyDataSetChanged();
    }
}