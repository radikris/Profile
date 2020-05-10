package com.example.login;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.Exclude;

public class Upload implements Parcelable {
    private String mName;
    private String mImageUrl;
    private String mEmailid;
    private String mKey;
    private String city;
    private String job;

    public Upload() {
        //empty constructor needed
    }

    public Upload(String name, String imageUrl, String email) {
        if (name.trim().equals("")) {
            name = "No Name";
        }

        mName = name;
        mImageUrl = imageUrl;
        mEmailid=email;
    }

    protected Upload(Parcel in) {
        mName = in.readString();
        mImageUrl = in.readString();
        mEmailid = in.readString();
        mKey = in.readString();
        city = in.readString();
        job = in.readString();
    }

    public static final Creator<Upload> CREATOR = new Creator<Upload>() {
        @Override
        public Upload createFromParcel(Parcel in) {
            return new Upload(in);
        }

        @Override
        public Upload[] newArray(int size) {
            return new Upload[size];
        }
    };

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getImageUrl() {
        return mImageUrl;
    }

    public void setImageUrl(String imageUrl) {
        mImageUrl = imageUrl;
    }

    public String getEmail() {
        return mEmailid;
    }

    public void setEmail(String email) {
        mEmailid = email;
    }

    @Exclude
    public String getKey() {
        return mKey;
    }

    @Exclude
    public void setKey(String key) {
        mKey = key;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String mcity) {
        city = mcity;
    }

    public String getJob(){return job;}

    public void setJob(String mJob){
        job=mJob;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mName);
        dest.writeString(mImageUrl);
        dest.writeString(mEmailid);
        dest.writeString(mKey);
        dest.writeString(city);
        dest.writeString(job);
    }
}
