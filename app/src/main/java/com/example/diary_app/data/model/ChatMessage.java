package com.example.diary_app.data.model;

import com.google.firebase.Timestamp;
public class ChatMessage {
    private String messageId;
    private String senderId;
    private String content;
    private Timestamp createdAt;

    public ChatMessage() {}

    public ChatMessage(String messageId, String senderId, String content, Timestamp createdAt){
        this.messageId = messageId;
        this.senderId = senderId;
        this.content = content;
        this.createdAt = createdAt;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
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
