package com.example.upmessenger.Models;

public class UpMesssage {

    String userId,message;
    long time;
    Integer seen =1;

    @Override
    public String toString() {
        return "UpMesssage{" +
                "userId='" + userId + '\'' +
                ", message='" + message + '\'' +
                ", time=" + time +
                ", seen=" + seen +
                '}';
    }

    public UpMesssage(String userId, String message, long time,Integer seen) {
        this.userId = userId;
        this.message = message;
        this.time = time;
        this.seen = seen;
    }

    public UpMesssage(){};

    public UpMesssage(String userId) {
        this.userId = userId;
    }

    public UpMesssage(String userId, String message) {
        this.userId = userId;
        this.message = message;
    }

    public Integer getSeen() {
        return seen;
    }

    public void setSeen(Integer seen) {
        this.seen = seen;
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

}
