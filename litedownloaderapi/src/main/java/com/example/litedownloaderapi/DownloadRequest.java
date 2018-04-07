package com.example.litedownloaderapi;

import com.example.litedownloaderapi.Interface.*;
import com.example.litedownloaderapi.Interface.Request;

/**
 * Created by rdas6313 on 3/4/18.
 * DownloadRequest class holds the data about requested download
 */

public final class DownloadRequest implements Request {

    /**
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
    private LiteDownloadListener mListener;

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

    /**
     * set download id
     * @param id
     */
    public void setId(int id){
        this.id = id;
    }

    /**
     * get download id
     * @return
     */
    public int getId(){
        return id;
    }

    /**
     * set file size of the request
     * @param filesize
     */
    public void setFilesize(long filesize){
        this.filesize = filesize;
    }

    /**
     * get file size
     * @return
     */
    public long getFilesize(){
        return filesize;
    }

    /**
     * set downloaded file size
     * @param downloadedSize
     */
    public void setDownloadedSize(long downloadedSize){
        downloaded_Size = downloadedSize;
    }

    /**
     * get downloaded file size
     * @return
     */
    public long getDownloadedSize(){
        return downloaded_Size;
    }

    /**
     * set download url for request
     * @param url
     * @return
     */
    @Override
    public Request setDownloadUrl(String url) {
        download_url = url;
        return this;
    }

    /**
     * set save dir for downlaod
     * @param uri
     * @return
     */
    @Override
    public Request setDir(String uri) {
        save_dir = uri;
        return this;
    }

    /**
     * set file name for download request
     * @param filename
     * @return
     */
    @Override
    public Request setFileName(String filename) {
        this.filename = filename;
        return this;
    }

    /**
     * setting callback listener
     * @param listener
     * @return
     */
    @Override
    public Request setCallBackListener(LiteDownloadListener listener) {
        mListener = listener;
        return this;
    }

    /**
     * getting listener
     * @return
     */
    public LiteDownloadListener getListener(){
        return mListener;
    }

    /**
     * set download state
     * @param state
     * @return
     */
    public DownloadRequest setDownloadState(int state){
        download_state = state;
        return this;
    }

    /**
     * set download cancel
     * @param cancel
     */
    public void setDownloadCancel(boolean cancel){
        isCancelled = cancel;
    }

    /**
     * check whether the download is cancelled or not
     * @return
     */
    public boolean isDownloadCancelled(){
        return isCancelled;
    }

    /**
     * get download state
     * @return
     */
    public int getDownloadState(){
        return download_state;
    }

    /**
     * get file name
     * @return
     */
    public String getFilename(){
        return filename;
    }

    /**
     * get download url
     * @return
     */
    public String getDownloadUrl(){
        return download_url;
    }

    /**
     * get save dir
     * @return
     */
    public String getDir(){
        return save_dir;
    }
}
