package com.example.myapplication.domain.models;

import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;

public class Message {

    private String messageId;
    private String senderId;
    private String senderUsername;
    private String content;
    private String allianceId;
    private Date timestamp;

    public Message() {
        // Obavezni prazan konstruktor za Firestore
    }

    public Message(String senderId, String senderUsername, String content, String allianceId) {
        this.senderId = senderId;
        this.senderUsername = senderUsername;
        this.content = content;
        this.allianceId = allianceId;
    }

    // Geteri i setteri
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

    public String getSenderUsername() {
        return senderUsername;
    }

    public void setSenderUsername(String senderUsername) {
        this.senderUsername = senderUsername;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getAllianceId() {
        return allianceId;
    }

    public void setAllianceId(String allianceId) {
        this.allianceId = allianceId;
    }

    @ServerTimestamp
    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}