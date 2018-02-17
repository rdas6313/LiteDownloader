package com.example.rdas6313.litedownloader;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.rdas6313.litedownloader.backgroundDownload.BackgroundDataUploadingService;
import com.example.rdas6313.litedownloader.backgroundDownload.BackgroundDownloaderService;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by rdas6313 on 11/2/18.
 */

public final class Utilities {
    public final static String DOWNLOAD_URL = "url";
    public final static String SAVE_DOWNLOAD_URI = "uri";
    public final static String DOWNLOAD_FILENAME = "filename";

    public final static String UPLOAD_PAUSE_ERROR_KEY = "key_pause_error";

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
    public static void uploadPauseErrorData(HashMap map, Context context){
        if(map == null || map.isEmpty())
            return;

        ArrayList<DownloadInformation>list = new ArrayList<>();
        for(Object key:map.keySet()){
            DownloadInformation information = (DownloadInformation)map.get(key);
            list.add(information);
        }

        Intent intent = new Intent(context, BackgroundDataUploadingService.class);
        intent.putParcelableArrayListExtra(UPLOAD_PAUSE_ERROR_KEY,list);
        context.startService(intent);
    }
}
