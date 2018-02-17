package com.example.rdas6313.litedownloader;

import android.app.Application;

/**
 * Created by rdas6313 on 17/2/18.
 */

public class App extends Application {
    public boolean isServiceAlive;
    public boolean isActivityAlive;
    @Override
    public void onCreate() {
        super.onCreate();
        isServiceAlive = false;
        isActivityAlive = false;
    }
}
