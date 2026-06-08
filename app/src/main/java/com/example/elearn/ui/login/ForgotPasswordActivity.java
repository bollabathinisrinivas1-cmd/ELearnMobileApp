package com.example.elearn.ui.login;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.elearn.databinding.ActivityForgotPasswordBinding;

/**
 * Forgot Password screen activity. Allows users to enter their email address
 * and request a password reset link. Displays a confirmation toast and returns
 * to the login screen upon submission.
 */
public class ForgotPasswordActivity extends AppCompatActivity {

    private ActivityForgotPasswordBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inflate layout with ViewBinding
        binding = ActivityForgotPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Enable up navigation (back arrow in action bar)
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Forgot Password");
        }

        // Set up click listeners
        setupClickListeners();
    }

    /**
     * Sets up click listeners for the reset password button.
     */
    private void setupClickListeners() {
        // Reset Password button: show confirmation toast and finish activity
        binding.resetPasswordButton.setOnClickListener(v -> {
            String email = binding.emailEditText.getText().toString().trim();

            if (email.isEmpty()) {
                binding.emailEditText.setError("Please enter your email");
                return;
            }

            // Placeholder behavior: show toast and return to login
            Toast.makeText(this, "Password reset link sent", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
