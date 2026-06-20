package com.example.elearn.ui.enrollments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.elearn.adapters.EnrollmentAdapter;
import com.example.elearn.auth.AuthService;
import com.example.elearn.databinding.ActivityEnrollmentsBinding;
import com.example.elearn.models.Enrollment;
import com.example.elearn.network.ApiClient;
import com.example.elearn.network.ApiException;
import com.example.elearn.ui.login.LoginActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Activity that displays the current user's enrollments fetched from the API.
 * Shows enrollment cards with course name, progress bar, status, and action buttons.
 */
public class EnrollmentsActivity extends AppCompatActivity {

    private ActivityEnrollmentsBinding binding;
    private AuthService authService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEnrollmentsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Set up toolbar with back navigation
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        authService = new AuthService(this);
        String token = authService.getAccessToken();
        String userId = authService.getUserId();

        // Show progress bar initially
        binding.progressBar.setVisibility(View.VISIBLE);

        Handler mainHandler = new Handler(Looper.getMainLooper());
        ExecutorService executor = Executors.newSingleThreadExecutor();

        executor.execute(() -> {
            try {
                JSONArray response = ApiClient.getArray("/enrollments/" + userId, token);

                List<Enrollment> enrollments = new ArrayList<>();
                for (int i = 0; i < response.length(); i++) {
                    JSONObject obj = response.getJSONObject(i);
                    Enrollment enrollment = Enrollment.fromJson(obj);

                    String courseId = obj.optString("courseId", "");
                    if (!courseId.isEmpty()) {
                        // Fetch course details to get the course name
                        try {
                            JSONObject course = ApiClient.getObject("/courses/" + courseId, token);
                            enrollment.setCourseName(course.optString("title", "Course"));
                        } catch (Exception ignored) {}

                        // Fetch progress: get lessons count and completed lessons count
                        try {
                            JSONArray lessons = ApiClient.getArray("/courses/" + courseId + "/lessons", token);
                            int totalLessons = lessons.length();

                            if (totalLessons > 0) {
                                JSONArray progress = ApiClient.getArray("/course-progress/" + userId + "/" + courseId, token);
                                int completedCount = 0;
                                for (int j = 0; j < progress.length(); j++) {
                                    if (progress.getJSONObject(j).optBoolean("isCompleted", false)) {
                                        completedCount++;
                                    }
                                }
                                int percent = Math.round((float) completedCount / totalLessons * 100);
                                enrollment.setProgressPercent(percent);
                                enrollment.setCompleted(completedCount == totalLessons);
                            }
                        } catch (Exception ignored) {}
                    }

                    enrollments.add(enrollment);
                }

                mainHandler.post(() -> {
                    binding.progressBar.setVisibility(View.GONE);

                    if (enrollments.isEmpty()) {
                        binding.emptyText.setText("No enrollments found");
                        binding.emptyText.setVisibility(View.VISIBLE);
                    } else {
                        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
                        boolean admin = authService.isAdmin();
                        final EnrollmentAdapter[] adapterHolder = new EnrollmentAdapter[1];
                        adapterHolder[0] = new EnrollmentAdapter(enrollments, new EnrollmentAdapter.OnEnrollmentActionListener() {
                            @Override
                            public void onActionClick(Enrollment enrollment) {
                                Toast.makeText(EnrollmentsActivity.this, 
                                    enrollment.getProgressPercent() == 100 ? "Review Course - Coming Soon" : "Start/Continue Course - Coming Soon", 
                                    Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onViewDetails(Enrollment enrollment) {
                                new AlertDialog.Builder(EnrollmentsActivity.this)
                                        .setTitle(enrollment.getCourseName())
                                        .setMessage("Course: " + enrollment.getCourseName() + "\nProgress: " + enrollment.getProgressPercent() + "%\nStatus: " + enrollment.getStatusLabel())
                                        .setPositiveButton("Close", null)
                                        .show();
                            }

                            @Override
                            public void onDelete(Enrollment enrollment) {
                                new AlertDialog.Builder(EnrollmentsActivity.this)
                                        .setTitle("Delete Enrollment")
                                        .setMessage("Are you sure you want to delete enrollment for \"" + enrollment.getCourseName() + "\"?")
                                        .setPositiveButton("Delete", (dialog, which) -> deleteEnrollment(enrollment, enrollments, adapterHolder[0]))
                                        .setNegativeButton("Cancel", null)
                                        .show();
                            }
                        }, admin);
                        binding.recyclerView.setAdapter(adapterHolder[0]);
                    }
                });

            } catch (ApiException e) {
                int statusCode = e.getStatusCode();
                mainHandler.post(() -> {
                    binding.progressBar.setVisibility(View.GONE);
                    if (statusCode == 401) {
                        authService.clearSession();
                        Intent intent = new Intent(EnrollmentsActivity.this, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        binding.errorText.setText(statusCode >= 500 ? "Server error. Please try again later." : "An unexpected error occurred. Please try again.");
                        binding.errorText.setVisibility(View.VISIBLE);
                    }
                });
            } catch (Exception e) {
                mainHandler.post(() -> {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.errorText.setText("An unexpected error occurred. Please try again.");
                    binding.errorText.setVisibility(View.VISIBLE);
                });
            }
        });
    }

    /**
     * Deletes an enrollment via API and removes it from the list.
     */
    private void deleteEnrollment(Enrollment enrollment, List<Enrollment> enrollments, EnrollmentAdapter adapter) {
        String token = authService.getAccessToken();
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            try {
                // DELETE /enrollments/{id}
                ApiClient.delete("/enrollments/" + enrollment.getId(), token);
                handler.post(() -> {
                    enrollments.remove(enrollment);
                    adapter.notifyDataSetChanged();
                    Toast.makeText(this, "Enrollment deleted", Toast.LENGTH_SHORT).show();
                    if (enrollments.isEmpty()) {
                        binding.emptyText.setText("No enrollments found");
                        binding.emptyText.setVisibility(View.VISIBLE);
                    }
                });
            } catch (Exception e) {
                handler.post(() -> Toast.makeText(this, "Failed to delete enrollment", Toast.LENGTH_SHORT).show());
            }
        });
    }
}
