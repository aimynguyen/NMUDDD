package com.example.diary_app.data.model;

import com.example.diary_app.core.NotiType;
import com.google.firebase.Timestamp;

public class Notification {
    private String notificationId;
    private String receiverId;
    private String senderId;
    private NotiType type; // Ví dụ: "DELETE_POST", "REACT_POST"
    private String targetId;
    private String message;
    private boolean isRead;
    private Timestamp createdAt;

    public Notification() {}

    public Notification(String notificationId, String receiverId, String senderId, NotiType type,
                        String targetId, String message, boolean isRead, Timestamp createdAt) {
        this.notificationId = notificationId;
        this.receiverId = receiverId;
        this.senderId = senderId;
        this.type = type;
        this.targetId = targetId;
        this.message = message;
        this.isRead = isRead;
        this.createdAt = createdAt;
    }

    public Notification(String receiverId, String senderId, NotiType type,
                        String targetId, String message) {
        this.receiverId = receiverId;
        this.senderId = senderId;
        this.type = type;
        this.targetId = targetId;
        this.message = message;
        this.isRead = false; // Mặc định là chưa đọc
        this.createdAt = Timestamp.now();
    }

    public String getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(String notificationId) {
        this.notificationId = notificationId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getTargetId() {
        return targetId;
    }

    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }

    public NotiType getType() {
        return type;
    }

    public void setType(NotiType type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}
