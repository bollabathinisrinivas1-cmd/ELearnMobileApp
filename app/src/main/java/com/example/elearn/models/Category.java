package com.example.elearn.models;

import org.json.JSONObject;

/**
 * Model representing a course category from the eLibrary API.
 */
public class Category {
    private int id;
    private String name;

    /**
     * Creates a Category instance from a JSON object returned by the API.
     * Uses opt* methods to handle missing fields gracefully.
     *
     * @param json the JSON object representing a category
     * @return a new Category instance
     */
    public static Category fromJson(JSONObject json) {
        Category category = new Category();
        category.id = json.optInt("id", 0);
        category.name = json.optString("name", "");
        return category;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
