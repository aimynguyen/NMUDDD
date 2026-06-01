package com.example.diary_app.model;

import com.google.firebase.Timestamp;

import java.util.ArrayList;

public class UserModel {

    private String uid;

    private String userName;

    private String email;

    private String avatarUrl;

    private String birthday;

    private String theme;

    private ArrayList<String> friendIds;

    private Timestamp createAt;

    // Empty constructor
    public UserModel() {
    }

    // Full constructor
    public UserModel(String uid,
                String userName,
                String email,
                String avatarUrl,
                String birthday,
                String theme,
                ArrayList<String> friendIds,
                Timestamp createAt) {

        this.uid = uid;
        this.userName = userName;
        this.email = email;
        this.avatarUrl = avatarUrl;
        this.birthday = birthday;
        this.theme = theme;
        this.friendIds = friendIds;
        this.createAt = createAt;
    }

    // Getter Setter

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public ArrayList<String> getFriendIds() {
        return friendIds;
    }

    public void setFriendIds(ArrayList<String> friendIds) {
        this.friendIds = friendIds;
    }

    public Timestamp getCreateAt() {
        return createAt;
    }

    public void setCreateAt(Timestamp createAt) {
        this.createAt = createAt;
    }
}