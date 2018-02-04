package com.example.rdas6313.litedownloader;

/**
 * Created by rdas6313 on 4/2/18.
 */

public class DownloadInformation {
    private String title;
    private int progress;
    private long fileSize,downloadedSize;

    DownloadInformation(){
        title = null;
        progress = 0;
        fileSize = 0;
        downloadedSize = 0;
    }

    DownloadInformation(String title,int progress,long fileSize,long downloadedSize){
        this.title = title;
        this.progress = progress;
        this.fileSize = fileSize;
        this.downloadedSize = downloadedSize;
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
        fileSize = size;
    }

}
