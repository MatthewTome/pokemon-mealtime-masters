package com.example.pokemonmealtimemasters.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.pokemonmealtimemasters.R;
import com.example.pokemonmealtimemasters.model.FoodSearchResponseModel;
import java.util.List;

/**
 * Adapter for displaying a list of foods returned from the search API.
 * Each item shows the food description and notifies a listener when tapped.
 */
public class FoodSearchResponseAdapter extends RecyclerView.Adapter<FoodSearchResponseAdapter.ViewHolder> {
    public interface OnItemClickListener {
        void onItemClick(FoodSearchResponseModel.FoodItem item);
    }

    private List<FoodSearchResponseModel.FoodItem> data;
    private OnItemClickListener listener;

    public FoodSearchResponseAdapter(List<FoodSearchResponseModel.FoodItem> data) {
        this.data = data;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public List<FoodSearchResponseModel.FoodItem> getData() {
        return data;
    }

    public void updateData(List<FoodSearchResponseModel.FoodItem> newData) {
        this.data = newData;
        notifyDataSetChanged();
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
        FoodSearchResponseModel.FoodItem item = data.get(position);
        holder.name.setText(item.getDescription());
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return data == null ? 0 : data.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.text_food_name);
        }
    }
}