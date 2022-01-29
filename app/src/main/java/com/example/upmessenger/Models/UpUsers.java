package com.example.upmessenger.Models;

import android.widget.ImageView;

import androidx.databinding.BindingAdapter;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.upmessenger.R;

public class UpUsers {
    String profilePic;
    String Name;
    String Email;
    String Password;
    String UserId;
    String lastMessage;
    String status;
    long selected=0;

    public long getIsSelected() {
        return selected;
    }

    public void setIsSelected(long isSelected) {
        this.selected = isSelected;
    }

    private long lastTime;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

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
                ", isSelected='" + selected + '\'' +
                '}';
    }

    public long getTime() { return lastTime; }

    public long getLastTime() {
        return lastTime;
    }

    public void setLastTime(long lastTime) {
        this.lastTime = lastTime;
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

    public long getSelected() {
        return selected;
    }

    public void setSelected(Integer selected) {
        selected = selected;
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
        selected=0;
        this.lastMessage = lastMessage;
    }

    public UpUsers(){}

    public UpUsers( String email, String password,String name) {
        Name = name;
        Email = email;
        Password = password;
        selected=0;
    }

    @BindingAdapter("android:loadProfileImage")
    public static void loadImage(ImageView imageView,String imageUrl){
        Glide.with(imageView.getContext())
             .load(imageUrl)
             .apply(RequestOptions.placeholderOf(R.drawable.ic_launcher_foreground))
             .into(imageView);

    }

}
