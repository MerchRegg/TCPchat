package mystuff.tcpchat.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

import java.sql.SQLException;
import java.util.HashMap;

import mystuff.tcpchat.db.MyDbHelper;

public class MyContentProvider extends ContentProvider {

    /**
     * Database specific constant declarations
     */
    private SQLiteDatabase db;

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "chatmessagesDB.db";
    static final String MESSAGES_TABLE_NAME = "messages";

    static final String AUTHORITY = "mystuff.tcpchat.provider.MyContentProvider";
    static final String URL = "content://" + AUTHORITY + "/" + MESSAGES_TABLE_NAME;
    static final Uri CONTENT_URI = Uri.parse(URL);

    static final String _ID = "_id";
    static final String SENDER = "sender";
    static final String RECEIVER = "receiver";
    static final String TIME = "time";

    private static HashMap<String, String> STUDENTS_PROJECTION_MAP;

    static final int MESSAGES = 1;
    static final int MESSAGE_ID = 2;
    static final int MESSAGE_SENDER = 3;
    static final int MESSAGE_RECEIVER = 4;
    static final int MESSAGE_TIME = 5;

    static final UriMatcher uriMatcher;
    static{
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(AUTHORITY, MESSAGES_TABLE_NAME, MESSAGES);
        uriMatcher.addURI(AUTHORITY, MESSAGES_TABLE_NAME + "/#", MESSAGE_ID);
        uriMatcher.addURI(AUTHORITY, MESSAGES_TABLE_NAME + "/sender", MESSAGE_SENDER);
        uriMatcher.addURI(AUTHORITY, MESSAGES_TABLE_NAME + "/receiver", MESSAGE_RECEIVER);
        uriMatcher.addURI(AUTHORITY, MESSAGES_TABLE_NAME + "/time", MESSAGE_TIME);
    }



    @Override
    public boolean onCreate() {
        Context context = getContext();
        MyDbHelper dbHelper = new MyDbHelper(context, DATABASE_NAME, null, DATABASE_VERSION);

        /**
         * Create a writable database which will trigger its
         * creation if it doesn't already exist.
         */
        db = dbHelper.getWritableDatabase();
        return (db != null);
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        /**
         * Add a new student record
         */
        long rowID = db.insert(	MESSAGES_TABLE_NAME, "", values);

        /**
         * If record is added successfully
         */

        if (rowID > 0){
            Uri _uri = ContentUris.withAppendedId(CONTENT_URI, rowID);
            getContext().getContentResolver().notifyChange(_uri, null);
            return _uri;
        }
        return null;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(MESSAGES_TABLE_NAME);

        switch (uriMatcher.match(uri)) {
            case MESSAGES:
                qb.setProjectionMap(STUDENTS_PROJECTION_MAP);
                break;

            case MESSAGE_ID:
                qb.appendWhere( _ID + "=" + uri.getPathSegments().get(1));
                break;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        if (sortOrder == null || sortOrder.equals("")){
            /**
             * By default sort on student names
             */
            sortOrder = TIME;
        }
        Cursor c = qb.query(db,	projection,	selection, selectionArgs,null, null, sortOrder);

        /**
         * register to watch a content URI for changes
         */
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int count;

        switch (uriMatcher.match(uri)){
            case MESSAGES:
                count = db.delete(MESSAGES_TABLE_NAME, selection, selectionArgs);
                break;

            case MESSAGE_ID:
                String id = uri.getPathSegments().get(1);
                count = db.delete( MESSAGES_TABLE_NAME, _ID +  " = " + id +
                        (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""), selectionArgs);
                break;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int count;

        switch (uriMatcher.match(uri)){
            case MESSAGES:
                count = db.update(MESSAGES_TABLE_NAME, values, selection, selectionArgs);
                break;

            case MESSAGE_ID:
                count = db.update(MESSAGES_TABLE_NAME, values, _ID + " = " + uri.getPathSegments().get(1) +
                        (!TextUtils.isEmpty(selection) ? " AND (" +selection + ')' : ""), selectionArgs);
                break;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri );
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)){
            /**
             * Get all student records
             */
            case MESSAGES:
                return "vnd.android.cursor.dir/vnd.example.messages";

            /**
             * Get a particular student
             */
            case MESSAGE_ID:
                return "vnd.android.cursor.item/vnd.example.messages";

            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }
}
