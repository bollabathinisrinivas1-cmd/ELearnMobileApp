package com.example.elearn.ui.signup;

import android.os.Handler;
import android.os.Looper;
import android.util.Patterns;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.elearn.network.ApiClient;
import com.example.elearn.network.ApiException;

import org.json.JSONObject;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * ViewModel for the Signup screen. Manages signup form state, input validation,
 * API communication, and success/error handling.
 */
public class SignupViewModel extends ViewModel {

    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<String> successMessage = new MutableLiveData<>();

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<String> getSuccessMessage() {
        return successMessage;
    }

    /**
     * Attempts to register a new user with the provided details.
     * Validates inputs, calls the signup API on a background thread,
     * and posts results back to the main thread via LiveData.
     *
     * @param name     the user's full name
     * @param email    the user's email address
     * @param phone    the user's phone number (optional, may be empty)
     * @param password the user's chosen password
     */
    public void signup(String name, String email, String phone, String password) {
        // Clear previous messages
        errorMessage.setValue(null);
        successMessage.setValue(null);

        // Validate name
        if (name == null || name.trim().isEmpty()) {
            errorMessage.setValue("Name is required");
            return;
        }

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
        if (password.length() < 6) {
            errorMessage.setValue("Password must be at least 6 characters");
            return;
        }

        // Set loading state
        isLoading.setValue(true);

        // Execute network call on background thread
        executorService.execute(() -> {
            try {
                // Build request body
                JSONObject body = new JSONObject();
                body.put("name", name.trim());
                body.put("email", email.trim());
                if (phone != null && !phone.trim().isEmpty()) {
                    body.put("phone", phone.trim());
                }
                body.put("password", password);
                body.put("role", "Student");

                // Call signup endpoint
                ApiClient.post("/users", body);

                // Post success on main thread
                mainHandler.post(() -> {
                    isLoading.setValue(false);
                    successMessage.setValue("Account created successfully! Redirecting to login...");
                });

            } catch (ApiException e) {
                mainHandler.post(() -> {
                    isLoading.setValue(false);
                    // Try to extract "message" field from response body JSON
                    String responseBody = e.getResponseBody();
                    if (responseBody != null && !responseBody.isEmpty()) {
                        try {
                            JSONObject errorJson = new JSONObject(responseBody);
                            if (errorJson.has("message")) {
                                errorMessage.setValue(errorJson.getString("message"));
                                return;
                            }
                        } catch (Exception ignored) {
                            // Not valid JSON, use raw response body
                        }
                        errorMessage.setValue(responseBody);
                    } else {
                        errorMessage.setValue("An unexpected error occurred. Please try again.");
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
