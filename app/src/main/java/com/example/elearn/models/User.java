package com.example.elearn.models;

import org.json.JSONObject;

/**
 * Model representing a platform user from the eLibrary API.
 */
public class User {
    private String id;
    private String name;
    private String email;
    private String phone;
    private String role;
    private String createdAt;

    /**
     * Creates a User instance from a JSON object returned by the API.
     * Uses opt* methods to handle missing fields gracefully.
     *
     * @param json the JSON object representing a user
     * @return a new User instance
     */
    public static User fromJson(JSONObject json) {
        User user = new User();
        user.id = json.optString("id", "");
        user.name = json.optString("name", "");
        user.email = json.optString("email", "");
        user.phone = json.optString("phone", "");
        user.role = json.optString("role", "");
        user.createdAt = json.optString("createdAt", "");
        return user;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getRole() {
        return role;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}
