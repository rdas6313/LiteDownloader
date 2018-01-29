package com.example.litedownloaderapi.Interfaces;


import com.example.litedownloaderapi.Request;

/**
 * Created by rdas6313 on 11/1/18.
 */

public interface DownloadEventListener {
    public void onProgress(int id, int progress, long downloadedSize, long fileSize);
    public void onError(int id, int errorCode, String errorMsg);
    public void onSuccess(Request request);
}
