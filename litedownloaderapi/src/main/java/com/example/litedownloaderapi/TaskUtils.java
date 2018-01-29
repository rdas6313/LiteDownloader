package com.example.litedownloaderapi;

import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
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
import java.net.URLConnection;
import java.util.List;
import java.util.Map;

/**
 * Created by rdas6313 on 16/1/18.
 */

public class TaskUtils {
    public static final String TAG = TaskUtils.class.getName();


    public static boolean checkIfFileExists(String Path,String filename){
        String filePath = Path + "/" + filename;
        File file = new File(filePath);
        if(file.exists()){
            return true;
        }
        return false;
    }
    public static long getFileSize(String path,String filename){
        String filepath = path + "/" +filename;
        File file = new File(filepath);
        if(file.exists())
            return file.length();
        return 0;
    }
    public static void connectAndprocessStream(Request request){
        String downloadUrl = request.getDownloadUrl();
        long downloadedSize = request.getDownloadedSize();
        int req_id = request.getId();

        HttpURLConnection connection = null;
        URL url = null;
        InputStream inputStream = null;
        long fileSize = 0;
        try {
            url = new URL(downloadUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.addRequestProperty("Range","bytes="+downloadedSize+"-");
            connection.connect();
            Log.e(TAG,"Response code - "+connection.getResponseCode());
            if(connection.getResponseCode() != connection.HTTP_OK && connection.getResponseCode() != connection.HTTP_PARTIAL){
                throw new Exception("Exception Response code "+connection.getResponseCode());
            }

            fileSize = getDownloadFileSize(connection.getHeaderField("Content-Range"));
            request.setFileSize(fileSize);
            inputStream = connection.getInputStream();
            if(inputStream != null)
                processInputStream(inputStream,request);
        }catch (MalformedURLException e){
            Log.e(TAG,e.getMessage());
            sendErrorMsg(req_id,DownloadCode.MALFORMED_URL_ERROR,e.getMessage());
        }catch (InterruptedIOException e){
            Log.e(TAG,e.getMessage());
            sendErrorMsg(req_id,DownloadCode.DOWNLOAD_INTERRUPT_ERROR,e.getMessage());
        }catch (IOException e){
            Log.e(TAG,e.getMessage());
            sendErrorMsg(req_id,DownloadCode.IO_ERROR,e.getMessage());
        }catch (Exception e){
            Log.e(TAG,e.getMessage());
            sendErrorMsg(req_id,DownloadCode.RESPONSE_ERROR,e.getMessage());
        }finally {
            if(connection != null) {
                connection.disconnect();
                connection = null;
            }
            if(inputStream != null)
                inputStream = null;
        }
    }

    private static long getDownloadFileSize(String data){
        String[] d = data.split("/");
       // Log.e(TAG,"FILESIZE - "+d[1]);
        return Long.valueOf(d[1]);
    }

    private static void processInputStream(InputStream inputStream,Request request){

        long downloadedSize = request.getDownloadedSize();
        long fileSize = request.getFileSize();
        String savePath = request.getSaveUri()+"/"+request.getFilename();
        int req_id = request.getId();

        int len = 0;
        int progress = (int)((downloadedSize*100)/fileSize);
        byte[] b = new byte[1024];
        FileOutputStream fileOutputStream = null;
        try{
            fileOutputStream = new FileOutputStream(new File(savePath),true);
            while ((len = inputStream.read(b)) != -1){
                fileOutputStream.write(b);
                downloadedSize += len;
                //Log.e(TAG,"Download Len - "+len);
                progress = (int)((downloadedSize*100)/fileSize);
                sendProgress(req_id,progress,downloadedSize,fileSize);
            }
            request.setDownloadedSize(downloadedSize);
            if(progress >= 100) {
                sendSuccess(request);
            }

        }catch (FileNotFoundException e){
            Log.e(TAG,e.getMessage());
            sendErrorMsg(req_id,DownloadCode.FILE_NOT_FOUND_ERROR,e.getMessage());
        }catch (InterruptedIOException e){
            Log.e(TAG,e.getMessage());
            sendErrorMsg(req_id,DownloadCode.DOWNLOAD_INTERRUPT_ERROR,e.getMessage());
        }catch (IOException e){
            Log.e(TAG,e.getMessage());
            sendErrorMsg(req_id,DownloadCode.IO_ERROR,e.getMessage());
        }finally {
            try{
                if(inputStream != null)
                    inputStream.close();
                if(fileOutputStream != null)
                    fileOutputStream.close();
            }catch (IOException e){
                Log.e(TAG,e.getMessage());
                sendErrorMsg(req_id,DownloadCode.IO_ERROR,e.getMessage());
            }
        }
    }
    private static synchronized void sendErrorMsg(int id,int error_id,String error_msg){
        Communication communication = Communication.getInstance();
        Bundle bundle = new Bundle();
        bundle.putInt(DownloadCode.REQUEST_ID,id);
        bundle.putInt(DownloadCode.REQUEST_ERROR_ID,error_id);
        bundle.putString(DownloadCode.REQUEST_ERROR_MSG,error_msg);
        Message msg = Message.obtain();
        msg.what = DownloadCode.DOWNLOAD_ERROR;
        msg.setData(bundle);
        communication.sendMessage(msg);
    }
    private static synchronized void sendProgress(int id,int progress,long downlodedSize,long fileSize){
        Communication communication = Communication.getInstance();
        Bundle bundle = new Bundle();
        bundle.putInt(DownloadCode.REQUEST_ID,id);
        bundle.putInt(DownloadCode.REQUEST_PROGRESS,progress);
        bundle.putLong(DownloadCode.REQUEST_DOWNLODED_FILE_SIZE,downlodedSize);
        bundle.putLong(DownloadCode.REQUEST_FILESIZE,fileSize);
        Message msg = Message.obtain();
        msg.what = DownloadCode.DOWNLOAD_PROGRESS;
        msg.setData(bundle);
        communication.sendMessage(msg);
    }
    private static synchronized void sendSuccess(Request request){
        Communication communication = Communication.getInstance();
        Message msg = Message.obtain();
        msg.what = DownloadCode.DOWNLOAD_COMPLETED;
        msg.obj = request;
        communication.sendMessage(msg);
    }
}
