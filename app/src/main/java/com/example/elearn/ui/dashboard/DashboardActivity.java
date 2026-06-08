package com.example.elearn.ui.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.elearn.R;
import com.example.elearn.adapters.DashboardCardAdapter;
import com.example.elearn.auth.AuthService;
import com.example.elearn.databinding.ActivityDashboardBinding;
import com.example.elearn.ui.categories.CategoriesActivity;
import com.example.elearn.ui.courses.CoursesActivity;
import com.example.elearn.ui.enrollments.EnrollmentsActivity;
import com.example.elearn.ui.login.LoginActivity;
import com.example.elearn.ui.users.UsersActivity;

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
            navigateToLogin();
            return;
        }

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

        // Set up logout button
        binding.logoutButton.setOnClickListener(v -> {
            authService.clearSession();
            navigateToLogin();
        });

        // Observe ViewModel LiveData
        observeViewModel();

        // Load dashboard data
        viewModel.loadDashboardData(authService);
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
                pieChartView.setData(paidPercent, freePercent);

                binding.pieChartContainer.addView(pieChartView,
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
