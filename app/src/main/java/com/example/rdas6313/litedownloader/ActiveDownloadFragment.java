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
import android.widget.Button;
import android.widget.ImageButton;

import com.example.litedownloaderapi.Manager;
import com.example.litedownloaderapi.Request;
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
    private ButtonListener listener;

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
        listener = (ButtonListener) getContext();
    }

    public void setDownloadData(ArrayList<DownloadInformation>data){
        if(data!= null && data.size()>0) {
            adapter.clearData();
            adapter.add(data);
        }
    }

    @Override
    public void onAddDownload(int id, String title, String downlaod_url, String save_Path) {
        DownloadInformation information = new DownloadInformation(title,0,0,0);
        information.setId(id);
        information.setDownloadUrl(downlaod_url);
        information.setSavePath(save_Path);
        adapter.add(information);
    }

    @Override
    public void onProgress(int id, int progress, long downloadedSize, long fileSize) {
        int Id = adapter.getAdapterPosition(id);
        if(Id == -1)
            return;
        DownloadInformation information = adapter.getDownloadInformation(Id);
        information.setProgress(progress);
        information.setDownloadedSize(downloadedSize);
        information.setFileSize(fileSize);
        Adapter.ViewHolder holder = (Adapter.ViewHolder) recyclerView.findViewHolderForAdapterPosition(Id);
        holder.setData(information);

    }

    @Override
    public void onError(int id, int errorCode, String errorMsg,Object object) {
        Log.e(TAG,"Id "+id+" "+errorCode+" "+errorMsg);
        adapter.remove(adapter.getAdapterPosition(id));
    }

    @Override
    public void onSuccess(Request request) {
        Log.i(TAG,"id "+request.getId()+" Success");
        adapter.remove(adapter.getAdapterPosition(request.getId()));
    }

    @Override
    public void itemButtonClick(int id,View v,int status) {
        DownloadInformation information = adapter.getDownloadInformation(id);
        ImageButton itemBtn = (ImageButton) v;
        switch (status){
            case DownloadInformation.RESUME_DOWNLOAD:
                information.setDownloadStatus(DownloadInformation.PAUSE_DOWNLOAD);
                itemBtn.setImageResource(R.drawable.ic_play_arrow_black_24dp);
                listener.itemButtonClick(information.getId(),null,status);//sending download id
                break;
        }
    }

    /*private void TestUI(){
        for(int i=0;i<10;i++){
            DownloadInformation information = new DownloadInformation("Pal by arijit singh",10*i,100,72);
            adapter.add(information);
        }
    }*/
}
