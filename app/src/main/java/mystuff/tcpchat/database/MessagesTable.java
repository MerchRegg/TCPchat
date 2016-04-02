package mystuff.tcpchat.database;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class MessagesTable {
    private static final String TAG = "messagestable";
    // Database table
    public static final String TABLE_MESSAGES = "messages";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_TEXT = "text";
    public static final String COLUMN_SENDER = "sender";
    public static final String COLUMN_RECEIVER = "receiver";
    public static final String COLUMN_DATE = "date";

    // Database creation SQL statement
    private static final String DATABASE_CREATE = "create table "
            + TABLE_MESSAGES
            + "("
            + COLUMN_ID + " integer primary key autoincrement, "
            + COLUMN_SENDER + " text not null,"
            + COLUMN_RECEIVER + " text not null, "
            + COLUMN_TEXT + " text not null, "
            + COLUMN_DATE + " text not null"
            + ");";

    public static void onCreate(SQLiteDatabase database) {
        Log.d(TAG, "create database");
        database.execSQL(DATABASE_CREATE);
    }

    public static void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        Log.d(TAG, "Upgrading database from version "
                + oldVersion + " to " + newVersion
                + ", which will destroy all old data");
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_MESSAGES);
        onCreate(database);
    }

    public static void deleteRecords (SQLiteDatabase database){
        Log.d(TAG, "deleting data from database");
        database.delete(TABLE_MESSAGES, null, null);
    }
}
