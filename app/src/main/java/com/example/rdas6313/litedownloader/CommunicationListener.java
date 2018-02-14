package com.example.rdas6313.litedownloader;

/**
 * Created by rdas6313 on 14/2/18.
 */

public interface CommunicationListener {
    public void onresumeDownload(int id,int status,String downloadUrl,String savePath,String filename);
    public void onpauseDownload(int id,int status);
}
