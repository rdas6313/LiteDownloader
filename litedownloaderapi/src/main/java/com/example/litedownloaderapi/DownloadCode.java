package com.example.litedownloaderapi;

/**
 * Created by rdas6313 on 18/1/18.
 */

public class DownloadCode {

    public static final int MALFORMED_URL_ERROR = 1001;
    public static final int IO_ERROR = 1002;
    public static final int RESPONSE_ERROR = 1003;
    public static final int FILE_NOT_FOUND_ERROR = 1004;
    public static final int DOWNLOAD_INTERRUPT_ERROR = 1005;

    public static final int DOWNLOAD_COMPLETED = 2001;
    public static final int DOWNLOAD_PROGRESS = 2002;
    public static final int DOWNLOAD_ERROR = 2003;

    public static final String REQUEST_ID = "req_id";
    public static final String REQUEST_PROGRESS = "req_progress";
    public static final String REQUEST_DOWNLODED_FILE_SIZE = "req_downloded";
    public static final String REQUEST_FILESIZE = "req_filesize";
    public static final String REQUEST_ERROR_ID = "error_id";
    public static final String REQUEST_ERROR_MSG = "error_msg";

}
