package com.example.rdas6313.litedownloader;

import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.litedownloaderapi.DownloadRequest;
import com.example.litedownloaderapi.Interface.LiteDownloadListener;
import com.example.litedownloaderapi.Interface.LiteDownloader;
import com.example.litedownloaderapi.Interface.Request;
import com.example.litedownloaderapi.TaskManager;

import java.io.File;

public class MainActivity extends AppCompatActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

}
