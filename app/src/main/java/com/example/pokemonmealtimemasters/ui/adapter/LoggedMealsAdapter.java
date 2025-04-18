// File: app/src/main/java/com/example/pokemonmealtimemasters/ui/adapter/LoggedMealsAdapter.java
package com.example.pokemonmealtimemasters.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pokemonmealtimemasters.R;
import com.example.pokemonmealtimemasters.model.LoggedMeal;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class LoggedMealsAdapter
        extends RecyclerView.Adapter<LoggedMealsAdapter.ViewHolder> {

    // 1) Listener interface
    public interface OnItemClickListener {
        void onItemClick(LoggedMeal meal);
    }

    // 2) Member fields
    private List<LoggedMeal> data;
    private OnItemClickListener listener;

    // 3) Constructor
    public LoggedMealsAdapter(List<LoggedMeal> data) {
        this.data = data;
    }

    // 4) Setter for the click listener
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    // 5) Update data method
    public void updateData(List<LoggedMeal> newData) {
        this.data = newData;
        notifyDataSetChanged();
    }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                         int viewType) {
        View item = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_logged_meal, parent, false);
        return new ViewHolder(item);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder,
                                 int position) {
        LoggedMeal meal = data.get(position);
        holder.name.setText(meal.getName());
        holder.details.setText(
                String.format(Locale.getDefault(),
                        "%d cal – %s",
                        (int)meal.getCalories(),
                        formatTime(meal.getTimestamp()))
        );

        // wire up click
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(meal);
        });
    }

    @Override
    public int getItemCount() {
        return data == null ? 0 : data.size();
    }

    // ViewHolder
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, details;
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            name    = itemView.findViewById(R.id.text_meal_name);
            details = itemView.findViewById(R.id.text_meal_details);
        }
    }

    // helper to format timestamp
    private String formatTime(long ts) {
        return new SimpleDateFormat("hh:mm a", Locale.getDefault())
                .format(new Date(ts));
    }
}