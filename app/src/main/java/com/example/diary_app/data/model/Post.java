package com.example.diary_app.data.model;

import com.google.firebase.Timestamp;

import java.util.List;
import java.util.Map;

public class Post {
    private String postId;
    private String userId;
    private String userName;
    private String userAvatar;
    private String imageUrl;
    private String caption;
    private List<String> tag;
    private String emotion;
    private String privacy;
    private Timestamp createAt;
    private Location location;
    private Map<String, String> reactions;

    public Post() {}

    public Post(String postId, String userId, String userName, String userAvatar, String imageUrl, String caption, List<String> tags, String emotion, String privacy, Timestamp createdAt, Location location, Map<String, String> reactions) {
        this.postId = postId;
        this.userId = userId;
        this.userName = userName;
        this.userAvatar = userAvatar;
        this.imageUrl = imageUrl;
        this.caption = caption;
        this.tag = tags;
        this.emotion = emotion;
        this.privacy = privacy;
        this.createAt = createdAt;
        this.location = location;
        this.reactions = reactions;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserAvatar() {
        return userAvatar;
    }

    public void setUserAvatar(String userAvatar) {
        this.userAvatar = userAvatar;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public List<String> getTags() {
        return tag;
    }

    public void setTags(List<String> tags) {
        this.tag = tags;
    }

    public String getEmotion() {
        return emotion;
    }

    public void setEmotion(String emotion) {
        this.emotion = emotion;
    }

    public String getPrivacy() {
        return privacy;
    }

    public void setPrivacy(String privacy) {
        this.privacy = privacy;
    }

    public Timestamp getCreateAt() {
        return createAt;
    }

    public void setCreateAt(Timestamp createAt) {
        this.createAt = createAt;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public Map<String, String> getReactions() {
        return reactions;
    }

    public void setReactions(Map<String, String> reactions) {
        this.reactions = reactions;
    }
}


