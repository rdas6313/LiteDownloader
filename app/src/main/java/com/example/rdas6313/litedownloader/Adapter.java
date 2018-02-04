package com.example.rdas6313.litedownloader;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by rdas6313 on 4/2/18.
 */

public class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder>{

    private ArrayList<DownloadInformation>data;
    private Context context;

    public Adapter(Context context){
        data = new ArrayList<>();
        this.context = context;
    }

    public void add(DownloadInformation information){
        if(data != null) {
            data.add(information);
            notifyDataSetChanged();
        }
    }

    public void add(ArrayList<DownloadInformation>informations){
        if(data != null){
            data.addAll(informations);
            notifyDataSetChanged();
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View root = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_layout,parent,false);
        ViewHolder holder = new ViewHolder(root);
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        DownloadInformation information = data.get(position);
        holder.setData(information);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public final class ViewHolder extends RecyclerView.ViewHolder{

        public TextView title,downloadSizeAndProgress;
        public ProgressBar progressBar;
        public ImageButton button;

        public ViewHolder(View item){
            super(item);
            title = (TextView)item.findViewById(R.id.downloadTitle);
            downloadSizeAndProgress = (TextView)item.findViewById(R.id.downloadSizeAndProgress);
            progressBar = (ProgressBar)item.findViewById(R.id.downloadProgressBar);
            button = (ImageButton)item.findViewById(R.id.ButtonView);
        }

        public void setData(DownloadInformation data){
            title.setText(data.getTitle());
            downloadSizeAndProgress.setText(context.getString(R.string.size_and_progress,data.getDownloadedSize(),data.getFileSize(),data.getProgress()));
            progressBar.setProgress(data.getProgress());
        }
    }
}
