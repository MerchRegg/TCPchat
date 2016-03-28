package mystuff.tcpchat.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class ChatMessageDbHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "messagestable.db";
    public static final int DATABASE_VERSION = 1;

    public ChatMessageDbHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        MessagesTable.onCreate(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        MessagesTable.onUpgrade(db, oldVersion, newVersion);
    }
}
