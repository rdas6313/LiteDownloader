package com.example.rdas6313.litedownloader.backgroundDownload;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.util.Log;

import com.example.rdas6313.litedownloader.DownloadInformation;
import com.example.rdas6313.litedownloader.Utilities;
import com.example.rdas6313.litedownloader.data.DownloaderContract;

import java.util.ArrayList;
import java.util.HashMap;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions and extra parameters.
 */
public class BackgroundDataUploadingService extends IntentService {

    private final static String TAG = BackgroundDataUploadingService.class.getName();

    public BackgroundDataUploadingService() {
        super("BackgroundDataUploadingService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if(intent != null && intent.hasExtra(Utilities.UPLOAD_PAUSE_ERROR_KEY)){
            ArrayList<DownloadInformation>list = intent.getParcelableArrayListExtra(Utilities.UPLOAD_PAUSE_ERROR_KEY);
            uploadPauseErrorData(list);
        }
    }

    private void uploadPauseErrorData(ArrayList<DownloadInformation>list){
        getContentResolver().delete(DownloaderContract.PausedError.CONTENT_URI,null,null);
        if(list == null || list.size() == 0)
            return;
        ContentValues values[] = new ContentValues[list.size()];
        int i;
        for(i=0;i<list.size();i++){
            ContentValues value = new ContentValues();
            DownloadInformation information = (DownloadInformation) list.get(i);
//            Log.e(TAG,information.getTitle()+" "+information.getFileSize()+" "+information.getProgress()+" "+
//            information.getDownloadStatus());
            value.put(DownloaderContract.PausedError.TITLE,information.getTitle());
            value.put(DownloaderContract.PausedError.DOWNLOAD_URL,information.getDownloadUrl());
            value.put(DownloaderContract.PausedError.SAVE_URI,information.getSavePath());
            value.put(DownloaderContract.PausedError.FILESIZE,information.getFileSize());
            value.put(DownloaderContract.PausedError.DOWNLOADED_SiZE,information.getDownloadedSize());
            value.put(DownloaderContract.PausedError.LAST_DOWNLOAD_STATUS,information.getDownloadStatus());
            values[i] = value;
        }

        getContentResolver().bulkInsert(DownloaderContract.PausedError.CONTENT_URI,values);
        //Log.e(TAG,"Deleted Entry "+a+" Inserted Entry "+b);
    }


}
