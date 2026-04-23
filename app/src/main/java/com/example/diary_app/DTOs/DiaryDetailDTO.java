package com.example.diary_app.DTOs;

import android.graphics.Bitmap;

public class DiaryDetailDTO {
    private String title;
    private String content;
    private String fullDate;
    private Bitmap fullImage;
    private String address;
    private String moodEmoji;

    public DiaryDetailDTO(String title, String content, String fullDate, Bitmap fullImage, String address, String moodEmoji) {
        this.title = title;
        this.content = content;
        this.fullDate = fullDate;
        this.fullImage = fullImage;
        this.address = address;
        this.moodEmoji = moodEmoji;
    }

    // Getters
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public String getFullDate() { return fullDate; }
    public Bitmap getFullImage() { return fullImage; }
    public String getAddress() { return address; }
    public String getMoodEmoji() { return moodEmoji; }
}
