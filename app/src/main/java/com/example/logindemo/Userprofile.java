package com.example.logindemo;

public class Userprofile {
    public String bio;
    public String name;


    public Userprofile(){

    }

    public Userprofile(String userbio,String username){
        this.bio=userbio;
        this.name=username;
    }

    public String getbio() {
        return bio;
    }

    public void setbio(String userbio) {
        this.bio = userbio;
    }

    public String getname() {
        return name;
    }

    public void setname(String username) {
        this.name = username;
    }
}
