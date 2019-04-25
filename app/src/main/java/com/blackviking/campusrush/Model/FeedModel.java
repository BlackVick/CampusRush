package com.blackviking.campusrush.Model;

/**
 * Created by Scarecrow on 2/4/2019.
 */

public class FeedModel {

    private String sourceType;
    private String update;
    private String sender;
    private String realSender;
    private String imageUrl;
    private String imageThumbUrl;
    private String videoUrl;
    private String timestamp;
    private String updateType;

    public FeedModel() {
    }

    public FeedModel(String sourceType, String update, String sender, String realSender, String imageUrl, String imageThumbUrl, String videoUrl, String timestamp, String updateType) {
        this.sourceType = sourceType;
        this.update = update;
        this.sender = sender;
        this.realSender = realSender;
        this.imageUrl = imageUrl;
        this.imageThumbUrl = imageThumbUrl;
        this.videoUrl = videoUrl;
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

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
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
