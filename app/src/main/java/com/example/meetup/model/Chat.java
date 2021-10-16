package com.example.meetup.model;

public class Chat {
    String sms,userId;

    public Chat() {
    }

    public Chat(String sms, String userId) {
        this.sms = sms;
        this.userId = userId;
    }

    public String getSms() {
        return sms;
    }

    public void setSms(String sms) {
        this.sms = sms;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
