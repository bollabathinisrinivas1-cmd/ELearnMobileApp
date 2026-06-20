package com.example.elearn.adapters;

import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.elearn.R;
import com.example.elearn.models.Category;

import java.util.List;

/**
 * RecyclerView adapter for displaying category cards in a grid layout.
 * Each card shows a colored circle with the first letter and the category name.
 */
public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {

    private final List<Category> categories;
    private final OnCategoryClickListener listener;

    private static final int[] COLORS = {
            0xFF1976D2, 0xFF4CAF50, 0xFFFF9800, 0xFFE91E63,
            0xFF9C27B0, 0xFF00BCD4, 0xFF795548, 0xFF607D8B
    };

    public interface OnCategoryClickListener {
        void onCategoryClick(Category category);
    }

    public interface OnCategoryLongClickListener {
        void onCategoryLongClick(Category category);
    }

    private final OnCategoryLongClickListener longClickListener;

    public CategoryAdapter(List<Category> categories, OnCategoryClickListener listener) {
        this(categories, listener, null);
    }

    public CategoryAdapter(List<Category> categories, OnCategoryClickListener listener, OnCategoryLongClickListener longClickListener) {
        this.categories = categories;
        this.listener = listener;
        this.longClickListener = longClickListener;
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

        // Show first letter of category name in a colored circle
        String name = category.getName();
        String initial = name != null && !name.isEmpty() ? name.substring(0, 1).toUpperCase() : "?";
        holder.categoryIcon.setText(initial);

        int color = COLORS[(category.getId() - 1) % COLORS.length];
        GradientDrawable circle = new GradientDrawable();
        circle.setShape(GradientDrawable.OVAL);
        circle.setColor(color);
        holder.categoryIcon.setBackground(circle);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCategoryClick(category);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) {
                longClickListener.onCategoryLongClick(category);
                return true;
            }
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return categories != null ? categories.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView categoryIcon;
        final TextView categoryName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryIcon = itemView.findViewById(R.id.categoryIcon);
            categoryName = itemView.findViewById(R.id.categoryName);
        }
    }
}
