package com.example.litedownloaderapi;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;

import com.example.litedownloaderapi.Interface.LiteDownloadListener;

import java.util.concurrent.Executor;

/**
 * Created by rdas6313 on 4/4/18.
 */

public class CallBack {

    private Executor executor;
    private LiteDownloadListener listener;

    public CallBack(){
        final Handler handler = new Handler(Looper.getMainLooper());
        executor = new Executor() {
            @Override
            public void execute(@NonNull Runnable command) {
                handler.post(command);
            }
        };
    }

    public void setCallback(LiteDownloadListener liteDownloadListener){
        listener = liteDownloadListener;
    }

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

    public void sendStart(final int id){
        executor.execute(new Runnable() {
            @Override
            public void run() {
                if(listener != null)
                    listener.onStart(id);
            }
        });
    }

    public void clearCallBack(){
        executor = null;
        listener = null;
    }
}
