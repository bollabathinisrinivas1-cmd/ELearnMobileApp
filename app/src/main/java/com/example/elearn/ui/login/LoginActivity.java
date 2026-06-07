package com.example.elearn.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.elearn.auth.AuthService;
import com.example.elearn.databinding.ActivityLoginBinding;
import com.example.elearn.ui.dashboard.DashboardActivity;
import com.example.elearn.ui.signup.SignupActivity;

/**
 * Login screen activity. Handles user authentication via email/password,
 * displays field-level validation errors, and navigates to Dashboard on success
 * or Signup when requested.
 */
public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private LoginViewModel viewModel;
    private AuthService authService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inflate layout with ViewBinding
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize AuthService and ViewModel
        authService = new AuthService(this);
        viewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        // If already logged in, navigate directly to Dashboard
        if (authService.isLoggedIn()) {
            navigateToDashboard();
            return;
        }

        // Set up observers
        observeViewModel();

        // Set up click listeners
        setupClickListeners();
    }

    /**
     * Observes LoginViewModel LiveData for loading state, errors, and login success.
     */
    private void observeViewModel() {
        // Observe loading state: show/hide progress, enable/disable button
        viewModel.getIsLoading().observe(this, isLoading -> {
            if (isLoading != null && isLoading) {
                binding.loginProgress.setVisibility(View.VISIBLE);
                binding.loginButton.setEnabled(false);
                binding.loginButton.setText("");
            } else {
                binding.loginProgress.setVisibility(View.GONE);
                binding.loginButton.setEnabled(true);
                binding.loginButton.setText("Log In");
            }
        });

        // Observe error messages: display field-level errors and clear password
        viewModel.getErrorMessage().observe(this, error -> {
            // Clear previous errors
            binding.emailError.setVisibility(View.GONE);
            binding.passwordError.setVisibility(View.GONE);

            if (error != null && !error.isEmpty()) {
                // Determine which field the error relates to
                if (error.toLowerCase().contains("email")) {
                    binding.emailError.setText(error);
                    binding.emailError.setVisibility(View.VISIBLE);
                } else if (error.toLowerCase().contains("password")) {
                    binding.passwordError.setText(error);
                    binding.passwordError.setVisibility(View.VISIBLE);
                } else {
                    // General error - show in email error area
                    binding.emailError.setText(error);
                    binding.emailError.setVisibility(View.VISIBLE);
                }

                // Clear password field on error (Requirement 3.9)
                binding.passwordEditText.setText("");
            }
        });

        // Observe login success: navigate to Dashboard
        viewModel.getLoginSuccess().observe(this, success -> {
            if (success != null && success) {
                navigateToDashboard();
            }
        });
    }

    /**
     * Sets up click listeners for login button, signup link, and password toggle.
     */
    private void setupClickListeners() {
        // Login button: get credentials and call viewModel.login()
        binding.loginButton.setOnClickListener(v -> {
            String email = binding.emailEditText.getText().toString();
            String password = binding.passwordEditText.getText().toString();

            // Clear previous errors before attempting login
            binding.emailError.setVisibility(View.GONE);
            binding.passwordError.setVisibility(View.GONE);

            viewModel.login(email, password, authService);
        });

        // Sign Up link: navigate to SignupActivity
        binding.signupLink.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
            startActivity(intent);
        });
    }

    /**
     * Navigates to DashboardActivity and finishes this activity.
     */
    private void navigateToDashboard() {
        Intent intent = new Intent(LoginActivity.this, DashboardActivity.class);
        startActivity(intent);
        finish();
    }
}
