package com.example.rdas6313.litedownloader;


import android.Manifest;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Environment;
import android.os.IBinder;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.example.rdas6313.litedownloader.backgroundDownload.BackgroundDownloaderService;


public class MainActivity extends AppCompatActivity implements View.OnClickListener,CommunicationListener{

    private final String TAG = MainActivity.class.getName();

    private final String ACTIVE_FRAG = "active_fragment";
    private ActiveDownloadFragment activeDownloadFragment;
    private boolean isbound;
    private BackgroundDownloaderService service;
    private FloatingActionButton addDownloadBtn;

    private final String PAUSE_ERROR_FRAG = "pause_error_fagment";
    private PauseErrorFragment pauseErrorFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        activeDownloadFragment = new ActiveDownloadFragment();
        pauseErrorFragment = new PauseErrorFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();

        fragmentManager.beginTransaction().add(R.id.container,activeDownloadFragment,ACTIVE_FRAG)
                .add(R.id.container,pauseErrorFragment,PAUSE_ERROR_FRAG).commit();

        addDownloadBtn = (FloatingActionButton)findViewById(R.id.addDownloadBtn);
        addDownloadBtn.setOnClickListener(this);
    }

   /* private void TestStart(){
        String url = "http://media.djmazadownload.xyz/music/Singles/Boond%20Boond%20Mein%20-%20Hate%20Story%204%20-190Kbps%20%5BDJMaza.Fun%5D.mp3";
        String uri = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();

        Intent intent = new Intent(this,BackgroundDownloaderService.class);
        Bundle bundle = new Bundle();
        bundle.putString("URL",url);
        bundle.putString("URI",uri);
        bundle.putString("TITLE","Boond Boond.mp3");
        intent.putExtras(bundle);
        startService(intent);
    }*/

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
    public void onresumeDownload(int id,int status, String downloadUrl,String savePath,String filename) {
        if(service != null){
            service.removePausedErrorDownload(id);
            service.startDownload(filename,downloadUrl,savePath);
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
}
