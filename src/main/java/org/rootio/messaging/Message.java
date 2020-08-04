package org.rootio.messaging;

import java.util.HashMap;

public class Message {
    private String event, category;
    private HashMap<String, Object> payLoad;

    public Message(String event, String category, HashMap<String, Object> payLoad) {
        this.event = event;
        this.category = category;
        this.payLoad = payLoad;
    }

    public HashMap<String, Object> getPayLoad() {
        return payLoad;
    }

    public String getEvent() {
        return event;
    }

    public String getCategory() {
        return category;
    }

    public Object getOriginatingAddress() {
        return null;
    }

    public Object getMessageBody() {
        return null;
    }
}
