package com.example.pokemonmealtimemasters.network;

import android.content.Context;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import retrofit2.Response;

/**
 * Caches Pokédex-ID lists for each type we care about so we only hit
 * the network once per type during an app session.
 */
public final class PokemonTypeRepository
{
    private static PokemonTypeRepository instance;

    private final PokeApiService api;
    private final HashMap<String, List<String>> cache = new HashMap<>();

    private PokemonTypeRepository(Context ctx)
    {
        api = PokeApiClient.getClient(ctx).create(PokeApiService.class);
    }

    public static synchronized PokemonTypeRepository getInstance(Context ctx)
    {
        if (instance == null)
        {
            instance = new PokemonTypeRepository(ctx.getApplicationContext());
        }
        return instance;
    }

    /**
     * Returns *Gen-1 only* IDs for the requested type.
     * Slow on first call (network) – call this off the main thread!
     */
    public List<String> getIdsForType(String typeName)
    {
        typeName = typeName.toLowerCase();

        if (cache.containsKey(typeName))
        {
            return new ArrayList<>(Objects.requireNonNull(cache.get(typeName)));
        }

        List<String> ids = fetchIds(typeName);
        cache.put(typeName, ids);
        return new ArrayList<>(ids);
    }

    private List<String> fetchIds(String typeName)
    {
        try
        {
            Response<PokeApiService.TypeResponse> res =
                    api.getType(typeName).execute();

            if (!res.isSuccessful() || res.body() == null)
            {
                return Collections.emptyList();
            }

            List<String> ids = new ArrayList<>();

            for (PokeApiService.TypeResponse.PokemonEntry entry : res.body().pokemon)
            {
                String url = entry.pokemon.url;   // …/pokemon/66/
                String idStr = url.replaceAll(".*/pokemon/(\\d+)/?$", "$1");

                try
                {
                    int id = Integer.parseInt(idStr);
                    if (id >= 1 && id <= 151)
                    {
                        ids.add(String.valueOf(id));
                    }
                }
                catch (NumberFormatException ignore)
                {
                }
            }
            return ids;
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
}