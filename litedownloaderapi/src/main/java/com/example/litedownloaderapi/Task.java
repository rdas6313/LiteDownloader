package com.example.litedownloaderapi;

import android.util.Log;

import com.example.litedownloaderapi.Interface.LiteDownloader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.BlockingQueue;

/**
 * Created by rdas6313 on 4/4/18.
 */

public class Task extends Thread {
    private final static String TAG = Task.class.getName();
    private BlockingQueue queue;
    private volatile boolean cancel = false;
    private CallBack callBack;

    public Task(BlockingQueue q,CallBack mback){
        queue = q;
        callBack = mback;
    }

    @Override
    public void run() {
        while(true){
            try{
                if(queue == null)
                    return;
                DownloadRequest request = (DownloadRequest) queue.take();
                Download(request);

            }catch (InterruptedException e){
                if(cancel){
                    if(queue != null) {
                        queue.clear();
                        queue = null;
                    }
                }

            }
        }
    }

    /**
     * Cancelling Thread Execution
     */

    public void cancelTask(){
        cancel = true;
        callBack = null;
        interrupt();
    }

    /**
     * Connection to server and fetching response and processing stream of data
     * @param request
     */

    public void Download(DownloadRequest request){
        long downloadedSize = request.getDownloadedSize();
        String downloadUrl = request.getDownloadUrl();
        int req_id = request.getId();
        Log.e(TAG,"URL - "+downloadUrl);
        if(downloadUrl == null || downloadUrl.length() == 0){
            sendError(req_id,"Download Url is Empty", LiteDownloader.EMPTY_URL_ERROR);
            return;
        }
        HttpURLConnection connection = null;
        URL url = null;
        InputStream inputStream = null;
        try {
            url = new URL(downloadUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.addRequestProperty("Range","bytes="+downloadedSize+"-");
            connection.connect();
            readResponse(connection,request);
            inputStream = connection.getInputStream();
            if(inputStream != null)
                processInputStream(inputStream,request);
        }catch (MalformedURLException e){
            Log.e(TAG,e.getMessage());
            sendError(req_id,"Malformed Url Error",LiteDownloader.MALFORMED_URL_ERROR);
        }catch (IOException e){
            Log.e(TAG,e.getMessage());
            sendError(req_id,"IO Exception Error",LiteDownloader.IO_ERROR);
        }catch (Exception e){
            Log.e(TAG,e.getMessage());
            sendError(req_id,"Response Error",LiteDownloader.RESPONSE_ERROR);
        }finally {
            if(connection != null) {
                connection.disconnect();
                connection = null;
            }
            if(inputStream != null)
                inputStream = null;
        }
    }

    /**
     * Reading Connection Response Here
     * @param connection
     * @param request
     * @throws IOException
     * @throws Exception
     */

    private void readResponse(HttpURLConnection connection,DownloadRequest request) throws IOException,Exception{
        Log.e(TAG,"Response code - "+connection.getResponseCode());

        switch (connection.getResponseCode()){
            case HttpURLConnection.HTTP_OK:
            case HttpURLConnection.HTTP_PARTIAL:
                long downloadedSize = request.getDownloadedSize();
                long filesize = downloadedSize + Long.valueOf(connection.getHeaderField("Content-length"));
                request.setFilesize(filesize);
                sendStart(request.getId());
                break;
            default:
                throw new Exception("Response Error "+connection.getResponseCode());
        }
    }

    /**
     * Checking If File Exist or Not
     * @param Path
     * @param filename
     * @return
     */

    public boolean checkIfFileExists(String Path,String filename){
        String filePath = Path + "/" + filename;
        File file = new File(filePath);
        if(file.exists()){
            return true;
        }
        return false;
    }

    /**
     * Processing Input Stream here and Writing to file
     * @param inputStream
     * @param request
     */

    private void processInputStream(InputStream inputStream,DownloadRequest request){

        long downloadedSize = request.getDownloadedSize();
        long fileSize = request.getFilesize();
        String savePath = request.getDir()+"/"+request.getFilename();
        int req_id = request.getId();

        File file = new File(savePath);
        boolean isFileExists = file.exists();
        int len = 0;
        int progress = (int)((downloadedSize*100)/fileSize);
        byte[] b = new byte[1024];
        FileOutputStream fileOutputStream = null;
        try{

            if(isFileExists) {
                if(fileSize <= file.length()){
                    sendError(req_id,"Already Downloaded Error",LiteDownloader.ALREADY_DOWNLOADED_ERROR);
                    return;
                }
                fileOutputStream = new FileOutputStream(file, true);
            }
            else {
                file.createNewFile();
                Log.e(TAG,"FILE NAME "+file.getName());
                fileOutputStream = new FileOutputStream(file);
            }
            while ((len = inputStream.read(b)) != -1){
                if(request.isDownloadCancelled()){
                    sendError(req_id,"Paused Download Error",LiteDownloader.PAUSED_ERROR);
                    break;
                }
                fileOutputStream.write(b,0,len);
                downloadedSize += len;
                request.setDownloadedSize(downloadedSize);
                sendProgress(request);
            }
            Log.e(TAG,"ACTUAL DOWNLOADED SIZE "+downloadedSize);
            if(progress >= 100) {
                sendSuccess(req_id);
            }

        }catch (FileNotFoundException e){
            Log.e(TAG,e.getMessage());
            sendError(req_id,"File Not Found Error",LiteDownloader.FILE_NOT_FOUND_ERROR);
        }catch (IOException e){
            Log.e(TAG,e.getMessage());
            sendError(req_id,"IO Error",LiteDownloader.IO_ERROR);
        }finally {
            try{
                if(fileOutputStream != null)
                    fileOutputStream.close();
            }catch (IOException e){
                Log.e(TAG,e.getMessage());
                sendError(req_id,"IO Error",LiteDownloader.IO_ERROR);
            }
        }
    }

    /**
     * Sending Error To MainThread Using CallBack
     * @param id
     * @param errorMsg
     * @param errorCode
     */

    private void sendError(int id,String errorMsg,int errorCode){
        if(callBack != null){
            callBack.sendError(id,errorMsg,errorCode);
        }
    }

    /**
     * Sending progress to MainThread using Callback
     * @param request
     */
    public void sendProgress(DownloadRequest request){
        if(callBack != null && !request.isDownloadCancelled()){
            int progress = (int)((request.getDownloadedSize()*100)/request.getFilesize());
            callBack.sendProgress(request.getId(),request.getDownloadedSize(),request.getFilesize(),progress);
        }
    }

    /**
     * sending Success to MainThread using CallBack
     * @param id
     */
    public void sendSuccess(int id){
        if(callBack != null)
            callBack.sendSuccess(id);
    }

    /**
     * sending Start callback To MainThread using CallBack
     * @param id
     */
    public void sendStart(int id){
        if(callBack != null)
            callBack.sendStart(id);
    }

}
