package com.example.elearn.ui.profile;

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
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.elearn.R;
import com.example.elearn.auth.AuthService;
import com.example.elearn.network.ApiClient;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONObject;

import java.util.concurrent.Executors;

/**
 * Activity that displays the user's profile fetched from GET /users/{userId}/profile.
 * Supports editing profile fields via a FAB that opens an edit dialog.
 */
public class ProfileActivity extends AppCompatActivity {

    private String currentGender = "";
    private String currentBio = "";
    private String currentCountry = "";
    private String currentState = "";
    private String currentDistrict = "";
    private String currentVillage = "";
    private String currentEducation = "";
    private AuthService authService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        authService = new AuthService(this);
        String userId = authService.getUserId();
        String token = authService.getAccessToken();

        ProgressBar progressBar = findViewById(R.id.progressBar);
        ScrollView profileContent = findViewById(R.id.profileContent);

        // Set name and role from stored values
        TextView profileName = findViewById(R.id.profileName);
        TextView profileRole = findViewById(R.id.profileRole);
        profileName.setText(authService.getUserName());
        profileRole.setText(authService.getUserRole());

        // Set up FAB for editing profile
        FloatingActionButton fabEditProfile = findViewById(R.id.fabEditProfile);
        fabEditProfile.setOnClickListener(v -> showEditProfileDialog());

        progressBar.setVisibility(View.VISIBLE);

        Handler handler = new Handler(Looper.getMainLooper());
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                JSONObject profile = ApiClient.getObject("/users/" + userId + "/profile", token);

                currentGender = profile.optString("gender", "");
                currentBio = profile.optString("bio", "");
                currentCountry = profile.optString("country", "");
                currentState = profile.optString("state", "");
                currentDistrict = profile.optString("district", "");
                currentVillage = profile.optString("village", "");
                String education = profile.optString("education", "");
                String qualification = profile.optString("qualification", "");
                currentEducation = !education.isEmpty() ? education : qualification;
                String imageUrl = profile.optString("profileImageUrl", "");

                handler.post(() -> {
                    progressBar.setVisibility(View.GONE);
                    profileContent.setVisibility(View.VISIBLE);

                    setText(R.id.profileGender, currentGender.isEmpty() ? "Not set" : currentGender);
                    setText(R.id.profileBio, currentBio.isEmpty() ? "Not set" : currentBio);
                    setText(R.id.profileCountry, currentCountry.isEmpty() ? "Not set" : currentCountry);
                    setText(R.id.profileState, currentState.isEmpty() ? "Not set" : currentState);
                    setText(R.id.profileDistrict, currentDistrict.isEmpty() ? "Not set" : currentDistrict);
                    setText(R.id.profileVillage, currentVillage.isEmpty() ? "Not set" : currentVillage);
                    setText(R.id.profileEducation, currentEducation.isEmpty() ? "Not set" : currentEducation);

                    // Load profile image
                    if (!imageUrl.isEmpty() && imageUrl.startsWith("data:image")) {
                        try {
                            String base64Data = imageUrl.substring(imageUrl.indexOf(",") + 1);
                            byte[] imageBytes = Base64.decode(base64Data, Base64.DEFAULT);
                            Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                            if (bitmap != null) {
                                ImageView profileImage = findViewById(R.id.profileImage);
                                profileImage.setImageBitmap(getCircularBitmap(bitmap));
                            }
                        } catch (Exception ignored) {}
                    }
                });
            } catch (Exception e) {
                handler.post(() -> {
                    progressBar.setVisibility(View.GONE);
                    profileContent.setVisibility(View.VISIBLE);
                    // Show basic info even if profile fetch fails
                    setText(R.id.profileGender, "Not set");
                    setText(R.id.profileBio, "Not set");
                    setText(R.id.profileCountry, "Not set");
                    setText(R.id.profileState, "Not set");
                    setText(R.id.profileDistrict, "Not set");
                    setText(R.id.profileVillage, "Not set");
                    setText(R.id.profileEducation, "Not set");
                });
            }
        });
    }

    private void showEditProfileDialog() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(48, 32, 48, 16);

        EditText genderInput = createEditText("Gender", currentGender);
        EditText bioInput = createEditText("Bio", currentBio);
        EditText countryInput = createEditText("Country", currentCountry);
        EditText stateInput = createEditText("State", currentState);
        EditText districtInput = createEditText("District", currentDistrict);
        EditText villageInput = createEditText("Village", currentVillage);
        EditText educationInput = createEditText("Education / Qualification", currentEducation);

        layout.addView(genderInput);
        layout.addView(bioInput);
        layout.addView(countryInput);
        layout.addView(stateInput);
        layout.addView(districtInput);
        layout.addView(villageInput);
        layout.addView(educationInput);

        new AlertDialog.Builder(this)
                .setTitle("Edit Profile")
                .setView(layout)
                .setPositiveButton("Save", (dialog, which) -> {
                    String gender = genderInput.getText().toString().trim();
                    String bio = bioInput.getText().toString().trim();
                    String country = countryInput.getText().toString().trim();
                    String state = stateInput.getText().toString().trim();
                    String district = districtInput.getText().toString().trim();
                    String village = villageInput.getText().toString().trim();
                    String education = educationInput.getText().toString().trim();

                    saveProfile(gender, bio, country, state, district, village, education);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private EditText createEditText(String hint, String currentValue) {
        EditText editText = new EditText(this);
        editText.setHint(hint);
        editText.setText(currentValue);
        editText.setSingleLine(true);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 8, 0, 8);
        editText.setLayoutParams(params);
        return editText;
    }

    private void saveProfile(String gender, String bio, String country, String state,
                             String district, String village, String education) {
        String userId = authService.getUserId();
        String token = authService.getAccessToken();
        Handler handler = new Handler(Looper.getMainLooper());

        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                JSONObject body = new JSONObject();
                body.put("gender", gender);
                body.put("bio", bio);
                body.put("country", country);
                body.put("state", state);
                body.put("district", district);
                body.put("village", village);
                body.put("education", education);

                ApiClient.put("/users/" + userId + "/profile", body, token);

                handler.post(() -> {
                    Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show();
                    // Update local state and UI
                    currentGender = gender;
                    currentBio = bio;
                    currentCountry = country;
                    currentState = state;
                    currentDistrict = district;
                    currentVillage = village;
                    currentEducation = education;

                    setText(R.id.profileGender, gender.isEmpty() ? "Not set" : gender);
                    setText(R.id.profileBio, bio.isEmpty() ? "Not set" : bio);
                    setText(R.id.profileCountry, country.isEmpty() ? "Not set" : country);
                    setText(R.id.profileState, state.isEmpty() ? "Not set" : state);
                    setText(R.id.profileDistrict, district.isEmpty() ? "Not set" : district);
                    setText(R.id.profileVillage, village.isEmpty() ? "Not set" : village);
                    setText(R.id.profileEducation, education.isEmpty() ? "Not set" : education);
                });
            } catch (Exception e) {
                handler.post(() -> Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void setText(int viewId, String text) {
        ((TextView) findViewById(viewId)).setText(text);
    }

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
}
