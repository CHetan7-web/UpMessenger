package com.example.upmessenger.Models;

public class UpMesssage {

    String userId,message;
    long time;

    public UpMesssage(String userId, String message, long time) {
        this.userId = userId;
        this.message = message;
        this.time = time;
    }

    public UpMesssage(){};

    public UpMesssage(String userId, String message) {
        this.userId = userId;
        this.message = message;
    }

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

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    @Override
    public String toString() {
        return "UpMesssage{" +
                "message='" + message + '\'' +
                ", time='" + time + '\'' +
                '}';
    }
}
