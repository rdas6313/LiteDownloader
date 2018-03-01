package com.example.rdas6313.litedownloader;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.example.rdas6313.litedownloader.backgroundDownload.BackgroundDataUploadingService;
import com.example.rdas6313.litedownloader.backgroundDownload.BackgroundDownloaderService;
import com.example.rdas6313.litedownloader.data.DownloaderContract;

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
        while(cursor.moveToNext()){
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
        }
        return list;
    }

    public static ArrayList ChangeCursorToArrayListForPauseError(Cursor cursor){
        if(cursor == null)
            return null;
        ArrayList<DownloadInformation>list = new ArrayList<>();
        int key = -1;
        while(cursor.moveToNext()){
            String title = cursor.getString(cursor.getColumnIndex(DownloaderContract.PausedError.TITLE));
            String url = cursor.getString(cursor.getColumnIndex(DownloaderContract.PausedError.DOWNLOAD_URL));
            String save_uri = cursor.getString(cursor.getColumnIndex(DownloaderContract.PausedError.SAVE_URI));
            long filesize = Long.parseLong(cursor.getString(cursor.getColumnIndex(DownloaderContract.PausedError.FILESIZE)));
            long downloadedSize = Long.parseLong(cursor.getString(cursor.getColumnIndex(DownloaderContract.PausedError.DOWNLOADED_SiZE)));
            int status = cursor.getInt(cursor.getColumnIndex(DownloaderContract.PausedError.LAST_DOWNLOAD_STATUS));
            int progress = 0;
            if(filesize > 0)
                progress = (int)((downloadedSize*100)/filesize);

         //   Log.e(TAG,title+" "+filesize+" "+progress+" "+status);

            DownloadInformation information = new DownloadInformation(title,progress,filesize,downloadedSize);
            information.setDownloadUrl(url);
            information.setId(key--);
            information.setDownloadStatus(status);
            information.setSavePath(save_uri);
            list.add(information);
        }
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


}
