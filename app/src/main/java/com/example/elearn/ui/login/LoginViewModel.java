package com.example.elearn.ui.login;

import android.os.Handler;
import android.os.Looper;
import android.util.Patterns;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.elearn.auth.AuthService;
import com.example.elearn.network.ApiClient;
import com.example.elearn.network.ApiException;

import org.json.JSONObject;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * ViewModel for the Login screen. Manages login form state, input validation,
 * API communication, and error handling.
 */
public class LoginViewModel extends ViewModel {

    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loginSuccess = new MutableLiveData<>();

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Boolean> getLoginSuccess() {
        return loginSuccess;
    }

    /**
     * Attempts to log in with the provided email and password.
     * Validates inputs, calls the auth API on a background thread, decodes the JWT,
     * stores tokens and user info, and posts results back to the main thread.
     *
     * @param email       the user's email address
     * @param password    the user's password
     * @param authService the AuthService instance for token storage and JWT decoding
     */
    public void login(String email, String password, AuthService authService) {
        // Clear previous error
        errorMessage.setValue(null);

        // Validate email
        if (email == null || email.trim().isEmpty()) {
            errorMessage.setValue("Email is required");
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) {
            errorMessage.setValue("Please enter a valid email");
            return;
        }

        // Validate password
        if (password == null || password.isEmpty()) {
            errorMessage.setValue("Password is required");
            return;
        }

        // Set loading state
        isLoading.setValue(true);

        // Execute network call on background thread
        executorService.execute(() -> {
            try {
                // Build request body
                JSONObject body = new JSONObject();
                body.put("email", email.trim());
                body.put("password", password);

                // Call login endpoint
                JSONObject response = ApiClient.post("/auth/token", body);

                // Extract tokens from response
                String accessToken = response.getString("accessToken");
                String refreshToken = response.getString("refreshToken");

                // Decode JWT to extract user claims
                Map<String, String> claims = AuthService.decodeJwt(accessToken);
                if (claims == null) {
                    mainHandler.post(() -> {
                        isLoading.setValue(false);
                        errorMessage.setValue("An unexpected error occurred. Please try again.");
                    });
                    return;
                }

                // Store tokens and user info
                authService.storeTokens(accessToken, refreshToken);
                authService.storeUserInfo(
                        claims.get("userId"),
                        claims.get("role"),
                        claims.get("name")
                );

                // Post success on main thread
                mainHandler.post(() -> {
                    isLoading.setValue(false);
                    loginSuccess.setValue(true);
                });

            } catch (ApiException e) {
                mainHandler.post(() -> {
                    isLoading.setValue(false);
                    int statusCode = e.getStatusCode();
                    if (statusCode == 401) {
                        errorMessage.setValue("Invalid email or password");
                    } else if (statusCode >= 500) {
                        errorMessage.setValue("Server error. Please try again later.");
                    } else {
                        // Other errors: use response body or fallback
                        String body = e.getResponseBody();
                        if (body != null && !body.isEmpty()) {
                            errorMessage.setValue(body);
                        } else {
                            errorMessage.setValue("An unexpected error occurred. Please try again.");
                        }
                    }
                });
            } catch (Exception e) {
                // IOException and any other exceptions
                mainHandler.post(() -> {
                    isLoading.setValue(false);
                    errorMessage.setValue("Unable to connect to server. Please try again later.");
                });
            }
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executorService.shutdown();
    }
}
