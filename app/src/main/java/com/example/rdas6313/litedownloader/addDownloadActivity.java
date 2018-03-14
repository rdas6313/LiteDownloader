package com.example.rdas6313.litedownloader;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.NonNull;
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
import android.widget.TextView;

import com.example.rdas6313.litedownloader.backgroundDownload.BackgroundDownloaderService;

import net.rdrei.android.dirchooser.DirectoryChooserConfig;
import net.rdrei.android.dirchooser.DirectoryChooserFragment;

import org.w3c.dom.Text;

public class addDownloadActivity extends AppCompatActivity implements View.OnClickListener,TextWatcher,View.OnTouchListener {

    private final String TAG = addDownloadActivity.class.getName();

    private EditText urlView,filenameView,dir;
    private Button Btn;
    private BackgroundDownloaderService service;
    private TextView fileSize_View;
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
        urlView.addTextChangedListener(this);
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
            urlView.setError("This Field Can't be empty");
            isThereError = true;
        }
        if(TextUtils.isEmpty(filename)) {
            filenameView.setError("This Field Can't be empty");
            isThereError = true;
        }
        if(TextUtils.isEmpty(location)){
            dir.setError("Select a Directory");
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

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {}

    @Override
    public void afterTextChanged(Editable s) {
        //Todo:- start AsyncTaskLoader Here
    }

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
}
