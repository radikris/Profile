package com.example.login;

public class Messages {
    private String message, type;
    private boolean seen;
    private long time;
    private String from;
    private String mdata;

    public Messages(String message, boolean seen, long time, String type){
        this.message=message;
        this.seen=seen;
        this.type=type;
        this.time=time;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public Messages(String from) {
        this.from = from;
    }

    public Messages(){}

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getMdata() {
        return mdata;
    }

    public void setMdata(String mdata) {
        this.mdata = mdata;
    }
}
