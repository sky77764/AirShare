package com.example.jaeseok.airshare;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.example.jaeseok.airshare.api.FilesObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.util.List;

/**
 * Created by swimming on 2016. 9. 28..
 */
public class SendByMotionActivity extends AppCompatActivity {
    private ApplicationClass applicationClass;
    private SwipeMenuListView mListView;
    private List<FilesObject> files_list;
    private ListViewAdapter mAdapter;
    private final int REQUEST_WRITE_STORAGE = 119;
    private String filename;

    @Override
    protected void onResume() {
        super.onResume();
        new GetFileList().execute();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_motion);

        applicationClass = (ApplicationClass)getApplicationContext();
        mListView = (SwipeMenuListView) findViewById(R.id.swipeMenuListView);

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_STORAGE);
        }

        SwipeMenuCreator creator = new SwipeMenuCreator() {
            @Override
            public void create(SwipeMenu menu) {
                SwipeMenuItem oneItem = new SwipeMenuItem(getApplicationContext());
                oneItem.setBackground(new ColorDrawable(Color.rgb(0x50, 0xCA, 0xD7)));
                oneItem.setWidth(dp2px(70));
                oneItem.setTitle("한 명");
                oneItem.setTitleSize(18);
                oneItem.setTitleColor(Color.WHITE);
                menu.addMenuItem(oneItem);

                SwipeMenuItem multipleItem = new SwipeMenuItem(getApplicationContext());
                multipleItem.setBackground(new ColorDrawable(Color.rgb(0xA2, 0xA1, 0xA1)));
                multipleItem.setWidth(dp2px(70));
                multipleItem.setTitle("여러명");
                multipleItem.setTitleSize(18);
                multipleItem.setTitleColor(Color.WHITE);
                menu.addMenuItem(multipleItem);
            }
        };

        mListView.setMenuCreator(creator);
        mListView.setSwipeDirection(SwipeMenuListView.DIRECTION_LEFT);
        mListView.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                switch(index) {
                    case 0:
                        Intent intentMapsActivity = new Intent(SendByMotionActivity.this, MapsActivity.class);
                        intentMapsActivity.putExtra("BODY", ((FilesObject)(mAdapter.getItem(position))).getName());
                        intentMapsActivity.putExtra("MODE", "ONE");
                        intentMapsActivity.putExtra("TYPE", "FILE");
                        startActivityForResult(intentMapsActivity,0);
                        break;
                    case 1:
                        Intent intent = new Intent(SendByMotionActivity.this, MapsActivity.class);
                        intent.putExtra("BODY", ((FilesObject)(mAdapter.getItem(position))).getName());
                        intent.putExtra("MODE", "MULTIPLE");
                        intent.putExtra("TYPE", "FILE");
                        startActivityForResult(intent,0);
                        break;
                }
                return false;
            }
        });
    }
    
    private int dp2px(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
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

    private class ViewHolder {
        public ImageView mIcon;
        public TextView mFileName;
    }

    private class ListViewAdapter extends BaseAdapter {
        private Context mContext = null;

        public ListViewAdapter(Context mContext) {
            super();
            this.mContext = mContext;
        }

        @Override
        public int getCount() {
            return files_list.size();
        }

        @Override
        public Object getItem(int position) {
            return files_list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder();

                LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.file_item, null);

                holder.mIcon = (ImageView) convertView.findViewById(R.id.default_file);
                holder.mFileName = (TextView) convertView.findViewById(R.id.file_name);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            FilesObject mData = files_list.get(position);

            holder.mFileName.setText(mData.getName());

            return convertView;
        }
    }

    public class GetFileList extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                files_list = applicationClass.client.listObjects("root");
                return true;
            } catch(Exception e) {
                System.out.println(e.toString());
                return false;
            }
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            if(success) {
                mAdapter = new ListViewAdapter(SendByMotionActivity.this);
                mListView.setAdapter(mAdapter);
            } else {
                Toast.makeText(SendByMotionActivity.this, "네트워크 연결에 실패하였습니다", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
}
