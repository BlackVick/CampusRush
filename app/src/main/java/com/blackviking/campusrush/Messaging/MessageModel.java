package com.blackviking.campusrush.Messaging;

public class MessageModel {

    private String message;
    private String imageUrl;
    private String imageThumbUrl;
    private String messageRead;
    private String messageFrom;
    private long messageTimestamp;
    private String isQuote;
    private String quotedId;

    public MessageModel() {
    }

    public MessageModel(String message, String imageUrl, String imageThumbUrl, String messageRead, String messageFrom, long messageTimestamp, String isQuote, String quotedId) {
        this.message = message;
        this.imageUrl = imageUrl;
        this.imageThumbUrl = imageThumbUrl;
        this.messageRead = messageRead;
        this.messageFrom = messageFrom;
        this.messageTimestamp = messageTimestamp;
        this.isQuote = isQuote;
        this.quotedId = quotedId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
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

    public String getMessageRead() {
        return messageRead;
    }

    public void setMessageRead(String messageRead) {
        this.messageRead = messageRead;
    }

    public String getMessageFrom() {
        return messageFrom;
    }

    public void setMessageFrom(String messageFrom) {
        this.messageFrom = messageFrom;
    }

    public long getMessageTimestamp() {
        return messageTimestamp;
    }

    public void setMessageTimestamp(long messageTimestamp) {
        this.messageTimestamp = messageTimestamp;
    }

    public String getIsQuote() {
        return isQuote;
    }

    public void setIsQuote(String isQuote) {
        this.isQuote = isQuote;
    }

    public String getQuotedId() {
        return quotedId;
    }

    public void setQuotedId(String quotedId) {
        this.quotedId = quotedId;
    }
}
