package mystuff.tcpchat.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

import mystuff.tcpchat.db.MyDbHelper;

public class ChatMessageProvider extends ContentProvider {

    private static final String AUTHORITY = "mystuff.tcpchat.provider.ChatMessageProvider";
    private static final String MESSAGES_TABLE = "messages";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + MESSAGES_TABLE);

    public static final int MESSAGES = 1;
    public static final int MESSAGE_ID = 2;
    public static final int MESSAGE_DATE = 3;

    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        uriMatcher.addURI(AUTHORITY, MESSAGES_TABLE, MESSAGES);
        uriMatcher.addURI(AUTHORITY, MESSAGES_TABLE + "/#", MESSAGE_ID);
        uriMatcher.addURI(AUTHORITY, MESSAGES_TABLE + "/?", MESSAGE_DATE);
    }

    private MyDbHelper dbHelper;


    public ChatMessageProvider() {
    }

    @Override
    public boolean onCreate() {
        dbHelper = new MyDbHelper(getContext(), "ChatMessagesDB.db", null, 1);
        return false;
    }


    @Override
    public Uri insert(Uri uri, ContentValues values) {
        int uriType = uriMatcher.match(uri);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        long id;
        switch (uriType){
            case MESSAGES:{
                id = db.insert(dbHelper.MESSAGES_TABLE, null, values);
                break;
            }
            default:{
                throw new IllegalArgumentException("Unknown URI: "+uri);
            }
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return Uri.parse(MESSAGES_TABLE + "/" + id);
    }


    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder.setTables(dbHelper.MESSAGES_TABLE);

        int uriType = uriMatcher.match(uri);

        switch (uriType){
            case MESSAGE_ID:{
                queryBuilder.appendWhere(dbHelper.COLUMN_ID + "=" + uri.getLastPathSegment());
            }
            case MESSAGE_DATE:{
                queryBuilder.appendWhere(dbHelper.COLUMN_DATE + "=" + uri.getLastPathSegment());
            }
            case MESSAGES:
                break;
        }

        Cursor cursor = queryBuilder.query(dbHelper.getReadableDatabase(), projection, selection, selectionArgs, null, null, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }


    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int uriType = uriMatcher.match(uri);
        SQLiteDatabase sqlDB = dbHelper.getWritableDatabase();
        int rowsUpdated = 0;

        switch (uriType) {
            case MESSAGES:
                rowsUpdated =
                        sqlDB.update(
                                dbHelper.MESSAGES_TABLE,
                                values,
                                selection,
                                selectionArgs);
                break;
            case MESSAGE_ID:
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsUpdated =
                            sqlDB.update(
                                    dbHelper.MESSAGES_TABLE,
                                    values,
                                    dbHelper.COLUMN_ID + "=" + id,
                                    null);
                } else {
                    rowsUpdated =
                            sqlDB.update(
                                    dbHelper.MESSAGES_TABLE,
                                    values,
                                    dbHelper.COLUMN_ID + "=" + id
                                            + " and "
                                            + selection,
                                    selectionArgs);
                }
                break;
            case MESSAGE_DATE:
                id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsUpdated =
                            sqlDB.update(
                                    dbHelper.MESSAGES_TABLE,
                                    values,
                                    dbHelper.COLUMN_DATE + "=" + id,
                                    null);
                } else {
                    rowsUpdated =
                            sqlDB.update(
                                    dbHelper.MESSAGES_TABLE,
                                    values,
                                    dbHelper.COLUMN_DATE + "=" + id
                                            + " and "
                                            + selection,
                                    selectionArgs);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " +
                        uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsUpdated;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int uriType = uriMatcher.match(uri);
        SQLiteDatabase sqlDB = dbHelper.getWritableDatabase();
        int rowsDeleted = 0;

        switch (uriType) {
            case MESSAGES:
                rowsDeleted = sqlDB.delete(
                        dbHelper.MESSAGES_TABLE,
                        selection,
                        selectionArgs);
                break;

            case MESSAGE_ID:
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsDeleted = sqlDB.delete(
                            dbHelper.MESSAGES_TABLE,
                            dbHelper.COLUMN_ID + "=" + id,
                            null);
                } else {
                    rowsDeleted = sqlDB.delete(
                            dbHelper.MESSAGES_TABLE,
                            dbHelper.COLUMN_ID + "=" + id
                                    + " and " + selection,
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
    public String getType(Uri uri) {
        // TODO: Implement this to handle requests for the MIME type of the data
        // at the given URI.
        throw new UnsupportedOperationException("Not yet implemented");
    }

}
