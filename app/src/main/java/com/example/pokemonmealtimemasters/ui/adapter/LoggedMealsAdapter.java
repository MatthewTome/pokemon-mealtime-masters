package com.example.pokemonmealtimemasters.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.pokemonmealtimemasters.R;
import com.example.pokemonmealtimemasters.model.LoggedMealModel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Adapter for displaying a list of meals the user has logged today.
 * Each item shows the meal name, calorie count, and the time logged.
 * Supports an optional click listener to handle item taps.
 */
public class LoggedMealsAdapter extends RecyclerView.Adapter<LoggedMealsAdapter.ViewHolder> {
    public interface OnItemClickListener {
        void onItemClick(LoggedMealModel meal);
    }

    private List<LoggedMealModel> data;
    private OnItemClickListener listener;

    public LoggedMealsAdapter(List<LoggedMealModel> data) {
        this.data = data;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    // Updates the adapter's data and refreshes the entire list.
    public void updateData(List<LoggedMealModel> newData) {
        this.data = newData;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View item = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_logged_meal, parent, false);
        return new ViewHolder(item);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LoggedMealModel meal = data.get(position);
        holder.name.setText(meal.getName());
        holder.details.setText(
                String.format(Locale.getDefault(),
                        "%d cal – %s",
                        (int) meal.getCalories(),
                        formatTime(meal.getTimestamp())
                )
        );
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(meal);
            }
        });
    }

    @Override
    public int getItemCount() {
        return data == null ? 0 : data.size();
    }

    // Holds references to the views for each list item to avoid repeated findViewById() calls.
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        TextView details;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            name    = itemView.findViewById(R.id.text_meal_name);
            details = itemView.findViewById(R.id.text_meal_details);
        }
    }

    // Formats a timestamp (ms since epoch) into a user-friendly time string (e.g. "02:30 PM").
    private String formatTime(long ts) {
        return new SimpleDateFormat("hh:mm a", Locale.getDefault())
                .format(new Date(ts));
    }
}