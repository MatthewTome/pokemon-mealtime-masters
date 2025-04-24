package com.example.pokemonmealtimemasters.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pokemonmealtimemasters.R;
import com.example.pokemonmealtimemasters.model.PokemonModel;

import java.util.List;

import coil.Coil;
import coil.ImageLoader;
import coil.request.ImageRequest;

/**
 * Adapter for displaying caught Pokémon in a RecyclerView grid.
 * Loads sprite images via Coil and displays the Pokémon’s name.
 */
public class PokemonAdapter extends RecyclerView.Adapter<PokemonAdapter.ViewHolder> {
    private final List<PokemonModel> data;

    public PokemonAdapter(List<PokemonModel> data) {
        this.data = data;
    }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pokemon, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int pos) {
        PokemonModel p = data.get(pos);
        holder.name.setText(p.getName());

        ImageLoader loader = Coil.imageLoader(holder.sprite.getContext());
        ImageRequest request = new ImageRequest.Builder(holder.sprite.getContext())
                .data(p.getSpriteUrl())
                .placeholder(R.drawable.ic_placeholder)
                .crossfade(true)
                .target(holder.sprite)
                .build();
        loader.enqueue(request);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView sprite;
        TextView name;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            sprite = itemView.findViewById(R.id.image_pokemon_sprite);
            name   = itemView.findViewById(R.id.text_pokemon_name);
        }
    }
}