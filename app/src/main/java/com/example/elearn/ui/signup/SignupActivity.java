package com.example.elearn.ui.signup;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.elearn.databinding.ActivitySignupBinding;
import com.example.elearn.ui.login.LoginActivity;

/**
 * Signup screen activity. Handles new user registration via name, email,
 * phone (optional), and password fields. Displays field-level validation errors,
 * shows a success message on registration, and navigates to LoginActivity
 * after a 2-second delay.
 */
public class SignupActivity extends AppCompatActivity {

    private ActivitySignupBinding binding;
    private SignupViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inflate layout with ViewBinding
        binding = ActivitySignupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(SignupViewModel.class);

        // Set up observers
        observeViewModel();

        // Set up click listeners
        setupClickListeners();
    }

    /**
     * Observes SignupViewModel LiveData for loading state, error messages, and success.
     */
    private void observeViewModel() {
        // Observe loading state: show/hide progress, enable/disable button
        viewModel.getIsLoading().observe(this, isLoading -> {
            if (isLoading != null && isLoading) {
                binding.signupProgress.setVisibility(View.VISIBLE);
                binding.signupButton.setEnabled(false);
                binding.signupButton.setText("");
            } else {
                binding.signupProgress.setVisibility(View.GONE);
                binding.signupButton.setEnabled(true);
                binding.signupButton.setText("Sign Up");
            }
        });

        // Observe error messages: display field-level errors
        viewModel.getErrorMessage().observe(this, error -> {
            // Clear previous errors
            binding.nameError.setVisibility(View.GONE);
            binding.emailError.setVisibility(View.GONE);
            binding.passwordError.setVisibility(View.GONE);
            binding.errorMessage.setVisibility(View.GONE);

            if (error != null && !error.isEmpty()) {
                // Determine which field the error relates to
                if (error.toLowerCase().contains("name")) {
                    binding.nameError.setText(error);
                    binding.nameError.setVisibility(View.VISIBLE);
                } else if (error.toLowerCase().contains("email")) {
                    binding.emailError.setText(error);
                    binding.emailError.setVisibility(View.VISIBLE);
                } else if (error.toLowerCase().contains("password")) {
                    binding.passwordError.setText(error);
                    binding.passwordError.setVisibility(View.VISIBLE);
                } else {
                    // General error - show in error message area
                    binding.errorMessage.setText(error);
                    binding.errorMessage.setVisibility(View.VISIBLE);
                }
            }
        });

        // Observe success message: show message and navigate to LoginActivity after delay
        viewModel.getSuccessMessage().observe(this, message -> {
            if (message != null && !message.isEmpty()) {
                binding.successMessage.setText(message);
                binding.successMessage.setVisibility(View.VISIBLE);

                // Navigate to LoginActivity after 2-second delay
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    navigateToLogin();
                }, 2000);
            } else {
                binding.successMessage.setVisibility(View.GONE);
            }
        });
    }

    /**
     * Sets up click listeners for signup button and login link.
     */
    private void setupClickListeners() {
        // Signup button: get form data and call viewModel.signup()
        binding.signupButton.setOnClickListener(v -> {
            String name = binding.nameEditText.getText().toString();
            String email = binding.emailEditText.getText().toString();
            String phone = binding.phoneEditText.getText().toString();
            String password = binding.passwordEditText.getText().toString();

            // Clear previous errors before attempting signup
            binding.nameError.setVisibility(View.GONE);
            binding.emailError.setVisibility(View.GONE);
            binding.passwordError.setVisibility(View.GONE);
            binding.errorMessage.setVisibility(View.GONE);
            binding.successMessage.setVisibility(View.GONE);

            viewModel.signup(name, email, phone, password);
        });

        // Log In link: navigate to LoginActivity
        binding.loginLink.setOnClickListener(v -> {
            navigateToLogin();
        });
    }

    /**
     * Navigates to LoginActivity and finishes this activity.
     */
    private void navigateToLogin() {
        Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}
