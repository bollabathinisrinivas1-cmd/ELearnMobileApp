package com.example.elearn.models;

/**
 * Model representing a dashboard card displayed in the RecyclerView grid.
 * Each card shows a summary metric (count) with title, subtitle, icon, and color.
 */
public class DashboardCard {
    private String title;
    private String subtitle;
    private String icon;       // Material icon name for lookup
    private int colorResId;    // Color resource
    private int count;
    private String route;      // Target navigation identifier

    public DashboardCard(String title, String subtitle, String icon, int colorResId, int count, String route) {
        this.title = title;
        this.subtitle = subtitle;
        this.icon = icon;
        this.colorResId = colorResId;
        this.count = count;
        this.route = route;
    }

    public String getTitle() {
        return title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public String getIcon() {
        return icon;
    }

    public int getColorResId() {
        return colorResId;
    }

    public int getCount() {
        return count;
    }

    public String getRoute() {
        return route;
    }
}
