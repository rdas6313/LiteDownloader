package com.example.rdas6313.litedownloader.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

public class LiteContentProvider extends ContentProvider {

    private final static String TAG = LiteContentProvider.class.getName();
    private dbHelper helper;

    private final static int PAUSE_ERROR_ITEM = 101;//each data by id
    private final static int PAUSE_ERROR_DIR = 100;//whole data

    private final static int SUCCESS_ITEM = 201;
    private final static int SUCESS_DIR = 200;

    private final static UriMatcher matcher = buildMatcher();

    private static UriMatcher buildMatcher(){
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(DownloaderContract.AUTHORITY,DownloaderContract.PausedError.PATH,PAUSE_ERROR_DIR);
        uriMatcher.addURI(DownloaderContract.AUTHORITY,DownloaderContract.PausedError.PATH+"/#",PAUSE_ERROR_ITEM);
        uriMatcher.addURI(DownloaderContract.AUTHORITY,DownloaderContract.Success.PATH,SUCESS_DIR);
        uriMatcher.addURI(DownloaderContract.AUTHORITY,DownloaderContract.Success.PATH+"/#",SUCCESS_ITEM);
        return uriMatcher;
    }

    public LiteContentProvider() {}

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = helper.getWritableDatabase();
        int match = matcher.match(uri);
        int row = 0;
        switch (match){
            case PAUSE_ERROR_DIR:
                row = db.delete(DownloaderContract.PausedError.TABLE,null,null);
                break;
            case PAUSE_ERROR_ITEM:
                selection = DownloaderContract.PausedError._ID+"=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                row = db.delete(DownloaderContract.PausedError.TABLE,selection,selectionArgs);
                break;
            case SUCESS_DIR:
                row = db.delete(DownloaderContract.Success.TABLE,null,null);
                break;
            case SUCCESS_ITEM:
                selection = DownloaderContract.Success._ID+"=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                row = db.delete(DownloaderContract.Success.TABLE,selection,selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Not yet implemented");
        }
        if(row>0)
            getContext().getContentResolver().notifyChange(uri,null);
        return row;

    }

    @Override
    public String getType(Uri uri) {
        int match = matcher.match(uri);
        switch (match){
            case PAUSE_ERROR_DIR:
                return DownloaderContract.PausedError.MIME_TYPE_DIR;
            case PAUSE_ERROR_ITEM:
                return DownloaderContract.PausedError.MIME_TYPE_ITEM;
            case SUCESS_DIR:
                return DownloaderContract.Success.MIME_TYPE_DIR;
            case SUCCESS_ITEM:
                return DownloaderContract.Success.MIME_TYPE_ITEM;
            default:
                throw new UnsupportedOperationException("Not yet implemented");
        }
    }

    @Override
    public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] values) {
        int match = matcher.match(uri);
        int row = 0;
        switch (match){
            case PAUSE_ERROR_DIR:
                row = bInsert(DownloaderContract.PausedError.TABLE,values);
                break;
            case SUCESS_DIR:
                row =  bInsert(DownloaderContract.Success.TABLE,values);
                break;
            default:
                Log.e(TAG,"Unknown URI");
        }
        if(row > 0)
            getContext().getContentResolver().notifyChange(uri,null);

        return row;
    }

    private int bInsert(String TABLE,ContentValues[] values){
        SQLiteDatabase db = helper.getWritableDatabase();
        int row = 0;
        long id;
        try {
            db.beginTransaction();
            for(ContentValues value:values){
                id = db.insert(TABLE,null,value);
                if(id != -1)
                    row++;
            }
            db.setTransactionSuccessful();
        }catch (Exception e){
            Log.e(TAG,"Error "+e);
        }finally {
            db.endTransaction();
        }
        return row;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase db = helper.getWritableDatabase();
        int match = matcher.match(uri);
        long id = -1;
        Uri localuri = null;
        switch (match){
            case PAUSE_ERROR_DIR:
                id = db.insert(DownloaderContract.PausedError.TABLE,null,values);
                localuri = ContentUris.withAppendedId(DownloaderContract.PausedError.CONTENT_URI,id);
                break;
            case SUCESS_DIR:
                id = db.insert(DownloaderContract.Success.TABLE,null,values);
                localuri = ContentUris.withAppendedId(DownloaderContract.Success.CONTENT_URI,id);
                break;
            default:
                throw new UnsupportedOperationException("Not yet implemented");
        }

        if(localuri != null)
            getContext().getContentResolver().notifyChange(uri,null);

        return localuri;
    }

    @Override
    public boolean onCreate() {
        // TODO: Implement this to initialize your content provider on startup.
        helper = new dbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        int match = matcher.match(uri);
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = null;
        switch (match){
            case PAUSE_ERROR_DIR:
                cursor = db.query(DownloaderContract.PausedError.TABLE,projection,selection,selectionArgs,null,null,sortOrder);
                break;
            case PAUSE_ERROR_ITEM:
                selection = DownloaderContract.PausedError._ID+"=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = db.query(DownloaderContract.PausedError.TABLE,projection,selection,selectionArgs,null,null,null);
                break;
            case SUCESS_DIR:
                cursor = db.query(DownloaderContract.Success.TABLE,projection,selection,selectionArgs,null,null,sortOrder);
                break;
            case SUCCESS_ITEM:
                selection = DownloaderContract.Success._ID+"=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = db.query(DownloaderContract.Success.TABLE,projection,selection,selectionArgs,null,null,sortOrder);
            default:
                throw new UnsupportedOperationException("Not yet implemented");
        }

        if(cursor != null)
            cursor.setNotificationUri(getContext().getContentResolver(),uri);

        return cursor;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        SQLiteDatabase db = helper.getWritableDatabase();
        int match = matcher.match(uri);
        int row = 0;
        switch (match){
            case PAUSE_ERROR_ITEM:
                selection = DownloaderContract.PausedError._ID+"=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                row = db.update(DownloaderContract.PausedError.TABLE,values,selection,selectionArgs);
                break;
            case SUCCESS_ITEM:
                selection = DownloaderContract.Success._ID+"=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                row = db.update(DownloaderContract.Success.TABLE,values,selection,selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Not yet implemented");
        }

        if(row>0){
            getContext().getContentResolver().notifyChange(uri,null);
        }
        return row;
    }
}
