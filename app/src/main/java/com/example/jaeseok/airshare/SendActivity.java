package com.example.jaeseok.airshare;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.chat.ChatManager;

import java.util.ArrayList;
import java.util.Calendar;

public class SendActivity extends Activity {
    final ChatManager chatManager = MainActivity.getChatManagerObject();
    public static ArrayList<User> Users = MainActivity.getUserObject();
//    public static TextView textView = MainActivity.getTextView();
    public static ListView mListView = MainActivity.mListView;
    public static MainActivity.ListViewAdapter mAdapter = MainActivity.mAdapter;

    Button Btn_Send;
    EditText Text_To;
    EditText Text_Body;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int screenWidth = (int) (metrics.widthPixels * 0.80);
        int screenHeight = (int) (metrics.heightPixels * 0.73);

        setContentView(R.layout.activity_send);
        getWindow().setLayout(screenWidth, screenHeight);

        Btn_Send = (Button) findViewById(R.id.btn_send);
        Text_To = (EditText) findViewById(R.id.text_to);
        Text_Body = (EditText) findViewById(R.id.text_body);

        Btn_Send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String USERNAME_TO = Text_To.getText().toString();
                String BODY = Text_Body.getText().toString();

                if (BODY.length() == 0) {
                    Toast.makeText(getApplicationContext(), "Fill message", Toast.LENGTH_SHORT).show();
                    return;
                }

                Chat chat = chatManager.createChat(USERNAME_TO + "@jaeseok");
                try {
                    chat.sendMessage(BODY);

                    Calendar time = Calendar.getInstance();

                    String cur_time = new String(MainActivity.MONTHS[time.get(Calendar.MONTH)] + " " + String.valueOf(time.get(Calendar.DAY_OF_MONTH)) + ", "
                            + String.format("%02d", time.get(Calendar.HOUR_OF_DAY)) + ":" + String.format("%02d", time.get(Calendar.MINUTE)));

                    int idx = findUsername(USERNAME_TO);
                    if(idx == -1) {
                        Users.add(new User(USERNAME_TO));
                        idx = findUsername(USERNAME_TO);
                        Users.get(idx).addMessage(BODY, time, false);

                        mAdapter.addItem(getResources().getDrawable(R.drawable.ic_person),
                                Users.get(idx).fromName,
                                Users.get(idx).getLastMessageBody(),
                                cur_time);
                    }
                    else {
                        Users.get(idx).addMessage(BODY, time, false);

                        mAdapter.mListData.get(mAdapter.findUsername(USERNAME_TO)).mBody = BODY;
                        mAdapter.mListData.get(mAdapter.findUsername(USERNAME_TO)).mDate = cur_time;
                    }

                    mAdapter.dataChange();

//                    String info = "";
//                    for (int i=0; i<Users.size(); i++) {
//                        info += Users.get(i).getLastMessageInfo();
//                    }
//                    textView.setText(info);

//                    Users.get(idx).postLastMessage(textView);
                    finish();

                } catch (SmackException.NotConnectedException e) {
                    Log.d("SendMsg", e.toString());
                    Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    public int findUsername(String fromName) {
        for(int i=0; i<this.Users.size(); i++) {
            if(this.Users.get(i).fromName.equals(fromName))
                return i;
        }
        return -1;
    }
}
