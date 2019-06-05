package com.blackviking.campusrush.Messaging;

public class MessagesModel {

    private long lastMessageTimestamp;

    public MessagesModel() {
    }

    public MessagesModel(long lastMessageTimestamp) {
        this.lastMessageTimestamp = lastMessageTimestamp;
    }

    public long getLastMessageTimestamp() {
        return lastMessageTimestamp;
    }

    public void setLastMessageTimestamp(long lastMessageTimestamp) {
        this.lastMessageTimestamp = lastMessageTimestamp;
    }
}
