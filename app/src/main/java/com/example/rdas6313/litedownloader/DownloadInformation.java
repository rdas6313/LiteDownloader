package com.example.rdas6313.litedownloader;

/**
 * Created by rdas6313 on 4/2/18.
 */

public class DownloadInformation {
    private String title;
    private int progress;
    private long fileSize,downloadedSize;
    private int id;
    private String downloadUrl,savePath;
    private int status;

    public final static int PAUSE_DOWNLOAD = -1;
    public final static int RESUME_DOWNLOAD = 1;
    public final static int CANCEL_DOWNLOAD = 0;


    public DownloadInformation(DownloadInformation information){
        title = information.getTitle();
        progress = information.getProgress();
        fileSize = information.getFileSize();
        downloadedSize = information.getDownloadedSize();
        id = information.getId();
        downloadUrl = information.getDownloadUrl();
        savePath = information.getSavePath();
        status = information.getDownloadStatus();
    }
    public DownloadInformation(){
        title = null;
        progress = 0;
        fileSize = 0;
        downloadedSize = 0;
        id = -1;
        downloadUrl = null;
        savePath = null;
        status = RESUME_DOWNLOAD;
    }

    public DownloadInformation(String title,int progress,long fileSize,long downloadedSize){
        this.title = title;
        this.progress = progress;
        this.fileSize = fileSize;
        this.downloadedSize = downloadedSize;
        status = RESUME_DOWNLOAD;
    }

    public void setDownloadStatus(int downloadStatus){
        status = downloadStatus;
    }

    public int getDownloadStatus(){
        return status;
    }

    public void setDownloadUrl(String url){
            downloadUrl = url;
    }

    public String getDownloadUrl(){
        return downloadUrl;
    }

    public void setSavePath(String path){
        savePath = path;
    }

    public String getSavePath(){
        return savePath;
    }

    public void setId(int Id){
        id = Id;
    }

    public int getId(){
        return id;
    }

    public void setTitle(String title){
        this.title = title;
    }

    public String getTitle(){
        return title;
    }

    public void setProgress(int progress){
        this.progress = progress;
    }

    public int getProgress(){
        return progress;
    }

    public void setFileSize(long size){
        fileSize = size;
    }

    public long getFileSize(){
        return fileSize;
    }

    public long getDownloadedSize(){
        return downloadedSize;
    }

    public void setDownloadedSize(long size){
        downloadedSize = size;
    }

}
