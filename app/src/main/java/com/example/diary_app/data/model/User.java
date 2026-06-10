package com.example.diary_app.data.model;

import com.google.firebase.Timestamp;

import java.util.List;

public class User {
    private String uid;
    private String userName;
    private String email;
    private String birthday;
    private String avatarUrl;
    private String theme;
    private List<String> friendIds;
    private Timestamp createAt;
    private String role;
    private PetInfo petInfo;

    public User(){}

    public User(String uid, String displayName, String email,String birthday, String avatarUrl, String theme, List<String> friendIds, Timestamp createAt, String role){
        this.uid = uid;
        this.userName = displayName;
        this.email = email;
        this.birthday = birthday;
        this.avatarUrl = avatarUrl;
        this.theme = theme;
        this.friendIds = friendIds;
        this.createAt = createAt;
        this.role = role;
    }

    public String getUid() {return uid;}
    public void setUid(String uid) {this.uid = uid;}

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getBirthday() {
        return birthday;
    }
    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public String getTheme() { return theme; }
    public void setTheme(String theme) { this.theme = theme; }

    public List<String> getFriendIds() { return friendIds; }
    public void setFriendIds(List<String> friendIds) { this.friendIds = friendIds; }

    public Timestamp getCreatedAt() { return createAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createAt = createdAt; }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public PetInfo getPetInfo() { return petInfo; }
    public void setPetInfo(PetInfo petInfo) { this.petInfo = petInfo; }
}
