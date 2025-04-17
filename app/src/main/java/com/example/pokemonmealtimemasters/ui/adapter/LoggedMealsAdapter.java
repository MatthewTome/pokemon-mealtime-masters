package com.example.pokemonmealtimemasters.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pokemonmealtimemasters.R;
import com.example.pokemonmealtimemasters.model.LoggedMeal;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

public class LoggedMealsAdapter extends RecyclerView.Adapter<LoggedMealsAdapter.ViewHolder>
{
    private List<LoggedMeal> meals;

    public LoggedMealsAdapter(List<LoggedMeal> meals)
    {
        this.meals = meals;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_logged_meal, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position)
    {
        LoggedMeal meal = meals.get(position);

        holder.nameText.setText(meal.getName());
        holder.caloriesText.setText(
                String.format("Calories: %.0f", meal.getCalories())
        );

        // Format timestamp to a short time string, e.g. "2:45 PM"
        String time = DateFormat.getTimeInstance(DateFormat.SHORT)
                .format(new Date(meal.getTimestamp()));
        holder.timeText.setText(time);
    }

    @Override
    public int getItemCount()
    {
        return meals.size();
    }

    /**
     * Replace the current list of meals and refresh.
     */
    public void updateData(List<LoggedMeal> newMeals)
    {
        meals.clear();
        meals.addAll(newMeals);
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder
    {
        TextView nameText;
        TextView caloriesText;
        TextView timeText;

        ViewHolder(@NonNull View itemView)
        {
            super(itemView);
            nameText     = itemView.findViewById(R.id.text_meal_name);
            caloriesText = itemView.findViewById(R.id.text_meal_calories);
            timeText     = itemView.findViewById(R.id.text_meal_time);
        }
    }
}