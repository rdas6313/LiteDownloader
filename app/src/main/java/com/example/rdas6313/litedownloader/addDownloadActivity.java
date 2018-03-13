package com.example.rdas6313.litedownloader;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Environment;
import android.os.IBinder;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.rdas6313.litedownloader.backgroundDownload.BackgroundDownloaderService;

import org.w3c.dom.Text;

public class addDownloadActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText urlView,filenameView;
    private Button Btn;
    private BackgroundDownloaderService service;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_download);
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("Add Download");
            actionBar.setElevation(0);
        }
        urlView = (EditText)findViewById(R.id.urlView);
        filenameView = (EditText)findViewById(R.id.file_name_view);
        Btn = (Button)findViewById(R.id.downloadBtn);
        Btn.setOnClickListener(this);
    }

    private boolean checkDataIfError(){
        boolean isThereError = false;
        String url = urlView.getText().toString();
        String filename = filenameView.getText().toString();
        if(TextUtils.isEmpty(url)) {
            urlView.setError("This Field Can't be empty");
            isThereError = true;
        }
        if(TextUtils.isEmpty(filename)) {
            filenameView.setError("This Field Can't be empty");
            isThereError = true;
        }
        return isThereError;
    }

    @Override
    public void onClick(View v) {
        if(checkDataIfError()){
           return;
        }
        String url = urlView.getText().toString();
        String filename = filenameView.getText().toString();
        String save_uri = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
        if(service != null){
            service.startDownload(filename,url,save_uri,0,0);
        }else{
            Bundle bundle = new Bundle();
            bundle.putString(Utilities.DOWNLOAD_FILENAME,filename);
            bundle.putString(Utilities.DOWNLOAD_URL,url);
            bundle.putString(Utilities.SAVE_DOWNLOAD_URI,save_uri);
            Intent intent = new Intent(this, BackgroundDownloaderService.class);
            intent.putExtras(bundle);
            startService(intent);
        }
        finish();
    }


    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = new Intent(this,BackgroundDownloaderService.class);
        bindService(intent,connection,0);
    }

    @Override
    protected void onPause() {
        unbindService(connection);
        service = null;
        super.onPause();
    }

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            BackgroundDownloaderService.MyBinder myBinder = (BackgroundDownloaderService.MyBinder)binder;
            service = myBinder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            service = null;
        }
    };
}
