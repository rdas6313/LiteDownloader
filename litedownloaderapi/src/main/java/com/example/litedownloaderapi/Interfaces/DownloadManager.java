package com.example.litedownloaderapi.Interfaces;


import com.example.litedownloaderapi.Request;

/**
 * Created by rdas6313 on 11/1/18.
 */

public interface DownloadManager {
    public int push(Request request);
    public boolean pause(int id);
    public boolean cancel(int id);
}
