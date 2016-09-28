package com.example.jaeseok.airshare;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.jaeseok.airshare.api.FilesClient;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.SASLAuthentication;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by swimming on 2016. 9. 28..
 */
public class LoadActivity extends Activity {
    private String mac_address;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load);

        WifiManager mng = (WifiManager) getSystemService(WIFI_SERVICE);
        WifiInfo info = mng.getConnectionInfo();
        mac_address = info.getMacAddress();

        new LoadTask().execute();
    }

    private class LoadTask extends AsyncTask<Void, Void, Boolean> {
        private String user_name;

        public JSONObject doPostJSON(JSONObject data) {
            String serverUrl = "";
            JSONObject response = null; // response json

            serverUrl = "http://52.6.95.227:8080/load.jsp";

            try {
                response = requestData(data.toString(), serverUrl);
            }  catch (Exception e) {
                e.printStackTrace();
            }

            return response;
        }

        public JSONObject requestData(String... urls) {
            JSONObject responseJSON = null;

            try {

                HttpURLConnection conn = (HttpURLConnection) new URL(urls[1]).openConnection();
                OutputStream os   = null;
                InputStream is   = null;
                ByteArrayOutputStream baos = null;

                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Cache-Control", "no-cache");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Accept", "application/json");
                conn.setDoOutput(true);
                conn.setDoInput(true);

                os = conn.getOutputStream();
                os.write(urls[0].getBytes());
                os.flush();

                String response;

                int responseCode = conn.getResponseCode();
                if(responseCode == HttpURLConnection.HTTP_OK) {
                    is = conn.getInputStream();
                    baos = new ByteArrayOutputStream();
                    byte[] byteBuffer = new byte[1024];
                    byte[] byteData = null;
                    int nLength = 0;
                    while((nLength = is.read(byteBuffer, 0, byteBuffer.length)) != -1) {
                        baos.write(byteBuffer, 0, nLength);
                    }
                    byteData = baos.toByteArray();
                    response = new String(byteData);
                    responseJSON = new JSONObject(response);
                }
            } catch (JSONException e) {
                e.printStackTrace();;
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return responseJSON;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            JSONObject data = new JSONObject();
            JSONObject result = null;
            try {
                data.put("address", mac_address);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            result = doPostJSON(data);

            try {
                user_name = result.getString("user_name");
                return !user_name.equals("null");
            } catch (JSONException e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            Intent intent = new Intent(LoadActivity.this, LoginActivity.class);

            if(success){
                intent.putExtra("login", true);
                intent.putExtra("user_name", user_name);
                startActivity(intent);
                LoadActivity.this.overridePendingTransition(0,0);
            } else {
                intent.putExtra("login", false);
                startActivity(intent);
                LoadActivity.this.overridePendingTransition(0,0);
            }

            finish();
        }
    }
}
