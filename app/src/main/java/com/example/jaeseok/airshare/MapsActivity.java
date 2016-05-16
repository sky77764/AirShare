package com.example.jaeseok.airshare;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.LevelListDrawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.chat.ChatManager;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    String USERNAME = LoginActivity.getUSERNAME();
    String DOMAIN;
    String phpFILENAME;
    String url;
    Button Btn_Swing;
    String USERNAME_TO;
    String BODY;
    private GoogleMap mMap;
    LatLng myPosition;
    final int REQUEST_CODE_LOCATION = 2;
    public static ArrayList<User> Users = MainActivity.getUserObject();
    final ChatManager chatManager = MainActivity.getChatManagerObject();
    public static ListView mListView = MainActivity.mListView;
    public static MainActivity.ListViewAdapter mAdapter = MainActivity.mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int screenWidth = (int) (metrics.widthPixels * 0.80);
        int screenHeight = (int) (metrics.heightPixels * 0.73);

        setContentView(R.layout.activity_send);
        getWindow().setLayout(screenWidth, screenHeight);

        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        DOMAIN = LoginActivity.getDOMAIN();
        phpFILENAME = "selectNear.php";
        url = "http://"+DOMAIN+"/"+phpFILENAME+"?username="+USERNAME;
        getData(url);

        Btn_Swing = (Button) findViewById(R.id.btn_swing);
        Btn_Swing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Btn_Swing.getText() != "Send") {
                    Toast.makeText(MapsActivity.this, "Swing", Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent = getIntent();
                    BODY=intent.getExtras().getString("BODY");

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

                        finish();

                    } catch (SmackException.NotConnectedException e) {
                        Log.d("SendMsg", e.toString());
                        Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_SHORT).show();
                    }



                }
            }
        });

    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        } else {
//            Toast.makeText(getApplicationContext(), "Need Location Permission", Toast.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_CODE_LOCATION);

        }

        // Getting LocationManager object from System Service LOCATION_SERVICE
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        // Creating a criteria object to retrieve provider
        Criteria criteria = new Criteria();

        // Getting the name of the best provider
        String provider = locationManager.getBestProvider(criteria, true);

        // Getting Current Location
        Location location = locationManager.getLastKnownLocation(provider);

        if(location!=null) {


            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            myPosition = new LatLng(latitude, longitude);

            mMap.moveCamera(CameraUpdateFactory.newLatLng(myPosition));
            mMap.moveCamera(CameraUpdateFactory.zoomTo(17));

            // Instantiates a new CircleOptions object and defines the center and radius
            CircleOptions circleOptions = new CircleOptions()
                    .center(new LatLng(latitude, longitude))
                    .radius(120)
                    .strokeColor(Color.argb(100, 102, 255, 102))
                    .fillColor(Color.argb(40, 102, 255, 102));

            // Get back the mutable Circle
            Circle circle = mMap.addCircle(circleOptions);

            mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(final Marker marker) {
                    marker.showInfoWindow();
                    USERNAME_TO = new String(marker.getTitle());
                    //Toast.makeText(MapsActivity.this,  marker.getTitle(), Toast.LENGTH_SHORT).show();
                    Btn_Swing.setText("Send");
                    return true;
                }
            });
        }

    }

    public void getData(String url){
        class GetDataJSON extends AsyncTask<String, Void, String> {

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
               // Toast.makeText(MapsActivity.this, result, Toast.LENGTH_SHORT).show();
                try {
                    JSONObject object = new JSONObject(result);
                    JSONArray jarray = new JSONArray(object.getString("result"));   // JSONArray 생성

                    for(int i=0; i < jarray.length(); i++){
                        JSONObject jObject = jarray.getJSONObject(i);  // JSONObject 추출
                        String username = jObject.getString("username");
                        double latitude = jObject.getDouble("latitude");
                        double longitude = jObject.getDouble("longitude");

                        if (username != USERNAME) {
                            mMap.addMarker(new MarkerOptions()
                                    .position(new LatLng(latitude, longitude))
                                    .title(username)
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker)));
                        }

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
//                Toast.makeText(MapsActivity.this, test, Toast.LENGTH_SHORT).show();
                //showList();

            }
        }
        GetDataJSON g = new GetDataJSON();
        g.execute(url);
    }


    public int findUsername(String fromName) {
        for(int i=0; i<this.Users.size(); i++) {
            if(this.Users.get(i).fromName.equals(fromName))
                return i;
        }
        return -1;
    }

}
