package com.example.rdas6313.litedownloader;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

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
    private CommunicationListener listener;

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
       // recyclerView.addItemDecoration(new DividerItemDecoration(getContext(),DividerItemDecoration.VERTICAL));
        listener = (CommunicationListener) getContext();
        ItemTouchHelper touchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int pos = viewHolder.getAdapterPosition();
                int downlaod_id = adapter.getDownloadInformation(pos).getId();
                adapter.remove(pos);
                if(listener != null)
                    listener.removePauseErrorDownload(downlaod_id);
            }
        });
        touchHelper.attachToRecyclerView(recyclerView);
    }

    public void setDownloadsData(ArrayList<DownloadInformation> list){
        if(list != null && list.size()>0) {
            adapter.clearData();
            adapter.add(list);
        }
    }

    @Override
    public void itemButtonClick(int id, View v, int status) {
        DownloadInformation information = adapter.getDownloadInformation(id);
        ImageButton itemBtn = (ImageButton) v;
        switch (status){
            case DownloadInformation.CANCEL_DOWNLOAD:
            case DownloadInformation.PAUSE_DOWNLOAD:
                information.setDownloadStatus(DownloadInformation.RESUME_DOWNLOAD);
                listener.onresumeDownload(information.getId(),status,information.getDownloadUrl(),information.getSavePath(),information.getTitle());//sending download id here
                adapter.remove(id);
                break;
        }
    }


    @Override
    public void onAddDownload(int id, String title, String downlaod_url, String save_Path) {}

    @Override
    public void onProgress(int id, int progress, long downloadedSize, long fileSize) {}

    @Override
    public void onError(int id, int errorCode, String errorMsg, Object object) {
        DownloadInformation information = (DownloadInformation) object;
        if(information != null) {
            adapter.add(information);
        }
        Log.e(TAG,"onError Called "+id);
    }

    @Override
    public void onSuccess(Request request) {}
}
