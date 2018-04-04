package com.example.litedownloaderapi;

import com.example.litedownloaderapi.Interface.*;
import com.example.litedownloaderapi.Interface.Request;

/**
 * Created by rdas6313 on 3/4/18.
 */

public final class DownloadRequest implements Request {

    /*
     * Declared Download Status Constants
     */
    public final static int DOWNLOAD_PENDING = 1;
    public final static int DOWNLOAD_RUNNING = (1<<1);
    public final static int DOWNLOAD_PAUSED = (1<<2);
    public final static int DOWNLOAD_RESUME = (1<<3);
    public final static int DOWNLOAD_CANCEL = (1<<4);
    /*
     * Constants End Here
     */

    private String download_url,save_dir,filename;
    private boolean isCancelled;
    private int download_state,id;
    private long filesize,downloaded_Size;

    private DownloadRequest(){
        download_url = null;
        save_dir = null;
        filename = null;
        isCancelled = false;
        download_state = DOWNLOAD_PENDING;
        id = -1;
        filesize = 0;
        downloaded_Size = 0;
    }

    public static DownloadRequest getRequest(){
        return new DownloadRequest();
    }

    public void setId(int id){
        this.id = id;
    }

    public int getId(){
        return id;
    }

    public void setFilesize(long filesize){
        this.filesize = filesize;
    }
    public long getFilesize(){
        return filesize;
    }
    public void setDownloadedSize(long downloadedSize){
        downloaded_Size = downloadedSize;
    }

    public long getDownloadedSize(){
        return downloaded_Size;
    }

    @Override
    public Request setDownloadUrl(String url) {
        download_url = url;
        return this;
    }

    @Override
    public Request setDir(String uri) {
        save_dir = uri;
        return this;
    }

    @Override
    public Request setFileName(String filename) {
        this.filename = filename;
        return this;
    }

    public DownloadRequest setDownloadState(int state){
        download_state = state;
        return this;
    }

    public void setDownloadCancel(boolean cancel){
        isCancelled = cancel;
    }

    public boolean isDownloadCancelled(){
        return isCancelled;
    }

    public int getDownloadState(){
        return download_state;
    }

    public String getFilename(){
        return filename;
    }

    public String getDownloadUrl(){
        return download_url;
    }

    public String getDir(){
        return save_dir;
    }
}
