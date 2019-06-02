package com.blackviking.campusrush.Model;

public class AdminManageModel {

    private String title;
    private String owner;
    private String mediaUrl;
    private String thumbnail;
    private String description;

    private String sourceType;
    private String update;
    private String sender;
    private String realSender;
    private String imageUrl;
    private String imageThumbUrl;
    private String timestamp;
    private String updateType;

    private String adminType;

    public AdminManageModel() {
    }

    public AdminManageModel(String title, String owner, String mediaUrl, String thumbnail, String description, String sourceType, String update, String sender, String realSender, String imageUrl, String imageThumbUrl, String timestamp, String updateType, String adminType) {
        this.title = title;
        this.owner = owner;
        this.mediaUrl = mediaUrl;
        this.thumbnail = thumbnail;
        this.description = description;
        this.sourceType = sourceType;
        this.update = update;
        this.sender = sender;
        this.realSender = realSender;
        this.imageUrl = imageUrl;
        this.imageThumbUrl = imageThumbUrl;
        this.timestamp = timestamp;
        this.updateType = updateType;
        this.adminType = adminType;
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

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public String getUpdate() {
        return update;
    }

    public void setUpdate(String update) {
        this.update = update;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getRealSender() {
        return realSender;
    }

    public void setRealSender(String realSender) {
        this.realSender = realSender;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getImageThumbUrl() {
        return imageThumbUrl;
    }

    public void setImageThumbUrl(String imageThumbUrl) {
        this.imageThumbUrl = imageThumbUrl;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getUpdateType() {
        return updateType;
    }

    public void setUpdateType(String updateType) {
        this.updateType = updateType;
    }

    public String getAdminType() {
        return adminType;
    }

    public void setAdminType(String adminType) {
        this.adminType = adminType;
    }
}
