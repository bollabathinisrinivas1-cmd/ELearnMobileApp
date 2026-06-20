package com.example.elearn.ui.courses;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.elearn.R;
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
 * Admin/Teacher users can create new courses via FAB.
 */
public class CoursesActivity extends AppCompatActivity {

    private ActivityCoursesBinding binding;
    private AuthService authService;
    private int categoryId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCoursesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbar.setNavigationOnClickListener(v -> finish());

        authService = new AuthService(this);

        // Show Create Course FAB for Admin/Teacher
        String role = authService.getUserRole();
        if ("Admin".equals(role) || "Teacher".equals(role)) {
            binding.fabCreateCourse.setVisibility(View.VISIBLE);
            binding.fabCreateCourse.setOnClickListener(v -> showCreateCourseDialog());
        }

        categoryId = getIntent().getIntExtra("categoryId", -1);
        loadCourses();
    }

    private void loadCourses() {
        String token = authService.getAccessToken();
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.recyclerView.setVisibility(View.GONE);
        binding.emptyText.setVisibility(View.GONE);
        binding.errorText.setVisibility(View.GONE);

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
                    courses.add(Course.fromJson(response.getJSONObject(i)));
                }

                mainHandler.post(() -> {
                    binding.progressBar.setVisibility(View.GONE);
                    if (courses.isEmpty()) {
                        binding.emptyText.setVisibility(View.VISIBLE);
                    } else {
                        binding.recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
                        binding.recyclerView.setAdapter(new CourseAdapter(courses, this::showCourseDetailsDialog));
                        binding.recyclerView.setVisibility(View.VISIBLE);
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
                        binding.errorText.setText(statusCode >= 500 ? "Server error." : "An unexpected error occurred.");
                        binding.errorText.setVisibility(View.VISIBLE);
                    }
                });
            } catch (Exception e) {
                mainHandler.post(() -> {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.errorText.setText("An unexpected error occurred.");
                    binding.errorText.setVisibility(View.VISIBLE);
                });
            }
        });
    }

    private void showCourseDetailsDialog(Course course) {
        String priceText = course.isFree() ? "Free" : String.format("₹%.2f", course.getPrice());
        String message = "Description: " + course.getDescription() + "\n\n"
                + "Price: " + priceText + "\n"
                + "Duration: " + String.format("%.1f hours", course.getDurationHours()) + "\n"
                + "Level: " + course.getLevel();

        new AlertDialog.Builder(this)
                .setTitle(course.getTitle())
                .setMessage(message)
                .setPositiveButton("Close", null)
                .show();
    }

    private void showCreateCourseDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_create_course, null);

        EditText titleInput = dialogView.findViewById(R.id.inputTitle);
        EditText descInput = dialogView.findViewById(R.id.inputDescription);
        EditText priceInput = dialogView.findViewById(R.id.inputPrice);
        EditText durationInput = dialogView.findViewById(R.id.inputDuration);
        Spinner levelSpinner = dialogView.findViewById(R.id.spinnerLevel);
        CheckBox isFreeCheckbox = dialogView.findViewById(R.id.checkboxIsFree);

        ArrayAdapter<String> levelAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item,
                new String[]{"Beginner", "Intermediate", "Advanced"});
        levelSpinner.setAdapter(levelAdapter);

        new AlertDialog.Builder(this)
                .setTitle("Create Course")
                .setView(dialogView)
                .setPositiveButton("Create", (dialog, which) -> {
                    String title = titleInput.getText().toString().trim();
                    if (title.isEmpty()) {
                        Toast.makeText(this, "Title is required", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    JSONObject body = new JSONObject();
                    try {
                        body.put("title", title);
                        body.put("description", descInput.getText().toString().trim());
                        body.put("price", Double.parseDouble(priceInput.getText().toString().isEmpty() ? "0" : priceInput.getText().toString()));
                        body.put("isFree", isFreeCheckbox.isChecked());
                        body.put("durationHours", Integer.parseInt(durationInput.getText().toString().isEmpty() ? "1" : durationInput.getText().toString()));
                        body.put("level", levelSpinner.getSelectedItem().toString());
                        body.put("categoryId", categoryId != -1 ? categoryId : 1);
                        body.put("instructorId", authService.getUserId());
                    } catch (Exception e) {
                        Toast.makeText(this, "Invalid input", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    createCourse(body);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void createCourse(JSONObject body) {
        String token = authService.getAccessToken();
        Handler handler = new Handler(Looper.getMainLooper());
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                ApiClient.postWithAuth("/courses", body, token);
                handler.post(() -> {
                    Toast.makeText(this, "Course created", Toast.LENGTH_SHORT).show();
                    loadCourses();
                });
            } catch (Exception e) {
                handler.post(() -> Toast.makeText(this, "Failed to create course", Toast.LENGTH_SHORT).show());
            }
        });
    }
}
