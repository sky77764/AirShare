package com.example.jaeseok.airshare;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Vibrator;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
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
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

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
import java.util.Vector;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, SensorEventListener {
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
    Vector<Marker> prev_markers;
    final Handler h = new Handler();
    double latitude, longitude;

    private SensorManager mSensorManager;
    private Sensor mAccelerometer, mField;
    private float[] mGravity;
    private float[] mMagnetic;
    Polyline direction_line;
    PolylineOptions direction_line_opt = new PolylineOptions();
    int direction_cnt = 0;
    boolean SWING_MODE = false;
    int SWING_MODE_cnt = 0;
    float SWING_minus_min = 10;
    float SWING_minus_max = -10;
    float SWING_plus_min = 10;
    float SWING_plus_max = -10;
    float SWING_start_region;
    float SWING_end_region;


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

        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mField = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        DOMAIN = LoginActivity.getDOMAIN();
//        phpFILENAME = "selectAll.php";
        phpFILENAME = "selectNear.php";
        url = "http://" + DOMAIN + "/" + phpFILENAME + "?username=" + USERNAME;

        Btn_Swing = (Button) findViewById(R.id.btn_swing);
        Btn_Swing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Btn_Swing.getText() != "Send") {
                    SWING_MODE = true;
                    SWING_MODE_cnt = 0;
                    direction_cnt = 0;

                    SWING_minus_min = 10;
                    SWING_minus_max = -10;
                    SWING_plus_min = 10;
                    SWING_plus_max = -10;

                    Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    // Vibrate for 500 milliseconds
                    vibrator.vibrate(300);
                    Btn_Swing.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorDetecting));
                    Btn_Swing.setText("Detecting...");
                    //Btn_Swing.setTextColor(Color.BLACK);
                    Btn_Swing.setClickable(false);

                } else {
                    Intent intent = getIntent();
                    BODY = intent.getExtras().getString("BODY");

                    Chat chat = chatManager.createChat(USERNAME_TO + "@" + DOMAIN);
                    try {
                        chat.sendMessage(BODY);

                        Calendar time = Calendar.getInstance();

                        String cur_time = new String(MainActivity.MONTHS[time.get(Calendar.MONTH)] + " " + String.valueOf(time.get(Calendar.DAY_OF_MONTH)) + ", "
                                + String.format("%02d", time.get(Calendar.HOUR_OF_DAY)) + ":" + String.format("%02d", time.get(Calendar.MINUTE)));

                        int idx = findUsername(USERNAME_TO);
                        if (idx == -1) {
                            Users.add(new User(USERNAME_TO));
                            idx = findUsername(USERNAME_TO);
                            Users.get(idx).addMessage(BODY, time, false);

                            mAdapter.addItem(getResources().getDrawable(R.drawable.ic_person),
                                    Users.get(idx).fromName,
                                    Users.get(idx).getLastMessageBody(),
                                    cur_time);
                        } else {
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
        mMap.getUiSettings().setCompassEnabled(true);

        // Getting LocationManager object from System Service LOCATION_SERVICE
        final LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        // Creating a criteria object to retrieve provider
        Criteria criteria = new Criteria();

        // Getting the name of the best provider
        final String provider = locationManager.getBestProvider(criteria, true);

        // Getting Current Location
        Location location = locationManager.getLastKnownLocation(provider);

        if (location != null) {

            final int delay = 1000; //milliseconds
            final Context temp = this;
            h.postDelayed(new Runnable() {
                Circle circle;

                public void run() {
                    if (ActivityCompat.checkSelfPermission(temp, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(temp, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(MapsActivity.this, "GPS Permission denied!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Location cur_location = locationManager.getLastKnownLocation(provider);
                    if(cur_location == null)
                        return;

                    latitude = cur_location.getLatitude();
                    longitude = cur_location.getLongitude();

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
                    if(circle != null)
                        circle.remove();
                    circle = mMap.addCircle(circleOptions);
                    getData(url);

                    h.postDelayed(this, delay);

                }
            }, delay);


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

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_UI);
        mSensorManager.registerListener(this, mField, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
        h.removeMessages(0);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) { }

    @Override
    public void onSensorChanged(SensorEvent event) {
        switch(event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                mGravity = event.values.clone();
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                mMagnetic = event.values.clone();
                break;
            default:
                return;
        }

        if(mGravity != null && mMagnetic != null) {
            updateDirection();
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
                    Vector<Marker> markers = new Vector<Marker>();

                    for(int i=0; i < jarray.length(); i++){
                        JSONObject jObject = jarray.getJSONObject(i);  // JSONObject 추출
                        String username = jObject.getString("username");
                        double latitude = jObject.getDouble("latitude");
                        double longitude = jObject.getDouble("longitude");


                        if (username != USERNAME) {
                            if(prev_markers != null && prev_markers.size() > jarray.length()) {
                                for(int j=0; j<prev_markers.size(); j++)
                                    prev_markers.elementAt(j).remove();
                                prev_markers.clear();
                            }

                            int idx = updateNearUser(username, prev_markers);

                            if(idx == -1) {
                                Marker m = mMap.addMarker(new MarkerOptions()
                                        .position(new LatLng(latitude, longitude))
                                        .title(username)
                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker)));
                                markers.add(m);
                            }
                            else {
                                if(latitude == prev_markers.elementAt(idx).getPosition().latitude && longitude == prev_markers.elementAt(idx).getPosition().longitude) {
                                    markers.add(prev_markers.elementAt(idx));
                                }
                                else {
                                    prev_markers.elementAt(idx).remove();
                                    Marker m = mMap.addMarker(new MarkerOptions()
                                            .position(new LatLng(latitude, longitude))
                                            .title(username)
                                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker)));
                                    markers.add(m);
                                }
                            }
                        }
                    }
                    prev_markers = (Vector)markers.clone();
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

    private int updateNearUser(String title, Vector<Marker> m) {
        if(m == null)
            return -1;

        for(int i=0; i<m.size(); i++) {
            if(title.equals(m.elementAt(i).getTitle()))
                return i;
        }
        return -1;
    }


    public int findUsername(String fromName) {
        for(int i=0; i<this.Users.size(); i++) {
            if(this.Users.get(i).fromName.equals(fromName))
                return i;
        }
        return -1;
    }

    private void updateDirection() {
        float[] temp = new float[9];
        //Load rotation matrix into R
        SensorManager.getRotationMatrix(temp, null, mGravity, mMagnetic);
        //Return the orientation values
        float[] values = new float[3];
        SensorManager.getOrientation(temp, values);
        //Log.d("updateDirection()", String.valueOf(values[0]));


        if(SWING_MODE) {
            direction_cnt++;
            SWING_MODE_cnt++;
            if(direction_cnt == 5) {
                Log.d("SWING", "values[0]: " + String.valueOf(values[0]));
                if(values[0] < 0) {
                    if(values[0] < SWING_minus_min)
                        SWING_minus_min = values[0];
                    if(values[0] > SWING_minus_max)
                        SWING_minus_max = values[0];
                }
                else {
                    if(values[0] < SWING_plus_min)
                        SWING_plus_min = values[0];
                    if(values[0] > SWING_plus_max)
                        SWING_plus_max = values[0];
                }

                if(SWING_minus_min < -3.0 && SWING_plus_max > 3.0) {
                    SWING_start_region = SWING_minus_max;
                    SWING_end_region = SWING_plus_min;
                }
                else if(SWING_minus_max == -10) {
                    SWING_start_region = SWING_plus_min;
                    SWING_end_region = SWING_plus_max;
                }
                else if(SWING_plus_max == -10) {
                    SWING_start_region = SWING_minus_min;
                    SWING_end_region = SWING_minus_max;
                }
                else {
                    SWING_start_region = SWING_minus_min;
                    SWING_end_region = SWING_plus_max;
                }


                if(SWING_MODE_cnt == 200) {
                    SWING_MODE = false;

                    LatLng from = new LatLng(latitude, longitude);
                    double r = 0.001;
                    LatLng to = new LatLng(latitude + r * Math.cos(SWING_start_region), longitude + r * Math.sin(SWING_start_region));
                    direction_line_opt.add(from, to).color(Color.argb(100, 255, 0, 0)).width(2);
                    mMap.addPolyline(direction_line_opt);
                    to = new LatLng(latitude + r * Math.cos(SWING_end_region), longitude + r * Math.sin(SWING_end_region));
                    direction_line_opt.add(from, to).color(Color.argb(100, 0, 255, 0)).width(6);
                    mMap.addPolyline(direction_line_opt);

                    Log.d("SWING", "[plus]max: " + String.valueOf(SWING_plus_max) + ", min: " + String.valueOf(SWING_plus_min));
                    Log.d("SWING", "[minus]max: " + String.valueOf(SWING_minus_max) + ", min: " + String.valueOf(SWING_minus_min));
                    Log.d("SWING", "start: " + String.valueOf(SWING_start_region) + ", end: " + String.valueOf(SWING_end_region));

                    Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    // Vibrate for 500 milliseconds
                    vibrator.vibrate(500);
                    Btn_Swing.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorAccent));
                    Btn_Swing.setText("Swing");
                    //Btn_Swing.setTextColor(Color.WHITE);
                    Btn_Swing.setClickable(true);

                }
                direction_cnt = 0;
            }
        }

        //valueView.setText(String.format("Azimuth: %1$1.2f, Pitch: %2$1.2f, Roll: %3$1.2f",
        //        values[0], values[1], values[2]));
    }
}
