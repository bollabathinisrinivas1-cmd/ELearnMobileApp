package com.example.elearn.ui.users;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.elearn.databinding.ActivityUsersBinding;

/**
 * Stub activity for the Users screen. Displays a placeholder title.
 */
public class UsersActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityUsersBinding binding = ActivityUsersBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Users");
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
