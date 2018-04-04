package com.example.litedownloaderapi.Interface;

/**
 * Created by rdas6313 on 4/4/18.
 */

public interface LiteDownloadListener {
    public void onStart(int id);
    public void onError(int id,String errorMsg,int errorCode);
    public void onProgress(int id,long fileSize,long downloadedSize,int progress);
    public void onSuccess(int id);
}
