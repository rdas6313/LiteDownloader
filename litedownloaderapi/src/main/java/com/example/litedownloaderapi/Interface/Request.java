package com.example.litedownloaderapi.Interface;

/**
 * Created by rdas6313 on 3/4/18.
 */

public interface Request {
    public Request setDownloadUrl(String url);
    public Request setDir(String uri);
    public Request setFileName(String filename);
    public Request setCallBackListener(LiteDownloadListener listener);
}
