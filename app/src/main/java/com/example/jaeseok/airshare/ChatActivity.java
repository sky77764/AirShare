package com.example.jaeseok.airshare;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.chat.ChatManager;
import org.jivesoftware.smack.chat.ChatManagerListener;
import org.jivesoftware.smack.chat.ChatMessageListener;
import org.jivesoftware.smack.packet.Message;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;


public class ChatActivity extends ActionBarActivity {
    final ChatManager chatManager = MainActivity.getChatManagerObject();
    private EditText messageET;
    public static ListView messagesContainer;
    private Button sendBtn;
    public static ChatAdapter adapter;
    private ArrayList<ChatMessage> chatHistory;
    public static ArrayList<User> Users = MainActivity.getUserObject();
    private int Users_idx;
    public static String USERNAME_TO;
    public static MainActivity.ListViewAdapter mAdapter = MainActivity.mAdapter;
    public static boolean isChatActivityInFront = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        isChatActivityInFront = true;

        Intent intent = getIntent();
        Users_idx = intent.getExtras().getInt("Users_idx");
        USERNAME_TO = new String(Users.get(Users_idx).fromName);
        initControls();

        getSupportActionBar().setTitle(USERNAME_TO);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Toast.makeText(ChatActivity.this, Users.get(Users_idx).fromName, Toast.LENGTH_SHORT).show();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_chat, menu);
        return true;
    }
/*
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
*/
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                //NavUtils.navigateUpFromSameTask(this);
                this.finish();
                return true;
            case R.id.action_settings:
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initControls() {
        messagesContainer = (ListView) findViewById(R.id.messagesContainer);
        messageET = (EditText) findViewById(R.id.messageEdit);
        sendBtn = (Button) findViewById(R.id.chatSendButton);

        //TextView meLabel = (TextView) findViewById(R.id.meLbl);
        //TextView companionLabel = (TextView) findViewById(R.id.friendLabel);
        RelativeLayout container = (RelativeLayout) findViewById(R.id.container);
       // companionLabel.setText(USERNAME_TO);

        loadHistory();

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String messageText = messageET.getText().toString();
                if (TextUtils.isEmpty(messageText)) {
                    return;
                }


                Chat chat = chatManager.createChat(USERNAME_TO + "@jaeseok");
                try {
                    chat.sendMessage(messageText);

                    Calendar time = Calendar.getInstance();
                    String cur_time = new String(MainActivity.MONTHS[time.get(Calendar.MONTH)] + " " + String.valueOf(time.get(Calendar.DAY_OF_MONTH)) + ", "
                            + String.format("%02d", time.get(Calendar.HOUR_OF_DAY)) + ":" + String.format("%02d", time.get(Calendar.MINUTE)));
                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.setId(122);//dummy
                    chatMessage.setMessage(messageText);
                    chatMessage.setDate(cur_time);
                    chatMessage.setMe(true);

                    Users.get(Users_idx).addMessage(messageText, time, false);

                    mAdapter.mListData.get(mAdapter.findUsername(USERNAME_TO)).mBody = messageText;
                    mAdapter.mListData.get(mAdapter.findUsername(USERNAME_TO)).mDate = cur_time;
                    mAdapter.dataChange();

                    displayMessage(chatMessage);
                }
                catch (SmackException.NotConnectedException e) {
                    Log.d("SendMsg", e.toString());
                    Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_SHORT).show();
                }

                messageET.setText("");

            }
        });


    }

    public static void displayMessage(ChatMessage message) {
        adapter.add(message);
        adapter.notifyDataSetChanged();
        scroll();
    }

    public static void scroll() {
        messagesContainer.setSelection(messagesContainer.getCount() - 1);
    }

    public void loadHistory(){

        chatHistory = new ArrayList<ChatMessage>();

        for (int i=0; i<Users.get(Users_idx).messages.size(); i++) {
            ChatMessage msg = new ChatMessage();
            msg.setId(i+1);
           if(Users.get(Users_idx).messages.get(i).bReceived)
               msg.setMe(false);
            else
               msg.setMe(true);
            msg.setMessage(Users.get(Users_idx).messages.get(i).body);
            msg.setDate(Users.get(Users_idx).messages.get(i).getTimeString());


            chatHistory.add(msg);
        }


        adapter = new ChatAdapter(ChatActivity.this, new ArrayList<ChatMessage>());
        messagesContainer.setAdapter(adapter);

        for(int i=0; i<chatHistory.size(); i++) {
            ChatMessage message = chatHistory.get(i);
            displayMessage(message);
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        isChatActivityInFront = true;
    }

    @Override
    public void onPause() {
        super.onPause();
        isChatActivityInFront = false;
    }

    public int findUsername(String fromName) {
        for(int i=0; i<this.Users.size(); i++) {
            if(this.Users.get(i).fromName.equals(fromName))
                return i;
        }
        return -1;
    }


}
