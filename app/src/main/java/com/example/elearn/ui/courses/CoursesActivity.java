package com.example.elearn.ui.courses;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.elearn.databinding.ActivityCoursesBinding;

/**
 * Stub activity for the Courses screen. Displays a placeholder title.
 */
public class CoursesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityCoursesBinding binding = ActivityCoursesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Courses");
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
