package com.example.jaeseok.airshare;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.File;

@SuppressLint("SdCardPath")
public class AddFileActivity extends AppCompatActivity {

    private String File_path = Environment.getExternalStorageDirectory().toString() + "/";
    private FileList _FileList;
    private ApplicationClass applicationClass;
    private LinearLayout upload_progress;
    private static final int REQUEST_WRITE_STORAGE = 1;

    private boolean isSDCARDMOUNTED() {
        String status = Environment.getExternalStorageState();
        if (status.equals(Environment.MEDIA_MOUNTED))
            return true;

        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_file);
        applicationClass = (ApplicationClass)getApplicationContext();
        upload_progress = (LinearLayout)findViewById(R.id.upload_progress);

        if(isSDCARDMOUNTED()) {
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_STORAGE);
            }
        }

        _FileList = new FileList(this, getLayoutInflater());

        _FileList.setOnPathChangedListener(new OnPathChangedListener() {
            @Override
            public void onChanged(Boolean success) {
                if(!success)
                    Toast.makeText(AddFileActivity.this, "해당 폴더를 읽을 권한이 없습니다", Toast.LENGTH_SHORT).show();
            }
        });

        _FileList.setOnFileSelected(new OnFileSelectedListener() {
            @Override
            public void onSelected(final String path, final String fileName) {
                new Popup(AddFileActivity.this)
                        .setCancelClickListener(new Popup.OnSweetClickListener() {
                            @Override
                            public void onClick(Popup pop_up) {
                                pop_up.dismissWithAnimation();
                            }
                        })
                        .setConfirmClickListener(new Popup.OnSweetClickListener() {
                            @Override
                            public void onClick(Popup pop_up) {
                                pop_up.dismissWithAnimation();
                                new UploadFile(new File(path + fileName + "/")).execute();
                            }
                        })
                        .show();
            }
        });

        LinearLayout layout = (LinearLayout)findViewById(R.id.add_file_list);
        layout.addView(_FileList);

        _FileList.setPath(File_path);
        _FileList.setFocusable(true);
        _FileList.setFocusableInTouchMode(true);
    }

    public class UploadFile extends AsyncTask<Void, Void, Boolean> {
        private File file;
        private int cnt_container;

        UploadFile(File file) {
            this.file = file;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            _FileList.setVisibility(View.GONE);
            upload_progress.setVisibility(View.VISIBLE);
            try {
                cnt_container = applicationClass.client.listObjects("root").size();
            } catch(Exception e) {
                System.out.println(e.toString());
            }
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                applicationClass.client.storeObject("root", file, null);
                return true;
            } catch(Exception e) {
                System.out.println(e.toString());
                return false;
            }
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            if (success) {
                Toast.makeText(AddFileActivity.this, "Upload Success", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(AddFileActivity.this, "Upload Failed", Toast.LENGTH_SHORT).show();
                _FileList.setVisibility(View.VISIBLE);
                upload_progress.setVisibility(View.GONE);
            }
        }
    }
}