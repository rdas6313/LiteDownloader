package com.example.litedownloaderapi.Interface;

/**
 * Created by rdas6313 on 4/4/18.
 */

public interface LiteDownloader {
    /*
     * Necessary features for LiteDownloaderApi
     */
    public void add(Request request);
    public void pause(int id);
    public void resume(int id);
    public void cancel(int id);
    public void cancelAll();

}
