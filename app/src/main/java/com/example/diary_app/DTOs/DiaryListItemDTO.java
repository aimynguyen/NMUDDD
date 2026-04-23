package com.example.diary_app.DTOs;

import android.graphics.Bitmap;

public class DiaryListItemDTO {
    private int id;
    private String title;
    private String date;        // Ngày đã được format đẹp từ DateHelper
    private Bitmap thumbnail;   // Ảnh đã được resize nhỏ từ ImageHelper
    private String moodEmoji;   // Emoji tương ứng với cảm xúc

    // Constructor
    public DiaryListItemDTO(int id, String title, String date, Bitmap thumbnail, String moodEmoji) {
        this.id = id;
        this.title = title;
        this.date = date;
        this.thumbnail = thumbnail;
        this.moodEmoji = moodEmoji;
    }

    // Getters
    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getDate() { return date; }
    public Bitmap getThumbnail() { return thumbnail; }
    public String getMoodEmoji() { return moodEmoji; }
}
