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

    // Listener interface for click events
    public interface OnItemClickListener {
        void onItemClick(FoodSearchResponse.FoodItem item);
    }

    private List<FoodSearchResponse.FoodItem> foodItems;
    private OnItemClickListener listener;

    public MealAdapter(List<FoodSearchResponse.FoodItem> foodItems) {
        this.foodItems = (foodItems != null) ? foodItems : new ArrayList<>();
    }

    /**
     * Set the click listener for adapter items.
     */
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_food, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FoodSearchResponse.FoodItem foodItem = foodItems.get(position);
        holder.textDescription.setText(foodItem.getDescription());

        // Handle item click
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(foodItem);
            }
        });
    }

    @Override
    public int getItemCount() {
        return foodItems.size();
    }

    /**
     * Update the adapter's data set.
     */
    public void updateData(List<FoodSearchResponse.FoodItem> newItems) {
        foodItems.clear();
        if (newItems != null) {
            foodItems.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    /**
     * ViewHolder class for food items.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView textDescription;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textDescription = itemView.findViewById(R.id.text_description);
        }
    }
}