package com.example.rdas6313.litedownloader;


import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.FileProvider;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import com.example.litedownloaderapi.Request;
import com.example.rdas6313.litedownloader.backgroundDownload.BackgroundDownloaderService;
import com.example.rdas6313.litedownloader.backgroundDownload.CallBackListener;
import com.example.rdas6313.litedownloader.data.DownloaderContract;

import java.io.File;
import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class SuccessDownloadFragment extends Fragment implements ButtonListener,LoaderManager.LoaderCallbacks<Cursor>{


    private RecyclerView recyclerView;
    private Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private BackgroundDownloaderService service;
    private boolean isAdapterAlreadyLoaded;
    private final String TAG = SuccessDownloadFragment.class.getName();
    private final int SUCCESS_DOWNLOAD_LOADER_ID = 12;

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
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int pos = viewHolder.getAdapterPosition();
                DownloadInformation information = adapter.getDownloadInformation(pos);
                adapter.remove(pos);
                removeSuccessfullDownload(information.getId());
            }
        });
        itemTouchHelper.attachToRecyclerView(recyclerView);
        getActivity().getSupportLoaderManager().initLoader(SUCCESS_DOWNLOAD_LOADER_ID,null,this);
    }

    @Override
    public void itemButtonClick(int id, View v, int status) {
        DownloadInformation information = (DownloadInformation)adapter.getDownloadInformation(id);
        openFile(information);
    }

    private void openFile(DownloadInformation information){
        if(information != null) {
            String uri_path = information.getSavePath()+"/"+information.getTitle();
            if(!Utilities.checkFileExist(uri_path)){
                Toast.makeText(getContext(), R.string.file_not_exists,Toast.LENGTH_SHORT).show();
                return;
            }
            Uri uri = null;
            String mimeType = Utilities.getMimeType(uri_path);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.N){
                uri = FileProvider.getUriForFile(getContext(),getString(R.string.fileProvider_auth),new File(uri_path));
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }else{
                uri = Uri.fromFile(new File(uri_path));
            }
            if(mimeType == null)
                intent.setDataAndType(uri,"*/*");
            else
                intent.setDataAndType(uri,mimeType);
            if(intent.resolveActivity(getContext().getPackageManager()) != null) {
                startActivity(intent);
            }
        }
    }

    private void removeSuccessfullDownload(int id){
        getContext().getContentResolver().delete(ContentUris.withAppendedId(DownloaderContract.Success.CONTENT_URI,id),null,null);
    }

    @Override
    public void onPause() {
        super.onPause();
        getContext().unbindService(connection);
    }

    @Override
    public void onResume() {
        super.onResume();
        Intent intent = new Intent(getContext(),BackgroundDownloaderService.class);
        getContext().bindService(intent,connection,0);
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
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getContext(),DownloaderContract.Success.CONTENT_URI,null,null,null,null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.e(TAG,"LOAD FINISED");
        ArrayList list = Utilities.changeCursorToArrayListForSuccess(data);
        adapter.clearData();
        adapter.add(list);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Log.e(TAG,"LOADER RESET");
        adapter.clearData();
    }
}
