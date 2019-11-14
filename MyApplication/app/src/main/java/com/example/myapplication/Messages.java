package com.example.myapplication;

public class Messages {
    private String from, type, messages;

    public Messages(){}

    public Messages(String from, String type, String messages) {
        this.from = from;
        this.type = type;
        this.messages = messages;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMessage() {
        return messages;
    }

    public void setMessage(String messages) {
        this.messages = messages;
    }
}
