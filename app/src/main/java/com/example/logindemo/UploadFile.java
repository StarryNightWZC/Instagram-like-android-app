package com.example.logindemo;

import java.net.URL;

public class UploadFile {
    private String storageRef;
    public String timeStamp;
    public String caption;
    public String photoId = null;
    public String userId;

    public UploadFile(){
        // Empty constructor needed for firebase
    }

    public UploadFile(String ref, String stamp){
        storageRef = ref;
        timeStamp = stamp;
        caption = "No caption";
    }

    public UploadFile(String ref, String stamp, String cap){
        storageRef = ref;
        timeStamp = stamp;
        if(cap == ""){
            cap = "No caption";
        }else {
            caption = cap;
        }
    }

    public UploadFile(String ref, String stamp, String cap, String pid){
        storageRef = ref;
        timeStamp = stamp;
        if(cap == ""){
            cap = "No caption";
        }else {
            caption = cap;
        }
        photoId = pid;
    }

    public UploadFile(String ref, String stamp, String cap, String pid, String uid){
        storageRef = ref;
        timeStamp = stamp;
        if(cap == ""){
            cap = "No caption";
        }else {
            caption = cap;
        }
        photoId = pid;
        userId = uid;
    }

    public String getStorageRef(){
        return storageRef;
    }

    public String getTimeStamp(){
        return timeStamp;
    }

    public String getCaption() { return caption;}

    public String getPhotoId() {return photoId;}

    public String getUserId() {return userId;}

}
