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
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.example.rdas6313.litedownloader.backgroundDownload.BackgroundDownloaderService;
import com.example.rdas6313.litedownloader.data.DownloaderContract;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity implements View.OnClickListener,LoaderManager.LoaderCallbacks<Cursor>{

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

    private ViewPager pager;
    private final int NUM_OF_PAGE = 2;
    private PagerAdapter pagerAdapter;

    private TabLayout tabLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        alreadySentDataToService = false;

        activeDownloadFragment = new ActiveDownloadFragment();
        pauseErrorFragment = new PauseErrorFragment();

        addDownloadBtn = (FloatingActionButton)findViewById(R.id.addDownloadBtn);
        addDownloadBtn.setOnClickListener(this);
        Utilities.changeActivityAliveValue(true,getApplication());
        if(!Utilities.isServiceAlive(getApplication())){
            getSupportLoaderManager().initLoader(PAUSE_ERROR_LOADER_ID,null,this);
            Intent intent = new Intent(this,BackgroundDownloaderService.class);
            startService(intent);
            bindService(intent,connection,0);
        }

        pager = (ViewPager)findViewById(R.id.view_pager);
        pagerAdapter = new PagerAdapter(getSupportFragmentManager());
        pager.setAdapter(pagerAdapter);

        tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(pager);
    }


    @Override
    protected void onPause() {
        super.onPause();
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
        if(data != null)
            data.close();
        if(service != null) {
            service.setPauseErrorData(list);
            alreadySentDataToService = true;
        }
        Log.e(TAG,"On LOAD FINISHED");
    }


    @Override
    public void onLoaderReset(Loader loader) {
    }

     private class PagerAdapter extends FragmentStatePagerAdapter{

         private final String ACTIVE_DOWNLOAD_TITLE = "OnGoing";
         private final String PAUSE_ERROR_DOWNLOAD_TITLE = "Paused/Error";

        public PagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position){
                case 0:
                    return activeDownloadFragment;
                case 1:
                    return pauseErrorFragment;
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return NUM_OF_PAGE;
        }

         @Override
         public CharSequence getPageTitle(int position) {
             switch (position){
                 case 0:
                     return ACTIVE_DOWNLOAD_TITLE;
                 case 1:
                     return PAUSE_ERROR_DOWNLOAD_TITLE;
                 default:
                     return null;
             }
         }
     }
}
