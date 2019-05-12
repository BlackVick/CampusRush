package com.blackviking.campusrush.Plugins.SkitCenter;

public class SkitModel {

    private String title;
    private String owner;
    private String mediaUrl;
    private String thumbnail;
    private String description;
    private String status;

    public SkitModel() {
    }

    public SkitModel(String title, String owner, String mediaUrl, String thumbnail, String description, String status) {
        this.title = title;
        this.owner = owner;
        this.mediaUrl = mediaUrl;
        this.thumbnail = thumbnail;
        this.description = description;
        this.status = status;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getMediaUrl() {
        return mediaUrl;
    }

    public void setMediaUrl(String mediaUrl) {
        this.mediaUrl = mediaUrl;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
