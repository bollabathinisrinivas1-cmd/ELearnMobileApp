package com.example.elearn.ui.dashboard;

import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.elearn.auth.AuthService;
import com.example.elearn.models.DashboardCard;
import com.example.elearn.models.PieChartData;
import com.example.elearn.network.ApiClient;
import com.example.elearn.network.ApiException;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * ViewModel for the Dashboard screen. Loads role-based dashboard data
 * from the backend API, builds card lists, and computes pie chart data for Admin users.
 */
public class DashboardViewModel extends ViewModel {

    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(true);
    private final MutableLiveData<List<DashboardCard>> cards = new MutableLiveData<>();
    private final MutableLiveData<PieChartData> pieChartData = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<List<DashboardCard>> getCards() {
        return cards;
    }

    public LiveData<PieChartData> getPieChartData() {
        return pieChartData;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    /**
     * Loads dashboard data based on the authenticated user's role.
     * Fetches courses, categories, enrollments, and (for Admin) users from the API.
     * Builds the appropriate card list and pie chart data, then posts results to LiveData.
     *
     * @param authService the AuthService instance for retrieving token, userId, and role
     */
    public void loadDashboardData(AuthService authService) {
        // Set loading state
        mainHandler.post(() -> isLoading.setValue(true));

        executorService.execute(() -> {
            try {
                String token = authService.getAccessToken();
                String userId = authService.getUserId();
                String role = authService.getUserRole();

                // Fetch courses count
                JSONArray coursesArray = ApiClient.getArray("/courses", token);
                int courseCount = coursesArray.length();

                // Fetch categories count
                JSONArray categoriesArray = ApiClient.getArray("/categories", token);
                int categoryCount = categoriesArray.length();

                // Fetch enrollments count for current user
                JSONArray enrollmentsArray = ApiClient.getArray("/enrollments/" + userId, token);
                int enrollmentCount = enrollmentsArray.length();

                List<DashboardCard> cardList = new ArrayList<>();
                PieChartData chartData = null;

                if ("Admin".equals(role)) {
                    // Fetch users for Admin
                    JSONArray usersArray = ApiClient.getArray("/users", token);
                    int studentCount = 0;
                    int staffCount = 0;

                    for (int i = 0; i < usersArray.length(); i++) {
                        JSONObject user = usersArray.getJSONObject(i);
                        String userRole = user.optString("role", "");
                        if ("Student".equals(userRole)) {
                            studentCount++;
                        } else {
                            staffCount++;
                        }
                    }

                    // Compute PieChartData
                    // paidStudents = those with enrollments (simplified: enrollmentCount)
                    // freeStudents = totalStudents - paidStudents
                    int totalStudents = studentCount;
                    int paidStudents = Math.min(enrollmentCount, totalStudents);
                    int freeStudents = totalStudents - paidStudents;
                    chartData = new PieChartData(paidStudents, freeStudents, totalStudents);

                    // Build 5 cards for Admin
                    cardList.add(new DashboardCard(
                            "Total Courses", "Available courses",
                            "school", android.R.color.holo_blue_light,
                            courseCount, "courses"));
                    cardList.add(new DashboardCard(
                            "Categories", "Course categories",
                            "category", android.R.color.holo_green_light,
                            categoryCount, "categories"));
                    cardList.add(new DashboardCard(
                            "Students", "Registered students",
                            "people", android.R.color.holo_orange_light,
                            studentCount, "users"));
                    cardList.add(new DashboardCard(
                            "Teachers & Admin", "Staff members",
                            "admin_panel_settings", android.R.color.holo_red_light,
                            staffCount, "users"));
                    cardList.add(new DashboardCard(
                            "My Enrollments", "Enrolled courses",
                            "assignment", android.R.color.holo_purple,
                            enrollmentCount, "enrollments"));
                } else {
                    // Build 3 cards for Student/Teacher
                    cardList.add(new DashboardCard(
                            "Total Courses", "Available courses",
                            "school", android.R.color.holo_blue_light,
                            courseCount, "courses"));
                    cardList.add(new DashboardCard(
                            "Categories", "Course categories",
                            "category", android.R.color.holo_green_light,
                            categoryCount, "categories"));
                    cardList.add(new DashboardCard(
                            "My Enrollments", "Enrolled courses",
                            "assignment", android.R.color.holo_purple,
                            enrollmentCount, "enrollments"));
                }

                // Post results on main thread
                final List<DashboardCard> finalCards = cardList;
                final PieChartData finalChartData = chartData;
                mainHandler.post(() -> {
                    cards.setValue(finalCards);
                    if (finalChartData != null) {
                        pieChartData.setValue(finalChartData);
                    }
                    isLoading.setValue(false);
                });

            } catch (ApiException e) {
                mainHandler.post(() -> {
                    int statusCode = e.getStatusCode();
                    if (statusCode == 401) {
                        errorMessage.setValue("Session expired. Please log in again.");
                    } else if (statusCode >= 500) {
                        errorMessage.setValue("Server error. Please try again later.");
                    } else {
                        String body = e.getResponseBody();
                        if (body != null && !body.isEmpty()) {
                            errorMessage.setValue(body);
                        } else {
                            errorMessage.setValue("An unexpected error occurred. Please try again.");
                        }
                    }
                    isLoading.setValue(false);
                });
            } catch (Exception e) {
                mainHandler.post(() -> {
                    errorMessage.setValue("Unable to connect to server. Please try again later.");
                    isLoading.setValue(false);
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
