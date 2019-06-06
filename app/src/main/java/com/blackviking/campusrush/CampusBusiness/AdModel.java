package com.blackviking.campusrush.CampusBusiness;

public class AdModel {

    private String sourceType;
    private String update;
    private String business;
    private String contact;
    private String sender;
    private String imageUrl;
    private String imageThumbUrl;
    private String timestamp;
    private String updateType;

    public AdModel() {
    }

    public AdModel(String sourceType, String update, String business, String contact, String sender, String imageUrl, String imageThumbUrl, String timestamp, String updateType) {
        this.sourceType = sourceType;
        this.update = update;
        this.business = business;
        this.contact = contact;
        this.sender = sender;
        this.imageUrl = imageUrl;
        this.imageThumbUrl = imageThumbUrl;
        this.timestamp = timestamp;
        this.updateType = updateType;
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

    public String getBusiness() {
        return business;
    }

    public void setBusiness(String business) {
        this.business = business;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
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
}
