package com.sohan.connectsphere;

public class Message {
    private String text;
    private String userId;
    private String userName;
    private long timestamp;

    public Message() {
        // Default constructor required for Firebase
    }

    public Message(String text, String userId, String userName, long timestamp) {
        this.text = text;
        this.userId = userId;
        this.userName = userName;
        this.timestamp = timestamp;
    }

    public String getText() {
        return text;
    }

    public String getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    public long getTimestamp() {
        return timestamp;
    }
} 