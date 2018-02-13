package com.example.rdas6313.litedownloader.backgroundDownload;

import com.example.litedownloaderapi.Request;

/**
 * Created by rdas6313 on 9/2/18.
 */

public interface CallBackListener {

    public void onAddDownload(int id,String title,String downlaod_url,String save_Path);
    public void onProgress(int id,int progress,long downloadedSize,long fileSize);
    public void onError(int id,int errorCode,String errorMsg,Object object);
    public void onSuccess(Request request);

}
