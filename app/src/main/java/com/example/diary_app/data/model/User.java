package com.example.diary_app.data.model;

import com.google.firebase.Timestamp;

import java.util.List;

public class User {
    private String uid;
    private String userName;
    private String email;
    private String avatarUrl;
    private String theme;
    private List<String> friendIds;
    private Timestamp createAt;

    public User(){}

    public User(String uid, String displayName, String email, String avatarUrl, String theme, List<String> friendIds, Timestamp createAt){
        this.uid = uid;
        this.userName = displayName;
        this.email = email;
        this.avatarUrl = avatarUrl;
        this.theme = theme;
        this.friendIds = friendIds;
        this.createAt = createAt;
    }

    public String getUid() {return uid;}
    public void setUid(String uid) {this.uid = uid;}

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public String getTheme() { return theme; }
    public void setTheme(String theme) { this.theme = theme; }

    public List<String> getFriendIds() { return friendIds; }
    public void setFriendIds(List<String> friendIds) { this.friendIds = friendIds; }

    public Timestamp getCreatedAt() { return createAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createAt = createdAt; }
}
