package com.example.rdas6313.litedownloader.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;


/**
 * Created by rdas6313 on 5/2/18.its a contract class
 */

public final class DownloaderContract {

    public final static String AUTHORITY = "com.example.rdas6313.litedownloader";

    public static class Success implements BaseColumns{

        public final static String PATH = "successfull_table";

        public final static String BASE_URI = AUTHORITY+"/"+PATH;
        public final static Uri CONTENT_URI = Uri.parse("content://"+BASE_URI);

        public final static String TABLE = "successfull_table";
        //Columns added here
        public final static String TITLE = "title";
        public final static String FILESIZE = "filesize";
        public final static String DOWNLOAD_URL = "download_url";
        public final static String SAVE_URI = "save_uri";


        public final static String MIME_TYPE_DIR = ContentResolver.CURSOR_DIR_BASE_TYPE
                +"/vnd."+AUTHORITY+"."+PATH;
        public final static String MIME_TYPE_ITEM = ContentResolver.CURSOR_ITEM_BASE_TYPE
                +"/vnd."+AUTHORITY+"."+PATH;

    }

    public static class PausedError implements BaseColumns{

        public final static String PATH = "paused_error_table";

        public final static String BASE_URI = AUTHORITY+"/"+PATH;
        public final static Uri CONTENT_URI = Uri.parse("content://"+BASE_URI);

        public final static String TABLE = "paused_error_table";
        //Columns added here
        public final static String TITLE = "title";
        public final static String FILESIZE = "filesize";
        public final static String DOWNLOADED_SiZE = "downloaded_size";
        public final static String DOWNLOAD_URL = "download_url";
        public final static String SAVE_URI = "save_uri";


        public final static String MIME_TYPE_DIR = ContentResolver.CURSOR_DIR_BASE_TYPE
                +"/vnd."+AUTHORITY+"."+PATH;
        public final static String MIME_TYPE_ITEM = ContentResolver.CURSOR_ITEM_BASE_TYPE
                +"/vnd."+AUTHORITY+"."+PATH;
    }

}
