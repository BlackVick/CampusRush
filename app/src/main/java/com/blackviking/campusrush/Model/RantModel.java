package com.blackviking.campusrush.Model;

public class RantModel {

    private String rantUsername;
    private String rantUserId;
    private String rantPrivacyState;
    private String rant;
    private long timestamp;
    private String rantImage;
    private String rantImageThumb;

    public RantModel() {
    }

    public RantModel(String rantUsername, String rantUserId, String rantPrivacyState, String rant, long timestamp, String rantImage, String rantImageThumb) {
        this.rantUsername = rantUsername;
        this.rantUserId = rantUserId;
        this.rantPrivacyState = rantPrivacyState;
        this.rant = rant;
        this.timestamp = timestamp;
        this.rantImage = rantImage;
        this.rantImageThumb = rantImageThumb;
    }

    public String getRantUsername() {
        return rantUsername;
    }

    public void setRantUsername(String rantUsername) {
        this.rantUsername = rantUsername;
    }

    public String getRantUserId() {
        return rantUserId;
    }

    public void setRantUserId(String rantUserId) {
        this.rantUserId = rantUserId;
    }

    public String getRantPrivacyState() {
        return rantPrivacyState;
    }

    public void setRantPrivacyState(String rantPrivacyState) {
        this.rantPrivacyState = rantPrivacyState;
    }

    public String getRant() {
        return rant;
    }

    public void setRant(String rant) {
        this.rant = rant;
    }

    public long getTimeStamp() {
        return timestamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timestamp = timeStamp;
    }

    public String getRantImage() {
        return rantImage;
    }

    public void setRantImage(String rantImage) {
        this.rantImage = rantImage;
    }

    public String getRantImageThumb() {
        return rantImageThumb;
    }

    public void setRantImageThumb(String rantImageThumb) {
        this.rantImageThumb = rantImageThumb;
    }
}
