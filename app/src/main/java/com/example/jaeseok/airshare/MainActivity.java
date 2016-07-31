package com.example.jaeseok.airshare;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.chat.ChatManager;
import org.jivesoftware.smack.chat.ChatManagerListener;
import org.jivesoftware.smack.chat.ChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smackx.delay.packet.DelayInformation;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    public static XMPPTCPConnection mConnection = LoginActivity.getConnectedObject();
    public static ChatManager chatManager = ChatManager.getInstanceFor(mConnection);
    public static ArrayList<User> Users;
//    public static TextView textView;
    public static ListView mListView = null;
    public static ListViewAdapter mAdapter = null;
    public static String MONTHS[] = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug",
        "Sep", "Oct", "Nov", "Dec"};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("MainActivity", "onCreate");
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Messages");

        Users = new ArrayList<>();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intentMainActivity = new Intent(MainActivity.this, SendActivity.class);
                startActivity(intentMainActivity);

//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();

            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        mListView = (ListView) findViewById(R.id.messageList);

        mAdapter = new ListViewAdapter(this);
        mListView.setAdapter(mAdapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                ListData mData = mAdapter.mListData.get(position);
//                Toast.makeText(MainActivity.this, mData.mTitle, Toast.LENGTH_SHORT).show();

                Intent intentChatActivity = new Intent(MainActivity.this, ChatActivity.class);
                intentChatActivity.putExtra("Users_idx", findUsername(mData.mTitle));
                startActivity(intentChatActivity);

            }
        });



        chatManager.addChatListener(new ChatManagerListener() {
            @Override
            public void chatCreated(Chat chat, boolean b) {
                chat.addMessageListener(new ChatMessageListener() {
                    @Override
                    public void processMessage(Chat chat, Message message) {
                        Log.d("ReceiveMsg", message.toString());

                        final String msg = new String(message.getBody());
                        String fromName_temp;
                        if(message.getFrom().contains("@"))
                            fromName_temp = new String(message.getFrom().substring(0, message.getFrom().indexOf("@")));
                        else
                            fromName_temp = new String(message.getFrom());
                        final String fromName = new String(fromName_temp);
                        Log.d("ReceiveMsg", fromName + ": " + msg);
                        DelayInformation inf = null;
                        try {
                            inf = (DelayInformation)message.getExtension("x","jabber:x:delay");
                        } catch (Exception e) {
                            Log.d("Delayinformation", e.getMessage());
                        }

                        // get offline message timestamp
                        Calendar time = Calendar.getInstance();

                        String cur_time = new String(MONTHS[time.get(Calendar.MONTH)] + " " + String.valueOf(time.get(Calendar.DAY_OF_MONTH)) + ", "
                            + String.format("%02d", time.get(Calendar.HOUR_OF_DAY)) + ":" + String.format("%02d", time.get(Calendar.MINUTE)));

//                        if(inf!=null) {
//                            Date date = inf.getStamp();
//                            timeStamp = date.toString();
//                        }

                        Log.d("ReceiveMsg", fromName + ": " + msg);
                        int idx = findUsername(fromName);
                        if(idx == -1) {
                            Users.add(new User(fromName));
                            idx = findUsername(fromName);
                            Users.get(idx).addMessage(msg, time, true);

                            mAdapter.addItem(getResources().getDrawable(R.drawable.ic_person),
                                    Users.get(idx).fromName,
                                    Users.get(idx).getLastMessageBody(),
                                    cur_time);
                            Log.d("ReceiveMsg", "add, " + idx);
                        }
                        else {
                            Users.get(idx).addMessage(msg, time, true);

                            idx = mAdapter.findUsername(fromName);
                            if(idx != 0) {
                                mAdapter.remove(idx);
                                mAdapter.addItem(getResources().getDrawable(R.drawable.ic_person),
                                        fromName,
                                        msg,
                                        cur_time);
                                Log.d("ReceiveMsg", "remove, " + idx);
                            }
                            else {
                                Log.d("ReceiveMsg", "update, " + idx);
                                mAdapter.mListData.get(idx).mBody = msg;
                                mAdapter.mListData.get(idx).mDate = cur_time;
                            }
                        }
                        mAdapter.dataChange();


                        if(ChatActivity.isChatActivityInFront && ChatActivity.USERNAME_TO.equals(fromName)) {
                            Log.d("INFRONT", "TRUE");
                            Log.d("INFRONT", ChatActivity.USERNAME_TO);
                            Log.d("INFRONT", fromName);
                            ChatMessage chatMessage = new ChatMessage();
                            chatMessage.setId(122);//dummy
                            chatMessage.setMessage(msg);
                            chatMessage.setDate(cur_time);
                            chatMessage.setMe(false);

                            ChatActivity.displayMessage(chatMessage);
                        }
                        else {
                            Log.d("INFRONT", "FALSE");
                        }
                    }
                });
            }
        });

    }

    public class ViewHolder {
        public ImageView mIcon;
        public TextView mText;
        public TextView mBody;
        public TextView mDate;
    }

    public class ListViewAdapter extends BaseAdapter {
        public Context mContext = null;
        public ArrayList<ListData> mListData = new ArrayList<ListData>();

        public ListViewAdapter(Context mContext) {
            super();
            this.mContext = mContext;
        }

        @Override
        public int getCount() { return mListData.size(); }

        @Override
        public Object getItem(int position) { return mListData.get(position); }

        @Override
        public long getItemId(int position) { return position; }

        public int findUsername(String fromName) {
            for(int i=0; i<mListData.size(); i++) {
                if(mListData.get(i).mTitle.equals(fromName))
                    return i;
            }
            return -1;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                ViewHolder holder;
                if (convertView == null) {
                    holder = new ViewHolder();

                    LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    convertView = inflater.inflate(R.layout.listview_item, null);

                    holder.mIcon = (ImageView) convertView.findViewById(R.id.mImage);
                    holder.mText = (TextView) convertView.findViewById(R.id.mText);
                    holder.mBody = (TextView) convertView.findViewById(R.id.mBody);
                    holder.mDate = (TextView) convertView.findViewById(R.id.mDate);

                convertView.setTag(holder);
            }else{
                holder = (ViewHolder) convertView.getTag();
            }

            ListData mData = mListData.get(position);

            if (mData.mIcon != null) {
                holder.mIcon.setVisibility(View.VISIBLE);
                holder.mIcon.setImageDrawable(mData.mIcon);
            }else{
                holder.mIcon.setVisibility(View.GONE);
            }

            holder.mText.setText(mData.mTitle);
            holder.mBody.setText(mData.mBody);
            holder.mDate.setText(mData.mDate);

            return convertView;
        }

        public void addItem(Drawable icon, String mTitle, String mBody, String mDate){
            ListData addInfo = null;
            addInfo = new ListData();
            addInfo.mIcon = icon;
            addInfo.mTitle = mTitle;
            addInfo.mBody = mBody;
            addInfo.mDate = mDate;

            mListData.add(0, addInfo);
        }

        public void remove(int position){
            mListData.remove(position);
            dataChange();
        }

        public void sort(){
            Collections.sort(mListData, ListData.ALPHA_COMPARATOR);
            dataChange();
        }

        public void dataChange(){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mAdapter.notifyDataSetChanged();
                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mAdapter.dataChange();
        Log.d("MainActivity", "onResume();");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_messages) {

        } else if (id == R.id.nav_contact) {

        } else if (id == R.id.nav_files) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    public static ChatManager getChatManagerObject() { return chatManager; }
    public static ArrayList<User> getUserObject() { return Users; }
//    public static TextView getTextView() { return textView; }
    public static ListViewAdapter getListViewAdapter() { return mAdapter; }

    public int findUsername(String fromName) {
        for(int i=0; i<this.Users.size(); i++) {
            if(this.Users.get(i).fromName.equals(fromName))
                return i;
        }
        return -1;
    }


}
