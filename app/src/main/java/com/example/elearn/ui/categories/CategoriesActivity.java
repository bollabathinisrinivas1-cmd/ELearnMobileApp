package com.example.elearn.ui.categories;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.elearn.databinding.ActivityCategoriesBinding;

/**
 * Stub activity for the Categories screen. Displays a placeholder title.
 */
public class CategoriesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityCategoriesBinding binding = ActivityCategoriesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Categories");
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
