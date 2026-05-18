package com.example.diary_app.DTOs;

import android.net.Uri;

public class DiaryCreateRequest {
    private String title;
    private String content;
    private Uri imageUri;       // Đường dẫn ảnh từ thư viện/camera
    private String moodName;    // Tên cảm xúc (vd: "HAPPY")
    private double latitude;
    private double longitude;

    public DiaryCreateRequest(String title, String content, Uri imageUri, String moodName, double lat, double lng) {
        this.title = title;
        this.content = content;
        this.imageUri = imageUri;
        this.moodName = moodName;
        this.latitude = lat;
        this.longitude = lng;
    }

    // Getters
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public Uri getImageUri() { return imageUri; }
    public String getMoodName() { return moodName; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
}
