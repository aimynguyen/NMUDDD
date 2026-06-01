package com.example.diary_app.data.model;

import com.google.firebase.Timestamp;

public class AiMessage {
    private String messageId;
    private String role; // Chỉ nhận 2 giá trị: "user" hoặc "model"
    private String content;
    private Timestamp createdAt;

    public AiMessage() {}

    public AiMessage(String messageId, String role, String content, Timestamp createdAt) {
        this.messageId = messageId;
        this.role = role;
        this.content = content;
        this.createdAt = createdAt;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}
