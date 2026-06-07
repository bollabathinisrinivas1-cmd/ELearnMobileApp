package com.example.elearn.models;

/**
 * Model representing pie chart data for the admin dashboard.
 * Displays the distribution of paid vs free students.
 */
public class PieChartData {
    private int paidStudents;
    private int freeStudents;
    private int totalStudents;

    public PieChartData(int paidStudents, int freeStudents, int totalStudents) {
        this.paidStudents = paidStudents;
        this.freeStudents = freeStudents;
        this.totalStudents = totalStudents;
    }

    public int getPaidStudents() {
        return paidStudents;
    }

    public int getFreeStudents() {
        return freeStudents;
    }

    public int getTotalStudents() {
        return totalStudents;
    }

    /**
     * Returns the percentage of paid students out of total students.
     * Returns 0 if totalStudents is zero to avoid division by zero.
     *
     * @return paid student percentage (0-100)
     */
    public float getPaidPercent() {
        if (totalStudents == 0) {
            return 0f;
        }
        return ((float) paidStudents / totalStudents) * 100f;
    }
}
