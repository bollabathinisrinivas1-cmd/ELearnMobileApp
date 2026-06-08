package com.example.elearn.ui.enrollments;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.elearn.databinding.ActivityEnrollmentsBinding;

/**
 * Stub activity for the Enrollments screen. Displays a placeholder title.
 */
public class EnrollmentsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityEnrollmentsBinding binding = ActivityEnrollmentsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("My Enrollments");
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
