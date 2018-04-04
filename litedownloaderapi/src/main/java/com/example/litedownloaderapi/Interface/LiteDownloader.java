package com.example.litedownloaderapi.Interface;

/**
 * Created by rdas6313 on 4/4/18.
 */

public interface LiteDownloader {

    int EMPTY_URL_ERROR = 1111;
    int MALFORMED_URL_ERROR = 1112;
    int IO_ERROR = 1113;
    int RESPONSE_ERROR = 1114;
    int PAUSED_ERROR = 1115;
    int FILE_NOT_FOUND_ERROR = 1116;

    /*
     * Necessary features for LiteDownloaderApi
     */
    public void add(Request request);
    public void pause(int id);
    public void resume(int id);
    public void cancel(int id);
    public void cancelAll();
    public void setCallbackListener(LiteDownloadListener listener);
    public void clear();

}
