package com.example.diary_app.model;

import com.google.firebase.Timestamp;

public class FriendRequestModel {

    private String senderId;

    private String receiverId;

    private String status;

    private Timestamp createAt;

    // Empty constructor
    public FriendRequestModel() {
    }

    // Constructor
    public FriendRequestModel(String senderId,
                         String receiverId,
                         String status,
                         Timestamp createAt) {

        this.senderId = senderId;
        this.receiverId = receiverId;
        this.status = status;
        this.createAt = createAt;
    }


    // Getter + Setter

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Timestamp getCreateAt() {
        return createAt;
    }

    public void setCreateAt(Timestamp createAt) {
        this.createAt = createAt;
    }
}