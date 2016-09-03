package com.example.jaeseok.airshare;

import android.app.Application;
import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

import com.example.jaeseok.airshare.api.FilesClientInterface;
import com.example.jaeseok.airshare.api.FilesContainer;

import java.util.List;

public class ApplicationClass extends Application {
    public FilesClientInterface client;
    public String files_to_transmit;
    public LruCache<String, Bitmap> mMemoryCache;
    public LruCache<String, Bitmap> mTempCache;

    public void cacheInit() {
        final int maxMemory = (int)(Runtime.getRuntime().maxMemory() / 1024);

        final int cacheSize = maxMemory / 8;

        mMemoryCache = new LruCache<String, Bitmap>( cacheSize){
            @Override
            protected int sizeOf( String key, Bitmap bitmap){
                return bitmap.getByteCount() / 1024;
            }
        };

        mTempCache = new LruCache<String, Bitmap>( cacheSize){
            @Override
            protected int sizeOf( String key, Bitmap bitmap){
                return bitmap.getByteCount() / 1024;
            }
        };
    }

    public void getReady() {
        try {
            List<FilesContainer> containers = client.listContainers();
            if(containers.isEmpty())
                client.createContainer("root");
        } catch(Exception e) {
            System.out.println(e.toString());
        }
    }

    public void addBitmapToMemoryCache(String key, Bitmap bitmap){
        if(bitmap != null) {
            if (getBitmapFromMemCache(key) != null) {
                mMemoryCache.remove(key);
            }
            mMemoryCache.put(key, bitmap);
        }
    }

    public Bitmap getBitmapFromMemCache(String key){
        return mMemoryCache.get(key);
    }

    public void addBitmapToTempCache(String key, Bitmap bitmap){
        if(bitmap != null) {
            if (getBitmapFromTempCache(key) != null) {
                mTempCache.remove(key);
            }
            mTempCache.put(key, bitmap);
        }
    }

    public Bitmap getBitmapFromTempCache(String key){
        return mTempCache.get(key);
    }
}
