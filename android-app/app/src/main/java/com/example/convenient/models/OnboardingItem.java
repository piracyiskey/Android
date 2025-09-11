package com.example.convenient.models;

public class OnboardingItem {
    private final String title;
    private final String description;
    private final int imageResId;

    public OnboardingItem(String title, String description, int imageResId) {
        this.title = title;
        this.description = description;
        this.imageResId = imageResId;
    }

    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public int getImageResId() { return imageResId; }
}

