package com.example.rdas6313.litedownloader;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.rdas6313.litedownloader.backgroundDownload.BackgroundDownloaderService;

import net.rdrei.android.dirchooser.DirectoryChooserConfig;
import net.rdrei.android.dirchooser.DirectoryChooserFragment;

import org.w3c.dom.Text;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class addDownloadActivity extends AppCompatActivity implements View.OnClickListener,View.OnFocusChangeListener,View.OnTouchListener,LoaderManager.LoaderCallbacks {

    private final static String TAG = addDownloadActivity.class.getName();

    private final int FETCH_DATA_INFO_LOADER_ID = 2012;
    private final String DOWNLOAD_URL = "download_url";

    private EditText urlView,filenameView,dir;
    private Button Btn;
    private BackgroundDownloaderService service;
    private TextView fileSize_View;
    private ProgressBar progressBar;
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
        progressBar = (ProgressBar)findViewById(R.id.loadProgressBar);
        progressBar.setVisibility(View.GONE);
        urlView = (EditText)findViewById(R.id.urlView);
        urlView.setOnFocusChangeListener(this);
        urlView.setOnTouchListener(this);
        filenameView = (EditText)findViewById(R.id.file_name_view);
        dir = (EditText)findViewById(R.id.save_folder);
        dir.setOnTouchListener(this);
        fileSize_View = (TextView)findViewById(R.id.fileSize);
        Btn = (Button)findViewById(R.id.downloadBtn);
        Btn.setOnClickListener(this);
    }

    private boolean checkDataIfError(){
        boolean isThereError = false;
        String url = urlView.getText().toString();
        String filename = filenameView.getText().toString();
        String location = dir.getText().toString();
        if(TextUtils.isEmpty(url)) {
            urlView.setError(getString(R.string.emptyFieldError));
            isThereError = true;
        }
        if(TextUtils.isEmpty(filename)) {
            filenameView.setError(getString(R.string.emptyFieldError));
            isThereError = true;
        }
        if(TextUtils.isEmpty(location)){
            dir.setError(getString(R.string.selectDirError));
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
        String save_uri = dir.getText().toString();
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



    private void chooseDir(){
        final DirectoryChooserConfig config = DirectoryChooserConfig.builder()
                .newDirectoryName("New Folder")
                .allowNewDirectoryNameModification(true)
                .build();
        final DirectoryChooserFragment mDialog = DirectoryChooserFragment.newInstance(config);
        mDialog.setDirectoryChooserListener(new DirectoryChooserFragment.OnFragmentInteractionListener() {
            @Override
            public void onSelectDirectory(@NonNull String path) {
                dir.setText(path);
                mDialog.dismiss();
            }

            @Override
            public void onCancelChooser() {
                mDialog.dismiss();
            }
        });
        mDialog.show(getFragmentManager(),null);
    }

    private void pasteData(){
        ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        String pastedata = "";
        if(clipboardManager.hasPrimaryClip()){
            ClipData.Item item = clipboardManager.getPrimaryClip().getItemAt(0);
            pastedata = item.getText().toString();
        }
        urlView.setText(pastedata);
    }

    private void clickDrawable(View v){
        switch (v.getId()){
            case R.id.urlView:
                pasteData();
                break;
            case R.id.save_folder:
                chooseDir();
                break;
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
       return clickRightDrawable(event,v);
    }

    private boolean clickRightDrawable(MotionEvent event,View v){
        EditText editText = (EditText)v;
        Drawable drawable[] = editText.getCompoundDrawables();
        if(drawable != null && drawable.length == 4){
            if(event.getAction() == MotionEvent.ACTION_DOWN){
                int x = (int) event.getRawX();
                if(x >= (editText.getRight() - drawable[2].getBounds().width())){
                    clickDrawable(v);
                    editText.setFocusable(true);
                    editText.requestFocus();
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        final String download_url = args.getString(DOWNLOAD_URL);
        return new AsyncTaskLoader(this) {
            @Override
            public Object loadInBackground() {
                try{
                    return loadDownloadData();
                }catch (Exception e){
                    Log.e(TAG,e.getMessage());
                }
                return null;
            }

            @Override
            protected void onStartLoading() {
                fileSize_View.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);
                forceLoad();
            }

            private long loadDownloadData(){
                long file_size = 0;
                HttpURLConnection connection = null;
                URL url = null;
                try{

                    url = new URL(download_url);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.connect();
                    if(connection.getResponseCode() != HttpURLConnection.HTTP_OK){
                        throw new Exception("Response Error "+connection.getResponseCode());
                    }

                    file_size = connection.getContentLength();

                }catch (MalformedURLException e){
                    Log.e(TAG,e.getMessage());
                    urlView.setError(getString(R.string.checkUrl));
                }catch (IOException e){
                    Log.e(TAG,e.getMessage());
                    Toast.makeText(getContext(),getString(R.string.fectchDownloadSizeError),Toast.LENGTH_SHORT).show();
                }catch (Exception e){
                    Log.e(TAG,e.getMessage());
                    Toast.makeText(getContext(),getString(R.string.unableToConnectError),Toast.LENGTH_SHORT).show();
                }finally {
                    if(connection != null)
                        connection.disconnect();
                    if(url != null)
                        url = null;
                }
                return file_size;
            }
        };
    }

    private String getFileName(String url_address){
        String filename = null;
        try{
            URL url = new URL(url_address);
            File file = new File(url.getPath().toString());
            filename = file.getName();
        }catch (Exception e){
            Log.e(TAG,"WRONG URL");
        }
        return filename;
    }

    @Override
    public void onLoadFinished(Loader loader, Object data) {
        progressBar.setVisibility(View.GONE);
        fileSize_View.setVisibility(View.VISIBLE);
        if(data == null) {
            fileSize_View.setText(getString(R.string.addDownloadUnableToFetch));
            return;
        }
        long fileSize = (long)data;
        fileSize_View.setText(getString(R.string.addDownloadFileSize,fileSize,"Bytes"));
        String url = urlView.getText().toString();
        if(url.length()>0)
            filenameView.setText(getFileName(url));
    }

    @Override
    public void onLoaderReset(Loader loader) {
        fileSize_View.setText(getString(R.string.unknownSize));
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        EditText editText = (EditText)v;
        String url = editText.getText().toString();
        if(!hasFocus && !TextUtils.isEmpty(url)){
            if(Utilities.checkIfInternetAvailable(this)){
                Bundle bundle = new Bundle();
                bundle.putString(DOWNLOAD_URL,url);
                getSupportLoaderManager().restartLoader(FETCH_DATA_INFO_LOADER_ID,bundle,this);
            }else
                Toast.makeText(this,getString(R.string.checkInternet),Toast.LENGTH_SHORT).show();
        }
    }
}
