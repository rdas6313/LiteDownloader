package com.example.rdas6313.litedownloader;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.example.rdas6313.litedownloader.backgroundDownload.BackgroundDownloaderService;

public class MainActivity extends AppCompatActivity {

    private final String TAG = MainActivity.class.getName();

    private final String ACTIVE_FRAG = "active_fragment";
    private ActiveDownloadFragment activeDownloadFragment;
    private boolean isbound;
    private BackgroundDownloaderService service;

    private final String PAUSE_ERROR_FRAG = "pause_error_fagment";
    private PauseErrorFragment pauseErrorFragment;

    private ViewPager pager;
    private PagerAdapter pagerAdapter;

    private TabLayout tabLayout;

    private SuccessDownloadFragment successDownloadFragment;
    private final int RESPONSE_CODE = 10023;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkPermissions();
        Utilities.changeActivityAliveValue(true,getApplication());
        activeDownloadFragment = new ActiveDownloadFragment();
        pauseErrorFragment = new PauseErrorFragment();
        successDownloadFragment = new SuccessDownloadFragment();

        pager = (ViewPager)findViewById(R.id.view_pager);
        pagerAdapter = new PagerAdapter(getSupportFragmentManager());
        pager.setAdapter(pagerAdapter);

        tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(pager);

        //throw new RuntimeException("Force Crash");
    }

    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE) && ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    // Todo:- should explain why u need these permissions
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, RESPONSE_CODE);
                } else {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, RESPONSE_CODE);
                }
            }
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case RESPONSE_CODE:
                Log.e(TAG,"Permission " + grantResults.length);
                if(grantResults.length == 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED){
                      finish();
                }

        }
    }

    @Override
    protected void onDestroy() {
        Utilities.changeActivityAliveValue(false,getApplication());
        if(!Utilities.hasThereAnyRunnningTask(getApplication())){
            Intent intent = new Intent(this,BackgroundDownloaderService.class);
            stopService(intent);
        }
        super.onDestroy();
    }


    private class PagerAdapter extends FragmentStatePagerAdapter {

         private final String ACTIVE_DOWNLOAD_TITLE = "OnGoing";
         private final String PAUSE_ERROR_DOWNLOAD_TITLE = "Paused/Error";
         private final String SUCCESS_DOWNLOAD_TITLE = "Success";
         private final int NUM_OF_PAGE = 3;

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
                case 2:
                    return successDownloadFragment;
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
                 case 2:
                     return SUCCESS_DOWNLOAD_TITLE;
                 default:
                     return null;
             }
         }
     }

}
