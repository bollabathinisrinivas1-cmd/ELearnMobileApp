package com.example.elearn.ui.users;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.elearn.adapters.UserAdapter;
import com.example.elearn.auth.AuthService;
import com.example.elearn.databinding.ActivityUsersBinding;
import com.example.elearn.models.User;
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
 * Activity that displays all platform users fetched from the API.
 * Shows user items with name, email, and a role badge colored by role.
 */
public class UsersActivity extends AppCompatActivity {

    private ActivityUsersBinding binding;
    private AuthService authService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUsersBinding.inflate(getLayoutInflater());
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
                JSONArray response = ApiClient.getArray("/users", token);

                List<User> users = new ArrayList<>();
                for (int i = 0; i < response.length(); i++) {
                    JSONObject obj = response.getJSONObject(i);
                    users.add(User.fromJson(obj));
                }

                mainHandler.post(() -> {
                    binding.progressBar.setVisibility(View.GONE);

                    if (users.isEmpty()) {
                        binding.emptyText.setText("No users found");
                        binding.emptyText.setVisibility(View.VISIBLE);
                    } else {
                        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
                        UserAdapter adapter = new UserAdapter(users);
                        binding.recyclerView.setAdapter(adapter);
                    }
                });

            } catch (ApiException e) {
                int statusCode = e.getStatusCode();
                mainHandler.post(() -> {
                    binding.progressBar.setVisibility(View.GONE);
                    if (statusCode == 401) {
                        authService.clearSession();
                        Intent intent = new Intent(UsersActivity.this, LoginActivity.class);
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
