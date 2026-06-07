package com.example.elearn.models;

/**
 * Model representing aggregated dashboard data for Admin users.
 * Contains counts for all entity types and pie chart data for student distribution.
 */
public class AdminDashboardData {
    private int courseCount;
    private int categoryCount;
    private int studentCount;
    private int staffCount;
    private int enrollmentCount;
    private PieChartData pieChartData;

    public AdminDashboardData(int courseCount, int categoryCount, int studentCount,
                              int staffCount, int enrollmentCount, PieChartData pieChartData) {
        this.courseCount = courseCount;
        this.categoryCount = categoryCount;
        this.studentCount = studentCount;
        this.staffCount = staffCount;
        this.enrollmentCount = enrollmentCount;
        this.pieChartData = pieChartData;
    }

    public int getCourseCount() {
        return courseCount;
    }

    public int getCategoryCount() {
        return categoryCount;
    }

    public int getStudentCount() {
        return studentCount;
    }

    public int getStaffCount() {
        return staffCount;
    }

    public int getEnrollmentCount() {
        return enrollmentCount;
    }

    public PieChartData getPieChartData() {
        return pieChartData;
    }
}
