package com.example.upmessenger.Models;

public class UpUsers {
    String profilePic,Name,Email,Password,UserId,lastMessage;

    private String lastTime;

    @Override
    public String toString() {
        return "UpUsers{" +
                "profilePic='" + profilePic + '\'' +
                ", Name='" + Name + '\'' +
                ", Email='" + Email + '\'' +
                ", Password='" + Password + '\'' +
                ", UserId='" + UserId + '\'' +
                ", lastMessage='" + lastMessage + '\'' +
                ", lastTime='" + lastTime + '\'' +
                '}';
    }

    public String getProfilePic() {
        return profilePic;
    }

    public void setProfilePic(String profilePic) {
        this.profilePic = profilePic;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public void setLastTime(String lastTime) {
        this.lastTime = lastTime;
    }

    public String getEmail() {
        return Email;
    }

    public void setEmail(String email) {
        Email = email;
    }

    public String getPassword() {
        return Password;
    }

    public void setPassword(String password) {
        Password = password;
    }

    public String getUserId() {
        return UserId;
    }

    public void setUserId(String userId) {
        UserId = userId;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public UpUsers(String profilePic, String name, String email, String password, String userId, String lastMessage) {
        this.profilePic = profilePic;
        Name = name;
        Email = email;
        Password = password;
        UserId = userId;
        this.lastMessage = lastMessage;
    }

    public UpUsers(){}

    public UpUsers( String email, String password,String name) {
        Name = name;
        Email = email;
        Password = password;
    }

    public String getTime() {
        return lastTime;
    }
}
