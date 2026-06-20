package com.example.elearn.models;

import org.json.JSONObject;

/**
 * Model representing a course from the eLibrary API.
 * Contains course metadata including pricing, duration, and category info.
 */
public class Course {
    private String id;
    private String title;
    private String description;
    private double price;
    private boolean isFree;
    private String duration;
    private double durationHours;
    private String level;
    private String thumbnailUrl;
    private int categoryId;
    private String instructorId;
    private String createdAt;

    /**
     * Creates a Course instance from a JSON object returned by the API.
     * Uses opt* methods to handle missing fields gracefully.
     *
     * @param json the JSON object representing a course
     * @return a new Course instance
     */
    public static Course fromJson(JSONObject json) {
        Course course = new Course();
        course.id = json.optString("id", "");
        course.title = json.optString("title", "");
        course.description = json.optString("description", "");
        course.price = json.optDouble("price", 0.0);
        course.isFree = json.optBoolean("isFree", false);
        course.duration = json.optString("duration", "");
        course.durationHours = json.optDouble("durationHours", 0.0);
        course.level = json.optString("level", "");
        course.thumbnailUrl = json.optString("thumbnailUrl", "");
        course.categoryId = json.optInt("categoryId", 0);
        course.instructorId = json.optString("instructorId", "");
        course.createdAt = json.optString("createdAt", "");
        return course;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public double getPrice() {
        return price;
    }

    public boolean isFree() {
        return isFree;
    }

    public String getDuration() {
        return duration;
    }

    public double getDurationHours() {
        return durationHours;
    }

    public String getLevel() {
        return level;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public String getInstructorId() {
        return instructorId;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}
