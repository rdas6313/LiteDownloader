package com.example.litedownloaderapi;


import com.example.litedownloaderapi.Interfaces.DownloadEventListener;
import com.example.litedownloaderapi.Interfaces.DownloadManager;

import java.util.HashMap;
import java.util.concurrent.Future;

/**
 * Created by rdas6313 on 11/1/18.
 */

public class Manager implements DownloadManager {
    private Communication communication;
    private static Manager ourInstance;
    private final int MAX_DOWNLOADS_IDS = 100;
    private int idGenerator = 0;
    private HashMap data;

    private Manager(int numberOfConcurrentThreads) {
        communication = Communication.getInstance();
        ManagerUtils.init(numberOfConcurrentThreads);
        data = new HashMap();
    }

    public static Manager getInstance(int numberOfConcurrentThreads) {
        if(ourInstance == null) {
            ourInstance = new Manager(numberOfConcurrentThreads);
        }
        return ourInstance;
    }

    public void bind(DownloadEventListener listener){
        if(communication != null)
            communication.setEventListener(listener);
    }

    public void unbind(){
        if(communication != null)
            communication.setEventListener(null);
    }

    @Override
    public int push(Request request) {
        idGenerator++;
        request.setId(idGenerator);
        Future future = ManagerUtils.addTask(request);
        data.put(idGenerator,future);
        return idGenerator;
    }

    @Override
    public boolean pause(int id) {
        if(!data.containsKey(id))
            return false;
        Future future = (Future) data.get(id);
        boolean isTaskPaused = ManagerUtils.cancelTask(future);
        data.remove(id);
        return isTaskPaused;
    }

    @Override
    public boolean cancel(int id) {
        if(!data.containsKey(id))
            return false;
        Future future = (Future) data.get(id);
        boolean isTaskCancelled = ManagerUtils.cancelTask(future);
        data.remove(id);
        return isTaskCancelled;
    }

    public void release(){
        if(data != null)
            data.clear();
        idGenerator = 0;
        ManagerUtils.destroy();
        communication = null;
        ourInstance = null;
    }

}
