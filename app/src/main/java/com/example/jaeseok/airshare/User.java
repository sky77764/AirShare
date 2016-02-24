package com.example.jaeseok.airshare;

import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by JaeSeok on 2016-02-22.
 */

class MyMessage {
    String body;
    String timeStamp;
    boolean bReceived;

    MyMessage(String body, String timeStamp, boolean bReceived) {
        this.body = body;
        this.timeStamp = timeStamp;
        this.bReceived = bReceived;
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

    public void addMessage(String body,  String timeStamp, boolean bReceived) {
        messages.add(new MyMessage(body, timeStamp, bReceived));
    }

    public String getLastMessageBody() { return messages.get(messages.size()-1).body; }
    public String getLastMessageTime() { return messages.get(messages.size()-1).timeStamp; }
    public Boolean getLastMessageBool() { return messages.get(messages.size()-1).bReceived; }
    public String getLastMessageInfo() { return fromName + "\n" + getLastMessageBody() + "\n" + getLastMessageTime() + "\n"; }

    public void postLastMessage(TextView textView) {
        String text = textView.getText().toString();
        textView.setText(text + "\n" + (getLastMessageBool() == true ? "[RECV]" : "[SEND]") + getLastMessageBody() + " - " + fromName + "(" + getLastMessageTime() + ")");
    }



}
