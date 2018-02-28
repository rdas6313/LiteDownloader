package com.example.rdas6313.litedownloader;


import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.litedownloaderapi.Request;
import com.example.rdas6313.litedownloader.backgroundDownload.BackgroundDownloaderService;
import com.example.rdas6313.litedownloader.backgroundDownload.CallBackListener;
import com.example.rdas6313.litedownloader.data.DownloaderContract;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class SuccessDownloadFragment extends Fragment implements ButtonListener,CallBackListener {

    private int LOADER_ID = 2;
    private RecyclerView recyclerView;
    private Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private BackgroundDownloaderService service;
    private boolean isDataLoadedFromService;
    private boolean isAdapterAlreadyLoaded;
    private final String TAG = SuccessDownloadFragment.class.getName();

    public SuccessDownloadFragment() {}


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_success_download, container, false);
        recyclerView = root.findViewById(R.id.listView);
        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        isAdapterAlreadyLoaded = false;
        adapter = new Adapter(getContext(),this);
        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(layoutManager);
        isDataLoadedFromService = false;

    }

    @Override
    public void itemButtonClick(int id, View v, int status) {

    }

    @Override
    public void onAddDownload(int id, String title, String downlaod_url, String save_Path, long fileSize, long downloadedSize) {}

    @Override
    public void onProgress(int id, int progress, long downloadedSize, long fileSize) {}

    @Override
    public void onError(int id, int errorCode, String errorMsg, Object objec) {}

    @Override
    public void onSuccess(Request request) {
        DownloadInformation information = new DownloadInformation(request.getFilename(),100,request.getFileSize(),request.getDownloadedSize());
        information.setDownloadStatus(DownloadInformation.SUCCESS_DOWNLOAD);
        information.setDownloadUrl(request.getDownloadUrl());
        information.setSavePath(request.getSaveUri());
        information.setId(request.getId());
        adapter.add(information);
    }

    @Override
    public void onPause() {
        super.onPause();
        if(service != null)
            service.setSuccessDownloadListener(null);
        getContext().unbindService(connection);
    }

    @Override
    public void onResume() {
        super.onResume();
        Intent intent = new Intent(getContext(),BackgroundDownloaderService.class);
        getContext().bindService(intent,connection,0);
    }

    @Override
    public void onGettingDownloads(ArrayList list) {
        if(!isAdapterAlreadyLoaded){
            adapter.clearData();
            adapter.add(list);
            isAdapterAlreadyLoaded = true;
        }
    }

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            BackgroundDownloaderService.MyBinder myBinder = (BackgroundDownloaderService.MyBinder)binder;
            service = myBinder.getService();
            service.setSuccessDownloadListener(SuccessDownloadFragment.this);
            if(!isAdapterAlreadyLoaded){
                ArrayList<DownloadInformation> list = (ArrayList<DownloadInformation>) service.getSuccessDownloadList();
                if(list != null && list.size() > 0){
                    adapter.clearData();
                    adapter.add(list);
                    isAdapterAlreadyLoaded = true;
                }
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            service = null;
        }
    };


}
