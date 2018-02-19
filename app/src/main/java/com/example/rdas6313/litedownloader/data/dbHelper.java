package com.example.rdas6313.litedownloader.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by rdas6313 on 5/2/18.
 */

public class dbHelper extends SQLiteOpenHelper {

    private final static String DATABASE_NAME = "Litedatabase.db";
    private final static int DATABASE_VERSION = 1;

    dbHelper(Context context){
        super(context,DATABASE_NAME,null,DATABASE_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_SUCCESS_TABLE = "CREATE TABLE "+DownloaderContract.Success.TABLE+"("
                +DownloaderContract.Success._ID+" integer PRIMARY KEY,"
                +DownloaderContract.Success.TITLE+" text NOT NULL,"
                +DownloaderContract.Success.DOWNLOAD_URL+" text,"
                +DownloaderContract.Success.FILESIZE+" text NOT NULL,"
                +DownloaderContract.Success.SAVE_URI+" text NOT NULL);";

        String CREATE_PAUSEDERROR_TABLE = "CREATE TABLE "+DownloaderContract.PausedError.TABLE+"("
                +DownloaderContract.PausedError._ID+" integer PRIMARY KEY,"
                +DownloaderContract.PausedError.TITLE+" text NOT NULL,"
                +DownloaderContract.PausedError.DOWNLOAD_URL+" text NOT NULL,"
                +DownloaderContract.PausedError.FILESIZE+" text NOT NULL,"
                +DownloaderContract.PausedError.LAST_DOWNLOAD_STATUS+" integer NOT NULL,"
                +DownloaderContract.PausedError.DOWNLOADED_SiZE+" text NOT NULL,"
                +DownloaderContract.PausedError.SAVE_URI+" text NOT NULL);";

        db.execSQL(CREATE_SUCCESS_TABLE);
        db.execSQL(CREATE_PAUSEDERROR_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS "+DownloaderContract.PausedError.TABLE);
        db.execSQL("DROP TABLE IF EXISTS "+DownloaderContract.Success.TABLE);
        onCreate(db);
    }
}
