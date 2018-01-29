package com.example.litedownloaderapi;

/**
 * Created by rdas6313 on 11/1/18.
 */

public class Task implements Runnable {
    private Request request;
    public Task(Request request){
        this.request = request;
    }
    @Override
    public void run() {
        request.setDownloadedSize(TaskUtils.getFileSize(request.getSaveUri(),request.getFilename()));
        TaskUtils.connectAndprocessStream(request);
    }
}
