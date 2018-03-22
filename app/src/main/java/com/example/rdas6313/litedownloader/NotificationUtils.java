package com.example.rdas6313.litedownloader;

import android.content.Context;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

/**
 * Created by rdas6313 on 21/3/18.
 */

public class NotificationUtils {
    private final static String TAG = NotificationUtils.class.getName();
    private static NotificationManagerCompat managerCompat;
    public static void initNotificationManager(Context context){
        managerCompat = NotificationManagerCompat.from(context);
    }
    public static NotificationCompat.Builder makeNotification(int id,Context context, String title, String content, int icon){
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setContentTitle(title)
                .setContentText(content)
                .setSmallIcon(icon)
                .setProgress(0,0,true);
        managerCompat.notify(id,builder.build());
        return builder;
    }
    public static void changeProgress(int progress,NotificationCompat.Builder builder,int id){
        builder.setProgress(100,progress,false)
        .setContentText("Downloading "+progress+"%");
        managerCompat.notify(id,builder.build());
    }
    public static void changeContent(String content,NotificationCompat.Builder builder,int id){
        builder.setContentText(content)
                .setOngoing(false);
        managerCompat.notify(id,builder.build());
    }
}
