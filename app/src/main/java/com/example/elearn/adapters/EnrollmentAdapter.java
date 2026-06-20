package com.example.elearn.adapters;

import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.elearn.R;
import com.example.elearn.models.Enrollment;

import java.util.List;

/**
 * RecyclerView adapter for displaying enrollment cards matching the Angular app design.
 * Shows course name, date, status chip, progress bar, and action buttons.
 */
public class EnrollmentAdapter extends RecyclerView.Adapter<EnrollmentAdapter.ViewHolder> {

    private final List<Enrollment> enrollments;
    private final OnEnrollmentActionListener listener;
    private final boolean isAdmin;

    public interface OnEnrollmentActionListener {
        void onActionClick(Enrollment enrollment);
        void onViewDetails(Enrollment enrollment);
        void onDelete(Enrollment enrollment);
    }

    public EnrollmentAdapter(List<Enrollment> enrollments, OnEnrollmentActionListener listener, boolean isAdmin) {
        this.enrollments = enrollments;
        this.listener = listener;
        this.isAdmin = isAdmin;
    }

    public EnrollmentAdapter(List<Enrollment> enrollments) {
        this(enrollments, null, false);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_enrollment_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Enrollment enrollment = enrollments.get(position);

        // Course name
        holder.enrollmentCourseName.setText(enrollment.getCourseName());

        // Enrolled date
        String dateStr = enrollment.getEnrolledAt();
        if (dateStr != null && !dateStr.isEmpty()) {
            // Format: show as-is or parse if needed
            if (dateStr.length() > 10) {
                dateStr = dateStr.substring(0, 10); // Take date part only
            }
            holder.enrolledDate.setText("Enrolled on " + dateStr);
        }

        // Progress
        int progress = enrollment.getProgressPercent();
        holder.enrollmentProgress.setProgress(progress);
        holder.progressPercent.setText(progress + "%");

        // Status chip with colored background
        String statusLabel = enrollment.getStatusLabel();
        holder.enrollmentStatus.setText(statusLabel);

        GradientDrawable statusBg = new GradientDrawable();
        statusBg.setCornerRadius(16f);
        switch (statusLabel) {
            case "Completed":
                statusBg.setColor(0xFF4CAF50); // Green
                break;
            case "In Progress":
                statusBg.setColor(0xFF2196F3); // Blue
                break;
            default: // Not Started
                statusBg.setColor(0xFF9E9E9E); // Grey
                break;
        }
        holder.enrollmentStatus.setBackground(statusBg);

        // Action button (Start Course / Continue / Review Course)
        String actionLabel;
        if (enrollment.isCompleted() || progress == 100) {
            actionLabel = "Review Course";
        } else if (progress > 0) {
            actionLabel = "Continue";
        } else {
            actionLabel = "Start Course";
        }
        holder.enrollmentActionButton.setText(actionLabel);
        holder.enrollmentActionButton.setVisibility(View.VISIBLE);

        if (listener != null) {
            holder.enrollmentActionButton.setOnClickListener(v -> listener.onActionClick(enrollment));
            holder.viewDetailsButton.setOnClickListener(v -> listener.onViewDetails(enrollment));
        }

        // Delete button (admin only)
        if (isAdmin && listener != null) {
            holder.deleteButton.setVisibility(View.VISIBLE);
            holder.deleteButton.setOnClickListener(v -> listener.onDelete(enrollment));
        } else {
            holder.deleteButton.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return enrollments != null ? enrollments.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView enrollmentCourseName;
        final TextView enrolledDate;
        final TextView enrollmentStatus;
        final TextView progressPercent;
        final ProgressBar enrollmentProgress;
        final Button enrollmentActionButton;
        final Button viewDetailsButton;
        final Button deleteButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            enrollmentCourseName = itemView.findViewById(R.id.enrollmentCourseName);
            enrolledDate = itemView.findViewById(R.id.enrolledDate);
            enrollmentStatus = itemView.findViewById(R.id.enrollmentStatus);
            progressPercent = itemView.findViewById(R.id.progressPercent);
            enrollmentProgress = itemView.findViewById(R.id.enrollmentProgress);
            enrollmentActionButton = itemView.findViewById(R.id.enrollmentActionButton);
            viewDetailsButton = itemView.findViewById(R.id.viewDetailsButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }
}
