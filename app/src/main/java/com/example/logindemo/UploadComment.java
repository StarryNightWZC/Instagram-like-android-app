package com.example.logindemo;

public class UploadComment {
    public String profilePicRef;
    public String timeStamp;
    public String comment;
    public String username;

    public UploadComment(){
        // Empty constructor needed for firebase
    }


    public UploadComment(String ref, String stamp, String com){
        profilePicRef = ref;
        timeStamp = stamp;
        comment = com;
    }

    public UploadComment(String ref, String stamp, String com, String user){
        profilePicRef = ref;
        timeStamp = stamp;
        comment = com;
        username = user;
    }


    public String getProfilePicRef(){
        return profilePicRef;
    }

    public String getTimeStamp(){
        return timeStamp;
    }

    public String getComment() {
        return comment;
    }

    public String getUsername() {return username; }

}
