package com.example.elearn.ui.splash;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.example.elearn.R;
import com.example.elearn.auth.AuthService;
import com.example.elearn.ui.dashboard.DashboardActivity;
import com.example.elearn.ui.login.LoginActivity;

/**
 * Splash screen activity that displays the brand image for ~2 seconds
 * before routing to LoginActivity or DashboardActivity based on auth state.
 */
public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DELAY_MS = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            AuthService authService = new AuthService(this);
            Intent intent;
            if (authService.isLoggedIn()) {
                intent = new Intent(SplashActivity.this, DashboardActivity.class);
            } else {
                intent = new Intent(SplashActivity.this, LoginActivity.class);
            }
            startActivity(intent);
            finish();
        }, SPLASH_DELAY_MS);
    }
}
