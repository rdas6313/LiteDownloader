package com.example.rdas6313.litedownloader.backgroundDownload;

import android.app.Service;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.example.litedownloaderapi.DownloadCode;
import com.example.litedownloaderapi.Interfaces.DownloadEventListener;
import com.example.litedownloaderapi.Manager;
import com.example.litedownloaderapi.Request;
import com.example.rdas6313.litedownloader.DownloadInformation;
import com.example.rdas6313.litedownloader.R;
import com.example.rdas6313.litedownloader.Utilities;
import com.example.rdas6313.litedownloader.data.DownloaderContract;

import java.util.ArrayList;
import java.util.HashMap;

public class BackgroundDownloaderService extends Service implements DownloadEventListener{

    private final String TAG = BackgroundDownloaderService.class.getName();
    private HashMap runningData;
    private HashMap pauseErrorData;

    private final MyBinder binder = new MyBinder();
    private CallBackListener runninglistener;
    private CallBackListener pauseErrorlistener;
    private Manager manager;

    private ArrayList successDownloadList;
    private CallBackListener successListener;

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

    public boolean startDownload(String filename,String download_url,String saveUri,long filesize,long downloadedSize){
        int progress = 0;
        if(filesize>0)
            progress = (int)((downloadedSize*100)/filesize);
        DownloadInformation information = new DownloadInformation(filename,progress,filesize,downloadedSize);
        information.setDownloadUrl(download_url);
        information.setDownloadStatus(DownloadInformation.RESUME_DOWNLOAD);
        information.setSavePath(saveUri);
        Request request = new Request(filename,download_url,saveUri);
        int id = manager.push(request);
        information.setId(id);
        runningData.put(id,information);
        if(runninglistener != null)
            runninglistener.onAddDownload(id,filename,download_url,saveUri,filesize,downloadedSize);

        Log.e(TAG,"Start Download Called");
        return true;
    }


    public void removeRunningDownload(int id){
        if(runningData != null)
            runningData.remove(id);
        if(manager != null)
            manager.cancel(id);
    }

    private void isThereAnyRunningDownload(){
        if(runningData != null && runningData.isEmpty()) {
            stopSelf();
        }
    }

    public BackgroundDownloaderService() {}

    @Override
    public void onCreate() {
        super.onCreate();
        manager = Manager.getInstance(Runtime.getRuntime().availableProcessors());
        manager.bind(this);
        runningData = new HashMap();

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
    //    Utilities.uploadData(pauseErrorData,successDownloadList,getApplicationContext());
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
            startDownload(bundle.getString(Utilities.DOWNLOAD_FILENAME), bundle.getString(Utilities.DOWNLOAD_URL), bundle.getString(Utilities.SAVE_DOWNLOAD_URI), bundle.getLong(Utilities.DOWNLOAD_FILE_SIZE), bundle.getLong(Utilities.DOWNLOAD_DOWNLOADED_SIZE));
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
       // Log.e(TAG,"OnProgress id "+id+" "+progress);

    }

    @Override
    public void onError(int id, int errorCode, String errorMsg) {
        if(errorCode != DownloadCode.DOWNLOAD_INTERRUPT_ERROR){
            PauseAndErrorMethod(id,errorCode,errorMsg);
        }
        Log.e(TAG,"OnError "+id+" "+errorCode+" "+errorMsg);
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
        if(runninglistener != null)
            runninglistener.onSuccess(request);
        if(runningData.containsKey(request.getId())){
            updateInformation(request.getId(),100,request.getFileSize(),request.getFileSize());
            DownloadInformation information = (DownloadInformation)runningData.get(request.getId());
            information.setDownloadStatus(DownloadInformation.SUCCESS_DOWNLOAD);
            uploadDataToSuccessDb(information);
            runningData.remove(request.getId());
        }
        isThereAnyRunningDownload();
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
        if(runningData.containsKey(id)){
            if(runninglistener != null)
                runninglistener.onError(id,errorCode,errorMsg,null);

                DownloadInformation information = (DownloadInformation) runningData.get(id);
                if(information != null) {
                    if(errorCode == DownloadCode.DOWNLOAD_INTERRUPT_ERROR)
                        information.setDownloadStatus(DownloadInformation.PAUSE_DOWNLOAD);
                    else
                        information.setDownloadStatus(DownloadInformation.CANCEL_DOWNLOAD);
                    //Todo:- Handel if there is other kind of Error happen in here like URL ERROR,FILE NOT FOUND ERROR

                    uploadDataToPauseErrorDb(information);

                }

            runningData.remove(id);
        }

        isThereAnyRunningDownload();
    }



    public class MyBinder extends Binder{
        public BackgroundDownloaderService getService(){
            return BackgroundDownloaderService.this;
        }
    }

}
