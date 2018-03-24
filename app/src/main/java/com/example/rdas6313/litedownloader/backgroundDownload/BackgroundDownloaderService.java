package com.example.rdas6313.litedownloader.backgroundDownload;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.example.litedownloaderapi.DownloadCode;
import com.example.litedownloaderapi.Interfaces.DownloadEventListener;
import com.example.litedownloaderapi.Manager;
import com.example.litedownloaderapi.Request;
import com.example.rdas6313.litedownloader.DownloadInformation;
import com.example.rdas6313.litedownloader.NotificationUtils;
import com.example.rdas6313.litedownloader.R;
import com.example.rdas6313.litedownloader.Utilities;
import com.example.rdas6313.litedownloader.data.DownloaderContract;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class BackgroundDownloaderService extends Service implements DownloadEventListener{

    private final String TAG = BackgroundDownloaderService.class.getName();
    private HashMap runningData;

    private final MyBinder binder = new MyBinder();
    private CallBackListener runninglistener;
    private Manager manager;

    private final int NOTIFICATION_ICON = R.mipmap.ic_launcher_round;
    private final static int MAX = 100;
    private static NotificationCompat.Builder nlist[] = new NotificationCompat.Builder[MAX];

    public void setRunninglistener(CallBackListener listener){
        runninglistener = listener;
    }


    public ArrayList getRunningDownloads(){
        ArrayList<DownloadInformation>informations = new ArrayList<>();
        for(Object key : runningData.keySet()){
            informations.add((DownloadInformation) runningData.get(key));
        }
        return informations;
    }


    public boolean pauseDownload(int id){
        if(manager != null) {
            boolean isPaused = manager.pause(id);
            if(isPaused){
                PauseAndErrorMethod(id,DownloadCode.DOWNLOAD_INTERRUPT_ERROR,"Paused Download");
                Log.e(TAG,"Paused Download "+id);
            }
            return isPaused;
        }
        return false;
    }

    public int startDownload(String filename,String download_url,String saveUri,long filesize,long downloadedSize,boolean isFirstDownload){
        int progress = 0;
        boolean shouldStartForeground = false;
        if(filesize>0)
            progress = (int)((downloadedSize*100)/filesize);
        DownloadInformation information = new DownloadInformation(filename,progress,filesize,downloadedSize);
        information.setDownloadUrl(download_url);
        information.setDownloadStatus(DownloadInformation.RESUME_DOWNLOAD);
        information.setSavePath(saveUri);
        Request request = new Request(filename,download_url,saveUri);
        int id = manager.push(request);
        information.setId(id);
        if(runningData != null && runningData.isEmpty())
            shouldStartForeground = true;
        runningData.put(id,information);
        if(runninglistener != null)
            runninglistener.onAddDownload(id,filename,download_url,saveUri,filesize,downloadedSize);
        NotificationCompat.Builder builder = NotificationUtils.makeNotification(id,getApplicationContext(),filename,"Download in progress",NOTIFICATION_ICON);
        nlist[id] = builder;
        if(shouldStartForeground){
            startForeground(id,builder.build());
        }
        Utilities.changeServiceRunningValue(true,getApplication());
        return id;
    }


    public void removeRunningDownload(int id){
        if(runningData != null)
            runningData.remove(id);
        if(manager != null)
            manager.cancel(id);
    }

    private void isThereAnyRunningDownload(String content,int id){
        if(nlist[id] != null) {
            NotificationUtils.changeContent(content,nlist[id],id);
            nlist[id] = null;
        }
        if(runningData != null && runningData.isEmpty()) {
            stopForeground(false);
            Utilities.changeServiceRunningValue(false,getApplication());
            if(!Utilities.isActivityAlive(getApplication())){
                stopSelf();
            }
        }
    }

    public BackgroundDownloaderService() {}

    @Override
    public void onCreate() {
        super.onCreate();
        manager = Manager.getInstance(Runtime.getRuntime().availableProcessors());
        manager.bind(this);
        runningData = new HashMap();
        for(int i=0;i<MAX;i++){
            nlist[i] = null;
        }
        NotificationUtils.initNotificationManager(getApplicationContext());
    }

    private void suddenPauseDownload(){
        if(runningData != null && !runningData.isEmpty()){
            for(Object key:runningData.keySet()){
                DownloadInformation information = (DownloadInformation) runningData.get(key);
                pauseDownload(information.getId());
            }
        }
    }

    private void release(){
        suddenPauseDownload();
        clearDownloadsData();
        if(manager != null) {
            manager.unbind();
            manager.release();
        }
        Log.e(TAG,"OnDestroy Called");
    }

    private void clearDownloadsData(){
        if(runningData != null)
            runningData.clear();
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
        if(bundle != null) {
            int id = startDownload(bundle.getString(Utilities.DOWNLOAD_FILENAME), bundle.getString(Utilities.DOWNLOAD_URL), bundle.getString(Utilities.SAVE_DOWNLOAD_URI), bundle.getLong(Utilities.DOWNLOAD_FILE_SIZE), bundle.getLong(Utilities.DOWNLOAD_DOWNLOADED_SIZE),false);
        }
        return START_NOT_STICKY;
    }

    private void updateInformation(int id,int progress,long downloadedSize,long fileSize){
        DownloadInformation information = (DownloadInformation) runningData.get(id);
        if(information == null)
            return;
        information.setProgress(progress);
        information.setDownloadedSize(downloadedSize);
        information.setFileSize(fileSize);
    }

    @Override
    public void onProgress(int id, int progress, long downloadedSize, long fileSize) {
        updateInformation(id,progress,downloadedSize,fileSize);
        if(runninglistener != null)
            runninglistener.onProgress(id,progress,downloadedSize,fileSize);
        if(nlist[id] != null){
            NotificationUtils.changeProgress(progress,nlist[id],id);
        }
    }

    @Override
    public void onError(int id, int errorCode, String errorMsg) {
        if(errorCode != DownloadCode.DOWNLOAD_INTERRUPT_ERROR){
            PauseAndErrorMethod(id,errorCode,errorMsg);
        }

    }


    private void uploadDataToSuccessDb(DownloadInformation information){
        ContentValues value = new ContentValues();
        value.put(DownloaderContract.Success.TITLE,information.getTitle());
        value.put(DownloaderContract.Success.DOWNLOAD_URL,information.getDownloadUrl());
        value.put(DownloaderContract.Success.SAVE_URI,information.getSavePath());
        value.put(DownloaderContract.Success.FILESIZE,information.getFileSize());
        getContentResolver().insert(DownloaderContract.Success.CONTENT_URI,value);
    }

    @Override
    public void onSuccess(Request request) {
        String content = "";
        if(runninglistener != null)
            runninglistener.onSuccess(request);
        if(runningData.containsKey(request.getId())){
            updateInformation(request.getId(),100,request.getFileSize(),request.getFileSize());
            DownloadInformation information = (DownloadInformation)runningData.get(request.getId());
            information.setDownloadStatus(DownloadInformation.SUCCESS_DOWNLOAD);
            uploadDataToSuccessDb(information);
            runningData.remove(request.getId());
            if(nlist[request.getId()] != null){
                content = getString(R.string.successfullNotification);
            }
        }

        isThereAnyRunningDownload(content,request.getId());
        Log.e(TAG,"OnSuccess "+request.getId());
    }

    private void uploadDataToPauseErrorDb(DownloadInformation information){
        ContentValues value = new ContentValues();
        value.put(DownloaderContract.PausedError.TITLE,information.getTitle());
        value.put(DownloaderContract.PausedError.DOWNLOAD_URL,information.getDownloadUrl());
        value.put(DownloaderContract.PausedError.SAVE_URI,information.getSavePath());
        value.put(DownloaderContract.PausedError.FILESIZE,information.getFileSize());
        value.put(DownloaderContract.PausedError.DOWNLOADED_SiZE,information.getDownloadedSize());
        value.put(DownloaderContract.PausedError.LAST_DOWNLOAD_STATUS,information.getDownloadStatus());
        getContentResolver().insert(DownloaderContract.PausedError.CONTENT_URI,value);
    }

    private void PauseAndErrorMethod(int id,int errorCode,String errorMsg){
        String content = "";
        if(runningData.containsKey(id)){
            if(runninglistener != null)
                runninglistener.onError(id,errorCode,errorMsg,null);

            DownloadInformation information = (DownloadInformation) runningData.get(id);
            runningData.remove(id);
            if(information != null) {
                if(errorCode == DownloadCode.DOWNLOAD_INTERRUPT_ERROR) {
                    information.setDownloadStatus(DownloadInformation.PAUSE_DOWNLOAD);
                    content = getString(R.string.pauseNotification)+" "+information.getProgress()+"%";
                }
                else {
                    information.setDownloadStatus(DownloadInformation.CANCEL_DOWNLOAD);
                    content = getString(R.string.errorNotification);
                }//Todo:- Handel if there is other kind of Error happen in here like URL ERROR,FILE NOT FOUND ERROR

                uploadDataToPauseErrorDb(information);

            }


        }

        isThereAnyRunningDownload(content,id);
    }



    public class MyBinder extends Binder{
        public BackgroundDownloaderService getService(){
            return BackgroundDownloaderService.this;
        }
    }

}
