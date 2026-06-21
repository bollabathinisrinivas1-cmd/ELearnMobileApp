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
import com.example.elearn.ui.dashboard.DashboardActivity;
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

        // Check if we should show a specific course's details (from EnrollmentsActivity)
        String viewCourseId = getIntent().getStringExtra("viewCourseId");
        if (viewCourseId != null && !viewCourseId.isEmpty()) {
            showCourseById(viewCourseId);
        }
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
                        String role = authService.getUserRole();
                        boolean canManage = "Admin".equals(role) || "Teacher".equals(role);
                        binding.recyclerView.setAdapter(new CourseAdapter(courses, this::showCourseDetailsDialog,
                                canManage ? this::showCourseOptionsDialog : null));
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
                .setNeutralButton("Enroll", (d, w) -> enrollInCourse(course.getId()))
                .show();
    }

    private void enrollInCourse(String courseId) {
        String token = authService.getAccessToken();
        String userId = authService.getUserId();
        Handler handler = new Handler(Looper.getMainLooper());
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                JSONObject body = new JSONObject();
                body.put("userId", userId);
                body.put("courseId", courseId);
                ApiClient.postWithAuth("/enrollments", body, token);
                handler.post(() -> {
                    Toast.makeText(this, "Enrolled successfully!", Toast.LENGTH_SHORT).show();
                    DashboardActivity.needsRefresh = true;
                });
            } catch (Exception e) {
                handler.post(() -> Toast.makeText(this, "Failed to enroll. You may already be enrolled.", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void showCourseById(String courseId) {
        String token = authService.getAccessToken();
        Handler handler = new Handler(Looper.getMainLooper());
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                JSONObject courseJson = ApiClient.getObject("/courses/" + courseId, token);
                Course course = Course.fromJson(courseJson);
                handler.post(() -> showCourseDetailsDialog(course));
            } catch (Exception ignored) {}
        });
    }

    private void showCreateCourseDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_create_course, null);

        EditText titleInput = dialogView.findViewById(R.id.inputTitle);
        EditText descInput = dialogView.findViewById(R.id.inputDescription);
        EditText priceInput = dialogView.findViewById(R.id.inputPrice);
        EditText durationInput = dialogView.findViewById(R.id.inputDuration);
        Spinner levelSpinner = dialogView.findViewById(R.id.spinnerLevel);
        Spinner categorySpinner = dialogView.findViewById(R.id.spinnerCategory);
        CheckBox isFreeCheckbox = dialogView.findViewById(R.id.checkboxIsFree);

        ArrayAdapter<String> levelAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item,
                new String[]{"Beginner", "Intermediate", "Advanced"});
        levelSpinner.setAdapter(levelAdapter);

        // Fetch categories and populate spinner
        String token = authService.getAccessToken();
        List<String> categoryNames = new ArrayList<>();
        List<Integer> categoryIds = new ArrayList<>();
        categoryNames.add("Select Category");
        categoryIds.add(0);

        Handler handler = new Handler(Looper.getMainLooper());
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                JSONArray cats = ApiClient.getArray("/categories", token);
                for (int i = 0; i < cats.length(); i++) {
                    JSONObject cat = cats.getJSONObject(i);
                    categoryNames.add(cat.optString("name", ""));
                    categoryIds.add(cat.optInt("id", 0));
                }
            } catch (Exception ignored) {}
            handler.post(() -> {
                ArrayAdapter<String> catAdapter = new ArrayAdapter<>(this,
                        android.R.layout.simple_spinner_dropdown_item, categoryNames);
                categorySpinner.setAdapter(catAdapter);
            });
        });

        new AlertDialog.Builder(this)
                .setTitle("Create Course")
                .setView(dialogView)
                .setPositiveButton("Create", (dialog, which) -> {
                    String title = titleInput.getText().toString().trim();
                    if (title.isEmpty()) {
                        Toast.makeText(this, "Title is required", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    int selectedCatPos = categorySpinner.getSelectedItemPosition();
                    int selectedCatId = selectedCatPos > 0 ? categoryIds.get(selectedCatPos) : (categoryId != -1 ? categoryId : 1);

                    JSONObject body = new JSONObject();
                    try {
                        body.put("title", title);
                        body.put("description", descInput.getText().toString().trim());
                        body.put("price", Double.parseDouble(priceInput.getText().toString().isEmpty() ? "0" : priceInput.getText().toString()));
                        body.put("isFree", isFreeCheckbox.isChecked());
                        body.put("durationHours", Integer.parseInt(durationInput.getText().toString().isEmpty() ? "1" : durationInput.getText().toString()));
                        body.put("level", levelSpinner.getSelectedItem().toString());
                        body.put("categoryId", selectedCatId);
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
                    DashboardActivity.needsRefresh = true;
                    loadCourses();
                });
            } catch (Exception e) {
                handler.post(() -> Toast.makeText(this, "Failed to create course", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void showCourseOptionsDialog(Course course) {
        new AlertDialog.Builder(this)
                .setTitle(course.getTitle())
                .setItems(new String[]{"Edit", "Delete"}, (dialog, which) -> {
                    if (which == 0) {
                        showEditCourseDialog(course);
                    } else {
                        confirmDeleteCourse(course);
                    }
                })
                .show();
    }

    private void showEditCourseDialog(Course course) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_create_course, null);

        EditText titleInput = dialogView.findViewById(R.id.inputTitle);
        EditText descInput = dialogView.findViewById(R.id.inputDescription);
        EditText priceInput = dialogView.findViewById(R.id.inputPrice);
        EditText durationInput = dialogView.findViewById(R.id.inputDuration);
        Spinner levelSpinner = dialogView.findViewById(R.id.spinnerLevel);
        Spinner categorySpinner = dialogView.findViewById(R.id.spinnerCategory);
        CheckBox isFreeCheckbox = dialogView.findViewById(R.id.checkboxIsFree);

        // Pre-fill with current values
        titleInput.setText(course.getTitle());
        descInput.setText(course.getDescription());
        priceInput.setText(String.valueOf(course.getPrice()));
        durationInput.setText(String.valueOf((int) course.getDurationHours()));
        isFreeCheckbox.setChecked(course.isFree());

        ArrayAdapter<String> levelAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item,
                new String[]{"Beginner", "Intermediate", "Advanced"});
        levelSpinner.setAdapter(levelAdapter);
        String level = course.getLevel();
        if ("Intermediate".equals(level)) levelSpinner.setSelection(1);
        else if ("Advanced".equals(level)) levelSpinner.setSelection(2);

        // Load categories
        String token = authService.getAccessToken();
        List<String> categoryNames = new ArrayList<>();
        List<Integer> categoryIds = new ArrayList<>();
        categoryNames.add("Select Category");
        categoryIds.add(0);

        Handler handler = new Handler(Looper.getMainLooper());
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                JSONArray cats = ApiClient.getArray("/categories", token);
                for (int i = 0; i < cats.length(); i++) {
                    JSONObject cat = cats.getJSONObject(i);
                    categoryNames.add(cat.optString("name", ""));
                    categoryIds.add(cat.optInt("id", 0));
                }
            } catch (Exception ignored) {}
            handler.post(() -> {
                ArrayAdapter<String> catAdapter = new ArrayAdapter<>(this,
                        android.R.layout.simple_spinner_dropdown_item, categoryNames);
                categorySpinner.setAdapter(catAdapter);
                // Select current category
                int currentCatId = course.getCategoryId();
                for (int i = 0; i < categoryIds.size(); i++) {
                    if (categoryIds.get(i) == currentCatId) {
                        categorySpinner.setSelection(i);
                        break;
                    }
                }
            });
        });

        new AlertDialog.Builder(this)
                .setTitle("Edit Course")
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, which) -> {
                    String title = titleInput.getText().toString().trim();
                    if (title.isEmpty()) {
                        Toast.makeText(this, "Title is required", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    int selectedCatPos = categorySpinner.getSelectedItemPosition();
                    int selectedCatId = selectedCatPos > 0 ? categoryIds.get(selectedCatPos) : course.getCategoryId();

                    JSONObject body = new JSONObject();
                    try {
                        body.put("title", title);
                        body.put("description", descInput.getText().toString().trim());
                        body.put("price", Double.parseDouble(priceInput.getText().toString().isEmpty() ? "0" : priceInput.getText().toString()));
                        body.put("isFree", isFreeCheckbox.isChecked());
                        body.put("durationHours", Integer.parseInt(durationInput.getText().toString().isEmpty() ? "1" : durationInput.getText().toString()));
                        body.put("level", levelSpinner.getSelectedItem().toString());
                        body.put("categoryId", selectedCatId);
                    } catch (Exception e) {
                        Toast.makeText(this, "Invalid input", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    updateCourse(course.getId(), body);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updateCourse(String courseId, JSONObject body) {
        String token = authService.getAccessToken();
        Handler handler = new Handler(Looper.getMainLooper());
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                ApiClient.put("/courses/" + courseId, body, token);
                handler.post(() -> {
                    Toast.makeText(this, "Course updated", Toast.LENGTH_SHORT).show();
                    loadCourses();
                });
            } catch (Exception e) {
                handler.post(() -> Toast.makeText(this, "Failed to update course", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void confirmDeleteCourse(Course course) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Course")
                .setMessage("Are you sure you want to delete \"" + course.getTitle() + "\"?")
                .setPositiveButton("Delete", (dialog, which) -> deleteCourse(course.getId()))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteCourse(String courseId) {
        String token = authService.getAccessToken();
        Handler handler = new Handler(Looper.getMainLooper());
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                ApiClient.delete("/courses/" + courseId, token);
                handler.post(() -> {
                    Toast.makeText(this, "Course deleted", Toast.LENGTH_SHORT).show();
                    DashboardActivity.needsRefresh = true;
                    loadCourses();
                });
            } catch (Exception e) {
                handler.post(() -> Toast.makeText(this, "Failed to delete course", Toast.LENGTH_SHORT).show());
            }
        });
    }
}
