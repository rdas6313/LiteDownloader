package com.example.litedownloaderapi;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.util.Log;

import com.example.litedownloaderapi.Interface.LiteDownloadListener;

import java.util.concurrent.Executor;

/**
 * Created by rdas6313 on 4/4/18.
 * CallBack class helps to send data to main thread
 */

public class CallBack {

    private Executor executor;
    private final static String TAG = CallBack.class.getName();
    private Handler handler;
    /**
     * initializing executor object.
     */
    public CallBack(){
        handler = new Handler(Looper.getMainLooper());
        executor = new Executor() {
            @Override
            public void execute(@NonNull Runnable command) {
                handler.post(command);
            }
        };
    }

    /**
     * sending progress to main thread
     * @param id
     * @param filesize
     * @param downloadedSize
     * @param progress
     * @param liteDownloadListener
     */
    public void sendProgress(final int id, final long filesize, final long downloadedSize, final int progress, final LiteDownloadListener liteDownloadListener){
        executor.execute(new Runnable() {
            @Override
            public void run() {
                if(liteDownloadListener != null){
                    liteDownloadListener.onProgress(id,filesize,downloadedSize,progress);
                }
            }
        });
    }

    /**
     * sending error to main thread
     * @param id
     * @param errorMsg
     * @param errorCode
     * @param liteDownloadListener
     */
    public void sendError(final int id, final String errorMsg, final int errorCode, final LiteDownloadListener liteDownloadListener){
        executor.execute(new Runnable() {
            @Override
            public void run() {
                if(liteDownloadListener != null){
                    liteDownloadListener.onError(id,errorMsg,errorCode);
                }
            }
        });
    }

    /**
     * sending success to main thread
     * @param id
     * @param liteDownloadListener
     */
    public void sendSuccess(final int id, final LiteDownloadListener liteDownloadListener){
        executor.execute(new Runnable() {
            @Override
            public void run() {
                if(liteDownloadListener != null){
                    liteDownloadListener.onSuccess(id);
                }
            }
        });
    }

    /**
     * sending start to main thread
     * @param id
     * @param liteDownloadListener
     */
    public void sendStart(final int id,final LiteDownloadListener liteDownloadListener){
       executor.execute(new Runnable() {
            @Override
            public void run() {
                if(liteDownloadListener != null)
                    liteDownloadListener.onStart(id);
            }
        });
    }

    /**
     * clearing callBacks and executor to free memory
     */
    public void clearCallBack(){
        executor = null;
    }
}
