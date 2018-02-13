package com.example.rdas6313.litedownloader;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.litedownloaderapi.Request;
import com.example.rdas6313.litedownloader.backgroundDownload.CallBackListener;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class PauseErrorFragment extends Fragment implements ButtonListener,CallBackListener{

    private final static String TAG = PauseErrorFragment.class.getName();
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private Adapter adapter;

    public PauseErrorFragment() {}


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_pause_error, container, false);
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
    }

    public void setDownloadsData(ArrayList<DownloadInformation> list){
        if(list != null && list.size()>0) {
            adapter.clearData();
            adapter.add(list);
        }
    }

    @Override
    public void itemButtonClick(int id, View v, int status) {

    }


    @Override
    public void onAddDownload(int id, String title, String downlaod_url, String save_Path) {}

    @Override
    public void onProgress(int id, int progress, long downloadedSize, long fileSize) {}

    @Override
    public void onError(int id, int errorCode, String errorMsg, Object object) {
        DownloadInformation information = (DownloadInformation) object;
        adapter.add(information);
        Log.e(TAG,"onError Called "+id);
    }

    @Override
    public void onSuccess(Request request) {}
}
