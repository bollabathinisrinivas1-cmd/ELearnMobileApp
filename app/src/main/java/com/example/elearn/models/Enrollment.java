package com.example.elearn.models;

import org.json.JSONObject;

/**
 * Model representing a user's enrollment in a course from the eLibrary API.
 * Includes progress tracking and status/button label computation.
 */
public class Enrollment {
    private String id;
    private String userId;
    private String courseId;
    private String courseName;
    private String enrolledAt;
    private int progressPercent;
    private boolean completed;

    /**
     * Creates an Enrollment instance from a JSON object returned by the API.
     * Uses opt* methods to handle missing fields gracefully.
     *
     * @param json the JSON object representing an enrollment
     * @return a new Enrollment instance
     */
    public static Enrollment fromJson(JSONObject json) {
        Enrollment enrollment = new Enrollment();
        enrollment.id = json.optString("id", "");
        enrollment.userId = json.optString("userId", "");
        enrollment.courseId = json.optString("courseId", "");
        enrollment.courseName = json.optString("courseName", "Course");
        enrollment.enrolledAt = json.optString("enrolledAt", "");
        enrollment.progressPercent = json.optInt("progressPercent", 0);
        enrollment.completed = json.optBoolean("completed", false);
        return enrollment;
    }

    /**
     * Returns the display label for the enrollment status.
     *
     * @return "Completed" if completed or 100% progress, "In Progress" if progress > 0, else "Not Started"
     */
    public String getStatusLabel() {
        if (completed || progressPercent == 100) return "Completed";
        if (progressPercent > 0) return "In Progress";
        return "Not Started";
    }

    /**
     * Returns the action button label for this enrollment.
     *
     * @return null if completed, "Continue" if in progress, "Start" if not started
     */
    public String getButtonLabel() {
        if (completed || progressPercent == 100) return null;
        if (progressPercent > 0) return "Continue";
        return "Start";
    }

    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public String getCourseId() {
        return courseId;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public void setProgressPercent(int progressPercent) {
        this.progressPercent = progressPercent;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public String getEnrolledAt() {
        return enrolledAt;
    }

    public int getProgressPercent() {
        return progressPercent;
    }

    public boolean isCompleted() {
        return completed;
    }
}
