package com.example.rdas6313.litedownloader;


import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.example.rdas6313.litedownloader.backgroundDownload.BackgroundDownloaderService;


public class MainActivity extends AppCompatActivity {

    private final String TAG = MainActivity.class.getName();

    private final String ACTIVE_FRAG = "active_fragment";
    private ActiveDownloadFragment activeDownloadFragment;
    private boolean isbound;
    private BackgroundDownloaderService service;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        activeDownloadFragment = new ActiveDownloadFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.container,activeDownloadFragment,ACTIVE_FRAG).commit();

    }

    /*private void TestStart(){
        String url = "http://media.djmazadownload.xyz/music/Singles/Boond%20Boond%20Mein%20-%20Hate%20Story%204%20-190Kbps%20%5BDJMaza.Fun%5D.mp3";
        String uri = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();

        Intent intent = new Intent(this,BackgroundDownloaderService.class);
        Bundle bundle = new Bundle();
        bundle.putString("URL",url);
        bundle.putString("URI",uri);
        bundle.putString("TITLE","Boond Boond.mp3");
        intent.putExtras(bundle);
        startService(intent);
    }
*/
    @Override
    protected void onPause() {
        super.onPause();
        if(service != null)
            service.setListener(null);
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
            service.setListener(activeDownloadFragment);
            activeDownloadFragment.setDownloadData(service.getRunningDownloads());
            Log.e(TAG,"OnService Connected");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isbound = false;
            service = null;
            Log.e(TAG,"OnService DisConnected");
        }
    };

}
