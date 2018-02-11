package com.example.rdas6313.litedownloader.backgroundDownload;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.IntDef;
import android.util.Log;

import com.example.litedownloaderapi.Interfaces.DownloadEventListener;
import com.example.litedownloaderapi.Interfaces.DownloadManager;
import com.example.litedownloaderapi.Manager;
import com.example.litedownloaderapi.Request;
import com.example.rdas6313.litedownloader.DownloadInformation;
import com.example.rdas6313.litedownloader.Utilities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Set;

public class BackgroundDownloaderService extends Service implements DownloadEventListener{

    private final String TAG = BackgroundDownloaderService.class.getName();
    private HashMap runningData;
    private int running;

    private final MyBinder binder = new MyBinder();
    private CallBackListener listener;
    private Manager manager;

    public void setListener(CallBackListener callBackListener){
        listener = callBackListener;
    }

    public ArrayList getRunningDownloads(){
        ArrayList<DownloadInformation>informations = new ArrayList<>();
        for(Object key : runningData.keySet()){
            informations.add((DownloadInformation) runningData.get(key));
        }
        return informations;
    }

    private void startDownload(String filename,String download_url,String saveUri){
        DownloadInformation information = new DownloadInformation(filename,0,0,0);
        information.setDownloadUrl(download_url);
        information.setSavePath(saveUri);
        Request request = new Request(filename,download_url,saveUri);
        int id = manager.push(request);
        information.setId(id);
        runningData.put(id,information);
        if(listener != null)
            listener.onAddDownload(id,filename,download_url,saveUri);

        Log.e(TAG,"Start Download Called");
    }


    public BackgroundDownloaderService() {}

    @Override
    public void onCreate() {
        super.onCreate();
        manager = Manager.getInstance(2);
        manager.bind(this);
        running = 0;
        runningData = new HashMap();
    }

    private void release(){
        manager.unbind();
        manager.release();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        release();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        release();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Bundle bundle = intent.getExtras();
        startDownload(bundle.getString(Utilities.DOWNLOAD_FILENAME),bundle.getString(Utilities.DOWNLOAD_URL),bundle.getString(Utilities.SAVE_DOWNLOAD_URI));
        return START_NOT_STICKY;
    }

    private void updateInformation(int id,int progress,long downloadedSize,long fileSize){
        DownloadInformation information = (DownloadInformation) runningData.get(id);
        information.setProgress(progress);
        information.setDownloadedSize(downloadedSize);
        information.setFileSize(fileSize);
    }

    @Override
    public void onProgress(int id, int progress, long downloadedSize, long fileSize) {
        if(listener != null)
            listener.onProgress(id,progress,downloadedSize,fileSize);
       // Log.e(TAG,"OnProgress id "+id+" "+progress);
        updateInformation(id,progress,downloadedSize,fileSize);
    }

    @Override
    public void onError(int id, int errorCode, String errorMsg) {
        if(listener != null)
            listener.onError(id,errorCode,errorMsg);
        if(runningData.containsKey(id)){
            runningData.remove(id);
        }
        Log.e(TAG,"OnError "+id+" "+errorCode+" "+errorMsg);
    }

    @Override
    public void onSuccess(Request request) {
        if(listener != null)
            listener.onSuccess(request);
        if(runningData.containsKey(request.getId())){
            updateInformation(request.getId(),100,request.getFileSize(),request.getFileSize());
            runningData.remove(request.getId());
        }
        Log.e(TAG,"OnSuccess "+request.getId());
    }

    public class MyBinder extends Binder{
        public BackgroundDownloaderService getService(){
            return BackgroundDownloaderService.this;
        }
    }
}
