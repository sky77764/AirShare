package com.example.jaeseok.airshare;

import java.util.ArrayList;

/**
 * Created by JaeSeok on 2016-02-22.
 */

class MyMessage {
    String body;
    String timeStamp;

    MyMessage(String body, String timeStamp) {
        this.body = body;
        this.timeStamp = timeStamp;
    }
}

public class User {
    public String fromName;
    public ArrayList<MyMessage> messages;

    public User(String fromName) {
        this.fromName = new String(fromName);
        messages = new ArrayList<>();
    }

    public void setfromName(String fromName) {
        this.fromName = fromName;
    }

    public void addMessage(String body,  String timeStamp) {
        messages.add(new MyMessage(body, timeStamp));
    }

    public String getLastMessageBody() {
        return messages.get(messages.size()-1).body;
    }

    public String getLastMessageTime() {
        return messages.get(messages.size()-1).timeStamp;
    }

}
