package mystuff.tcpchat.contentprovider;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import java.util.Arrays;
import java.util.HashSet;

import mystuff.tcpchat.database.ChatMessageDbHelper;
import mystuff.tcpchat.database.MessagesTable;

public class ChatMessagesContentProvider extends ContentProvider {
    private static final String TAG = "contentprovider";

    //database
    private ChatMessageDbHelper databaseHelper;

    //urimatcher
    private static final int MESSAGES = 100;
    private static final int MESSAGE_SENDER = 200;
    private static final int MESSAGE_RECEIVER = 300;

    private static final String AUTHORITY = "mystuff.tcpchat.contentprovider.ChatMessagesCOntentProvider";

    private static final String BASE_PATH = "messages";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + BASE_PATH);

    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sURIMatcher.addURI(AUTHORITY, BASE_PATH, MESSAGES);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH + "/sender/*", MESSAGE_SENDER);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH + "/receiver/*", MESSAGE_RECEIVER);
    }


    @Override
    public boolean onCreate() {
        databaseHelper = new ChatMessageDbHelper(getContext());
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        // Uisng SQLiteQueryBuilder instead of query() method
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        // check if the caller has requested a column which does not exists
        checkColumns(projection);

        // Set the table
        queryBuilder.setTables(MessagesTable.TABLE_MESSAGES);

        int uriType = sURIMatcher.match(uri);
        Log.d(TAG, "called query()   uritype:" + uriType);
        switch (uriType) {
            case MESSAGES:
                break;
            case MESSAGE_SENDER: {
                // adding the ID to the original query
                queryBuilder.appendWhere(MessagesTable.COLUMN_SENDER + "="
                        + uri.getLastPathSegment());
                break;
            }
            case MESSAGE_RECEIVER: {
                // adding the ID to the original query
                queryBuilder.appendWhere(MessagesTable.COLUMN_RECEIVER + "="
                        + uri.getLastPathSegment());
                break;
            }
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        Cursor cursor = queryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
        // make sure that potential listeners are getting notified
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        /**
         * just for testing
        cursor.moveToFirst();
        Log.d(TAG, "first is: " + cursor.getString(0) + " "+ cursor.getString(1) + " "+ cursor.isAfterLast() + " ");
         */

        return cursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        int uritype = sURIMatcher.match(uri);
        long id;

        switch(uritype){
            case MESSAGES:{
                id = db.insert(MessagesTable.TABLE_MESSAGES, null, values);
                break;
            }
            default:
                throw new IllegalArgumentException("insert: PATH NOT KNOWN");
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return Uri.parse(BASE_PATH + "/" + id);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        int rowsDeleted = 0;
        switch (uriType) {
            case MESSAGES:
                rowsDeleted = db.delete(MessagesTable.TABLE_MESSAGES, selection, selectionArgs);
                break;
            case MESSAGE_SENDER:
                String sender = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsDeleted = db.delete(MessagesTable.TABLE_MESSAGES, MessagesTable.COLUMN_SENDER + "=" + sender, null);
                } else {
                    rowsDeleted = db.delete(MessagesTable.TABLE_MESSAGES,
                            MessagesTable.COLUMN_SENDER + "=" + sender + " and " + selection,
                            selectionArgs);
                }
                break;
            case MESSAGE_RECEIVER:
                String receiver = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsDeleted = db.delete(MessagesTable.TABLE_MESSAGES, MessagesTable.COLUMN_RECEIVER + "=" + receiver, null);
                } else {
                    rowsDeleted = db.delete(MessagesTable.TABLE_MESSAGES,
                            MessagesTable.COLUMN_RECEIVER + "=" + receiver + " and " + selection,
                            selectionArgs);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        int rowsUpdated = 0;
        switch (uriType) {
            case MESSAGES:
                rowsUpdated = db.update(MessagesTable.TABLE_MESSAGES, values, selection, selectionArgs);
                break;
            case MESSAGE_SENDER:
                String sender = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsUpdated = db.update(MessagesTable.TABLE_MESSAGES, values, MessagesTable.COLUMN_SENDER + "=" + sender, null);
                } else {
                    rowsUpdated = db.update(MessagesTable.TABLE_MESSAGES, values,
                            MessagesTable.COLUMN_SENDER + "=" + sender + " and " + selection,
                            selectionArgs);
                }
                break;
            case MESSAGE_RECEIVER:
                String receiver = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsUpdated = db.update(MessagesTable.TABLE_MESSAGES, values, MessagesTable.COLUMN_RECEIVER + "=" + receiver, null);
                } else {
                    rowsUpdated = db.update(MessagesTable.TABLE_MESSAGES, values,
                            MessagesTable.COLUMN_RECEIVER + "=" + receiver + " and " + selection,
                            selectionArgs);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsUpdated;
    }



    private void checkColumns(String[] projection) {
        String[] available = { MessagesTable.COLUMN_DATE,
                MessagesTable.COLUMN_RECEIVER, MessagesTable.COLUMN_SENDER,
                MessagesTable.COLUMN_ID, MessagesTable.COLUMN_TEXT };
        if (projection != null) {
            HashSet<String> requestedColumns = new HashSet<>(Arrays.asList(projection));
            HashSet<String> availableColumns = new HashSet<>(Arrays.asList(available));
            // check if all columns which are requested are available
            if (!availableColumns.containsAll(requestedColumns)) {
                throw new IllegalArgumentException("Unknown columns in projection");
            }
        }
    }

    @Override
    public String getType(Uri uri) {
        // TODO: Implement this to handle requests for the MIME type of the data
        // at the given URI.
        throw new UnsupportedOperationException("Not yet implemented");
    }





}
