package com.example.upmessenger.Models;

public class UpMesssage {

    String userId,message,time;

    public UpMesssage(String userId, String message, String time) {
        this.userId = userId;
        this.message = message;
        this.time = time;
    }

    public UpMesssage(String userId, String message) {
        this.userId = userId;
        this.message = message;
    }

    public UpMesssage(){}

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }


}
