package com.example.elearn.ui.categories;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.elearn.adapters.CategoryAdapter;
import com.example.elearn.auth.AuthService;
import com.example.elearn.databinding.ActivityCategoriesBinding;
import com.example.elearn.models.Category;
import com.example.elearn.network.ApiClient;
import com.example.elearn.network.ApiException;
import com.example.elearn.ui.courses.CoursesActivity;
import com.example.elearn.ui.login.LoginActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Activity that displays a grid of course categories fetched from the API.
 * Tapping a category navigates to CoursesActivity filtered by that category.
 */
public class CategoriesActivity extends AppCompatActivity {

    private ActivityCategoriesBinding binding;
    private AuthService authService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCategoriesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Set up toolbar with back navigation
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        authService = new AuthService(this);
        String token = authService.getAccessToken();

        // Show progress bar initially
        binding.progressBar.setVisibility(View.VISIBLE);

        Handler mainHandler = new Handler(Looper.getMainLooper());
        ExecutorService executor = Executors.newSingleThreadExecutor();

        executor.execute(() -> {
            try {
                JSONArray response = ApiClient.getArray("/categories", token);

                List<Category> categories = new ArrayList<>();
                for (int i = 0; i < response.length(); i++) {
                    JSONObject obj = response.getJSONObject(i);
                    categories.add(Category.fromJson(obj));
                }

                mainHandler.post(() -> {
                    binding.progressBar.setVisibility(View.GONE);

                    if (categories.isEmpty()) {
                        binding.emptyText.setVisibility(View.VISIBLE);
                    } else {
                        binding.recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
                        CategoryAdapter adapter = new CategoryAdapter(categories, category -> {
                            Intent intent = new Intent(CategoriesActivity.this, CoursesActivity.class);
                            intent.putExtra("categoryId", category.getId());
                            startActivity(intent);
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
                        Intent intent = new Intent(CategoriesActivity.this, LoginActivity.class);
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
