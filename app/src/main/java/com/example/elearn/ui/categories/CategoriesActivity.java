package com.example.elearn.ui.categories;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.elearn.adapters.CategoryAdapter;
import com.example.elearn.auth.AuthService;
import com.example.elearn.databinding.ActivityCategoriesBinding;
import com.example.elearn.models.Category;
import com.example.elearn.network.ApiClient;
import com.example.elearn.network.ApiException;
import com.example.elearn.ui.courses.CoursesActivity;
import com.example.elearn.ui.dashboard.DashboardActivity;
import com.example.elearn.ui.login.LoginActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Activity that displays a grid of course categories fetched from the API.
 * Admin users can add new categories via FAB.
 */
public class CategoriesActivity extends AppCompatActivity {

    private ActivityCategoriesBinding binding;
    private AuthService authService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCategoriesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbar.setNavigationOnClickListener(v -> finish());

        authService = new AuthService(this);

        // Show Add Category FAB for Admin
        if (authService.isAdmin()) {
            binding.fabAddCategory.setVisibility(View.VISIBLE);
            binding.fabAddCategory.setOnClickListener(v -> showAddCategoryDialog());
        }

        loadCategories();
    }

    private void loadCategories() {
        String token = authService.getAccessToken();
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.recyclerView.setVisibility(View.GONE);
        binding.emptyText.setVisibility(View.GONE);
        binding.errorText.setVisibility(View.GONE);

        Handler mainHandler = new Handler(Looper.getMainLooper());
        ExecutorService executor = Executors.newSingleThreadExecutor();

        executor.execute(() -> {
            try {
                JSONArray response = ApiClient.getArray("/categories", token);

                List<Category> categories = new ArrayList<>();
                for (int i = 0; i < response.length(); i++) {
                    categories.add(Category.fromJson(response.getJSONObject(i)));
                }

                mainHandler.post(() -> {
                    binding.progressBar.setVisibility(View.GONE);

                    if (categories.isEmpty()) {
                        binding.emptyText.setVisibility(View.VISIBLE);
                    } else {
                        binding.recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
                        boolean isAdmin = authService.isAdmin();
                        CategoryAdapter adapter = new CategoryAdapter(categories, category -> {
                            Intent intent = new Intent(CategoriesActivity.this, CoursesActivity.class);
                            intent.putExtra("categoryId", category.getId());
                            startActivity(intent);
                        }, isAdmin ? category -> showCategoryOptionsDialog(category) : null);
                        binding.recyclerView.setAdapter(adapter);
                        binding.recyclerView.setVisibility(View.VISIBLE);
                    }
                });

            } catch (ApiException e) {
                int statusCode = e.getStatusCode();
                mainHandler.post(() -> {
                    binding.progressBar.setVisibility(View.GONE);
                    if (statusCode == 401) {
                        authService.clearSession();
                        Intent intent = new Intent(CategoriesActivity.this, LoginActivity.class);
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

    private void showAddCategoryDialog() {
        EditText input = new EditText(this);
        input.setHint("Category Name");
        input.setPadding(48, 32, 48, 32);

        new AlertDialog.Builder(this)
                .setTitle("Add Category")
                .setView(input)
                .setPositiveButton("Create", (dialog, which) -> {
                    String name = input.getText().toString().trim();
                    if (name.isEmpty()) {
                        Toast.makeText(this, "Name is required", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    createCategory(name);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void createCategory(String name) {
        String token = authService.getAccessToken();
        Handler handler = new Handler(Looper.getMainLooper());

        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                JSONObject body = new JSONObject();
                body.put("name", name);
                ApiClient.postWithAuth("/categories", body, token);
                handler.post(() -> {
                    Toast.makeText(this, "Category created", Toast.LENGTH_SHORT).show();
                    DashboardActivity.needsRefresh = true;
                    loadCategories();
                });
            } catch (Exception e) {
                handler.post(() -> Toast.makeText(this, "Failed to create category", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void showCategoryOptionsDialog(Category category) {
        new AlertDialog.Builder(this)
                .setTitle(category.getName())
                .setItems(new String[]{"Edit", "Delete"}, (dialog, which) -> {
                    if (which == 0) {
                        showEditCategoryDialog(category);
                    } else {
                        confirmDeleteCategory(category);
                    }
                })
                .show();
    }

    private void showEditCategoryDialog(Category category) {
        EditText input = new EditText(this);
        input.setHint("Category Name");
        input.setText(category.getName());
        input.setPadding(48, 32, 48, 32);

        new AlertDialog.Builder(this)
                .setTitle("Edit Category")
                .setView(input)
                .setPositiveButton("Save", (dialog, which) -> {
                    String name = input.getText().toString().trim();
                    if (name.isEmpty()) {
                        Toast.makeText(this, "Name is required", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    updateCategory(category.getId(), name);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updateCategory(int categoryId, String name) {
        String token = authService.getAccessToken();
        Handler handler = new Handler(Looper.getMainLooper());

        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                JSONObject body = new JSONObject();
                body.put("name", name);
                ApiClient.put("/categories/" + categoryId, body, token);
                handler.post(() -> {
                    Toast.makeText(this, "Category updated", Toast.LENGTH_SHORT).show();
                    loadCategories();
                });
            } catch (Exception e) {
                handler.post(() -> Toast.makeText(this, "Failed to update category", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void confirmDeleteCategory(Category category) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Category")
                .setMessage("Are you sure you want to delete \"" + category.getName() + "\"?")
                .setPositiveButton("Delete", (dialog, which) -> deleteCategory(category.getId()))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteCategory(int categoryId) {
        String token = authService.getAccessToken();
        Handler handler = new Handler(Looper.getMainLooper());

        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                ApiClient.delete("/categories/" + categoryId, token);
                handler.post(() -> {
                    Toast.makeText(this, "Category deleted", Toast.LENGTH_SHORT).show();
                    DashboardActivity.needsRefresh = true;
                    loadCategories();
                });
            } catch (Exception e) {
                handler.post(() -> Toast.makeText(this, "Failed to delete category", Toast.LENGTH_SHORT).show());
            }
        });
    }
}
