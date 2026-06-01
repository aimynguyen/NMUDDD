package com.example.diary_app.DTOs;

import android.graphics.Bitmap;

public class DiaryListItemDTO {
    private int id;
    private String title;
    private String date;        // Ngày đã được format đẹp từ DateHelper
    private Bitmap thumbnail;   // Ảnh đã được resize nhỏ từ ImageHelper
    private String moodEmoji;   // Emoji tương ứng với cảm xúc
    private double latitude;
    private double longitude;
    // Constructor
    public DiaryListItemDTO(int id, String title, String date, Bitmap thumbnail, String moodEmoji, double latitude, double longitude) {
        this.id = id;
        this.title = title;
        this.date = date;
        this.thumbnail = thumbnail;
        this.moodEmoji = moodEmoji;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    // Getters
    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getDate() { return date; }
    public Bitmap getThumbnail() { return thumbnail; }
    public String getMoodEmoji() { return moodEmoji; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
}
