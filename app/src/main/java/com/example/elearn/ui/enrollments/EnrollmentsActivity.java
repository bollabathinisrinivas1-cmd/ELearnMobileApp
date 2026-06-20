package com.example.elearn.ui.enrollments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

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

                    // Fetch course details to get the course name
                    try {
                        String courseId = obj.optString("courseId", "");
                        if (!courseId.isEmpty()) {
                            JSONObject course = ApiClient.getObject("/courses/" + courseId, token);
                            enrollment.setCourseName(course.optString("title", "Course"));
                        }
                    } catch (Exception ignored) {
                        // Keep default course name if fetch fails
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
                        EnrollmentAdapter adapter = new EnrollmentAdapter(enrollments);
                        binding.recyclerView.setAdapter(adapter);
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
}
