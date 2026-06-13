package com.example.elearn.adapters;

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
 * RecyclerView adapter for displaying enrollment cards in a list layout.
 * Each card shows course name, enrolled date, progress bar, status, and action button.
 */
public class EnrollmentAdapter extends RecyclerView.Adapter<EnrollmentAdapter.ViewHolder> {

    private final List<Enrollment> enrollments;

    public EnrollmentAdapter(List<Enrollment> enrollments) {
        this.enrollments = enrollments;
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

        holder.enrollmentCourseName.setText(enrollment.getCourseName());
        holder.enrolledDate.setText(enrollment.getEnrolledAt());
        holder.enrollmentProgress.setProgress(enrollment.getProgressPercent());
        holder.enrollmentStatus.setText(enrollment.getStatusLabel());

        String buttonLabel = enrollment.getButtonLabel();
        if (buttonLabel != null) {
            holder.enrollmentActionButton.setText(buttonLabel);
            holder.enrollmentActionButton.setVisibility(View.VISIBLE);
        } else {
            holder.enrollmentActionButton.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return enrollments != null ? enrollments.size() : 0;
    }

    /**
     * ViewHolder for enrollment card items.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView enrollmentCourseName;
        final TextView enrolledDate;
        final ProgressBar enrollmentProgress;
        final TextView enrollmentStatus;
        final Button enrollmentActionButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            enrollmentCourseName = itemView.findViewById(R.id.enrollmentCourseName);
            enrolledDate = itemView.findViewById(R.id.enrolledDate);
            enrollmentProgress = itemView.findViewById(R.id.enrollmentProgress);
            enrollmentStatus = itemView.findViewById(R.id.enrollmentStatus);
            enrollmentActionButton = itemView.findViewById(R.id.enrollmentActionButton);
        }
    }
}
