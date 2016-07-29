package com.example.jaeseok.airshare;

import android.Manifest;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.SASLAuthentication;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class LocationService extends Service {

    //    private Location mDestination;
    static String DOMAIN = LoginActivity.getDOMAIN();
    static String USERNAME = LoginActivity.getUSERNAME();
    String phpFILENAME = "insert.php";
    String url;
    Intent intent;
    public static double latitude, longitude;
    public static float bearing;

    private LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            DOMAIN = LoginActivity.getDOMAIN();
            USERNAME = LoginActivity.getUSERNAME();
            latitude = location.getLatitude();
            longitude = location.getLongitude();
//            if (location.hasBearing())
                bearing = location.getBearing();

            if(DOMAIN == null) {
                stopSelf();
                return;
            }
            url = "http://"+DOMAIN+"/"+phpFILENAME+"?username="+USERNAME+"&latitude="+String.valueOf(latitude)+"&longitude="+String.valueOf(longitude);
            insertData(url);
            Log.d("onLocationChanged2", url);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {
        }
    };

    public LocationService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("LocationService", "onStart: " + intent);
        latitude = longitude = -1;

        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        //mDestination = intent.getParcelableExtra("destination");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) !=
                    PackageManager.PERMISSION_GRANTED &&
                    checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) !=
                            PackageManager.PERMISSION_GRANTED) {
                return START_NOT_STICKY;
            }
        }
        locationManager.removeUpdates(locationListener);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0,
                locationListener);



        return START_STICKY;
    }

    public void insertData(String url){
        class insertDATA extends AsyncTask<String, Void, String> {

            @Override
            protected String doInBackground(String... params) {

                String uri = params[0];

                BufferedReader bufferedReader = null;
                try {
                    URL url = new URL(uri);
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    StringBuilder sb = new StringBuilder();

                    bufferedReader = new BufferedReader(new InputStreamReader(con.getInputStream()));

                    String json;
                    while((json = bufferedReader.readLine())!= null){
                        sb.append(json+"\n");
                    }

                    return sb.toString().trim();

                }catch(Exception e){
                    Log.d("getData", e.toString());
                    return null;
                }

            }

            @Override
            protected void onPostExecute(String result){
                //myJSON=result;
//                Toast.makeText(MapsActivity.this, myJSON, Toast.LENGTH_SHORT).show();
                //showList();
            }
        }
        insertDATA g = new insertDATA();
        g.execute(url);
    }

}
