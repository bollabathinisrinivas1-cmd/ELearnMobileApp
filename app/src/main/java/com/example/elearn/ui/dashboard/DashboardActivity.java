package com.example.elearn.ui.dashboard;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.elearn.R;
import com.example.elearn.auth.AuthService;
import com.example.elearn.databinding.ActivityDashboardBinding;
import com.example.elearn.network.ApiClient;
import com.example.elearn.ui.categories.CategoriesActivity;
import com.example.elearn.ui.courses.CoursesActivity;
import com.example.elearn.ui.enrollments.EnrollmentsActivity;
import com.example.elearn.ui.login.LoginActivity;
import com.example.elearn.ui.users.UsersActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Redesigned Dashboard screen with overview cards, quick actions,
 * enrollment trend chart, recent courses/users, and bottom navigation.
 */
public class DashboardActivity extends AppCompatActivity {

    public static boolean needsRefresh = false;

    private ActivityDashboardBinding binding;
    private AuthService authService;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize AuthService and check login state
        authService = new AuthService(this);
        if (!authService.isLoggedIn()) {
            new Thread(() -> {
                boolean refreshed = authService.refreshToken();
                runOnUiThread(() -> {
                    if (refreshed) {
                        initDashboard();
                    } else {
                        navigateToLogin();
                    }
                });
            }).start();
            return;
        }

        initDashboard();
    }

    /**
     * Initializes the dashboard UI after authentication is confirmed.
     */
    private void initDashboard() {
        binding = ActivityDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Set greeting based on time of day
        setupGreeting();

        // Profile avatar click
        binding.profileAvatar.setOnClickListener(this::showUserMenu);

        // Load profile image
        loadProfileImage();

        // Setup quick actions
        setupQuickActions();

        // Setup bottom navigation
        setupBottomNav();

        // Apply role-based visibility
        applyRoleBasedVisibility();

        // Load data from API
        loadDashboardData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (needsRefresh && authService != null && binding != null) {
            loadDashboardData();
            needsRefresh = false;
        }
    }

    /**
     * Sets the greeting text based on current time of day.
     */
    private void setupGreeting() {
        String userName = authService.getUserName();
        if (userName == null || userName.isEmpty()) userName = "User";

        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        String greeting;
        if (hour < 12) {
            greeting = "Good Morning";
        } else if (hour < 17) {
            greeting = "Good Afternoon";
        } else {
            greeting = "Good Evening";
        }

        binding.welcomeText.setText("Hello, " + userName);
    }

    /**
     * Sets up quick action button click handlers.
     */
    private void setupQuickActions() {
        binding.actionAddCourse.setOnClickListener(v ->
                startActivity(new Intent(this, CoursesActivity.class)));

        binding.actionAddUser.setOnClickListener(v -> showCreateUserDialog());

        binding.actionAddCategory.setOnClickListener(v ->
                startActivity(new Intent(this, CategoriesActivity.class)));

        binding.actionReports.setOnClickListener(v ->
                Toast.makeText(this, "Coming Soon", Toast.LENGTH_SHORT).show());
    }

    /**
     * Shows a "Create User" dialog with Name, Email, Phone, and Role fields.
     */
    private void showCreateUserDialog() {
        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(48, 32, 48, 16);

        android.widget.EditText nameInput = new android.widget.EditText(this);
        nameInput.setHint("Name");
        nameInput.setInputType(android.text.InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
        layout.addView(nameInput);

        android.widget.EditText emailInput = new android.widget.EditText(this);
        emailInput.setHint("Email");
        emailInput.setInputType(android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        android.widget.LinearLayout.LayoutParams emailParams = new android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
        emailParams.topMargin = 16;
        emailInput.setLayoutParams(emailParams);
        layout.addView(emailInput);

        android.widget.EditText phoneInput = new android.widget.EditText(this);
        phoneInput.setHint("Phone");
        phoneInput.setInputType(android.text.InputType.TYPE_CLASS_PHONE);
        android.widget.LinearLayout.LayoutParams phoneParams = new android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
        phoneParams.topMargin = 16;
        phoneInput.setLayoutParams(phoneParams);
        layout.addView(phoneInput);

        android.widget.Spinner roleSpinner = new android.widget.Spinner(this);
        android.widget.ArrayAdapter<String> roleAdapter = new android.widget.ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item,
                new String[]{"Student", "Teacher", "Admin"});
        roleSpinner.setAdapter(roleAdapter);
        android.widget.LinearLayout.LayoutParams spinnerParams = new android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
        spinnerParams.topMargin = 16;
        roleSpinner.setLayoutParams(spinnerParams);
        layout.addView(roleSpinner);

        // Info text
        android.widget.TextView infoText = new android.widget.TextView(this);
        infoText.setText("An email invitation will be sent to the user to set their password.");
        infoText.setTextColor(0xFF1976D2);
        infoText.setTextSize(12);
        infoText.setPadding(0, 24, 0, 0);
        infoText.setBackgroundColor(0xFFE3F2FD);
        infoText.setPadding(16, 12, 16, 12);
        android.widget.LinearLayout.LayoutParams infoParams = new android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
        infoParams.topMargin = 24;
        infoText.setLayoutParams(infoParams);
        layout.addView(infoText);

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Create User")
                .setView(layout)
                .setPositiveButton("Create", (dialog, which) -> {
                    String name = nameInput.getText().toString().trim();
                    String email = emailInput.getText().toString().trim();
                    String phone = phoneInput.getText().toString().trim();
                    String role = roleSpinner.getSelectedItem().toString();

                    if (name.isEmpty()) {
                        Toast.makeText(this, "Name is required", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (email.isEmpty()) {
                        Toast.makeText(this, "Email is required", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    createUser(name, email, phone, role);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Creates a new user via the admin API endpoint.
     */
    private void createUser(String name, String email, String phone, String role) {
        String token = authService.getAccessToken();
        executor.execute(() -> {
            try {
                org.json.JSONObject body = new org.json.JSONObject();
                body.put("name", name);
                body.put("email", email);
                body.put("phone", phone);
                body.put("role", role);
                ApiClient.postWithAuth("/users/admin/user", body, token);
                mainHandler.post(() -> {
                    Toast.makeText(this, "User created successfully", Toast.LENGTH_SHORT).show();
                    needsRefresh = true;
                    loadDashboardData();
                });
            } catch (Exception e) {
                mainHandler.post(() -> Toast.makeText(this, "Failed to create user. Email may already exist.", Toast.LENGTH_SHORT).show());
            }
        });
    }

    /**
     * Sets up bottom navigation click handlers.
     */
    private void setupBottomNav() {
        binding.navDashboard.setOnClickListener(v -> {
            // Already on dashboard, do nothing
        });

        binding.navCourses.setOnClickListener(v ->
                startActivity(new Intent(this, CoursesActivity.class)));

        binding.navStudents.setOnClickListener(v -> {
                Intent intent = new Intent(this, UsersActivity.class);
                intent.putExtra("roleFilter", "Student");
                startActivity(intent);
        });

        binding.navReports.setOnClickListener(v -> {
                Intent intent = new Intent(this, UsersActivity.class);
                intent.putExtra("roleFilter", "Staff");
                startActivity(intent);
        });

        binding.navSettings.setOnClickListener(v ->
                Toast.makeText(this, "Coming Soon", Toast.LENGTH_SHORT).show());
    }

    /**
     * Hides admin-only UI elements for non-Admin roles.
     * Admin: sees everything
     * Teacher: sees Total Courses, Categories, My Enrollments + Add Course quick action
     * Student: sees Total Courses, Categories, My Enrollments only (no quick actions)
     */
    private void applyRoleBasedVisibility() {
        boolean isAdmin = authService.isAdmin();
        String role = authService.getUserRole();
        boolean isTeacher = "Teacher".equals(role);

        if (!isAdmin) {
            // Hide Students and Teachers & Admin overview cards
            binding.cardStudents.setVisibility(View.GONE);
            binding.cardStaff.setVisibility(View.GONE);

            // Hide Students and Teachers bottom nav tabs
            binding.navStudents.setVisibility(View.GONE);
            binding.navReports.setVisibility(View.GONE);

            // Hide Enrollment Trend chart (Admin-only)
            binding.chartContainer.setVisibility(View.GONE);

            if (isTeacher) {
                // Teacher: show Add Course only, hide Add User, Add Category, Reports
                binding.actionAddUser.setVisibility(View.GONE);
                binding.actionAddCategory.setVisibility(View.GONE);
                binding.actionReports.setVisibility(View.GONE);
            } else {
                // Student: hide all quick actions
                binding.quickActionsRow.setVisibility(View.GONE);
            }
        }
    }

    /**
     * Loads all dashboard data from API in background thread.
     */
    private void loadDashboardData() {
        binding.dashboardProgress.setVisibility(View.VISIBLE);
        binding.dashboardError.setVisibility(View.GONE);

        executor.execute(() -> {
            try {
                String token = authService.getAccessToken();
                String userId = authService.getUserId();

                // Fetch courses
                JSONArray coursesArray = ApiClient.getArray("/courses", token);
                int courseCount = coursesArray.length();

                // Fetch categories
                JSONArray categoriesArray = ApiClient.getArray("/categories", token);
                int categoryCount = categoriesArray.length();

                // Fetch users (Admin only - will 403 for other roles)
                JSONArray usersArray = new JSONArray();
                int studentCount = 0;
                int staffCount = 0;
                if (authService.isAdmin()) {
                    try {
                        usersArray = ApiClient.getArray("/users", token);
                        for (int i = 0; i < usersArray.length(); i++) {
                            JSONObject user = usersArray.getJSONObject(i);
                            String role = user.optString("role", "");
                            if ("Student".equals(role)) {
                                studentCount++;
                            } else {
                                staffCount++;
                            }
                        }
                    } catch (Exception ignored) {}
                }

                // Fetch enrollments for current user
                JSONArray enrollmentsArray = ApiClient.getArray("/enrollments/" + userId, token);
                int enrollmentCount = enrollmentsArray.length();

                // Prepare recent data
                final int finalCourseCount = courseCount;
                final int finalStudentCount = studentCount;
                final int finalStaffCount = staffCount;
                final int finalCategoryCount = categoryCount;
                final int finalEnrollmentCount = enrollmentCount;
                final JSONArray finalCoursesArray = coursesArray;
                final JSONArray finalUsersArray = usersArray;

                mainHandler.post(() -> {
                    binding.dashboardProgress.setVisibility(View.GONE);

                    // Populate overview cards
                    populateOverviewCards(finalCourseCount, finalStudentCount,
                            finalStaffCount, finalCategoryCount, finalEnrollmentCount);

                    // Populate enrollment count
                    binding.enrollmentCount.setText(String.valueOf(finalEnrollmentCount));

                    // Setup line chart
                    setupLineChart();

                    // Populate recent courses
                    populateRecentCourses(finalCoursesArray);

                    // Populate recent users (Admin only)
                    if (authService.isAdmin()) {
                        populateRecentUsers(finalUsersArray);
                    } else {
                        // Hide the recent users card for non-admin
                        binding.viewAllUsers.setVisibility(View.GONE);
                        binding.recentUsersContainer.setVisibility(View.GONE);
                    }

                    // View All click handlers
                    binding.viewAllCourses.setOnClickListener(v ->
                            startActivity(new Intent(this, CoursesActivity.class)));
                    binding.viewAllUsers.setOnClickListener(v ->
                            startActivity(new Intent(this, UsersActivity.class)));
                });

            } catch (Exception e) {
                mainHandler.post(() -> {
                    binding.dashboardProgress.setVisibility(View.GONE);
                    String msg = e.getMessage();
                    if (msg != null && msg.contains("401")) {
                        authService.clearSession();
                        navigateToLogin();
                    } else {
                        binding.dashboardError.setText("Unable to load dashboard data. Please try again.");
                        binding.dashboardError.setVisibility(View.VISIBLE);
                    }
                });
            }
        });
    }

    /**
     * Populates the overview stat cards with API data.
     */
    private void populateOverviewCards(int courses, int students, int staff, int categories, int enrollments) {
        binding.countCourses.setText(String.valueOf(courses));
        binding.countCategories.setText(String.valueOf(categories));

        if (authService.isAdmin()) {
            binding.countStudents.setText(String.valueOf(students));
            binding.countStaff.setText(String.valueOf(staff));
        }
        // For non-admin: cardStudents and cardStaff are hidden via applyRoleBasedVisibility
    }

    /**
     * Sets up the line chart with static sample enrollment data.
     */
    private void setupLineChart() {
        LineChartView lineChart = new LineChartView(this);
        float[] values = {2, 3, 4, 6, 8, 10};
        String[] labels = {"Jan", "Feb", "Mar", "Apr", "May", "Jun"};
        lineChart.setData(values, labels);

        FrameLayout chartFrame = binding.getRoot().findViewById(R.id.lineChartFrame);
        chartFrame.removeAllViews();
        chartFrame.addView(lineChart, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));
    }

    /**
     * Populates the Recent Courses section with first 3 courses.
     */
    private void populateRecentCourses(JSONArray coursesArray) {
        LinearLayout container = binding.recentCoursesContainer;
        container.removeAllViews();

        int count = Math.min(3, coursesArray.length());
        for (int i = 0; i < count; i++) {
            try {
                JSONObject course = coursesArray.getJSONObject(i);
                String title = course.optString("title", "Untitled");

                TextView tv = new TextView(this);
                tv.setText("• " + title);
                tv.setTextSize(11);
                tv.setTextColor(0xFF4B5563);
                tv.setPadding(0, 4, 0, 4);
                tv.setMaxLines(1);
                tv.setEllipsize(android.text.TextUtils.TruncateAt.END);
                container.addView(tv);
            } catch (Exception ignored) {
            }
        }

        if (count == 0) {
            TextView tv = new TextView(this);
            tv.setText("No courses yet");
            tv.setTextSize(11);
            tv.setTextColor(0xFF9CA3AF);
            container.addView(tv);
        }
    }

    /**
     * Populates the Recent Users section with first 3 users.
     */
    private void populateRecentUsers(JSONArray usersArray) {
        LinearLayout container = binding.recentUsersContainer;
        container.removeAllViews();

        int count = Math.min(3, usersArray.length());
        for (int i = 0; i < count; i++) {
            try {
                JSONObject user = usersArray.getJSONObject(i);
                String name = user.optString("name", "Unknown");

                TextView tv = new TextView(this);
                tv.setText("• " + name);
                tv.setTextSize(11);
                tv.setTextColor(0xFF4B5563);
                tv.setPadding(0, 4, 0, 4);
                tv.setMaxLines(1);
                tv.setEllipsize(android.text.TextUtils.TruncateAt.END);
                container.addView(tv);
            } catch (Exception ignored) {
            }
        }

        if (count == 0) {
            TextView tv = new TextView(this);
            tv.setText("No users yet");
            tv.setTextSize(11);
            tv.setTextColor(0xFF9CA3AF);
            container.addView(tv);
        }
    }

    /**
     * Displays a popup menu with profile name, Settings, and Sign Out.
     */
    private void showUserMenu(View anchor) {
        PopupMenu popup = new PopupMenu(this, anchor);
        popup.inflate(R.menu.user_menu);

        // Set user name as the first item title with bold styling
        MenuItem userNameItem = popup.getMenu().findItem(R.id.menu_user_name);
        String userName = authService.getUserName();
        if (userName != null && !userName.isEmpty()) {
            android.text.SpannableString boldName = new android.text.SpannableString(userName);
            boldName.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD),
                    0, boldName.length(), 0);
            userNameItem.setTitle(boldName);
            userNameItem.setEnabled(true);
        }

        popup.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.menu_sign_out) {
                authService.clearSession();
                navigateToLogin();
                return true;
            } else if (id == R.id.menu_profile) {
                startActivity(new Intent(DashboardActivity.this,
                        com.example.elearn.ui.profile.ProfileActivity.class));
                return true;
            } else if (id == R.id.menu_settings) {
                Toast.makeText(this, "Settings - Coming Soon", Toast.LENGTH_SHORT).show();
                return true;
            }
            return false;
        });

        popup.show();
    }

    /**
     * Fetches the user's profile image from the API and sets it on the avatar.
     */
    private void loadProfileImage() {
        String userId = authService.getUserId();
        String token = authService.getAccessToken();
        if (userId == null || token == null) return;

        executor.execute(() -> {
            try {
                JSONObject profile = ApiClient.getObject("/users/" + userId + "/profile", token);
                String imageUrl = profile.optString("profileImageUrl", "");

                if (!imageUrl.isEmpty() && imageUrl.startsWith("data:image")) {
                    String base64Data = imageUrl.substring(imageUrl.indexOf(",") + 1);
                    byte[] imageBytes = Base64.decode(base64Data, Base64.DEFAULT);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);

                    if (bitmap != null) {
                        Bitmap circularBitmap = getCircularBitmap(bitmap);
                        mainHandler.post(() -> {
                            binding.profileAvatar.setImageBitmap(circularBitmap);
                            binding.profileAvatar.setBackground(null);
                        });
                    }
                }
            } catch (Exception e) {
                // Silently fail - keep default avatar
            }
        });
    }

    /**
     * Creates a circular bitmap from a rectangular source bitmap.
     */
    private Bitmap getCircularBitmap(Bitmap bitmap) {
        int size = Math.min(bitmap.getWidth(), bitmap.getHeight());
        Bitmap output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        Paint paint = new Paint();
        paint.setAntiAlias(true);

        Rect rect = new Rect(0, 0, size, size);
        canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }

    /**
     * Navigates to LoginActivity and finishes this activity.
     */
    private void navigateToLogin() {
        Intent intent = new Intent(DashboardActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}
