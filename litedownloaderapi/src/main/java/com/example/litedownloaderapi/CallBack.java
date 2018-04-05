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
    private LiteDownloadListener listener;
    private final static String TAG = CallBack.class.getName();

    /**
     * initializing executor object.
     */
    public CallBack(){
        final Handler handler = new Handler(Looper.getMainLooper());
        executor = new Executor() {
            @Override
            public void execute(@NonNull Runnable command) {
                handler.post(command);
            }
        };
    }

    /**
     * setting callback listener
     * @param liteDownloadListener
     */
    public void setCallback(LiteDownloadListener liteDownloadListener){
        listener = liteDownloadListener;
    }

    /**
     * sending progress to main thread
     * @param id
     * @param downloadedSize
     * @param fileSize
     * @param progress
     */
    public void sendProgress(final int id, final long downloadedSize, final long fileSize, final int progress){
        executor.execute(new Runnable() {
            @Override
            public void run() {
                if(listener != null){
                    listener.onProgress(id,fileSize,downloadedSize,progress);
                }
            }
        });
    }

    /**
     * sending error to main thread
     * @param id
     * @param errorMsg
     * @param errorCode
     */
    public void sendError(final int id, final String errorMsg, final int errorCode){
        executor.execute(new Runnable() {
            @Override
            public void run() {
                if(listener != null){
                    listener.onError(id,errorMsg,errorCode);
                }
            }
        });
    }

    /**
     * sending success to main thread
     * @param id
     */
    public void sendSuccess(final int id){
        executor.execute(new Runnable() {
            @Override
            public void run() {
                if(listener != null){
                    listener.onSuccess(id);
                }
            }
        });
    }

    /**
     * sending start to main thread
     * @param id
     */
    public void sendStart(final int id){
        executor.execute(new Runnable() {
            @Override
            public void run() {
                if(listener != null)
                    listener.onStart(id);
            }
        });
    }

    /**
     * clearing callBacks and executor to free memory
     */
    public void clearCallBack(){
        executor = null;
        listener = null;
    }
}
