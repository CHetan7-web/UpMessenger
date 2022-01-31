package com.example.upmessenger.Models;

public class UpLastMessage {
    String lastMessage;
    Long lastTime ;
    Integer state=0;
    Integer typing = 0;

    public String getLastMessage() {
        return lastMessage;
    }

    public Integer getTyping() {
        return typing;
    }

    public void setTyping(Integer typing) {
        this.typing = typing;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public Long getLastTime() {
        return lastTime;
    }

    public void setLastTime(Long lastTime) {
        this.lastTime = lastTime;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public UpLastMessage() {}

    public UpLastMessage(String lastMessage, Long time, Integer senderState) {
        this.lastMessage = lastMessage;
        this.lastTime = time;
        this.state = senderState;
    }

    public UpLastMessage(String lastMessage, Long time) {
        this.lastMessage = lastMessage;
        this.lastTime = time;
    }

    @Override
    public String toString() {
        return "UpLastMessage{" +
                "lastMessage='" + lastMessage + '\'' +
                ", lastTime=" + lastTime +
                ", state=" + state +
                ", typing=" + typing +
                '}';
    }

}
