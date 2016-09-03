package com.example.jaeseok.airshare;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
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
 * Created by swimming on 2016. 8. 4..
 */
public class SendFileActivity extends AppCompatActivity {
    private ApplicationClass applicationClass;
    private ListView mListView;
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
        setContentView(R.layout.activity_send_files);

        applicationClass = (ApplicationClass)getApplicationContext();
        mListView = (ListView) findViewById(R.id.swipeMenuListView);

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_STORAGE);
        }

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                filename = ((FilesObject)(mAdapter.getItem(position))).getName();
                new TransmitFile(getIntent().getStringExtra("to")).execute();
            }
        });
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
                mAdapter = new ListViewAdapter(SendFileActivity.this);
                mListView.setAdapter(mAdapter);
            } else {
                Toast.makeText(SendFileActivity.this, "네트워크 연결에 실패하였습니다", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    public class TransmitFile extends AsyncTask<Void, Void, Boolean> {

        private String to;

        public TransmitFile(String to) {
            this.to = to;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                String serverIP = "52.6.95.227";
                String msg = applicationClass.client.getUserName().split(":")[0] + ":" + to + ":" + filename;
                System.out.println("서버에 연결중입니다. 서버 IP : " + serverIP);

                Socket socket = new Socket(serverIP, 8020);

                OutputStream out = socket.getOutputStream();
                DataOutputStream dos = new DataOutputStream(out);

                dos.writeUTF(msg);

                InputStream in = socket.getInputStream();
                DataInputStream dis = new DataInputStream(in);

                System.out.println("서버로부터 받은 메세지 : " + dis.readUTF());
                System.out.println("연결을 종료합니다.");

                dis.close();
                dos.close();
                socket.close();

                return true;
            } catch (ConnectException ce) {
                ce.printStackTrace();
                return false;
            } catch (IOException ie) {
                ie.printStackTrace();
                return false;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            if(success)
                Toast.makeText(SendFileActivity.this, "파일 전송을 완료하였습니다", Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(SendFileActivity.this, "파일 전송에 실패하였습니다", Toast.LENGTH_SHORT).show();
            setResult(SendFileActivity.RESULT_OK, new Intent().putExtra("filename", filename));
            finish();
        }
    }
}
