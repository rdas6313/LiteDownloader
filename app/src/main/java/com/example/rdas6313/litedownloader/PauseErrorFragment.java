package com.example.rdas6313.litedownloader;


import android.content.ComponentName;
import android.content.ContentUris;
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
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.rdas6313.litedownloader.backgroundDownload.BackgroundDownloaderService;
import com.example.rdas6313.litedownloader.backgroundDownload.CallBackListener;
import com.example.rdas6313.litedownloader.data.DownloaderContract;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class PauseErrorFragment extends Fragment implements ButtonListener,LoaderManager.LoaderCallbacks<Cursor>{

    private final static String TAG = PauseErrorFragment.class.getName();
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private Adapter adapter;
    private BackgroundDownloaderService service;
    private boolean isAdapterAlreadyLoaded;
    private final int PAUSE_ERROR_LOADER_ID = 1;

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
        isAdapterAlreadyLoaded = false;
        layoutManager = new LinearLayoutManager(getContext());
        adapter = new Adapter(getContext(),this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
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
                removePausedErrorDownload(downlaod_id);
            }
        });
        touchHelper.attachToRecyclerView(recyclerView);

        getActivity().getSupportLoaderManager().initLoader(PAUSE_ERROR_LOADER_ID,null,this);
    }



    @Override
    public void itemButtonClick(int id, View v, int status) {
        DownloadInformation information = adapter.getDownloadInformation(id);
        ImageButton itemBtn = (ImageButton) v;
        switch (status){
            case DownloadInformation.CANCEL_DOWNLOAD:
            case DownloadInformation.PAUSE_DOWNLOAD:
                if(!Utilities.checkIfInternetAvailable(getContext())) {
                    Toast.makeText(getContext(), R.string.checkInternet, Toast.LENGTH_SHORT).show();
                    break;
                }
                if(service == null){
                    Bundle bundle = new Bundle();
                    bundle.putString(Utilities.DOWNLOAD_FILENAME,information.getTitle());
                    bundle.putString(Utilities.SAVE_DOWNLOAD_URI,information.getSavePath());
                    bundle.putString(Utilities.DOWNLOAD_URL,information.getDownloadUrl());
                    bundle.putLong(Utilities.DOWNLOAD_FILE_SIZE,information.getFileSize());
                    bundle.putLong(Utilities.DOWNLOAD_DOWNLOADED_SIZE,information.getDownloadedSize());
                    bundle.putBoolean(Utilities.SHOULD_REMOVE_PAUSE_ERROR_DOWNLOAD,true);
                    bundle.putInt(Utilities.DOWNLOAD_ID,information.getId());
                    Intent intent = new Intent(getContext(),BackgroundDownloaderService.class);
                    intent.putExtras(bundle);
                    getContext().startService(intent);
                }else{
                    service.startDownload(information.getTitle(),information.getDownloadUrl(),information.getSavePath(),information.getFileSize(),information.getDownloadedSize(),false);
                }
                removePausedErrorDownload(information.getId());
                adapter.remove(id);
                break;
        }
    }

    private void removePausedErrorDownload(int id){
        getContext().getContentResolver().delete(ContentUris.withAppendedId(DownloaderContract.PausedError.CONTENT_URI,id),null,null);
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
        service = null;
        getContext().unbindService(connection);
    }

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder iBinder) {
            BackgroundDownloaderService.MyBinder myBinder = (BackgroundDownloaderService.MyBinder)iBinder;
            service = myBinder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            service = null;
        }
    };

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getContext(), DownloaderContract.PausedError.CONTENT_URI,null,null,null,null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        ArrayList list = Utilities.ChangeCursorToArrayListForPauseError(data);
        if(list !=null && list.size()>0) {
            adapter.clearData();
            adapter.add(list);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.clearData();
    }
}
