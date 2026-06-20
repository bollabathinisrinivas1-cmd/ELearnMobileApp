package com.example.elearn.ui.dashboard;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.elearn.R;
import com.example.elearn.adapters.DashboardCardAdapter;
import com.example.elearn.auth.AuthService;
import com.example.elearn.databinding.ActivityDashboardBinding;
import com.example.elearn.network.ApiClient;
import com.example.elearn.ui.categories.CategoriesActivity;
import com.example.elearn.ui.courses.CoursesActivity;
import com.example.elearn.ui.enrollments.EnrollmentsActivity;
import com.example.elearn.ui.login.LoginActivity;
import com.example.elearn.ui.users.UsersActivity;

import org.json.JSONObject;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Dashboard screen activity. Displays role-based summary cards and statistics.
 * Checks authentication on launch, shows a navigation drawer with role-based items,
 * and observes ViewModel LiveData to render dashboard cards, loading, and errors.
 */
public class DashboardActivity extends AppCompatActivity {

    private ActivityDashboardBinding binding;
    private DashboardViewModel viewModel;
    private AuthService authService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize AuthService and check login state
        authService = new AuthService(this);
        if (!authService.isLoggedIn()) {
            // Attempt token refresh on a background thread
            new Thread(() -> {
                boolean refreshed = authService.refreshToken();
                runOnUiThread(() -> {
                    if (refreshed) {
                        initDashboard();
                    } else {
                        navigateToLogin();
                    }
                });
            }).start();
            return;
        }

        initDashboard();
    }

    /**
     * Initializes the dashboard UI after authentication is confirmed.
     */
    private void initDashboard() {

        // Inflate layout with ViewBinding
        binding = ActivityDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Set up ViewModel
        viewModel = new ViewModelProvider(this).get(DashboardViewModel.class);

        // Set up toolbar
        setSupportActionBar(binding.toolbar);

        // Set up navigation drawer header
        setupNavHeader();

        // Hide "Users" nav item for non-Admin roles
        if (!authService.isAdmin()) {
            binding.navView.getMenu().findItem(R.id.nav_users).setVisible(false);
        }

        // Set up RecyclerView with 2-column grid
        binding.dashboardRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        // Set up welcome text with user name
        String userName = authService.getUserName();
        if (userName != null && !userName.isEmpty()) {
            binding.welcomeText.setText("Welcome, " + userName);
        }

        // Set up profile avatar click
        binding.profileAvatar.setOnClickListener(v -> showUserMenu(v));

        // Load profile image from API
        loadProfileImage();

        // Observe ViewModel LiveData
        observeViewModel();

        // Load dashboard data
        viewModel.loadDashboardData(authService);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload dashboard data when returning from other activities (e.g., after deleting enrollment)
        if (viewModel != null && authService != null) {
            viewModel.loadDashboardData(authService);
        }
    }

    /**
     * Displays a popup menu with profile name, Settings, and Sign Out.
     */
    private void showUserMenu(View anchor) {
        PopupMenu popup = new PopupMenu(this, anchor);
        popup.inflate(R.menu.user_menu);

        // Set user name as the first item title
        MenuItem userNameItem = popup.getMenu().findItem(R.id.menu_user_name);
        String userName = authService.getUserName();
        if (userName != null && !userName.isEmpty()) {
            userNameItem.setTitle(userName);
        }

        popup.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.menu_sign_out) {
                authService.clearSession();
                navigateToLogin();
                return true;
            } else if (id == R.id.menu_profile) {
                startActivity(new Intent(DashboardActivity.this, com.example.elearn.ui.profile.ProfileActivity.class));
                return true;
            } else if (id == R.id.menu_settings) {
                Toast.makeText(this, "Settings - Coming Soon", Toast.LENGTH_SHORT).show();
                return true;
            }
            return false;
        });

        popup.show();
    }

    /**
     * Fetches the user's profile image from the API and sets it on the avatar.
     */
    private void loadProfileImage() {
        String userId = authService.getUserId();
        String token = authService.getAccessToken();
        if (userId == null || token == null) return;

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            try {
                JSONObject profile = ApiClient.getObject("/users/" + userId + "/profile", token);
                String imageUrl = profile.optString("profileImageUrl", "");

                if (!imageUrl.isEmpty() && imageUrl.startsWith("data:image")) {
                    // Parse base64 data URI: "data:image/png;base64,XXXXX"
                    String base64Data = imageUrl.substring(imageUrl.indexOf(",") + 1);
                    byte[] imageBytes = Base64.decode(base64Data, Base64.DEFAULT);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);

                    if (bitmap != null) {
                        Bitmap circularBitmap = getCircularBitmap(bitmap);
                        handler.post(() -> {
                            binding.profileAvatar.setImageBitmap(circularBitmap);
                            binding.profileAvatar.setBackground(null);
                        });
                    }
                }
            } catch (Exception e) {
                // Silently fail - keep default avatar
            }
        });
    }

    /**
     * Creates a circular bitmap from a rectangular source bitmap.
     */
    private Bitmap getCircularBitmap(Bitmap bitmap) {
        int size = Math.min(bitmap.getWidth(), bitmap.getHeight());
        Bitmap output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        Paint paint = new Paint();
        paint.setAntiAlias(true);

        Rect rect = new Rect(0, 0, size, size);
        canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }

    /**
     * Sets up the navigation drawer header with user name and role.
     */
    private void setupNavHeader() {
        View headerView = binding.navView.getHeaderView(0);
        TextView navHeaderName = headerView.findViewById(R.id.navHeaderName);
        TextView navHeaderRole = headerView.findViewById(R.id.navHeaderRole);

        String userName = authService.getUserName();
        String userRole = authService.getUserRole();

        if (userName != null) {
            navHeaderName.setText(userName);
        }
        if (userRole != null) {
            navHeaderRole.setText(userRole);
        }
    }

    /**
     * Observes DashboardViewModel LiveData for loading, cards, pie chart data, and errors.
     */
    private void observeViewModel() {
        // Observe loading state
        viewModel.getIsLoading().observe(this, isLoading -> {
            if (isLoading != null && isLoading) {
                binding.dashboardProgress.setVisibility(View.VISIBLE);
                binding.dashboardRecyclerView.setVisibility(View.GONE);
            } else {
                binding.dashboardProgress.setVisibility(View.GONE);
            }
        });

        // Observe cards list
        viewModel.getCards().observe(this, cards -> {
            if (cards != null) {
                DashboardCardAdapter adapter = new DashboardCardAdapter(cards, card -> {
                    // Card click handling: navigate based on route
                    String route = card.getRoute();
                    if (route == null) return;

                    Intent intent = null;
                    switch (route) {
                        case "courses":
                            intent = new Intent(DashboardActivity.this, CoursesActivity.class);
                            break;
                        case "categories":
                            intent = new Intent(DashboardActivity.this, CategoriesActivity.class);
                            break;
                        case "enrollments":
                            intent = new Intent(DashboardActivity.this, EnrollmentsActivity.class);
                            break;
                        case "users":
                            intent = new Intent(DashboardActivity.this, UsersActivity.class);
                            break;
                        case "users_students":
                            intent = new Intent(DashboardActivity.this, UsersActivity.class);
                            intent.putExtra("roleFilter", "Student");
                            break;
                        case "users_staff":
                            intent = new Intent(DashboardActivity.this, UsersActivity.class);
                            intent.putExtra("roleFilter", "Staff");
                            break;
                    }
                    if (intent != null) {
                        startActivity(intent);
                    }
                });
                binding.dashboardRecyclerView.setAdapter(adapter);
                binding.dashboardRecyclerView.setVisibility(View.VISIBLE);
            }
        });

        // Observe pie chart data (Admin only)
        viewModel.getPieChartData().observe(this, pieChartData -> {
            if (pieChartData != null) {
                binding.pieChartContainer.setVisibility(View.VISIBLE);

                // Create and add PieChartView with data
                PieChartView pieChartView = new PieChartView(this);
                float paidPercent = pieChartData.getPaidPercent();
                float freePercent = 100f - paidPercent;
                pieChartView.setData(paidPercent, freePercent,
                        pieChartData.getPaidStudents(), pieChartData.getFreeStudents());

                android.widget.FrameLayout pieChartFrame = binding.getRoot().findViewById(R.id.pieChartFrame);
                pieChartFrame.removeAllViews();
                pieChartFrame.addView(pieChartView,
                        new android.widget.FrameLayout.LayoutParams(
                                android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
                                android.widget.FrameLayout.LayoutParams.MATCH_PARENT));
            }
        });

        // Observe error messages
        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                binding.dashboardError.setText(error);
                binding.dashboardError.setVisibility(View.VISIBLE);

                // Handle session expiration (401)
                if (error.contains("Session expired")) {
                    authService.clearSession();
                    navigateToLogin();
                }
            } else {
                binding.dashboardError.setVisibility(View.GONE);
            }
        });
    }

    /**
     * Navigates to LoginActivity and finishes this activity.
     */
    private void navigateToLogin() {
        Intent intent = new Intent(DashboardActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
