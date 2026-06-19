package com.example.diary_app.data.model;

import com.google.firebase.Timestamp;
import java.util.List;

public class ChatRoom {
    private String chatId;
    private List<String> participants;
    private String lastMessage;
    private Timestamp lastUpdated;

    public ChatRoom() {}

    public ChatRoom(String chatId, List<String> participants, String lastMessage, Timestamp lastUpdated){
        this.chatId = chatId;
        this.participants = participants;
        this.lastMessage = lastMessage;
        this.lastUpdated = lastUpdated;
    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public List<String> getParticipants() {
        return participants;
    }

    public void setParticipants(List<String> participants) {
        this.participants = participants;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public Timestamp getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Timestamp lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    private String roomName;
    public String getRoomName() { return roomName; }
    public void setRoomName(String roomName) { this.roomName = roomName; }
}
