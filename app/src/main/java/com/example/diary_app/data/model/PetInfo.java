package com.example.diary_app.data.model;

import com.example.diary_app.core.PetConstants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PetInfo {
    private int level;
    private int currentExp;
    private int dailyExp;
    private String lastExpDate;
    private int streakDays;
    private List<String> unlockedItems;
    private Map<String, String> equippedItems;

    public PetInfo() {}

    // Constructor khởi tạo mặc định cho User mới
    public PetInfo(String todayDate) {
        this.level = 1;
        this.currentExp = 0;
        this.dailyExp = 0;
        this.lastExpDate = todayDate;
        this.streakDays = 0;

        this.unlockedItems = new ArrayList<>();
        this.unlockedItems.add(PetConstants.DEFAULT_BACKGROUND_ID);

        this.equippedItems = new HashMap<>();
        this.equippedItems.put(PetConstants.ITEM_TYPE_BACKGROUND, PetConstants.DEFAULT_BACKGROUND_ID);
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getCurrentExp() {
        return currentExp;
    }

    public void setCurrentExp(int currentExp) {
        this.currentExp = currentExp;
    }

    public int getDailyExp() {
        return dailyExp;
    }

    public void setDailyExp(int dailyExp) {
        this.dailyExp = dailyExp;
    }

    public String getLastExpDate() {
        return lastExpDate;
    }

    public void setLastExpDate(String lastExpDate) {
        this.lastExpDate = lastExpDate;
    }

    public int getStreakDays() {
        return streakDays;
    }

    public void setStreakDays(int streakDays) {
        this.streakDays = streakDays;
    }

    public List<String> getUnlockedItems() {
        return unlockedItems;
    }

    public void setUnlockedItems(List<String> unlockedItems) {
        this.unlockedItems = unlockedItems;
    }

    public Map<String, String> getEquippedItems() {
        return equippedItems;
    }

    public void setEquippedItems(Map<String, String> equippedItems) {
        this.equippedItems = equippedItems;
    }
}
