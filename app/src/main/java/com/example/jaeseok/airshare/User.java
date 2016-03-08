package com.example.jaeseok.airshare;

import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by JaeSeok on 2016-02-22.
 */

class MyMessage {
    String body;
    Calendar time;
    boolean bReceived;

    MyMessage(String body, Calendar time, boolean bReceived) {
        this.body = body;
        this.time = time;
        this.bReceived = bReceived;
    }

    String getTimeString() {
        String timeString = new String(MainActivity.MONTHS[time.get(Calendar.MONTH)] + " " + String.valueOf(time.get(Calendar.DAY_OF_MONTH)) + ", "
                + String.format("%02d", time.get(Calendar.HOUR_OF_DAY)) + ":" + String.format("%02d", time.get(Calendar.MINUTE)));
        return timeString;
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

    public void addMessage(String body,  Calendar time, boolean bReceived) {
        messages.add(new MyMessage(body, time, bReceived));
    }

    public String getLastMessageBody() { return messages.get(messages.size()-1).body; }
    public Calendar getLastMessageTime() { return messages.get(messages.size()-1).time; }
    public Boolean getLastMessageBool() { return messages.get(messages.size()-1).bReceived; }
    public String getLastMessageInfo() { return fromName + "\n" + getLastMessageBody() + "\n" + getLastMessageTime() + "\n"; }

    public void postLastMessage(TextView textView) {
        String text = textView.getText().toString();
        textView.setText(text + "\n" + (getLastMessageBool() == true ? "[RECV]" : "[SEND]") + getLastMessageBody() + " - " + fromName + "(" + getLastMessageTime() + ")");
    }



}
