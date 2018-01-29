package com.example.litedownloaderapi;

import android.util.Log;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by rdas6313 on 11/1/18.
 * For Managing Thread.
 */

public class ManagerUtils {

    private static final String TAG = ManagerUtils.class.getName();
    private static ExecutorService executorService;


    public static void init(int numberOfThreads){
        if(executorService == null)
            executorService = Executors.newFixedThreadPool(numberOfThreads);
    }

    public static Future addTask(Request request){
        if(executorService == null)
            return null;
        Future futureObject = executorService.submit(new Task(request));
        return futureObject;
    }

    public static boolean cancelTask(Future future){
        if(future != null) {
            Log.e(TAG,"Trying to cancel Thread");
            return future.cancel(true);
        }
        return false;
    }


    public static void destroy(){
        if(executorService != null) {
            //executorService.shutdown();
            executorService.shutdownNow();
        }
    }
}
