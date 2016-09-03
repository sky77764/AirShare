package com.example.jaeseok.airshare;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.LruCache;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.entity.mime.content.FileBody;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.Socket;

/**
 * Created by swimming on 2016. 8. 30..
 */
public class ProfileActivity extends AppCompatActivity {

    private ImageButton register_image;
    private ImageButton submit_button;
    private final int REQ_CODE_PICK_IMAGE = 0;
    private final int REQUEST_WRITE_STORAGE = 1;
    private String filePath;
    private ApplicationClass applicationClass;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        applicationClass = (ApplicationClass)getApplicationContext();

        ImageButton back_button = (ImageButton)findViewById(R.id.back_button);
        back_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(SendFileActivity.RESULT_CANCELED);
                finish();
            }
        });

        register_image = (ImageButton) findViewById(R.id.default_user_image);
        register_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType(android.provider.MediaStore.Images.Media.CONTENT_TYPE);
                intent.setData(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, REQ_CODE_PICK_IMAGE);
            }
        });

        submit_button = (ImageButton) findViewById(R.id.submit_button);
        submit_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new RegisterProfile().execute();
            }
        });

        Bitmap profile = applicationClass.getBitmapFromMemCache(LoginActivity.getUSERNAME());
        if(profile != null){
            register_image.setImageBitmap(getCircleBitmap(profile));
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == REQ_CODE_PICK_IMAGE){
            try {
                Bitmap selectedImage = MediaStore.Images.Media.getBitmap(getContentResolver(), data.getData());
                applicationClass.addBitmapToTempCache(LoginActivity.getUSERNAME(), selectedImage);
                submit_button.setVisibility(View.VISIBLE);
                register_image.setImageBitmap(getCircleBitmap(selectedImage));
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private boolean isSDCARDMOUNTED() {
        String status = Environment.getExternalStorageState();
        if (status.equals(Environment.MEDIA_MOUNTED))
            return true;

        return false;
    }

    public class RegisterProfile extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                if (isSDCARDMOUNTED()) {
                    if (ContextCompat.checkSelfPermission(ProfileActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                        ActivityCompat.requestPermissions(ProfileActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_STORAGE);
                    }

                    File directory = new File(Environment.getExternalStorageDirectory().toString() + "/AirShare");
                    if (!directory.exists())
                        directory.mkdirs();
                    File profile = new File(directory, "user_" + LoginActivity.getUSERNAME() + ".jpg");
                    profile.createNewFile();
                    OutputStream out = new FileOutputStream(profile);
                    applicationClass.getBitmapFromTempCache(LoginActivity.getUSERNAME()).compress(Bitmap.CompressFormat.JPEG, 100, out);
                    out.close();

                    HttpClient client = new DefaultHttpClient();
                    String url = "http://52.6.95.227:8080/profile.jsp?id=" + LoginActivity.getUSERNAME();
                    HttpPost post = new HttpPost(url);
                    FileBody bin = new FileBody(profile);
                    MultipartEntity multipart = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
                    multipart.addPart("images", bin);

                    post.setEntity(multipart);
                    client.execute(post);
                    profile.delete();
                    directory.delete();
                    return true;
                } else {
                    return false;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            if(success) {
                setResult(SendFileActivity.RESULT_OK);
                applicationClass.addBitmapToMemoryCache(LoginActivity.getUSERNAME(), applicationClass.getBitmapFromTempCache(LoginActivity.getUSERNAME()));
            }
            else {
                setResult(SendFileActivity.RESULT_CANCELED);
                Toast.makeText(ProfileActivity.this, "프로필 등록에 실패하였습니다", Toast.LENGTH_SHORT).show();
            }
            finish();
        }
    }
}
