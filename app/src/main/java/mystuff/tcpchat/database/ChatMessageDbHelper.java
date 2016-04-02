package mystuff.tcpchat.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class ChatMessageDbHelper extends SQLiteOpenHelper {
    private static final String TAG = "dbhelper";
    public static final String DATABASE_NAME = "messagestable.db";
    public static final int DATABASE_VERSION = 1;

    public ChatMessageDbHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "database creation");
        MessagesTable.onCreate(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "database upgrade");
        MessagesTable.onUpgrade(db, oldVersion, newVersion);
    }

    public void deleteAll(SQLiteDatabase db){
        Log.d(TAG, "database data wipe out");
        MessagesTable.deleteRecords(db);
    }
}
