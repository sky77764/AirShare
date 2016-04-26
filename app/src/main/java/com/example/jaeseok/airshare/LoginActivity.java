package com.example.jaeseok.airshare;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.SASLAuthentication;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;

import java.io.IOException;


public class LoginActivity extends AppCompatActivity {
    public static XMPPTCPConnection mConnection;
    String DOMAIN;
    final int PORT = 5222;
    public static String USERNAME;
    String PASSWORD;

    private static final boolean AUTO_HIDE = true;
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
        }
    };
    private View mControlsView;
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
            mControlsView.setVisibility(View.VISIBLE);
        }
    };
    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };

    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        mVisible = true;
        mControlsView = findViewById(R.id.fullscreen_content_controls);

        Button Bsignin = (Button) findViewById(R.id.signin_button);


        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        Bsignin.setOnTouchListener(mDelayHideTouchListener);

        Bsignin.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                EditText id = (EditText) findViewById(R.id.TVid);
                EditText pw = (EditText) findViewById(R.id.TVpw);
                EditText ipaddr = (EditText) findViewById(R.id.TVipaddr);

                USERNAME = id.getText().toString();
                PASSWORD = pw.getText().toString();
                DOMAIN = ipaddr.getText().toString();

                if (USERNAME.length() == 0) {
                    Toast toastId = Toast.makeText(getApplicationContext(), "Please input ID", Toast.LENGTH_SHORT);
                    toastId.show();
                    return;
                } else if (PASSWORD.length() == 0) {
                    Toast toastPw = Toast.makeText(getApplicationContext(), "Please input PW", Toast.LENGTH_SHORT);
                    toastPw.show();
                    return;
                }

                new SigninTask().execute(null, null, null);

            }
        });
    }



    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mControlsView.setVisibility(View.GONE);
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar

        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    private class SigninTask extends AsyncTask<Void, Void, String> {
        XMPPTCPConnection mConnection_temp;

        @Override
        protected String doInBackground(Void... urls) {

            XMPPTCPConnectionConfiguration.Builder config = XMPPTCPConnectionConfiguration.builder();
            config.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);
            SASLAuthentication.unBlacklistSASLMechanism("PLAIN");
            SASLAuthentication.blacklistSASLMechanism("DIGEST-MD5");
            config.setUsernameAndPassword(USERNAME + "@" + DOMAIN, PASSWORD);
            config.setServiceName(DOMAIN);
            config.setHost(DOMAIN);
            config.setPort(PORT);
            config.setDebuggerEnabled(true);

            mConnection_temp = new XMPPTCPConnection(config.build());
            mConnection_temp.setPacketReplyTimeout(10000);

            try {
                mConnection_temp.connect();
                mConnection_temp.login(USERNAME, PASSWORD);

            } catch (SmackException | IOException | XMPPException e) {
                Log.d("AsyncTask", e.toString());
                return "fail";
            }


        return "succeed";
    }

    @Override
        protected void onPostExecute(String result) {

            if(result == "succeed") {
                Log.i("User Logged In", USERNAME);
                mConnection = mConnection_temp;

                Intent intentMainActivity = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intentMainActivity);
            }
            else {
                Toast errMsg = Toast.makeText(getApplicationContext(), "Sign in Failed!", Toast.LENGTH_SHORT);
                errMsg.show();
            }
        }
    }

    public static XMPPTCPConnection getConnectedObject() {
        return mConnection;
    }

}

