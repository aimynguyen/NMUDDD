package com.example.diary_app.model;

import com.google.firebase.Timestamp;

public class NotificationModel {

    private String notificationId;
    private String senderId;
    private String receiverId;
    private String targetId;
    private String message;
    private String type;
    private boolean isRead;
    private Timestamp createdAt;

    public NotificationModel() {
    }

    public NotificationModel(String notificationId,
                        String senderId,
                        String receiverId,
                        String targetId,
                        String message,
                        String type,
                        boolean isRead,
                        Timestamp createdAt) {

        this.notificationId = notificationId;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.targetId = targetId;
        this.message = message;
        this.type = type;
        this.isRead = isRead;
        this.createdAt = createdAt;
    }

    public String getNotificationId() {
        return notificationId;
    }

    public String getSenderId() {
        return senderId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public String getTargetId() {
        return targetId;
    }

    public String getMessage() {
        return message;
    }

    public String getType() {
        return type;
    }

    public boolean isRead() {
        return isRead;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setNotificationId(String notificationId) {
        this.notificationId = notificationId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}