package com.example.litedownloaderapi;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.example.litedownloaderapi.Interfaces.DownloadEventListener;


/**
 * Created by rdas6313 on 18/1/18.
 */

public class Communication extends Handler {
    private static Communication obj = null;
    private DownloadEventListener listener;
    private Communication(){
        super();
    }

    public static Communication getInstance(){
        if(obj == null)
            obj = new Communication();
        return obj;
    }

    public void setEventListener(DownloadEventListener eventListener){
        listener = eventListener;
    }

    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        switch (msg.what){
            case DownloadCode.DOWNLOAD_COMPLETED:
                Request request = (Request) msg.obj;
                if(listener != null)
                    listener.onSuccess(request);
                break;
            case DownloadCode.DOWNLOAD_PROGRESS:
                Bundle bundle = msg.getData();
                if(listener != null)
                    listener.onProgress(bundle.getInt(DownloadCode.REQUEST_ID),bundle.getInt(DownloadCode.REQUEST_PROGRESS),bundle.getLong(DownloadCode.REQUEST_DOWNLODED_FILE_SIZE),bundle.getLong(DownloadCode.REQUEST_FILESIZE));
                break;
            case DownloadCode.DOWNLOAD_ERROR:
                Bundle bun = msg.getData();
                if(listener != null)
                    listener.onError(bun.getInt(DownloadCode.REQUEST_ID),bun.getInt(DownloadCode.REQUEST_ERROR_ID),bun.getString(DownloadCode.REQUEST_ERROR_MSG));
                break;
        }
    }
}
