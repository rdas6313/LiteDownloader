package com.example.litedownloaderapi;

/**
 * Created by rdas6313 on 11/1/18.
 */

public class Request {
    private int id;
    private String filename,downloadUrl,saveUri;
    private long fileSize,downloadedSize;

    public Request(String filename,String downloadUrl,String saveUri){
        this.id = -1;
        this.filename = filename;
        this.downloadUrl = downloadUrl;
        this.saveUri = saveUri;
        this.fileSize = 0;
        this.downloadedSize = 0;
    }
    public void setId(int Id){
        id = Id;
    }
    public int getId(){
        return id;
    }
    public String getFilename(){
        return filename;
    }
    public String getDownloadUrl(){
        return downloadUrl;
    }
    public String getSaveUri(){
        return saveUri;
    }
    public long getFileSize(){
        return fileSize;
    }
    public long getDownloadedSize(){
        return downloadedSize;
    }
    public void setFileSize(long fileSize){
        this.fileSize = fileSize;
    }
    public void setDownloadedSize(long downloadedSize){
        this.downloadedSize = downloadedSize;
    }

}
