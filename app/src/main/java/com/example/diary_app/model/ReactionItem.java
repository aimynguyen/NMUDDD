package com.example.diary_app.model;

public class ReactionItem {

    private String uid;
    private String username;
    private String avatarUrl;
    private String reaction;

    public ReactionItem() {}

    public ReactionItem(String uid, String username,
                        String avatarUrl, String reaction) {
        this.uid = uid;
        this.username = username;
        this.avatarUrl = avatarUrl;
        this.reaction = reaction;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getReaction() {
        return reaction;
    }

    public void setReaction(String reaction) {
        this.reaction = reaction;
    }
}
