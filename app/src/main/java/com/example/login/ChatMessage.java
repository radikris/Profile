package com.example.login;

import java.util.Date;

public class ChatMessage {
    private String messageText;
    private String messageUser;
    private String messageToWhom;
    private String key;
    private long messageTime;

    public ChatMessage(String messageText, String messageUser, String messageTo) {
        this.messageText = messageText;
        this.messageUser = messageUser;
        this.messageToWhom=messageTo;
        messageTime=new Date().getTime();
        //key=mkey;
    }

    public ChatMessage(){}

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public String getMessageUser() {
        return messageUser;
    }

    public void setMessageUser(String messageUser) {
        this.messageUser = messageUser;
    }

    public long getMessageTime() {
        return messageTime;
    }

    public void setMessageToWhom(String messagetowhom) {
        this.messageToWhom = messagetowhom;
    }

    public String getMessageToWhom() {
        return messageToWhom;
    }


    public void setMessageTime(long messageTime) {
        this.messageTime = messageTime;
    }

    public String getKeyId(){return key;}
    public void setKey(String mkey){key = mkey;}
}
