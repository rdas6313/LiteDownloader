package com.example.rdas6313.litedownloader;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.webkit.MimeTypeMap;

import com.example.rdas6313.litedownloader.backgroundDownload.BackgroundDataUploadingService;
import com.example.rdas6313.litedownloader.backgroundDownload.BackgroundDownloaderService;
import com.example.rdas6313.litedownloader.data.DownloaderContract;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;


/**
 * Created by rdas6313 on 11/2/18.
 */

public final class Utilities {
    private final static String TAG = Utilities.class.getName();

    public final static String DOWNLOAD_URL = "url";
    public final static String SAVE_DOWNLOAD_URI = "uri";
    public final static String DOWNLOAD_FILENAME = "filename";
    public final static String DOWNLOAD_FILE_SIZE = "filesize";
    public final static String DOWNLOAD_DOWNLOADED_SIZE = "downloaded_size";
    public final static String SHOULD_REMOVE_PAUSE_ERROR_DOWNLOAD = "isItFirstTimeDownload";
    public final static String DOWNLOAD_ID = "downlaod_id";

    public final static String UPLOAD_PAUSE_ERROR_KEY = "key_pause_error";
    public final static String UPLOAD_SUCCESS_KEY = "success_key";

    public static void changeServiceAliveValue(boolean value, Application application){
        App app = (App)application;
        app.isServiceAlive = value;
    }
    public static boolean isServiceAlive(Application application){
        App app = (App)application;
        return app.isServiceAlive;
    }

    public static boolean isActivityAlive(Application application){
        App app = (App)application;
        return app.isActivityAlive;
    }

    public static void changeActivityAliveValue(boolean value,Application application){
        App app = (App)application;
        app.isActivityAlive = value;
    }
    public static ArrayList MapToArrayList(HashMap map){
        if(map == null || map.isEmpty())
            return null;

//        Log.e(TAG,"PAUSE ERROR MAP SIZE "+map.size());
        ArrayList<DownloadInformation>list = new ArrayList<>();

        for(Object key:map.keySet()){
            DownloadInformation information = (DownloadInformation)map.get(key);
            list.add(information);
        }
        return list;
    }

    public static ArrayList changeCursorToArrayListForSuccess(Cursor cursor){
        if(cursor == null)
            return null;
        ArrayList list = new ArrayList();
        if(!cursor.moveToFirst())
            return null;
        do{
            String title = cursor.getString(cursor.getColumnIndex(DownloaderContract.Success.TITLE));
            String url = cursor.getString(cursor.getColumnIndex(DownloaderContract.Success.DOWNLOAD_URL));
            String save_uri = cursor.getString(cursor.getColumnIndex(DownloaderContract.Success.SAVE_URI));
            long filesize = Long.parseLong(cursor.getString(cursor.getColumnIndex(DownloaderContract.Success.FILESIZE)));
            DownloadInformation information = new DownloadInformation(title,100,filesize,filesize);
            information.setDownloadStatus(DownloadInformation.SUCCESS_DOWNLOAD);
            information.setId(cursor.getInt(cursor.getColumnIndex(DownloaderContract.Success._ID)));
            information.setSavePath(save_uri);
            information.setDownloadUrl(url);
            list.add(information);
        }while(cursor.moveToNext());
        return list;
    }

    public static ArrayList ChangeCursorToArrayListForPauseError(Cursor cursor){
        if(cursor == null)
            return null;
        ArrayList<DownloadInformation>list = new ArrayList<>();
        int key = -1;
        if(!cursor.moveToFirst())
            return null;
        do{
            String title = cursor.getString(cursor.getColumnIndex(DownloaderContract.PausedError.TITLE));
            String url = cursor.getString(cursor.getColumnIndex(DownloaderContract.PausedError.DOWNLOAD_URL));
            String save_uri = cursor.getString(cursor.getColumnIndex(DownloaderContract.PausedError.SAVE_URI));
            long filesize = Long.parseLong(cursor.getString(cursor.getColumnIndex(DownloaderContract.PausedError.FILESIZE)));
            long downloadedSize = Long.parseLong(cursor.getString(cursor.getColumnIndex(DownloaderContract.PausedError.DOWNLOADED_SiZE)));
            int status = cursor.getInt(cursor.getColumnIndex(DownloaderContract.PausedError.LAST_DOWNLOAD_STATUS));
            int id = (int)cursor.getLong(cursor.getColumnIndex(DownloaderContract.PausedError._ID));
            int progress = 0;
            if(filesize > 0)
                progress = (int)((downloadedSize*100)/filesize);

         //   Log.e(TAG,title+" "+filesize+" "+progress+" "+status);

            DownloadInformation information = new DownloadInformation(title,progress,filesize,downloadedSize);
            information.setDownloadUrl(url);
            information.setId(id);
            information.setDownloadStatus(status);
            information.setSavePath(save_uri);
            list.add(information);
        }while(cursor.moveToNext());
        return list;
    }
    public static HashMap changeArrayListToHashMap(ArrayList list){
        if(list == null || list.size() == 0)
            return null;
        HashMap map = new HashMap();
        for(int i=0;i<list.size();i++){
            DownloadInformation information = (DownloadInformation) list.get(i);
            Log.e(TAG,information.getTitle());
            map.put(information.getId(),information);
        }
        return map;
    }
    public static void uploadData(HashMap pauseErrorData,ArrayList successData,Context context){
        ArrayList pauseErrorList = MapToArrayList(pauseErrorData);
        Intent intent = new Intent(context, BackgroundDataUploadingService.class);
        intent.putParcelableArrayListExtra(UPLOAD_SUCCESS_KEY,successData);
        intent.putParcelableArrayListExtra(UPLOAD_PAUSE_ERROR_KEY,pauseErrorList);
        context.startService(intent);
    }

    public static boolean checkIfInternetAvailable(Context context){
        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }

    public static String getMimeType(String uri){
        uri = uri.replaceAll(" ","_");
       // Log.e(TAG,"URI "+uri);
        String extension = MimeTypeMap.getFileExtensionFromUrl(uri);
        if(extension != null)
            return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        return null;
    }
    public static boolean checkFileExist(String uri){
        File file = new File(uri);
        return file.exists();
    }

    public static String convertSize(long size){
        long GB = ((long)1024*1024*1024);
        long MB = ((long)1024*1024);
        long KB = 1024;
        double actual_size = 0;
        String data = "";
        DecimalFormat df = new DecimalFormat("#.##");
        if(GB<=size){
            actual_size = ((double) size/GB);
            data = df.format(actual_size)+" "+"GB";

        }else if(MB<=size){
            actual_size = ((double)size/MB);
            data = df.format(actual_size)+" "+"MB";
        }else if(KB<=size){
            actual_size = ((double)size/KB);
            data = df.format(actual_size)+" "+"KB";
        }else{
            data = size+" "+"Bytes";
        }
        return data;
    }

}
