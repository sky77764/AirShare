package com.example.jaeseok.airshare;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.jaeseok.airshare.api.FilesObject;
import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;

import java.io.File;
import java.util.List;

public class FilesActivity extends AppCompatActivity {

    private FloatingActionMenu ic_add;

    private FloatingActionButton add_file;
    private FloatingActionButton add_folder;

    private ApplicationClass applicationClass;
    private SwipeMenuListView mListView;
    private List<FilesObject> files_list;
    private ListViewAdapter mAdapter;
    private final int REQUEST_WRITE_STORAGE = 119;

    @Override
    protected void onResume() {
        super.onResume();
        new GetFileList().execute();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_files);

        applicationClass = (ApplicationClass)getApplicationContext();
        ic_add = (FloatingActionMenu)findViewById(R.id.ic_add);
        add_file = (FloatingActionButton)findViewById(R.id.add_file);
        add_folder = (FloatingActionButton)findViewById(R.id.add_folder);
        mListView = (SwipeMenuListView) findViewById(R.id.swipeMenuListView);
        ic_add.setClosedOnTouchOutside(true);

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_STORAGE);
        }

        SwipeMenuCreator creator = new SwipeMenuCreator() {
            @Override
            public void create(SwipeMenu menu) {
                SwipeMenuItem transmitItem = new SwipeMenuItem(getApplicationContext());
                transmitItem.setBackground(new ColorDrawable(Color.rgb(0x50, 0xCA, 0xD7)));
                transmitItem.setWidth(dp2px(70));
                transmitItem.setTitle("전송");
                transmitItem.setTitleSize(18);
                transmitItem.setTitleColor(Color.WHITE);
                menu.addMenuItem(transmitItem);

                SwipeMenuItem modifyItem = new SwipeMenuItem(getApplicationContext());
                modifyItem.setBackground(new ColorDrawable(Color.rgb(0xA2, 0xA1, 0xA1)));
                modifyItem.setWidth(dp2px(70));
                modifyItem.setTitle("다운로드");
                modifyItem.setTitleSize(18);
                modifyItem.setTitleColor(Color.WHITE);
                menu.addMenuItem(modifyItem);

                SwipeMenuItem deleteItem = new SwipeMenuItem(getApplicationContext());
                deleteItem.setBackground(new ColorDrawable(Color.rgb(0xFF, 0x25, 0x25)));
                deleteItem.setWidth(dp2px(70));
                deleteItem.setTitle("삭제");
                deleteItem.setTitleSize(18);
                deleteItem.setTitleColor(Color.WHITE);
                menu.addMenuItem(deleteItem);
            }
        };

        add_file.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ic_add.close(false);
                Intent intent = new Intent(FilesActivity.this, AddFileActivity.class);
                startActivity(intent);
            }
        });

        mListView.setMenuCreator(creator);
        mListView.setSwipeDirection(SwipeMenuListView.DIRECTION_LEFT);
        mListView.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                switch(index) {
                    case 0:
                        applicationClass.files_to_transmit = ((FilesObject)(mAdapter.getItem(position))).getName();
                        Intent intent = new Intent(FilesActivity.this, TransmitFileActivity.class);
                        startActivity(intent);
                        break;
                    case 1:
                        new DownloadFile(((FilesObject)(mAdapter.getItem(position))).getName()).execute();
                        break;
                    case 2:
                        mAdapter.remove(position);
                        break;
                }
                return false;
            }
        });
    }

    private int dp2px(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
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

        public void remove(int position) {
            new DeleteFile(((FilesObject)getItem(position))).execute();
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
                mAdapter = new ListViewAdapter(FilesActivity.this);
                mListView.setAdapter(mAdapter);
            } else {
                Toast.makeText(FilesActivity.this, "네트워크 연결에 실패하였습니다", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    public class DeleteFile extends AsyncTask<Void, Void, Boolean> {

        private FilesObject file;
        private int cnt_container = files_list.size();

        DeleteFile(FilesObject file) {
            this.file = file;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                applicationClass.client.deleteObject("root", file.getName());
                while(cnt_container == applicationClass.client.listObjects("root").size());
                return true;
            } catch(Exception e) {
                System.out.println(e.toString());
                return false;
            }
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            if(success) {
                files_list.remove(file);
                mAdapter.notifyDataSetChanged();
            } else {
                Toast.makeText(FilesActivity.this, "네트워크 연결에 실패하였습니다", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    public class DownloadFile extends AsyncTask<Void, Void, Boolean> {

        private String objectName;

        DownloadFile(String objectName){
            this.objectName = objectName;
        }

        private boolean downloadObject() {

            File directory = new File(Environment.getExternalStorageDirectory().toString() + "/Download");
            if (!directory.exists())
                directory.mkdirs();

            try {
                for(FilesObject object : files_list) {
                    if(object.getName().equals(objectName)) {
                        File outfile = new File(directory, object.getName());
                        object.writeObjectToFile(outfile);
                        return true;
                    }
                }
            } catch(Exception e) {
                System.out.println(e.toString());
            }
            return false;
        }


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Toast.makeText(FilesActivity.this, "다운로드를 시작합니다", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                return downloadObject();
            } catch(Exception e) {
                System.out.println(e.toString());
                return false;
            }
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            if(success) {
                Toast.makeText(FilesActivity.this, "다운로드를 완료하였습니다.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(FilesActivity.this, "네트워크 연결에 실패하였습니다", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
}