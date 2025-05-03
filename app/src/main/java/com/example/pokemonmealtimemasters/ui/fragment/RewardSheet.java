package com.example.pokemonmealtimemasters.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.pokemonmealtimemasters.R;
import com.example.pokemonmealtimemasters.network.PokeApiService;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import coil.ImageLoader;
import coil.request.ImageRequest;

/**
 * Bottom sheet that congratulates the user with a new Pokémon.
 * We fetch only the Pokémon’s name via PokeAPI, then load the
 * high-res “official-artwork” sprite from GitHub’s sprite repo.
 */
public class RewardSheet extends BottomSheetDialogFragment {
    private static final String ARG_ID = "pokemon_id";
    private String pokemonId;

    public static RewardSheet newInstance(String buddyKey) {
        RewardSheet sheet = new RewardSheet();
        Bundle args = new Bundle();
        args.putString(ARG_ID, buddyKey);
        sheet.setArguments(args);
        return sheet;
    }

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        if (getArguments() != null) {
            pokemonId = getArguments().getString(ARG_ID);
        }
        return inflater.inflate(R.layout.sheet_reward, container, false);
    }

    @Override public void onViewCreated(@NonNull View view,
                                        @Nullable Bundle savedInstanceState) {
        TextView title    = view.findViewById(R.id.text_reward_title);
        ImageView image   = view.findViewById(R.id.image_reward);
        MaterialButton ok = view.findViewById(R.id.button_ok);

        title.setText(R.string.reward_title); // “You caught a new Pokémon!”

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://pokeapi.co/api/v2/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        PokeApiService api = retrofit.create(PokeApiService.class);
        api.getPokemon(pokemonId).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<PokeApiService.Pokemon> call,
                                   @NonNull Response<PokeApiService.Pokemon> resp) {
                String displayName = pokemonId;
                if (resp.isSuccessful() && resp.body() != null) {
                    String name = resp.body().name;
                    displayName = Character.toUpperCase(name.charAt(0)) + name.substring(1);
                }
                title.setText(getString(R.string.reward_caught_format, displayName));

                // Build the high-res official artwork URL
                String artUrl =
                        "https://raw.githubusercontent.com/PokeAPI/sprites/master/" +
                                "sprites/pokemon/other/official-artwork/" +
                                pokemonId + ".png";

                // Load with Coil
                ImageLoader loader = new ImageLoader.Builder(requireContext()).build();
                ImageRequest req = new ImageRequest.Builder(requireContext())
                        .data(artUrl)
                        .crossfade(true)
                        .placeholder(R.drawable.pokeball_silhouette)
                        .target(image)
                        .build();
                loader.enqueue(req);
            }

            @Override
            public void onFailure(@NonNull Call<PokeApiService.Pokemon> call,
                                  @NonNull Throwable t) {
                title.setText(getString(R.string.reward_caught_format, pokemonId));
                image.setImageResource(R.drawable.pokeball_silhouette);
            }
        });

        ok.setOnClickListener(v -> dismiss());
    }
}