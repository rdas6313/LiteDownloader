package com.example.litedownloaderapi;

import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
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

    public Task(BlockingQueue q){
        queue = q;
    }

    @Override
    public void run() {
        while(true){
            try{
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
        interrupt();
    }

    /**
     * Connection to server and fetching response and processing stream of data
     */

    public void Download(DownloadRequest request){
        long downloadedSize = request.getDownloadedSize();
        String downloadUrl = request.getDownloadUrl();
        int req_id = request.getId();
        Log.e(TAG,"URL - "+downloadUrl);
        if(downloadUrl == null || downloadUrl.length() == 0){
            //Todo:-Generate Error Here
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
            //Todo:- send Error here
        }catch (InterruptedIOException e){
            Log.e(TAG,e.getMessage());
            //Todo:- send Error here
        }catch (IOException e){
            Log.e(TAG,e.getMessage());
            //Todo:- send Error here
        }catch (Exception e){
            Log.e(TAG,e.getMessage());
            //Todo:- send Error here
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
     */

    private void readResponse(HttpURLConnection connection,DownloadRequest request) throws IOException,Exception{
        Log.e(TAG,"Response code - "+connection.getResponseCode());

        switch (connection.getResponseCode()){
            case HttpURLConnection.HTTP_OK:
            case HttpURLConnection.HTTP_PARTIAL:
                long downloadedSize = request.getDownloadedSize();
                long filesize = downloadedSize + Long.valueOf(connection.getHeaderField("Content-length"));
                request.setFilesize(filesize);
                break;
            default:
                throw new Exception("Response Error "+connection.getResponseCode());
        }
    }
    /**
     * Check If File Exist or Not
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
     */

    private void processInputStream(InputStream inputStream,DownloadRequest request){

        long downloadedSize = request.getDownloadedSize();
        long fileSize = request.getFilesize();
        String savePath = request.getDir()+"/"+request.getFilename();
        boolean isFileExists = checkIfFileExists(request.getDir(),request.getFilename());
        int req_id = request.getId();

        int len = 0;
        int progress = (int)((downloadedSize*100)/fileSize);
        byte[] b = new byte[1024];
        FileOutputStream fileOutputStream = null;
        try{

            if(isFileExists)
                fileOutputStream = new FileOutputStream(new File(savePath),true);
            else {
                File file = new File(savePath);
                file.createNewFile();
                Log.e(TAG,"FILE NAME "+file.getName());
                fileOutputStream = new FileOutputStream(file);
            }
            while ((len = inputStream.read(b)) != -1){
                if(request.isDownloadCancelled()){
                    //Todo:- paused download here
                    break;
                }
                fileOutputStream.write(b,0,len);
                downloadedSize += len;
                //Log.e(TAG,"Download Len - "+len);
                progress = (int)((downloadedSize*100)/fileSize);
                //Todo: - send Progress here
            }
            Log.e(TAG,"ACTUAL DOWNLOADED SIZE "+downloadedSize);
            request.setDownloadedSize(downloadedSize);
            if(progress >= 100) {
                //Todo:- send Success here
            }

        }catch (FileNotFoundException e){
            Log.e(TAG,e.getMessage());
            //Todo:- send Error here
        }catch (InterruptedIOException e){
            Log.e(TAG,e.getMessage());
            //Todo:- send Error here
        }catch (IOException e){
            Log.e(TAG,e.getMessage());
            //Todo:- send Error here
        }finally {
            try{
                if(fileOutputStream != null)
                    fileOutputStream.close();
            }catch (IOException e){
                Log.e(TAG,e.getMessage());
                //Todo:- send Error here
            }
        }
    }

}
