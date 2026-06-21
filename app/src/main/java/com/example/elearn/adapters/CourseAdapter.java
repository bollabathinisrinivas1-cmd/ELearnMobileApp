package com.example.elearn.adapters;

import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.elearn.R;
import com.example.elearn.models.Course;

import java.util.List;

/**
 * RecyclerView adapter for displaying course cards in a grid layout.
 * Each card shows a colored icon circle, course title, price badge, duration,
 * and a gradient "View Details" button colored based on free/paid status.
 */
public class CourseAdapter extends RecyclerView.Adapter<CourseAdapter.ViewHolder> {

    private final List<Course> courses;
    private final OnCourseClickListener listener;

    private static final int[] ICON_COLORS = {
            0xFF5B4FCF, 0xFF1976D2, 0xFF4CAF50, 0xFFFF9800,
            0xFFE91E63, 0xFF00BCD4, 0xFF9C27B0, 0xFF795548
    };

    public interface OnCourseClickListener {
        void onCourseClick(Course course);
    }

    public interface OnCourseLongClickListener {
        void onCourseLongClick(Course course);
    }

    private final OnCourseLongClickListener longClickListener;

    public CourseAdapter(List<Course> courses, OnCourseClickListener listener) {
        this(courses, listener, null);
    }

    public CourseAdapter(List<Course> courses, OnCourseClickListener listener, OnCourseLongClickListener longClickListener) {
        this.courses = courses;
        this.listener = listener;
        this.longClickListener = longClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_course_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Course course = courses.get(position);

        // Course icon: first letter in colored circle
        String title = course.getTitle();
        String initial = (title != null && !title.isEmpty()) ? title.substring(0, 1).toUpperCase() : "C";
        holder.courseIcon.setText(initial);

        int iconColor = ICON_COLORS[position % ICON_COLORS.length];
        GradientDrawable iconBg = new GradientDrawable();
        iconBg.setShape(GradientDrawable.OVAL);
        iconBg.setColor(iconColor);
        holder.courseIcon.setBackground(iconBg);

        // Course title
        holder.courseTitle.setText(course.getTitle());

        // Price badge
        if (course.isFree()) {
            holder.coursePrice.setText("Free");
            holder.coursePrice.setTextColor(0xFF388E3C);
            holder.coursePrice.setBackgroundResource(R.drawable.badge_green);
        } else {
            holder.coursePrice.setText(String.format("₹%.2f", course.getPrice()));
            holder.coursePrice.setTextColor(0xFFE91E63);
            holder.coursePrice.setBackgroundResource(R.drawable.badge_pink);
        }

        // Duration with clock emoji
        holder.courseDuration.setText(String.format("⏱ %.1f hours", course.getDurationHours()));

        // View Details button: blue/purple gradient for free, pink gradient for paid
        if (course.isFree()) {
            holder.viewDetailsButton.setBackgroundResource(R.drawable.gradient_button_blue);
        } else {
            holder.viewDetailsButton.setBackgroundResource(R.drawable.gradient_button_pink);
        }

        holder.viewDetailsButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCourseClick(course);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) {
                longClickListener.onCourseLongClick(course);
                return true;
            }
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return courses != null ? courses.size() : 0;
    }

    /**
     * ViewHolder for course card items.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView courseIcon;
        final TextView courseTitle;
        final TextView coursePrice;
        final TextView courseDuration;
        final Button viewDetailsButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            courseIcon = itemView.findViewById(R.id.courseIcon);
            courseTitle = itemView.findViewById(R.id.courseTitle);
            coursePrice = itemView.findViewById(R.id.coursePrice);
            courseDuration = itemView.findViewById(R.id.courseDuration);
            viewDetailsButton = itemView.findViewById(R.id.viewDetailsButton);
        }
    }
}
