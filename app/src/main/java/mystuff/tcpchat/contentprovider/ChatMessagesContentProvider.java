package mystuff.tcpchat.contentprovider;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import java.util.Arrays;
import java.util.HashSet;

import mystuff.tcpchat.database.ChatMessageDbHelper;
import mystuff.tcpchat.database.MessagesTable;

public class ChatMessagesContentProvider extends ContentProvider {
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
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Implement this to handle requests to delete one or more rows.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public String getType(Uri uri) {
        // TODO: Implement this to handle requests for the MIME type of the data
        // at the given URI.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        // TODO: Implement this to handle requests to insert a new row.
        throw new UnsupportedOperationException("Not yet implemented");
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
        switch (uriType) {
            case MESSAGES:
                break;
            case MESSAGE_SENDER:
                // adding the ID to the original query
                queryBuilder.appendWhere(MessagesTable.COLUMN_SENDER + "="
                        + uri.getLastPathSegment());
                break;
            case MESSAGE_RECEIVER:
                // adding the ID to the original query
                queryBuilder.appendWhere(MessagesTable.COLUMN_RECEIVER + "="
                        + uri.getLastPathSegment());
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        Cursor cursor = queryBuilder.query(db, projection, selection,
                selectionArgs, null, null, sortOrder);
        // make sure that potential listeners are getting notified
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        // TODO: Implement this to handle requests to update one or more rows.
        throw new UnsupportedOperationException("Not yet implemented");
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
}
