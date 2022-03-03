package com.example.upmessenger.Models;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class UpMesssage {

    //plain text
    public static Integer MESSAGE_WITH_TEXT = 1;

    //Only Multi-media
    public static Integer MESSAGE_WITH_IMAGE = 2;
    public static Integer MESSAGE_WITH_VIDEO = 3;
    public static Integer MESSAGE_WITH_GIF = 4;
    public static Integer MESSAGE_WITH_STICKER = 5;
    public static Integer MESSAGE_WITH_DOCUMENT = 6;

    //Message with Multimedia
    public static Integer MESSAGE_WITH_MESSAGE_AND_IMAGE = 7;
    public static Integer MESSAGE_WITH_MESSAGE_AND_VIDEO = 8;
    public static Integer MESSAGE_WITH_MESSAGE_AND_GIF = 9;
    public static Integer MESSAGE_WITH_MESSAGE_AND_STICKER = 10;
    public static Integer MESSAGE_WITH_MESSAGE_AND_DOCUMENT = 11;

    //Media State
    public static Integer MEDIA_NOT_YET_STARTED = 0;
    public static Integer MEDIA_ON_PROGRESS = 1;
    public static Integer MEDIA_DELIEVERD = 2;

    //download state
    public static Integer MEDIA_DOWNLOADED = 1;
    public static Integer MEDIA_NOT_DOWNLOADED = 0;

    String userId, message="";
    long time;
    Integer seen = 1;
    Integer msgType = 1,mediaState = 0,isDownloaded = 0;
    float mediaProgress = 0f;
    long seenTime, delieverdTime;
    String mediaURL, thumbPath, mediaPath;
    String thumbUri , MediaUri;
    Map<String, Object> mapVariables;

    public UpMesssage(String userId, long time, int i, Integer messageType, String thumbName, String mediaName) {
        this.userId=userId;
        this.time=time;
        this.seen = 1;
        this.msgType = messageType;
        this.thumbPath = thumbName;
        this.mediaPath = mediaName;
    }

    @Override
    public String toString() {
        return "UpMesssage{" +
                "userId='" + userId + '\'' +
                ", message='" + message + '\'' +
                ", time=" + time +
                ", seen=" + seen +
                ", msgType=" + msgType +
                ", mediaState=" + mediaState +
                ", mediaProgress=" + mediaProgress +
                ", seenTime=" + seenTime +
                ", delieverdTime=" + delieverdTime +
                ", mediaURL='" + mediaURL + '\'' +
                ", thumbPath='" + thumbPath + '\'' +
                ", mediaPath='" + mediaPath + '\'' +
                '}';
    }

    public UpMesssage(String userId, String message, long time, Integer seen) {
        this.userId = userId;
        this.message = message;
        this.time = time;
        this.seen = seen;
    }

    public UpMesssage() {
    }

    ;

    public UpMesssage(String userId) {
        this.userId = userId;
    }

    public UpMesssage(String userId, String message) {
        this.userId = userId;
        this.message = message;
    }

    public float getMediaProgress() {
        return mediaProgress;
    }

    public void setMediaProgress(float mediaProgress) {
        this.mediaProgress = mediaProgress;
    }

    public String getThumbUri() {
        return thumbUri;
    }

    public void setThumbUri(String thumbUri) {
        this.thumbUri = thumbUri;
    }

    public String getMediaUri() {
        return MediaUri;
    }

    public void setMediaUri(String mediaUri) {
        MediaUri = mediaUri;
    }

    public Integer getIsDownloaded() {
        return isDownloaded;
    }

    public void setIsDownloaded(Integer isDownloaded) {
        this.isDownloaded = isDownloaded;
    }

    public Integer getSeen() {
        return seen;
    }

    public void setSeen(Integer seen) {
        this.seen = seen;
    }

    public Integer getMediaState() {
        return mediaState;
    }

    public void setMediaState(Integer mediaState) {
        this.mediaState = mediaState;
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

    public Integer getMsgType() {
        return msgType;
    }

    public void setMsgType(Integer msgType) {
        this.msgType = msgType;
    }

    public long getSeenTime() {
        return seenTime;
    }

    public void setSeenTime(long seenTime) {
        this.seenTime = seenTime;
    }

    public long getDelieverdTime() {
        return delieverdTime;
    }

    public void setDelieverdTime(long delieverdTime) {
        this.delieverdTime = delieverdTime;
    }

    public String getMediaURL() {
        return mediaURL;
    }

    public void setMediaURL(String mediaURL) {
        this.mediaURL = mediaURL;
    }

    public String getThumbPath() {
        return thumbPath;
    }

    public void setThumbPath(String thumbPath) {
        this.thumbPath = thumbPath;
    }

    public String getMediaPath() {
        return mediaPath;
    }

    public void setMediaPath(String mediaPath) {
        this.mediaPath = mediaPath;
    }

    public Map<String, Object> toMap(){

        mapVariables = new HashMap<>();

        for (Field field : UpMesssage.class.getDeclaredFields()) {
            field.setAccessible(true);

            try {
                mapVariables.put(field.getName(),  field.get(this));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }

        }
        return mapVariables;
    }

}
