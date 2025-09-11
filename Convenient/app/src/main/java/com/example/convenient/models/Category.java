package com.example.convenient.models;

public class Category {
    public final int id;
    public final String title;
    public final int iconResId;
    public final int bgColorResId;

    public Category(int id, String title, int iconResId, int bgColorResId) {
        this.id = id;
        this.title = title;
        this.iconResId = iconResId;
        this.bgColorResId = bgColorResId;
    }
}
