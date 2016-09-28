package com.example.jaeseok.airshare;

import android.app.Activity;
import android.content.Intent;
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
    Button Btn_Send, Btn_Send_Multiple;
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
        Btn_Send_Multiple = (Button) findViewById(R.id.btn_send_multiple);
        //Text_To = (EditText) findViewById(R.id.text_to);
        Text_Body = (EditText) findViewById(R.id.text_body);

        Btn_Send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String BODY = Text_Body.getText().toString();

                if (BODY.length() == 0) {
                    Toast.makeText(getApplicationContext(), "Fill message", Toast.LENGTH_SHORT).show();
                    return;
                }

                Intent intentMapsActivity = new Intent(SendActivity.this, MapsActivity.class);
                intentMapsActivity.putExtra("BODY", BODY);
                intentMapsActivity.putExtra("MODE", "ONE");
                intentMapsActivity.putExtra("TYPE", "MESSAGE");
                startActivityForResult(intentMapsActivity,0);
            }
        });
        Btn_Send_Multiple.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String BODY = Text_Body.getText().toString();

                if (BODY.length() == 0) {
                    Toast.makeText(getApplicationContext(), "Fill message", Toast.LENGTH_SHORT).show();
                    return;
                }

                Intent intentMapsActivity = new Intent(SendActivity.this, MapsActivity.class);
                intentMapsActivity.putExtra("BODY", BODY);
                intentMapsActivity.putExtra("MODE", "MULTIPLE");
                intentMapsActivity.putExtra("TYPE", "MESSAGE");
                startActivityForResult(intentMapsActivity,0);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("TEST", "SendActivity-onActivityResult " + resultCode);
        switch (resultCode) {
            case 100:
                finish();
                break;

            default:
                break;
        }
    }



}
