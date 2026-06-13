package com.example.elearn.ui.courses;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.elearn.adapters.CourseAdapter;
import com.example.elearn.auth.AuthService;
import com.example.elearn.databinding.ActivityCoursesBinding;
import com.example.elearn.models.Course;
import com.example.elearn.network.ApiClient;
import com.example.elearn.network.ApiException;
import com.example.elearn.ui.login.LoginActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Activity that displays a grid of courses fetched from the API.
 * Supports optional filtering by categoryId passed as an intent extra.
 * Shows course details in an AlertDialog when "View Details" is tapped.
 */
public class CoursesActivity extends AppCompatActivity {

    private ActivityCoursesBinding binding;
    private AuthService authService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCoursesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Set up toolbar with back navigation
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        authService = new AuthService(this);
        String token = authService.getAccessToken();

        // Check for categoryId intent extra
        int categoryId = getIntent().getIntExtra("categoryId", -1);

        // Show progress bar initially
        binding.progressBar.setVisibility(View.VISIBLE);

        Handler mainHandler = new Handler(Looper.getMainLooper());
        ExecutorService executor = Executors.newSingleThreadExecutor();

        executor.execute(() -> {
            try {
                JSONArray response;
                if (categoryId != -1) {
                    Map<String, String> params = new HashMap<>();
                    params.put("categoryId", String.valueOf(categoryId));
                    response = ApiClient.getArrayWithParams("/courses", params, token);
                } else {
                    response = ApiClient.getArray("/courses", token);
                }

                List<Course> courses = new ArrayList<>();
                for (int i = 0; i < response.length(); i++) {
                    JSONObject obj = response.getJSONObject(i);
                    courses.add(Course.fromJson(obj));
                }

                mainHandler.post(() -> {
                    binding.progressBar.setVisibility(View.GONE);

                    if (courses.isEmpty()) {
                        binding.emptyText.setVisibility(View.VISIBLE);
                    } else {
                        binding.recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
                        CourseAdapter adapter = new CourseAdapter(courses, course -> {
                            showCourseDetailsDialog(course);
                        });
                        binding.recyclerView.setAdapter(adapter);
                    }
                });

            } catch (ApiException e) {
                int statusCode = e.getStatusCode();
                mainHandler.post(() -> {
                    binding.progressBar.setVisibility(View.GONE);
                    if (statusCode == 401) {
                        authService.clearSession();
                        Intent intent = new Intent(CoursesActivity.this, LoginActivity.class);
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
     * Shows an AlertDialog with full course details.
     */
    private void showCourseDetailsDialog(Course course) {
        String priceText = course.isFree() ? "Free" : String.format("$%.2f", course.getPrice());
        String message = "Title: " + course.getTitle() + "\n\n"
                + "Description: " + course.getDescription() + "\n\n"
                + "Price: " + priceText + "\n\n"
                + "Duration: " + String.format("%.1f hours", course.getDurationHours()) + "\n\n"
                + "Level: " + course.getLevel();

        new AlertDialog.Builder(this)
                .setTitle(course.getTitle())
                .setMessage(message)
                .setPositiveButton("Close", null)
                .show();
    }
}
