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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    private boolean hasAttemptedRefresh = false;

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

                    // Build a map of courseId → isFree from the courses array
                    Map<String, Boolean> courseIsFreeMap = new HashMap<>();
                    for (int i = 0; i < coursesArray.length(); i++) {
                        JSONObject c = coursesArray.getJSONObject(i);
                        courseIsFreeMap.put(c.optString("id", ""), c.optBoolean("isFree", false));
                    }

                    // Count enrollments by course type
                    int paidEnrollments = 0;
                    int freeEnrollments = 0;
                    for (int i = 0; i < enrollmentsArray.length(); i++) {
                        JSONObject e = enrollmentsArray.getJSONObject(i);
                        String courseId = e.optString("courseId", "");
                        Boolean isFree = courseIsFreeMap.get(courseId);
                        if (isFree != null && isFree) {
                            freeEnrollments++;
                        } else {
                            paidEnrollments++;
                        }
                    }

                    int totalEnrollments = paidEnrollments + freeEnrollments;
                    chartData = new PieChartData(paidEnrollments, freeEnrollments, totalEnrollments);

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
                int statusCode = e.getStatusCode();
                if (statusCode == 401 && !hasAttemptedRefresh) {
                    hasAttemptedRefresh = true;
                    boolean refreshed = authService.refreshToken();
                    if (refreshed) {
                        // Retry the data load with new token
                        loadDashboardDataInternal(authService);
                        return;
                    } else {
                        mainHandler.post(() -> {
                            errorMessage.setValue("Session expired. Please log in again.");
                            isLoading.setValue(false);
                        });
                    }
                } else if (statusCode == 401) {
                    mainHandler.post(() -> {
                        errorMessage.setValue("Session expired. Please log in again.");
                        isLoading.setValue(false);
                    });
                } else {
                    mainHandler.post(() -> {
                        if (statusCode >= 500) {
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
                }
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

    /**
     * Internal method that performs the actual data loading.
     * Called directly (not via executor) when retrying after a token refresh,
     * since we are already on the background thread.
     */
    private void loadDashboardDataInternal(AuthService authService) {
        try {
            String token = authService.getAccessToken();
            String userId = authService.getUserId();
            String role = authService.getUserRole();

            JSONArray coursesArray = ApiClient.getArray("/courses", token);
            int courseCount = coursesArray.length();

            JSONArray categoriesArray = ApiClient.getArray("/categories", token);
            int categoryCount = categoriesArray.length();

            JSONArray enrollmentsArray = ApiClient.getArray("/enrollments/" + userId, token);
            int enrollmentCount = enrollmentsArray.length();

            List<DashboardCard> cardList = new ArrayList<>();
            PieChartData chartData = null;

            if ("Admin".equals(role)) {
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

                // Build a map of courseId → isFree from the courses array
                Map<String, Boolean> courseIsFreeMap = new HashMap<>();
                for (int i = 0; i < coursesArray.length(); i++) {
                    JSONObject c = coursesArray.getJSONObject(i);
                    courseIsFreeMap.put(c.optString("id", ""), c.optBoolean("isFree", false));
                }

                // Count enrollments by course type
                int paidEnrollments = 0;
                int freeEnrollments = 0;
                for (int i = 0; i < enrollmentsArray.length(); i++) {
                    JSONObject e = enrollmentsArray.getJSONObject(i);
                    String courseId = e.optString("courseId", "");
                    Boolean isFree = courseIsFreeMap.get(courseId);
                    if (isFree != null && isFree) {
                        freeEnrollments++;
                    } else {
                        paidEnrollments++;
                    }
                }

                int totalEnrollments = paidEnrollments + freeEnrollments;
                chartData = new PieChartData(paidEnrollments, freeEnrollments, totalEnrollments);

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

            final List<DashboardCard> finalCards = cardList;
            final PieChartData finalChartData = chartData;
            mainHandler.post(() -> {
                cards.setValue(finalCards);
                if (finalChartData != null) {
                    pieChartData.setValue(finalChartData);
                }
                isLoading.setValue(false);
            });

        } catch (Exception e) {
            mainHandler.post(() -> {
                errorMessage.setValue("Unable to connect to server. Please try again later.");
                isLoading.setValue(false);
            });
        }
    }
}
