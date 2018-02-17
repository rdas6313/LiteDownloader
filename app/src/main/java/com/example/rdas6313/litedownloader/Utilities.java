package com.example.rdas6313.litedownloader;

import android.app.Application;

/**
 * Created by rdas6313 on 11/2/18.
 */

public final class Utilities {
    public final static String DOWNLOAD_URL = "url";
    public final static String SAVE_DOWNLOAD_URI = "uri";
    public final static String DOWNLOAD_FILENAME = "filename";

    public static void changeServiceAliveValue(boolean value, Application application){
        App app = (App)application;
        app.isServiceAlive = value;
    }
    public static boolean isServiceAlive(Application application){
        App app = (App)application;
        return app.isServiceAlive;
    }

    public static boolean isActivityAlive(Application application){
        App app = (App)application;
        return app.isActivityAlive;
    }

    public static void changeActivityAliveValue(boolean value,Application application){
        App app = (App)application;
        app.isActivityAlive = value;
    }
}
