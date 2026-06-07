package com.example.elearn.models;

/**
 * Model representing aggregated dashboard data for Student users.
 * Contains counts for courses, categories, and enrollments.
 */
public class StudentDashboardData {
    private int courseCount;
    private int categoryCount;
    private int enrollmentCount;

    public StudentDashboardData(int courseCount, int categoryCount, int enrollmentCount) {
        this.courseCount = courseCount;
        this.categoryCount = categoryCount;
        this.enrollmentCount = enrollmentCount;
    }

    public int getCourseCount() {
        return courseCount;
    }

    public int getCategoryCount() {
        return categoryCount;
    }

    public int getEnrollmentCount() {
        return enrollmentCount;
    }
}
