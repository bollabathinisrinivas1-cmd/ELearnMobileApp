package com.example.elearn.auth;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages authentication state including JWT token storage, user info persistence,
 * and session lifecycle via SharedPreferences.
 */
public class AuthService {
    private static final String PREFS_NAME = "elibrary_auth";
    private static final String KEY_ACCESS_TOKEN = "access_token";
    private static final String KEY_REFRESH_TOKEN = "refresh_token";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_ROLE = "user_role";
    private static final String KEY_USER_NAME = "user_name";

    private static final String DOTNET_ROLE_CLAIM =
            "http://schemas.microsoft.com/ws/2008/06/identity/claims/role";

    private final SharedPreferences prefs;

    public AuthService(Context context) {
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Stores access and refresh tokens in SharedPreferences.
     */
    public void storeTokens(String accessToken, String refreshToken) {
        prefs.edit()
                .putString(KEY_ACCESS_TOKEN, accessToken)
                .putString(KEY_REFRESH_TOKEN, refreshToken)
                .apply();
    }

    /**
     * Stores user information (userId, role, name) in SharedPreferences.
     */
    public void storeUserInfo(String userId, String role, String name) {
        prefs.edit()
                .putString(KEY_USER_ID, userId)
                .putString(KEY_USER_ROLE, role)
                .putString(KEY_USER_NAME, name)
                .apply();
    }

    /**
     * Returns the stored access token, or null if not present.
     */
    public String getAccessToken() {
        return prefs.getString(KEY_ACCESS_TOKEN, null);
    }

    /**
     * Returns the stored user ID, or null if not present.
     */
    public String getUserId() {
        return prefs.getString(KEY_USER_ID, null);
    }

    /**
     * Returns the stored user role, or null if not present.
     */
    public String getUserRole() {
        return prefs.getString(KEY_USER_ROLE, null);
    }

    /**
     * Returns the stored user name, or null if not present.
     */
    public String getUserName() {
        return prefs.getString(KEY_USER_NAME, null);
    }

    /**
     * Returns true if a valid access token is present in storage.
     */
    public boolean isLoggedIn() {
        String token = getAccessToken();
        return token != null && !token.isEmpty();
    }

    /**
     * Returns true if the stored user role is "Admin".
     */
    public boolean isAdmin() {
        return "Admin".equals(getUserRole());
    }

    /**
     * Clears all authentication-related data from SharedPreferences.
     */
    public void clearSession() {
        prefs.edit()
                .remove(KEY_ACCESS_TOKEN)
                .remove(KEY_REFRESH_TOKEN)
                .remove(KEY_USER_ID)
                .remove(KEY_USER_ROLE)
                .remove(KEY_USER_NAME)
                .apply();
    }

    /**
     * Decodes a JWT token and extracts user claims from the payload segment.
     * <p>
     * Splits the token by ".", Base64-decodes the second segment (payload),
     * parses it as JSON, and extracts:
     * <ul>
     *   <li>"sub" claim as userId</li>
     *   <li>Role from .NET claim URI or "role" field</li>
     *   <li>"name" claim</li>
     * </ul>
     *
     * @param token the JWT token string
     * @return a Map with keys "userId", "role", "name", or null if decoding fails
     */
    public static Map<String, String> decodeJwt(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                return null;
            }

            // Base64-decode the payload (second segment)
            byte[] decodedBytes = Base64.decode(parts[1], Base64.URL_SAFE | Base64.NO_WRAP);
            String payload = new String(decodedBytes, "UTF-8");

            JSONObject json = new JSONObject(payload);

            Map<String, String> claims = new HashMap<>();

            // Extract "sub" as userId
            String userId = json.optString("sub", null);
            claims.put("userId", userId);

            // Extract role: check .NET claim URI first, fall back to "role"
            String role = json.optString(DOTNET_ROLE_CLAIM, null);
            if (role == null || role.isEmpty()) {
                role = json.optString("role", null);
            }
            claims.put("role", role);

            // Extract "name" claim
            String name = json.optString("name", null);
            claims.put("name", name);

            return claims;
        } catch (Exception e) {
            return null;
        }
    }
}
