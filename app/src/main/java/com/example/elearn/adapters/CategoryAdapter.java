package com.example.elearn.adapters;

import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.elearn.R;
import com.example.elearn.models.Category;

import java.util.List;

/**
 * RecyclerView adapter for displaying category cards in a grid layout.
 * Each card shows a colored icon and the category name.
 */
public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {

    private final List<Category> categories;
    private final OnCategoryClickListener listener;

    private static final int[] COLORS = {
            0xFF1976D2,
            0xFF388E3C,
            0xFFF57C00,
            0xFF7B1FA2,
            0xFFC62828,
            0xFF00838F
    };

    /**
     * Interface for handling category card tap events.
     */
    public interface OnCategoryClickListener {
        void onCategoryClick(Category category);
    }

    public CategoryAdapter(List<Category> categories, OnCategoryClickListener listener) {
        this.categories = categories;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Category category = categories.get(position);

        holder.categoryName.setText(category.getName());

        int color = COLORS[position % COLORS.length];
        holder.categoryIcon.setBackground(new ColorDrawable(color));

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCategoryClick(category);
            }
        });
    }

    @Override
    public int getItemCount() {
        return categories != null ? categories.size() : 0;
    }

    /**
     * ViewHolder for category card items.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView categoryIcon;
        final TextView categoryName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryIcon = itemView.findViewById(R.id.categoryIcon);
            categoryName = itemView.findViewById(R.id.categoryName);
        }
    }
}
