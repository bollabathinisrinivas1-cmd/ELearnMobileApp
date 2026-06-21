package com.example.elearn.ui.users;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.elearn.R;
import com.example.elearn.adapters.UserAdapter;
import com.example.elearn.auth.AuthService;
import com.example.elearn.databinding.ActivityUsersBinding;
import com.example.elearn.models.User;
import com.example.elearn.network.ApiClient;
import com.example.elearn.network.ApiException;
import com.example.elearn.ui.dashboard.DashboardActivity;
import com.example.elearn.ui.login.LoginActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Activity that displays users with role filtering and admin edit/delete.
 */
public class UsersActivity extends AppCompatActivity {

    private ActivityUsersBinding binding;
    private AuthService authService;
    private String roleFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUsersBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbar.setNavigationOnClickListener(v -> finish());

        authService = new AuthService(this);
        roleFilter = getIntent().getStringExtra("roleFilter");

        // Set header title based on role filter
        android.widget.TextView headerTitle = findViewById(R.id.headerTitle);
        android.widget.TextView headerSubtitle = findViewById(R.id.headerSubtitle);
        if ("Student".equals(roleFilter)) {
            headerTitle.setText("Students");
            headerSubtitle.setText("Manage enrolled students");
        } else if ("Staff".equals(roleFilter)) {
            headerTitle.setText("Teachers & Admin");
            headerSubtitle.setText("Manage staff members");
        } else {
            headerTitle.setText("All Users");
            headerSubtitle.setText("Manage platform users");
        }

        loadUsers();
    }

    private void loadUsers() {
        String token = authService.getAccessToken();
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.recyclerView.setVisibility(View.GONE);
        binding.emptyText.setVisibility(View.GONE);
        binding.errorText.setVisibility(View.GONE);

        Handler mainHandler = new Handler(Looper.getMainLooper());
        ExecutorService executor = Executors.newSingleThreadExecutor();

        executor.execute(() -> {
            try {
                JSONArray response = ApiClient.getArray("/users", token);

                List<User> users = new ArrayList<>();
                for (int i = 0; i < response.length(); i++) {
                    JSONObject obj = response.getJSONObject(i);
                    User user = User.fromJson(obj);

                    if (roleFilter == null) {
                        users.add(user);
                    } else if ("Student".equals(roleFilter)) {
                        if ("Student".equals(user.getRole())) {
                            users.add(user);
                        }
                    } else if ("Staff".equals(roleFilter)) {
                        if ("Admin".equals(user.getRole()) || "Teacher".equals(user.getRole())) {
                            users.add(user);
                        }
                    }
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
                        binding.recyclerView.setVisibility(View.VISIBLE);

                        // Admin: long-press on user for Edit/Delete
                        if (authService.isAdmin()) {
                            setupAdminLongPress(users, adapter);
                        }
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
                        binding.errorText.setText("An unexpected error occurred.");
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

    private void setupAdminLongPress(List<User> users, UserAdapter adapter) {
        // Set item click listener on RecyclerView items via adapter itemView
        for (int i = 0; i < binding.recyclerView.getChildCount(); i++) {
            // Can't easily do this here, use RecyclerView addOnItemTouchListener or update adapter
        }
        // Simpler: use RecyclerView's addOnChildAttachStateChangeListener
        binding.recyclerView.addOnChildAttachStateChangeListener(new androidx.recyclerview.widget.RecyclerView.OnChildAttachStateChangeListener() {
            @Override
            public void onChildViewAttachedToWindow(View view) {
                view.setOnLongClickListener(v -> {
                    int pos = binding.recyclerView.getChildAdapterPosition(v);
                    if (pos >= 0 && pos < users.size()) {
                        showUserOptionsDialog(users.get(pos), users, adapter);
                    }
                    return true;
                });
            }
            @Override
            public void onChildViewDetachedFromWindow(View view) {}
        });
    }

    private void showUserOptionsDialog(User user, List<User> users, UserAdapter adapter) {
        new AlertDialog.Builder(this)
                .setTitle(user.getName())
                .setItems(new String[]{"Edit", "Delete"}, (dialog, which) -> {
                    if (which == 0) {
                        showEditUserDialog(user);
                    } else {
                        confirmDeleteUser(user, users, adapter);
                    }
                })
                .show();
    }

    private void showEditUserDialog(User user) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(48, 32, 48, 16);

        EditText nameInput = new EditText(this);
        nameInput.setHint("Name");
        nameInput.setText(user.getName());
        layout.addView(nameInput);

        EditText emailInput = new EditText(this);
        emailInput.setHint("Email");
        emailInput.setText(user.getEmail());
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        p.topMargin = 16;
        emailInput.setLayoutParams(p);
        layout.addView(emailInput);

        EditText phoneInput = new EditText(this);
        phoneInput.setHint("Phone");
        phoneInput.setText(user.getPhone());
        phoneInput.setLayoutParams(p);
        layout.addView(phoneInput);

        new AlertDialog.Builder(this)
                .setTitle("Edit User")
                .setView(layout)
                .setPositiveButton("Save", (dialog, which) -> {
                    String name = nameInput.getText().toString().trim();
                    String email = emailInput.getText().toString().trim();
                    String phone = phoneInput.getText().toString().trim();
                    if (name.isEmpty() || email.isEmpty()) {
                        Toast.makeText(this, "Name and email are required", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    updateUser(user.getId(), name, email, phone);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updateUser(String userId, String name, String email, String phone) {
        String token = authService.getAccessToken();
        Handler handler = new Handler(Looper.getMainLooper());
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                JSONObject body = new JSONObject();
                body.put("name", name);
                body.put("email", email);
                body.put("phone", phone);
                ApiClient.put("/users/" + userId, body, token);
                handler.post(() -> {
                    Toast.makeText(this, "User updated", Toast.LENGTH_SHORT).show();
                    DashboardActivity.needsRefresh = true;
                    loadUsers();
                });
            } catch (Exception e) {
                handler.post(() -> Toast.makeText(this, "Failed to update user", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void confirmDeleteUser(User user, List<User> users, UserAdapter adapter) {
        new AlertDialog.Builder(this)
                .setTitle("Delete User")
                .setMessage("Are you sure you want to delete \"" + user.getName() + "\"?")
                .setPositiveButton("Delete", (dialog, which) -> deleteUser(user, users, adapter))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteUser(User user, List<User> users, UserAdapter adapter) {
        String token = authService.getAccessToken();
        Handler handler = new Handler(Looper.getMainLooper());
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                ApiClient.delete("/users/" + user.getId(), token);
                handler.post(() -> {
                    users.remove(user);
                    adapter.notifyDataSetChanged();
                    Toast.makeText(this, "User deleted", Toast.LENGTH_SHORT).show();
                    DashboardActivity.needsRefresh = true;
                    if (users.isEmpty()) {
                        binding.emptyText.setText("No users found");
                        binding.emptyText.setVisibility(View.VISIBLE);
                    }
                });
            } catch (Exception e) {
                handler.post(() -> Toast.makeText(this, "Failed to delete user", Toast.LENGTH_SHORT).show());
            }
        });
    }
}
