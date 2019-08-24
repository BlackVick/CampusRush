package com.blackviking.campusrush.Messaging;

public class MessageQuoteModel {

    private String message;
    private String imageThumbUrl;
    private String messageFrom;

    public MessageQuoteModel() {
    }

    public MessageQuoteModel(String message, String imageThumbUrl, String messageFrom) {
        this.message = message;
        this.imageThumbUrl = imageThumbUrl;
        this.messageFrom = messageFrom;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getImageThumbUrl() {
        return imageThumbUrl;
    }

    public void setImageThumbUrl(String imageThumbUrl) {
        this.imageThumbUrl = imageThumbUrl;
    }

    public String getMessageFrom() {
        return messageFrom;
    }

    public void setMessageFrom(String messageFrom) {
        this.messageFrom = messageFrom;
    }
}
