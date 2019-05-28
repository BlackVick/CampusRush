package com.blackviking.campusrush.Model;

public class NotificationModel {

    private String title;
    private String details;
    private String comment;
    private String type;
    private String status;
    private String intentPrimaryKey;
    private String intentSecondaryKey;
    private String user;
    private long timestamp;

    public NotificationModel() {
    }

    public NotificationModel(String title, String details, String comment, String type, String status, String intentPrimaryKey, String intentSecondaryKey, String user, long timestamp) {
        this.title = title;
        this.details = details;
        this.comment = comment;
        this.type = type;
        this.status = status;
        this.intentPrimaryKey = intentPrimaryKey;
        this.intentSecondaryKey = intentSecondaryKey;
        this.user = user;
        this.timestamp = timestamp;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getIntentPrimaryKey() {
        return intentPrimaryKey;
    }

    public void setIntentPrimaryKey(String intentPrimaryKey) {
        this.intentPrimaryKey = intentPrimaryKey;
    }

    public String getIntentSecondaryKey() {
        return intentSecondaryKey;
    }

    public void setIntentSecondaryKey(String intentSecondaryKey) {
        this.intentSecondaryKey = intentSecondaryKey;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
