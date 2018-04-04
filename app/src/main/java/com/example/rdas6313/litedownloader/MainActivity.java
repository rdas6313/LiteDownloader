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

public class MainActivity extends AppCompatActivity implements LiteDownloadListener {

    private TextView fnameView,statusView;
    private ProgressBar progressBarView;
    private LiteDownloader liteDownloader;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        statusView = (TextView)findViewById(R.id.status);
        fnameView = (TextView)findViewById(R.id.filename);
        progressBarView = (ProgressBar)findViewById(R.id.progressBar);
        String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
        String url = "http://media.djmazadownload.xyz/music/Singles/Ghar%20Se%20Nikalte%20Hi%20-%20DJMaza.Fun%20-190Kbps.mp3";
        String name = "abc";
        statusView.setText("No Status");
        Request request = DownloadRequest.getRequest();
        request.setDownloadUrl(url).setDir(path).setFileName(name);
        liteDownloader = TaskManager.getManager();
        liteDownloader.add(request);
        liteDownloader.setCallbackListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        liteDownloader.clear();
    }

    @Override
    public void onStart(int id) {
        statusView.setText("Download Started");
    }

    @Override
    public void onError(int id, String errorMsg, int errorCode) {
        statusView.setText("Downlaod Error");
        Log.e("MainActivity",errorCode+" "+errorMsg);
    }

    @Override
    public void onProgress(int id, long fileSize, long downloadedSize, int progress) {
        progressBarView.setProgress(progress);
        statusView.setText("Download Progressing");
    }

    @Override
    public void onSuccess(int id) {
        progressBarView.setProgress(100);
        statusView.setText("Download Success");
    }
}
