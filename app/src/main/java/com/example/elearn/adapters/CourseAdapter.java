package com.example.elearn.adapters;

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
 * Each card shows the course title, price, duration, and a "View Details" button.
 */
public class CourseAdapter extends RecyclerView.Adapter<CourseAdapter.ViewHolder> {

    private final List<Course> courses;
    private final OnCourseClickListener listener;

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

        holder.courseTitle.setText(course.getTitle());

        if (course.isFree()) {
            holder.coursePrice.setText("Free");
        } else {
            holder.coursePrice.setText(String.format("₹%.2f", course.getPrice()));
        }

        holder.courseDuration.setText(String.format("%.1f hours", course.getDurationHours()));

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
        final TextView courseTitle;
        final TextView coursePrice;
        final TextView courseDuration;
        final Button viewDetailsButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            courseTitle = itemView.findViewById(R.id.courseTitle);
            coursePrice = itemView.findViewById(R.id.coursePrice);
            courseDuration = itemView.findViewById(R.id.courseDuration);
            viewDetailsButton = itemView.findViewById(R.id.viewDetailsButton);
        }
    }
}
