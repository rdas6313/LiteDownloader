package com.example.litedownloaderapi;

import com.example.litedownloaderapi.Interface.LiteDownloadListener;
import com.example.litedownloaderapi.Interface.LiteDownloader;
import com.example.litedownloaderapi.Interface.Request;

import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by rdas6313 on 4/4/18.
 * TaskManager class helps to initialize all the threads and clear the memory when this api is no longer needed
 * and helps to pause,resume,cancel and cancel all downlaod.
 */

public final class TaskManager implements LiteDownloader {

    private int Id;
    private static TaskManager taskManager;
    private BlockingQueue<DownloadRequest>taskList;
    private ArrayList<DownloadRequest>searchList;
    private Task[] tasks;
    private CallBack callBack;

    private TaskManager(){
        taskList = new LinkedBlockingQueue<>();
        searchList = new ArrayList<>();
        int thread_num = Runtime.getRuntime().availableProcessors();
        tasks = new Task[thread_num];
        callBack = new CallBack();
        initTask(thread_num);
        Id = 1;
    }

    public static TaskManager getManager(){
        if(taskManager == null)
            taskManager = new TaskManager();
        return taskManager;
    }

    /**
     * initializing all Task Thread to handel Download
     * @param number
     */
    private void initTask(int number){
        for(int i=0;i<number;i++){
            Task task = new Task(taskList,callBack);
            tasks[i] = task;
            task.start();
        }
    }

    /**
     * clearing Memory
     */
    @Override
    public void clear(){
        stopTasks();
        if(taskList != null){
            taskList.clear();
            taskList = null;
        }
        if(searchList != null){
            searchList.clear();
            searchList = null;
        }
        if(callBack != null){
            callBack.setCallback(null);
            callBack.clearCallBack();
            callBack = null;
        }
        taskManager = null;
    }

    /**
     * stoping all Task Thread
     */
    private void stopTasks(){
        if(tasks != null){
            for(int i=0;i<tasks.length;i++){
                tasks[i].cancelTask();
                tasks[i] = null;
            }
            tasks = null;
        }
    }

    /**
     * add Download to execute using Task Thread
     * @param request
     * @return
     */
    @Override
    public int add(Request request) {
        if(taskList != null && searchList != null){
            if(request != null && taskList != null && searchList != null){
                DownloadRequest req = (DownloadRequest) request;
                req.setId(Id++);
                req.setDownloadCancel(false);
                taskList.add(req);
                searchList.add(req);
                return req.getId();
            }
        }
        return -1;
    }

    /**
     * pause Download
     * @param id
     * @return
     */
    @Override
    public boolean pause(int id) {
        if(searchList != null){
            for(int i=0;i<searchList.size();i++){
                DownloadRequest request = (DownloadRequest) searchList.get(i);
                if(request.getId() == id && !request.isDownloadCancelled()){
                    request.setDownloadCancel(true);
                    searchList.remove(i);
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * cancel Download
     * @param id
     * @return
     */
    @Override
    public boolean cancel(int id) {
        if(searchList != null){
            for(int i=0;i<searchList.size();i++){
                DownloadRequest request = (DownloadRequest) searchList.get(i);
                if(request.getId() == id){
                    request.setDownloadCancel(true);
                    searchList.remove(i);
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * canceling All Download
     */
    @Override
    public void cancelAll() {
        if(searchList != null){
            for(int i=searchList.size()-1;i>=0;i--){
                DownloadRequest request = (DownloadRequest) searchList.get(i);
                request.setDownloadCancel(true);
                searchList.remove(i);
            }
            if(taskList != null){
                taskList.clear();
            }
        }
    }

    /**
     * setting callBack listener
     * @param listener
     */
    @Override
    public void setCallbackListener(LiteDownloadListener listener) {
        if(callBack != null){
            callBack.setCallback(listener);
        }
    }

}
