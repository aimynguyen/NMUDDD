package com.example.diary_app.DTOs;

public class InventoryItem {
    private String id;
    private int imageResId;
    private String name;
    private boolean isUnlocked;

    public InventoryItem(String id, int imageResId, String name, boolean isUnlocked) {
        this.id = id;
        this.imageResId = imageResId;
        this.name = name;
        this.isUnlocked = isUnlocked;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getImageResId() {
        return imageResId;
    }

    public void setImageResId(int imageResId) {
        this.imageResId = imageResId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isUnlocked() {
        return isUnlocked;
    }

    public void setUnlocked(boolean unlocked) {
        isUnlocked = unlocked;
    }
}
