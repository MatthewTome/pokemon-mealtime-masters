package com.example.pokemonmealtimemasters.ui.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pokemonmealtimemasters.R;
import com.example.pokemonmealtimemasters.model.BadgeModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

/**
 * Adapter for displaying earned badges in a RecyclerView grid.
 */
public class BadgesAdapter extends RecyclerView.Adapter<BadgesAdapter.BadgeVH>
{
    private final List<BadgeModel> badges;
    private final Context          ctx;

    public BadgesAdapter(List<BadgeModel> badges, Context ctx)
    {
        this.badges = badges;
        this.ctx    = ctx;
    }

    @NonNull
    @Override
    public BadgeVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_badge, parent, false);
        return new BadgeVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull BadgeVH h, int pos)
    {
        BadgeModel b   = badges.get(pos);
        @DrawableRes
        int resId      = getDrawableId(b.getId(), b.isEarned());

        h.image.setImageResource(resId);
        h.image.setAlpha(b.isEarned() ? 1f : 0.25f);

        h.itemView.setOnClickListener(v ->
        {
            if (!b.isEarned())
            {
                Toast.makeText(ctx, R.string.badge_locked, Toast.LENGTH_SHORT).show();
                return;
            }
            showPopup(b);
        });
    }

    @Override
    public int getItemCount()
    {
        return badges.size();
    }

    @SuppressLint("DiscouragedApi")
    @DrawableRes
    private int getDrawableId(String id, boolean earned)
    {
        String name   = earned ? ("badge_" + id.toLowerCase()) : "ic_badge_silhouette";
        int    resId  = ctx.getResources().getIdentifier(name, "drawable", ctx.getPackageName());
        return resId != 0 ? resId : R.drawable.pokeball_silhouette;
    }

    private void showPopup(BadgeModel b)
    {
        View v = LayoutInflater.from(ctx).inflate(R.layout.popup_badge, null);

        ((ImageView) v.findViewById(R.id.popupBadgeImage))
                .setImageResource(getDrawableId(b.getId(), true));

        ((TextView) v.findViewById(R.id.popupBadgeTitle))
                .setText(b.getTitle());

        ((TextView) v.findViewById(R.id.popupBadgeDesc))
                .setText(b.getDescription());

        String date = DateFormat.getDateInstance().format(new Date(b.getAwardedAt()));
        ((TextView) v.findViewById(R.id.popupBadgeDate))
                .setText(ctx.getString(R.string.badge_awarded_on, date));

        new MaterialAlertDialogBuilder(ctx)
                .setView(v)
                .show();
    }

    public static class BadgeVH extends RecyclerView.ViewHolder
    {
        final ImageView image;

        public BadgeVH(@NonNull View itemView)
        {
            super(itemView);
            image = itemView.findViewById(R.id.badgeImage);
        }
    }
}