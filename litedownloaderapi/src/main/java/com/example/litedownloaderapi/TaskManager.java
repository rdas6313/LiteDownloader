package com.example.litedownloaderapi;

import com.example.litedownloaderapi.Interface.LiteDownloadListener;
import com.example.litedownloaderapi.Interface.LiteDownloader;
import com.example.litedownloaderapi.Interface.Request;

import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by rdas6313 on 4/4/18.
 */

public final class TaskManager implements LiteDownloader {

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
    }

    public static TaskManager getManager(){
        if(taskManager == null)
            taskManager = new TaskManager();
        return taskManager;
    }

    private void initTask(int number){
        for(int i=0;i<number;i++){
            Task task = new Task(taskList,callBack);
            tasks[i] = task;
            task.start();
        }
    }

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
            callBack = null;
        }
        taskManager = null;
    }

    private void stopTasks(){
        if(tasks != null){
            for(int i=0;i<tasks.length;i++){
                tasks[i].cancelTask();
                tasks[i] = null;
            }
            tasks = null;
        }
    }

    @Override
    public void add(Request request) {

    }

    @Override
    public void pause(int id) {

    }

    @Override
    public void resume(int id) {

    }

    @Override
    public void cancel(int id) {

    }

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

    @Override
    public void setCallbackListener(LiteDownloadListener listener) {
        if(callBack != null){
            callBack.setCallback(listener);
        }
    }

}
