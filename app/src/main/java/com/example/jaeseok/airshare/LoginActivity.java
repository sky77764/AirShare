package com.example.jaeseok.airshare;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.jaeseok.airshare.api.FilesClient;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.SASLAuthentication;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.ping.PingManager;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


public class LoginActivity extends Activity {
    public static XMPPTCPConnection mConnection;
    public static String DOMAIN;
    String DOMAIN2;
    final int PORT = 5222;
    public static String USERNAME;
    String USERNAME2;
    public static String PASSWORD;
    public static ComponentName locService;
    private ApplicationClass applicationClass;
    private FrameLayout login_form;
    private LinearLayout login_progress;

    private static final boolean AUTO_HIDE = true;
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    private boolean isFirst = true;

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
//            ActionBar actionBar = getSupportActionBar();
//            if (actionBar != null) {
//                actionBar.show();
//            }
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
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onResume() {
        super.onResume();
        if(isFirst){
            isFirst = false;
            Intent intent = getIntent();
            if(intent.getExtras().getBoolean("login")) {
                USERNAME = intent.getExtras().getString("user_name");
                PASSWORD = "0000";
                DOMAIN = "52.6.95.227";
                USERNAME2 = USERNAME;
                DOMAIN2 = DOMAIN;

                login_form.setVisibility(View.GONE);
                login_progress.setVisibility(View.VISIBLE);

                new SigninTask().execute(null, null, null);
            }
        } else {
            login_form.setVisibility(View.VISIBLE);
            login_progress.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        applicationClass = (ApplicationClass) getApplicationContext();

        mVisible = true;
        mControlsView = findViewById(R.id.fullscreen_content_controls);
        login_form = (FrameLayout) findViewById(R.id.login_frame);
        login_progress = (LinearLayout) findViewById(R.id.login_progress);

        Button mSignIn = (Button) findViewById(R.id.signin_button);

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        mSignIn.setOnTouchListener(mDelayHideTouchListener);

        mSignIn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                EditText id = (EditText) findViewById(R.id.TVid);
                EditText pw = (EditText) findViewById(R.id.TVpw);

                USERNAME = id.getText().toString();
                PASSWORD = pw.getText().toString();
                DOMAIN = "52.6.95.227";
                USERNAME2 = USERNAME;
                DOMAIN2 = DOMAIN;


                if (USERNAME.length() == 0) {
                    Toast toastId = Toast.makeText(getApplicationContext(), "Please input ID", Toast.LENGTH_SHORT);
                    toastId.show();
                    return;
                } else if (PASSWORD.length() == 0) {
                    Toast toastPw = Toast.makeText(getApplicationContext(), "Please input PW", Toast.LENGTH_SHORT);
                    toastPw.show();
                    return;
                }

                new InsertSessionInfo().execute();

                new SigninTask().execute(null, null, null);

            }
        });
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }


    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 2);
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 3);
        }

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
//        ActionBar actionBar = getSupportActionBar();
//        if (actionBar != null) {
//            actionBar.hide();
//        }
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

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Login Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.example.jaeseok.airshare/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Login Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.example.jaeseok.airshare/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }

    private class InsertSessionInfo extends AsyncTask<Void, Void, Boolean> {
        private String mac_address;

        public InsertSessionInfo() {
            WifiManager mng = (WifiManager) getSystemService(WIFI_SERVICE);
            WifiInfo info = mng.getConnectionInfo();
            mac_address = info.getMacAddress();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            JSONObject data = new JSONObject();

            try {
                data.put("address", mac_address);
                data.put("user_name", USERNAME);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                HttpURLConnection conn = (HttpURLConnection) new URL("http://52.6.95.227:8080/insert.jsp").openConnection();
                OutputStream os = null;

                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Cache-Control", "no-cache");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Accept", "application/json");
                conn.setDoOutput(true);
                conn.setDoInput(true);

                os = conn.getOutputStream();
                os.write(data.toString().getBytes());
                os.flush();

                int responseCode = conn.getResponseCode();
                if(responseCode == HttpURLConnection.HTTP_OK)
                    return true;
                else
                    return false;
            } catch (MalformedURLException e) {
                e.printStackTrace();
                return false;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            if(!success)
                Toast.makeText(LoginActivity.this, "로그인 유지 실패", Toast.LENGTH_SHORT).show();
        }
    }

    private class SigninTask extends AsyncTask<Void, Void, String> {
        XMPPTCPConnection mConnection_temp;

        public SigninTask() {
            applicationClass.client = new FilesClient(USERNAME + ":" + USERNAME, PASSWORD,
                    "http://52.6.95.227:8010/auth/v1.0");
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            login_form.setVisibility(View.GONE);
            login_progress.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(Void... urls) {

            boolean result = false;

            try {
                result = applicationClass.client.login();
                if (result) {
                    applicationClass.getReady();
                    applicationClass.cacheInit();
                    URL url = new URL("http://52.6.95.227:8080/download.jsp?id=" + USERNAME);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setDoInput(true);
                    connection.connect();
                    InputStream input = connection.getInputStream();

                    if (input != null) {
                        Bitmap profile = BitmapFactory.decodeStream(input);
                        input.close();
                        applicationClass.addBitmapToMemoryCache(USERNAME, profile);
                    }
                } else
                    return "fail";
            } catch (Exception e) {
                e.printStackTrace();
                return "fail";
            }

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

                PingManager pingManager = PingManager.getInstanceFor(mConnection_temp);
                pingManager.setPingInterval(300); // seconds


            } catch (SmackException | IOException | XMPPException e) {
                e.printStackTrace();
                Toast.makeText(LoginActivity.this, "Openfire 로그인 실패", Toast.LENGTH_SHORT).show();
                return "fail";
            }




            return "succeed";
        }

        @Override
        protected void onPostExecute(String result) {

            if (result == "succeed") {
                Log.i("User Logged In", USERNAME);
                mConnection = mConnection_temp;

                if (locService != null) {
                    Intent i = new Intent();
                    i.setComponent(locService);
                    stopService(i);
                }

                Intent intent = new Intent(LoginActivity.this, LocationService.class);

                intent.putExtra("DOMAIN", DOMAIN2);
                intent.putExtra("USERNAME", USERNAME2);
                locService = startService(intent);

                Intent intentMainActivity = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intentMainActivity);

            } else {
                Toast errMsg = Toast.makeText(getApplicationContext(), "Sign in Failed!", Toast.LENGTH_SHORT);
                errMsg.show();
                Toast err2 = Toast.makeText(getApplicationContext(), DOMAIN, Toast.LENGTH_SHORT);
                err2.show();
                login_form.setVisibility(View.VISIBLE);
                login_progress.setVisibility(View.GONE);
            }
        }
    }

    public static XMPPTCPConnection getConnectedObject() {
        return mConnection;
    }

    public static String getDOMAIN() {
        return DOMAIN;
    }

    public static String getUSERNAME() {
        return USERNAME;
    }

}

