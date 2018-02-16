package com.example.rdas6313.litedownloader.backgroundDownload;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.IntDef;
import android.util.Log;

import com.example.litedownloaderapi.DownloadCode;
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
    private HashMap pauseErrorData;
    private int running;

    private final MyBinder binder = new MyBinder();
    private CallBackListener runninglistener;
    private CallBackListener pauseErrorlistener;
    private Manager manager;

    public void setListeners(CallBackListener RcallBackListener,CallBackListener PcallBackListener2){
        runninglistener = RcallBackListener;
        pauseErrorlistener = PcallBackListener2;
    }

    public ArrayList getRunningDownloads(){
        ArrayList<DownloadInformation>informations = new ArrayList<>();
        for(Object key : runningData.keySet()){
            informations.add((DownloadInformation) runningData.get(key));
        }
        return informations;
    }

    public ArrayList getPausedErrorDownloads(){
        ArrayList<DownloadInformation>informations = new ArrayList<>();
        for(Object key : pauseErrorData.keySet()){
            informations.add((DownloadInformation) pauseErrorData.get(key));
        }
        return informations;
    }

    public void pauseDownload(int id){
        if(manager != null) {
            if(runningData.containsKey(id)){
                DownloadInformation information = (DownloadInformation)runningData.get(id);
            }
            manager.pause(id);

        }
    }

    public void startDownload(String filename,String download_url,String saveUri){
        DownloadInformation information = new DownloadInformation(filename,0,0,0);
        information.setDownloadUrl(download_url);
        information.setSavePath(saveUri);
        Request request = new Request(filename,download_url,saveUri);
        int id = manager.push(request);
        information.setId(id);
        runningData.put(id,information);
        if(runninglistener != null)
            runninglistener.onAddDownload(id,filename,download_url,saveUri);

        Log.e(TAG,"Start Download Called");
    }

    public void removePausedErrorDownload(int id){
        if(pauseErrorData != null && pauseErrorData.containsKey(id)){
            pauseErrorData.remove(id);
        }
    }


    public BackgroundDownloaderService() {}

    @Override
    public void onCreate() {
        super.onCreate();
        manager = Manager.getInstance(2);
        manager.bind(this);
        running = 0;
        runningData = new HashMap();
        pauseErrorData = new HashMap();
    }

    private void release(){
        clearDownloadsData();
        if(manager != null) {
            manager.unbind();
            manager.release();
        }
    }

    private void clearDownloadsData(){
        if(runningData != null)
            runningData.clear();
        if(pauseErrorData != null)
            pauseErrorData.clear();
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
        if(runninglistener != null)
            runninglistener.onProgress(id,progress,downloadedSize,fileSize);
       // Log.e(TAG,"OnProgress id "+id+" "+progress);
        updateInformation(id,progress,downloadedSize,fileSize);
    }

    @Override
    public void onError(int id, int errorCode, String errorMsg) {
        if(runninglistener != null)
            runninglistener.onError(id,errorCode,errorMsg,null);
        if(runningData.containsKey(id)){
            if(pauseErrorData != null){
                DownloadInformation information = (DownloadInformation) runningData.get(id);
                if(information != null) {
                    if(errorCode == DownloadCode.RESPONSE_ERROR)
                        information.setDownloadStatus(DownloadInformation.CANCEL_DOWNLOAD);
                    else if(errorCode == DownloadCode.DOWNLOAD_INTERRUPT_ERROR)
                        information.setDownloadStatus(DownloadInformation.PAUSE_DOWNLOAD);

                    //Todo:- Handel if there is other kind of Error happen in here like URL ERROR,FILE NOT FOUND ERROR
                    DownloadInformation newinfo = new DownloadInformation(information);

                    pauseErrorData.put(id, information);
                    if (pauseErrorlistener != null)
                        pauseErrorlistener.onError(id, errorCode, errorMsg, newinfo);
                }
            }
            runningData.remove(id);
        }

        Log.e(TAG,"OnError "+id+" "+errorCode+" "+errorMsg);
    }

    @Override
    public void onSuccess(Request request) {
        if(runninglistener != null)
            runninglistener.onSuccess(request);
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
