package com.example.rdas6313.litedownloader;


import android.Manifest;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.Environment;
import android.os.IBinder;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.example.rdas6313.litedownloader.backgroundDownload.BackgroundDownloaderService;
import com.example.rdas6313.litedownloader.data.DownloaderContract;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity implements View.OnClickListener,CommunicationListener,LoaderManager.LoaderCallbacks<Cursor>{

    private final String TAG = MainActivity.class.getName();

    private final String ACTIVE_FRAG = "active_fragment";
    private ActiveDownloadFragment activeDownloadFragment;
    private boolean isbound;
    private BackgroundDownloaderService service;
    private FloatingActionButton addDownloadBtn;

    private final String PAUSE_ERROR_FRAG = "pause_error_fagment";
    private PauseErrorFragment pauseErrorFragment;

    private ArrayList<DownloadInformation> list;
    private boolean alreadySentDataToService;

    private final int PAUSE_ERROR_LOADER_ID = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        alreadySentDataToService = false;

        activeDownloadFragment = new ActiveDownloadFragment();
        pauseErrorFragment = new PauseErrorFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();

        fragmentManager.beginTransaction().add(R.id.container,activeDownloadFragment,ACTIVE_FRAG)
                .add(R.id.container,pauseErrorFragment,PAUSE_ERROR_FRAG).commit();

        addDownloadBtn = (FloatingActionButton)findViewById(R.id.addDownloadBtn);
        addDownloadBtn.setOnClickListener(this);
        Utilities.changeActivityAliveValue(true,getApplication());
        if(!Utilities.isServiceAlive(getApplication())){
            getSupportLoaderManager().initLoader(PAUSE_ERROR_LOADER_ID,null,this);
            Intent intent = new Intent(this,BackgroundDownloaderService.class);
            startService(intent);
            bindService(intent,connection,0);
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        if(service != null)
            service.setListeners(null,null);
        unbindService(connection);
        isbound = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = new Intent(this,BackgroundDownloaderService.class);
        bindService(intent,connection,0);
    }


    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            isbound = true;
            BackgroundDownloaderService.MyBinder object = (BackgroundDownloaderService.MyBinder)binder;
            service = object.getService();
            service.setListeners(activeDownloadFragment,pauseErrorFragment);
            activeDownloadFragment.setDownloadData(service.getRunningDownloads());
            pauseErrorFragment.setDownloadsData(service.getPausedErrorDownloads());
            if(list != null && !alreadySentDataToService){
                service.setPauseErrorData(list);
                alreadySentDataToService = true;
            }
            Log.e(TAG,"OnService Connected");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isbound = false;
            service = null;
            Log.e(TAG,"OnService DisConnected");
        }
    };

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(this,addDownloadActivity.class);
        startActivity(intent);
    }


    @Override
    public void onresumeDownload(int id,int status, String downloadUrl,String savePath,String filename,long filesize,long downloadedSize) {
        if(service != null){
            service.removePausedErrorDownload(id);
            service.startDownload(filename,downloadUrl,savePath,filesize,downloadedSize);
        }
    }

    @Override
    public void removeOngoingDownlaod(int id) {
        if(service != null){
            service.removeRunningDownload(id);
        }
    }

    @Override
    public void removePauseErrorDownload(int id) {
        if(service != null)
            service.removePausedErrorDownload(id);
    }

    @Override
    public void onpauseDownload(int id, int status) {
        if(service != null)
            service.pauseDownload(id);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Utilities.changeActivityAliveValue(false,getApplication());
        if(!Utilities.isServiceAlive(getApplication())){
            Intent intent = new Intent(this,BackgroundDownloaderService.class);
            stopService(intent);
        }
    }

    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, DownloaderContract.PausedError.CONTENT_URI,null,null,null,null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        list = Utilities.ChangeCursorToArrayListForPauseError(data);
        pauseErrorFragment.setDownloadsData(list);
      //  Utilities.sendPauseErrorDownloadDataToService(list,getApplication());
        if(service != null) {
            service.setPauseErrorData(list);
            alreadySentDataToService = true;
        }
        Log.e(TAG,"On LOAD FINISHED");
    }


    @Override
    public void onLoaderReset(Loader loader) {}
}
