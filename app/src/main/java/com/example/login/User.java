package com.example.login;

public class User {
    public String name, city, job, email;
    public String mImageUrl;
    public String uid;

    public User(){}

    public User(String name, String city, String job, String mail, String imageUrl) {
        this.name = name;
        this.city = city;
        this.job = job;
        this.email=mail;
        this.mImageUrl=imageUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getJob() {
        return job;
    }

    public void setJob(String job) {
        this.job = job;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getImageUrl(){return mImageUrl;}

    public void setImageUrl(String imageurl){mImageUrl=imageurl;}

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
