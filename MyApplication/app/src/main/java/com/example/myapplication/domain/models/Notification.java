package com.example.myapplication.domain.models;

import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;

public class Notification {
    private String notificationId;
    private String userId;
    private String type;
    private String message;
    private boolean isRead;
    private Date timestamp;
    private String sourceId; // ID saveza, korisnika, itd.

    public Notification() {
        // Obavezni prazan konstruktor za Firebase
    }

    public Notification(String userId, String type, String message, boolean isRead, String sourceId) {
        this.userId = userId;
        this.type = type;
        this.message = message;
        this.isRead = isRead;
        this.sourceId = sourceId;
    }

    // Geteri i seteri
    public String getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(String notificationId) {
        this.notificationId = notificationId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean getIsRead() {
        return isRead;
    }

    public void setIsRead(boolean read) {
        isRead = read;
    }

    @ServerTimestamp
    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }
}