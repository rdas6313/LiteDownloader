package com.example.rdas6313.litedownloader;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import com.example.litedownloaderapi.Manager;
import com.example.litedownloaderapi.Request;
import com.example.rdas6313.litedownloader.backgroundDownload.BackgroundDownloaderService;
import com.example.rdas6313.litedownloader.backgroundDownload.CallBackListener;

import java.util.ArrayList;

/**
 * Created by rdas6313 on 9/2/18.
 */

public class ActiveDownloadFragment extends Fragment implements CallBackListener,ButtonListener{

    private final String TAG = ActiveDownloadFragment.class.getName();

    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private Adapter adapter;
    private BackgroundDownloaderService service;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.activedownload_fragment,container,false);
        recyclerView = (RecyclerView)root.findViewById(R.id.listView);
        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        layoutManager = new LinearLayoutManager(getContext());
        adapter = new Adapter(getContext(),this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
//        listener = (CommunicationListener) getContext();
        ItemTouchHelper touchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int pos = viewHolder.getAdapterPosition();
                int download_id = adapter.getDownloadInformation(pos).getId();
                adapter.remove(pos);
                /*if(listener != null)
                    listener.removeOngoingDownlaod(download_id);*/
                if(service != null){
                    service.removeRunningDownload(download_id);
                }
            }
        });
        touchHelper.attachToRecyclerView(recyclerView);
    }


    @Override
    public void onAddDownload(int id, String title, String downlaod_url, String save_Path,long fileSize,long downloadedSize) {
        int progress = 0;
        if(fileSize>0)
            progress = (int)((downloadedSize*100)/fileSize);
        DownloadInformation information = new DownloadInformation(title,progress,fileSize,downloadedSize);
        information.setId(id);
        information.setDownloadStatus(DownloadInformation.RESUME_DOWNLOAD);
        information.setDownloadUrl(downlaod_url);
        information.setSavePath(save_Path);
        adapter.add(information);
    }

    @Override
    public void onProgress(int id, int progress, long downloadedSize, long fileSize) {
        int Id = adapter.getAdapterPositionByDownloadId(id);
        if(Id == -1)
            return;
        DownloadInformation information = adapter.getDownloadInformation(Id);
        if(information == null)
            return;
        information.setProgress(progress);
        information.setDownloadedSize(downloadedSize);
        information.setFileSize(fileSize);
        if(information.getDownloadStatus() == DownloadInformation.PAUSE_DOWNLOAD || information.getDownloadStatus() == DownloadInformation.CANCEL_DOWNLOAD)
            return;
        Adapter.ViewHolder holder = (Adapter.ViewHolder) recyclerView.findViewHolderForAdapterPosition(Id);
        if(holder != null)
            holder.setData(information);

    }

    @Override
    public void onError(int id, int errorCode, String errorMsg,Object object) {
        Log.e(TAG,"Id "+id+" "+errorCode+" "+errorMsg);
        adapter.remove(adapter.getAdapterPositionByDownloadId(id));
    }

    @Override
    public void onSuccess(Request request) {
        Log.i(TAG,"id "+request.getId()+" Success");
        adapter.remove(adapter.getAdapterPositionByDownloadId(request.getId()));
    }

    @Override
    public void onGettingDownloads(ArrayList list) {}



    @Override
    public void itemButtonClick(int id,View v,int status) {
        DownloadInformation information = adapter.getDownloadInformation(id);
        ImageButton itemBtn = (ImageButton) v;
        switch (status){
            case DownloadInformation.RESUME_DOWNLOAD:
                information.setDownloadStatus(DownloadInformation.PAUSE_DOWNLOAD);
                /*listener.onpauseDownload(information.getId(),status);//sending download id*/
                if(service != null){
                    service.pauseDownload(information.getId());
                }
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Intent intent = new Intent(getContext(),BackgroundDownloaderService.class);
        getContext().bindService(intent,connection,0);
    }

    @Override
    public void onPause() {
        super.onPause();
        if(service != null)
            service.setRunninglistener(null);
        getContext().unbindService(connection);
    }

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder iBinder) {
            BackgroundDownloaderService.MyBinder myBinder = (BackgroundDownloaderService.MyBinder)iBinder;
            service = myBinder.getService();
            service.setRunninglistener(ActiveDownloadFragment.this);
            ArrayList list = service.getRunningDownloads();
            if(list != null && list.size()>0){
                adapter.clearData();
                adapter.add(list);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            service = null;
        }
    };

}
