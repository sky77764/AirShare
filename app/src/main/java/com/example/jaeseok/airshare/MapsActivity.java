package com.example.jaeseok.airshare;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Environment;
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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.chat.ChatManager;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
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
    float bearing;
    private View myContentsView;
    private ImageView mImageProfile;
    private SensorManager mSensorManager;
    private Sensor mAccelerometer, mField;
    private float[] mGravity;
    private float[] mMagnetic;
    Polyline direction_line;

    int direction_cnt = 0;
    boolean SWING_MODE = false;
    int SWING_MODE_cnt = 0;
    float SWING_minus_min = 10;
    float SWING_minus_max = -10;
    float SWING_plus_min = 10;
    float SWING_plus_max = -10;
    float SWING_start_region;
    float SWING_end_region;
    Vector<Receivers> Receiver = new Vector<Receivers>();

    Polyline firstPolyline, secondPolyline;
    boolean bPolylineCreated = false;
    static LatLng prev_to;
    final double motion_tuning = 0.7;
    private ApplicationClass applicationClass;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int screenWidth = (int) (metrics.widthPixels * 0.80);
        int screenHeight = (int) (metrics.heightPixels * 0.73);
        bPolylineCreated = false;

        applicationClass = (ApplicationClass)getApplicationContext();

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
        Btn_Swing.setEnabled(false);
        Btn_Swing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Btn_Swing.getText() != "Send") {
                    if(bPolylineCreated) {
                        firstPolyline.remove();
                        secondPolyline.remove();
                    }
                    prev_to = new LatLng(-10, -10);
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
                    Log.d("createChat", USERNAME_TO + "@" + DOMAIN);
                    try {
                        chat.sendMessage(BODY);
                        Log.d("createChat", BODY);

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

                            idx = mAdapter.findUsername(USERNAME_TO);
                            if(idx != 0) {
                                mAdapter.remove(idx);
                                mAdapter.addItem(getResources().getDrawable(R.drawable.ic_person),
                                        USERNAME_TO,
                                        BODY,
                                        cur_time);
                                Log.d("SENDMSG", "remove, " + idx);
                            }
                            else {
                                Log.d("SENDMSG", "update, " + idx);
                                mAdapter.mListData.get(idx).mBody = BODY;
                                mAdapter.mListData.get(idx).mDate = cur_time;
                            }

//                            mAdapter.mListData.get(mAdapter.findUsername(USERNAME_TO)).mBody = BODY;
//                            mAdapter.mListData.get(mAdapter.findUsername(USERNAME_TO)).mDate = cur_time;
                        }
                        mAdapter.dataChange();

                        Intent intentChatActivity = new Intent(MapsActivity.this, ChatActivity.class);
                        intentChatActivity.putExtra("Users_idx", findUsername(USERNAME_TO));
                        startActivityForResult(intentChatActivity, 0);

                    } catch (SmackException.NotConnectedException e) {
                        Log.d("SendMsg", e.toString());
                        Toast.makeText(getApplicationContext(), "Disconnected from the messaging server", Toast.LENGTH_SHORT).show();
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
        bPolylineCreated = false;
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
//        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.setInfoWindowAdapter(new MyInfoWindowAdapter());
        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener()
        {
            int i=0;
            @Override
            public void onInfoWindowClick(Marker arg0) {
                Log.d("onInfoWindowClick", "i: " + String.valueOf(i) + ", Receiver.size(): " + String.valueOf(Receiver.size()));
                if(i < Receiver.size()) {
                    arg0.hideInfoWindow();
                    arg0.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.marker));
                    i = (i+1) % Receiver.size();
                    prev_markers.elementAt(Receiver.elementAt(i).index).setIcon(BitmapDescriptorFactory.fromResource(R.drawable.marker_green));
                    new SetProfile(prev_markers.elementAt(Receiver.elementAt(i).index)).execute();
//                    prev_markers.elementAt(Receiver.elementAt(i).index).showInfoWindow();
                    USERNAME_TO = new String(prev_markers.elementAt(Receiver.elementAt(i).index).getTitle());

                }
            }

        });

        if(LocationService.latitude == -1 && LocationService.longitude == -1) {
            Toast.makeText(MapsActivity.this, "Enable GPS!", Toast.LENGTH_SHORT).show();

            if(LoginActivity.locService != null) {
                Intent i = new Intent();
                i.setComponent(LoginActivity.locService);
                stopService(i);
            }
            Intent intent = new Intent(MapsActivity.this, LocationService.class);
            intent.putExtra("DOMAIN", LoginActivity.DOMAIN);
            intent.putExtra("USERNAME", LoginActivity.USERNAME);
            LoginActivity.locService = startService(intent);
            finish();
        }

        else {
            final int delay = 1000; //milliseconds
            final Context temp = this;
            h.postDelayed(new Runnable() {
                Circle circle;
                boolean first_time = true;
                int loading_cnt = 0;

                public void run() {
                    if (ActivityCompat.checkSelfPermission(temp, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(temp, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(MapsActivity.this, "GPS Permission denied!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    latitude = LocationService.latitude;
                    longitude = LocationService.longitude;
//                    bearing = LocationService.bearing;
                    Log.d("onLocationChanged1", "latitude=" + latitude + ", longitude=" + longitude + ", bearing=" + bearing);


                    myPosition = new LatLng(latitude, longitude);

                    if(first_time) {
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(myPosition));
                        mMap.moveCamera(CameraUpdateFactory.zoomTo(16));
                        first_time = false;
                    }
                    if(!SWING_MODE && loading_cnt > 3) {
                        Btn_Swing.setEnabled(true);
                        CameraPosition cameraPosition = new CameraPosition.Builder()
                                .target(new LatLng(latitude, longitude))             // Sets the center of the map to current location
                                .zoom(17)                   // Sets the zoom
                                .bearing(bearing) // Sets the orientation of the camera to east
                                .tilt(55)                   // Sets the tilt of the camera to 0 degrees
                                .build();                   // Creates a CameraPosition from the builder
                        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                    }

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


                    loading_cnt++;
                    h.postDelayed(this, delay);
                }
            }, delay);


            mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(final Marker marker) {
                    for(int i=0; i<prev_markers.size(); i++)
                        prev_markers.elementAt(i).setIcon(BitmapDescriptorFactory.fromResource(R.drawable.marker));

                    marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.marker_green));
                    new SetProfile(marker).execute();
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
        bearing = values[0] < 0 ? (float)rad2deg(values[0] + 2*(float)Math.PI) : (float)rad2deg(values[0]);


        if(SWING_MODE) {
//            direction_cnt++;
            SWING_MODE_cnt++;

            LatLng from_temp = new LatLng(latitude, longitude);
            double r_temp = 0.001;
            LatLng to_temp = new LatLng(latitude + r_temp * Math.cos(values[0]), longitude + r_temp * Math.sin(values[0]));
            boolean isIgnored = false;

            PolylineOptions direction_line_opt_temp = new PolylineOptions();
            if(prev_to.latitude != -10) {
                double prev_direction_degree = calculateDegree(from_temp, prev_to);
                double cur_direction_degree = calculateDegree(from_temp, to_temp);
                double prev_cur_angle = Math.abs(prev_direction_degree - cur_direction_degree);

//                Log.d("TuningTest", "[prev] "+ prev_to.latitude + " / " + prev_to.longitude + " / " + prev_direction_degree);
//                Log.d("TuningTest", "[cur] "+ to_temp.latitude + " / " + to_temp.longitude + " / " + cur_direction_degree);

                if(prev_cur_angle > motion_tuning && prev_cur_angle < 2*Math.PI - motion_tuning) {
                    direction_line_opt_temp.add(from_temp, to_temp).color(Color.argb(100, 0, 255, 255)).width(3);
                    mMap.addPolyline(direction_line_opt_temp);
                    isIgnored = true;
                }

            }

            if(!isIgnored) {
                direction_line_opt_temp.add(from_temp, to_temp).color(Color.argb(100, 0, 255, 0)).width(3);
                mMap.addPolyline(direction_line_opt_temp);
                prev_to = to_temp;

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
            }

            if(SWING_MODE_cnt >= 200) {
                if(SWING_minus_min < -2.8 && SWING_plus_max > 2.8) {
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

                Log.d("SWING_MODE", "[minus] min=" + SWING_minus_min + ", max=" + SWING_minus_max);
                Log.d("SWING_MODE", "[plus] min=" + SWING_plus_min + ", max=" + SWING_plus_max);
                Log.d("SWING_MODE", "start: " + SWING_start_region + ", end: " + SWING_end_region);

                SWING_MODE = false;

                LatLng from = new LatLng(latitude, longitude);
                double r = 0.001;
                LatLng to = new LatLng(latitude + r * Math.cos(SWING_start_region), longitude + r * Math.sin(SWING_start_region));
                PolylineOptions direction_line_opt = new PolylineOptions();
                direction_line_opt.add(from, to).color(Color.argb(100, 255, 0, 0)).width(6);
                firstPolyline = mMap.addPolyline(direction_line_opt);

                to = new LatLng(latitude + r * Math.cos(SWING_end_region), longitude + r * Math.sin(SWING_end_region));
                PolylineOptions direction_line_opt2 = new PolylineOptions();
                direction_line_opt2.add(from, to).color(Color.argb(100, 0, 0, 255)).width(6);
                secondPolyline = mMap.addPolyline(direction_line_opt2);
                bPolylineCreated = true;

//                    Toast.makeText(getApplicationContext(), "start=" + SWING_start_region + ", end=" + SWING_end_region, Toast.LENGTH_LONG).show();


//                    Log.d("SWING", "[plus]max: " + String.valueOf(SWING_plus_max) + ", min: " + String.valueOf(SWING_plus_min));
//                    Log.d("SWING", "[minus]max: " + String.valueOf(SWING_minus_max) + ", min: " + String.valueOf(SWING_minus_min));
//                    Log.d("SWING", "start: " + String.valueOf(SWING_start_region) + ", end: " + String.valueOf(SWING_end_region));

                Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                // Vibrate for 500 milliseconds
                vibrator.vibrate(500);
                Btn_Swing.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorAccent));
                Btn_Swing.setText("Swing");
                //Btn_Swing.setTextColor(Color.WHITE);
                Btn_Swing.setClickable(true);

                calculateReceiver(from, SWING_start_region, SWING_end_region);
            }
        }
    }

    private void calculateReceiver(LatLng userLocation, double start_region, double end_region){
        if(prev_markers == null)
            return ;

//        Vector<Receivers> Receiver = new Vector<Receivers>();
//        if(Receiver != null)
//            Receiver.clear();

        for (int i=0; i < prev_markers.size(); i++) {
            Receivers temp = new Receivers();
//            Log.d("fillData", "[" + prev_markers.elementAt(i).getTitle() + "]");
            temp.fillData(i, prev_markers.elementAt(i).getPosition(), userLocation, start_region, end_region);
            if(temp.isIncluded) {
//                prev_markers.elementAt(i).setIcon(BitmapDescriptorFactory.fromResource(R.drawable.marker_green));
                Receiver.addElement(temp);
            }
        }

        if(Receiver.size() > 0) {
            Collections.sort(Receiver);
            prev_markers.elementAt(Receiver.elementAt(0).index).setIcon(BitmapDescriptorFactory.fromResource(R.drawable.marker_green));
            new SetProfile(prev_markers.elementAt(Receiver.elementAt(0).index)).execute();
//            prev_markers.elementAt(Receiver.elementAt(0).index).showInfoWindow();
            Btn_Swing.setText("Send");
        }
    }

    public Bitmap getCircleBitmap(Bitmap bitmap) {
        final int length = bitmap.getWidth() < bitmap.getHeight() ? bitmap.getWidth() : bitmap.getHeight();
        Bitmap output = Bitmap.createBitmap(length, length, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, length, length);
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        int size = length / 2;
        canvas.drawCircle(size, size, size, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }

    class MyInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

        MyInfoWindowAdapter(){
            myContentsView = getLayoutInflater().inflate(R.layout.custom_infowindow, null);
            mImageProfile = (ImageView)myContentsView.findViewById(R.id.profile);
        }

        @Override
        public View getInfoContents(Marker marker) {
            TextView tvTitle = ((TextView)myContentsView.findViewById(R.id.title));
            tvTitle.setText(marker.getTitle());
            Log.d("getInfoContents", marker.getTitle());
            TextView tvSnippet = ((TextView)myContentsView.findViewById(R.id.snippet));
            tvSnippet.setText(marker.getSnippet());
            tvSnippet.setText("Snippet");

            return myContentsView;
        }

        @Override
        public View getInfoWindow(Marker marker) {
            // TODO Auto-generated method stub
            return null;
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//        Log.d("TEST", "MapsActivity-onActivityResult " + resultCode);
        switch (resultCode) {
            case 100:
                setResult(100);
                finish();
                break;

            default:
                break;
        }
    }

    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }
    private double rad2deg(double rad) {
        return (rad * 180 / Math.PI);
    }

    private double calculateDegree(LatLng userLocation, LatLng markerLocation) {
        // 가로 longitude 오른쪽이 큼, 세로 latitude 위쪽이 큼
        double dLon = (markerLocation.longitude - userLocation.longitude);

        double y = Math.sin(dLon) * Math.cos(markerLocation.latitude);
        double x = Math.cos(userLocation.latitude) * Math.sin(markerLocation.latitude) - Math.sin(userLocation.latitude)
                * Math.cos(markerLocation.latitude) * Math.cos(dLon);

        double brng = Math.atan2(y, x);

        brng = Math.toDegrees(brng);
        brng = (brng + 360) % 360;
        brng = 360 - brng;
        brng = deg2rad(brng);
        if(brng > Math.PI) {
            brng = brng - 2*Math.PI;
        }
        return brng;
    }

    public class SetProfile extends AsyncTask<Void, Void, Boolean> {
        private String id;
        private Bitmap profile;
        private Marker marker;

        public SetProfile(Marker marker){
            id = marker.getTitle();
            this.marker = marker;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                URL url = new URL("http://52.6.95.227:8080/download.jsp?id=" + id);
                HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();

                if(input != null) {
                    profile = BitmapFactory.decodeStream(input);
                    applicationClass.addBitmapToMemoryCache(id, profile);
                    input.close();
                    return true;
                } else
                    return false;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            if(success && applicationClass.getBitmapFromMemCache(id) != null)
                mImageProfile.setImageBitmap(getCircleBitmap(applicationClass.getBitmapFromMemCache(id)));
            else
                mImageProfile.setImageResource(R.drawable.default_profile);
            marker.showInfoWindow();
        }
    }
}


