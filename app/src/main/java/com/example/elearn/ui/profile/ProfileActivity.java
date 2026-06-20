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
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.elearn.R;
import com.example.elearn.auth.AuthService;
import com.example.elearn.network.ApiClient;

import org.json.JSONObject;

import java.util.concurrent.Executors;

/**
 * Activity that displays the user's profile fetched from GET /users/{userId}/profile.
 */
public class ProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        AuthService authService = new AuthService(this);
        String userId = authService.getUserId();
        String token = authService.getAccessToken();

        ProgressBar progressBar = findViewById(R.id.progressBar);
        ScrollView profileContent = findViewById(R.id.profileContent);

        // Set name and role from stored values
        TextView profileName = findViewById(R.id.profileName);
        TextView profileRole = findViewById(R.id.profileRole);
        profileName.setText(authService.getUserName());
        profileRole.setText(authService.getUserRole());

        progressBar.setVisibility(View.VISIBLE);

        Handler handler = new Handler(Looper.getMainLooper());
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                JSONObject profile = ApiClient.getObject("/users/" + userId + "/profile", token);

                String gender = profile.optString("gender", "");
                String bio = profile.optString("bio", "");
                String country = profile.optString("country", "");
                String state = profile.optString("state", "");
                String district = profile.optString("district", "");
                String village = profile.optString("village", "");
                String education = profile.optString("education", "");
                String qualification = profile.optString("qualification", "");
                String imageUrl = profile.optString("profileImageUrl", "");

                handler.post(() -> {
                    progressBar.setVisibility(View.GONE);
                    profileContent.setVisibility(View.VISIBLE);

                    setText(R.id.profileGender, gender.isEmpty() ? "Not set" : gender);
                    setText(R.id.profileBio, bio.isEmpty() ? "Not set" : bio);
                    setText(R.id.profileCountry, country.isEmpty() ? "Not set" : country);
                    setText(R.id.profileState, state.isEmpty() ? "Not set" : state);
                    setText(R.id.profileDistrict, district.isEmpty() ? "Not set" : district);
                    setText(R.id.profileVillage, village.isEmpty() ? "Not set" : village);

                    String eduText = !education.isEmpty() ? education : (!qualification.isEmpty() ? qualification : "Not set");
                    setText(R.id.profileEducation, eduText);

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
