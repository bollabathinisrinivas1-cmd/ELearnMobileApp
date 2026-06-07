package com.example.elearn.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.elearn.R;
import com.example.elearn.models.DashboardCard;

import java.util.List;

/**
 * RecyclerView adapter for displaying dashboard cards in a grid layout.
 * Each card shows an icon, title, subtitle, and count value.
 */
public class DashboardCardAdapter extends RecyclerView.Adapter<DashboardCardAdapter.ViewHolder> {

    private final List<DashboardCard> cards;
    private final OnCardClickListener listener;

    /**
     * Interface for handling card tap events.
     */
    public interface OnCardClickListener {
        void onCardClick(DashboardCard card);
    }

    public DashboardCardAdapter(List<DashboardCard> cards, OnCardClickListener listener) {
        this.cards = cards;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_dashboard_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DashboardCard card = cards.get(position);
        Context context = holder.itemView.getContext();

        holder.cardTitle.setText(card.getTitle());
        holder.cardSubtitle.setText(card.getSubtitle());
        holder.cardCount.setText(String.valueOf(card.getCount()));
        holder.cardIcon.setColorFilter(ContextCompat.getColor(context, card.getColorResId()));

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCardClick(card);
            }
        });
    }

    @Override
    public int getItemCount() {
        return cards != null ? cards.size() : 0;
    }

    /**
     * ViewHolder for dashboard card items.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView cardIcon;
        final TextView cardTitle;
        final TextView cardSubtitle;
        final TextView cardCount;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardIcon = itemView.findViewById(R.id.cardIcon);
            cardTitle = itemView.findViewById(R.id.cardTitle);
            cardSubtitle = itemView.findViewById(R.id.cardSubtitle);
            cardCount = itemView.findViewById(R.id.cardCount);
        }
    }
}
