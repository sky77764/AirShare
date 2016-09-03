package com.example.jaeseok.airshare;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Created by swimming on 2016. 7. 12..
 */
public class TransmitFileActivity extends AppCompatActivity {

    private ArrayList<String> mUserList;
    private ListViewAdapter mAdapter;
    private ApplicationClass applicationClass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transmit_file);
        ListView mListView = (ListView) findViewById(R.id.list_view);
        mAdapter = new ListViewAdapter();
        mUserList = new ArrayList<String>();
        applicationClass = (ApplicationClass)getApplicationContext();

        mListView.setAdapter(mAdapter);

        for(int i = 0; i < 40; i++)
            if(!applicationClass.client.getUserName().split(":")[0].equals("test"+(i+1)))
                mUserList.add("test"+(i+1));

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                new TransmitFile((String)mAdapter.getItem(position)).execute();
            }
        });

    }

    private class ViewHolder {
        public ImageView mIcon;
        public TextView mUserName;
    }

    private class ListViewAdapter extends BaseAdapter {
        private LayoutInflater mInflator;

        public ListViewAdapter() {
            mInflator = getLayoutInflater();
        }

        @Override
        public int getCount() {
            return mUserList.size();
        }

        @Override
        public Object getItem(int position) {
            return mUserList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            ViewHolder holder;
            // General ListView optimization code.
            if (view == null) {
                view = mInflator.inflate(R.layout.user_item, null);
                holder = new ViewHolder();
                holder.mIcon = (ImageView) view.findViewById(R.id.default_profile);
                holder.mUserName = (TextView) view.findViewById(R.id.user_name);
                view.setTag(holder);
            } else {
                holder = (ViewHolder) view.getTag();
            }

            holder.mUserName.setText(mUserList.get(position));
            return view;
        }
    }

    public class TransmitFile extends AsyncTask<Void, Void, Boolean> {

        private String filename;

        public TransmitFile(String filename) {
            this.filename = filename;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                String serverIP = "52.6.95.227";
                String msg = applicationClass.client.getUserName().split(":")[0] + ":" + filename + ":" + applicationClass.files_to_transmit;
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
                Toast.makeText(TransmitFileActivity.this, "파일 전송을 완료하였습니다", Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(TransmitFileActivity.this, "파일 전송에 실패하였습니다", Toast.LENGTH_SHORT).show();
        }
    }
}

