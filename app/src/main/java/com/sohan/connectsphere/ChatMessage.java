package com.sohan.connectsphere;

public class ChatMessage {
    private String text;
    private String senderName;
    private long timestamp;
    private boolean fromAlumni;

    // Required empty constructor for Firestore
    public ChatMessage(String text, Long timestamp, boolean b) {
    }

    public ChatMessage(String text, String senderName, Long timestamp, boolean fromAlumni) {
        this.text = text;
        this.senderName = senderName;
        this.timestamp = this.timestamp;
        this.fromAlumni = fromAlumni;
    }

    public String getText() {
        return text;
    }

    public String getSenderName() {
        return senderName;
    }

    public boolean isFromAlumni() {
        return fromAlumni;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
