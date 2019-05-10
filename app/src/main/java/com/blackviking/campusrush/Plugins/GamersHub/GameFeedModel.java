package com.blackviking.campusrush.Plugins.GamersHub;

public class GameFeedModel {

    private String title;
    private String imageLink;
    private String imageLinkThumb;
    private String description;
    private long timeStamp;

    public GameFeedModel() {
    }

    public GameFeedModel(String title, String imageLink, String imageLinkThumb, String description, long timeStamp) {
        this.title = title;
        this.imageLink = imageLink;
        this.imageLinkThumb = imageLinkThumb;
        this.description = description;
        this.timeStamp = timeStamp;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImageLink() {
        return imageLink;
    }

    public void setImageLink(String imageLink) {
        this.imageLink = imageLink;
    }

    public String getImageLinkThumb() {
        return imageLinkThumb;
    }

    public void setImageLinkThumb(String imageLinkThumb) {
        this.imageLinkThumb = imageLinkThumb;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }
}
