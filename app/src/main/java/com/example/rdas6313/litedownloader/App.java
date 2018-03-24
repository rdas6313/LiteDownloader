package com.example.rdas6313.litedownloader;

import android.app.Application;
import com.crashlytics.android.Crashlytics;
import io.fabric.sdk.android.Fabric;

/**
 * Created by rdas6313 on 17/2/18.
 */

public class App extends Application {
    public boolean isServiceAlive;
    public boolean isActivityAlive;
    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
        isServiceAlive = false;
        isActivityAlive = false;
    }
}
