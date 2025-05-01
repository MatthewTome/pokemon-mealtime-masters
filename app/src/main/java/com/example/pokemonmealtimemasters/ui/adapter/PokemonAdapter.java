package com.example.pokemonmealtimemasters.ui.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.pokemonmealtimemasters.R;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import coil.Coil;
import coil.ImageLoader;
import coil.request.ImageRequest;

/**
 * Adapter for displaying caught Pokémon in a RecyclerView grid.
 * Loads sprite images via Coil and displays the Pokémon’s name.
 */
public class PokemonAdapter extends RecyclerView.Adapter<PokemonAdapter.ViewHolder> {

    private final List<String> pokemonIds; // All Pokémon IDs you want
    private final Set<String> caughtPokemonIds; // IDs the user caught
    private final Context context;

    public PokemonAdapter(List<String> pokemonIds, Set<String> caughtPokemonIds, Context context) {
        this.pokemonIds = pokemonIds;
        this.caughtPokemonIds = caughtPokemonIds;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pokemon, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String id = pokemonIds.get(position);
        boolean isCaught = caughtPokemonIds.contains(id);

        String imageUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/" + id + ".png";

        ImageLoader loader = Coil.imageLoader(context);
        ImageRequest request = new ImageRequest.Builder(context)
                .data(imageUrl)
                .placeholder(R.drawable.pokeball_silhouette)
                .crossfade(true)
                .target(holder.pokemonImage)
                .build();
        loader.enqueue(request);

        if (!isCaught) {
            holder.pokemonImage.setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_IN);
            holder.pokemonImage.setAlpha(0.3f);  // dark silhouette effect
        } else {
            holder.pokemonImage.clearColorFilter();
            holder.pokemonImage.setAlpha(1f);
        }

        holder.itemView.setOnClickListener(v -> {
            if (isCaught) {
                showDetailsPopup(id);
            } else {
                Toast.makeText(context, "Pokémon not yet caught!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDetailsPopup(String id) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.popup_pokemon_detail, null);
        builder.setView(dialogView);

        ImageView pokemonImage = dialogView.findViewById(R.id.popupPokemonImage);
        TextView pokemonName = dialogView.findViewById(R.id.popupPokemonName);
        TextView pokemonType = dialogView.findViewById(R.id.popupPokemonType);
        TextView pokemonRegion = dialogView.findViewById(R.id.popupPokemonRegion);

        // Load Pokémon image
        String imageUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/" + id + ".png";
        Coil.imageLoader(context).enqueue(new ImageRequest.Builder(context)
                .data(imageUrl)
                .placeholder(R.drawable.pokeball_silhouette)
                .target(pokemonImage)
                .build());

        // Fetch details from PokeAPI
        fetchPokemonDetails(id, pokemonName, pokemonType, pokemonRegion);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private String getGeneration(String id) {
        int numericId = Integer.parseInt(id);
        if (numericId <= 151) return "1";
        else if (numericId <= 251) return "2";
        else if (numericId <= 386) return "3";
        else if (numericId <= 493) return "4";
        else if (numericId <= 649) return "5";
        else if (numericId <= 721) return "6";
        else if (numericId <= 809) return "7";
        else if (numericId <= 905) return "8";
        else return "9";
    }

    private void fetchPokemonDetails(String id, TextView nameView, TextView typeView, TextView regionView) {
        String url = "https://pokeapi.co/api/v2/pokemon/" + id;

        RequestQueue queue = Volley.newRequestQueue(context);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        // Set Pokémon Name
                        String name = response.getString("name");
                        nameView.setText(capitalize(name));

                        // Get Pokémon Types
                        JSONArray typesArray = response.getJSONArray("types");
                        List<String> typesList = new ArrayList<>();
                        for (int i = 0; i < typesArray.length(); i++) {
                            JSONObject typeObject = typesArray.getJSONObject(i).getJSONObject("type");
                            typesList.add(capitalize(typeObject.getString("name")));
                        }
                        typeView.setText("Type: " + TextUtils.join(", ", typesList));

                        // Get Region/Generation
                        fetchRegionInfo(id, regionView, queue);

                    } catch (JSONException e) {
                        typeView.setText("Type: Unknown");
                        regionView.setText("Region: Unknown");
                    }
                },
                error -> {
                    typeView.setText("Type: Unknown");
                    regionView.setText("Region: Unknown");
                });

        queue.add(request);
    }

    private void fetchRegionInfo(String id, TextView regionView, RequestQueue queue) {
        String speciesUrl = "https://pokeapi.co/api/v2/pokemon-species/" + id;

        JsonObjectRequest speciesRequest = new JsonObjectRequest(Request.Method.GET, speciesUrl, null,
                response -> {
                    try {
                        JSONObject generation = response.getJSONObject("generation");
                        String genName = generation.getString("name");

                        String region = generationToRegion(genName);
                        regionView.setText("Region: " + region);

                    } catch (JSONException e) {
                        regionView.setText("Region: Unknown");
                    }
                },
                error -> regionView.setText("Region: Unknown"));

        queue.add(speciesRequest);
    }

    private String generationToRegion(String gen) {
        switch (gen) {
            case "generation-i": return "Kanto (Generation 1)";
            case "generation-ii": return "Johto (Generation 2)";
            case "generation-iii": return "Hoenn (Generation 3)";
            case "generation-iv": return "Sinnoh (Generation 4)";
            case "generation-v": return "Unova (Generation 5)";
            case "generation-vi": return "Kalos (Generation 6)";
            case "generation-vii": return "Alola (Generation 7)";
            case "generation-viii": return "Galar (Generation 8)";
            case "generation-ix": return "Paldea (Generation 9)";
            default: return "Unknown Region";
        }
    }

    private String capitalize(String text) {
        return text.substring(0, 1).toUpperCase() + text.substring(1).toLowerCase();
    }

    @Override
    public int getItemCount() {
        return pokemonIds.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView pokemonImage;

        ViewHolder(View view) {
            super(view);
            pokemonImage = view.findViewById(R.id.pokemonImage);
        }
    }
}