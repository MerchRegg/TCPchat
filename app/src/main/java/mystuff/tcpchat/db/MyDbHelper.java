package mystuff.tcpchat.db;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.Date;

import mystuff.tcpchat.provider.ChatMessageProvider;


public class MyDbHelper extends SQLiteOpenHelper {

    public static final String MESSAGES_TABLE = "messages";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_TEXT = "_text";
    public static final String COLUMN_SENDER = "_sender";
    public static final String COLUMN_RECEIVER = "_receiver";
    public static final String COLUMN_DATE = "_date";
    private static int count;

    private ContentResolver contentResolver;

    public MyDbHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        contentResolver = context.getContentResolver();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_PRODUCTS_TABLE = "CREATE TABLE " +
                MESSAGES_TABLE + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_TEXT + " TEXT,"
                + COLUMN_SENDER + " TEXT,"
                + COLUMN_RECEIVER + " TEXT,"
                + COLUMN_DATE + "TEXT" + ")";
        db.execSQL(CREATE_PRODUCTS_TABLE);
        count = 0;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + MESSAGES_TABLE);
        onCreate(db);
    }

    public void addChatMessage(ChatMessage message){
        ContentValues values = new ContentValues();
        values.put(COLUMN_TEXT, message.get_text());
        values.put(COLUMN_SENDER, message.get_sender());
        values.put(COLUMN_RECEIVER, message.get_receiver());
        //values.put(COLUMN_DATE, message.get_time());
        contentResolver.insert(ChatMessageProvider.CONTENT_URI, values);
        count++;
    }

    public ChatMessage findChatMessage(int id){
        /* BEFORE THE UPDATE
        String query = "Select * FROM " + MESSAGES_TABLE + " WHERE " + COLUMN_ID + " =  \"" + id + "\"";

        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.rawQuery(query, null);

        ChatMessage message;

        if (cursor.moveToFirst()) {
            cursor.moveToFirst();
            message = new ChatMessage(Integer.parseInt(cursor.getString(0)), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4));
            cursor.close();
        } else {
            message = null;
        }
        db.close();
        return message;
        */
        String[] projection = {COLUMN_ID, COLUMN_TEXT, COLUMN_RECEIVER, COLUMN_SENDER, COLUMN_DATE };

        String selection = "id = \"" + id + "\"";

        Cursor cursor = contentResolver.query(
                ChatMessageProvider.CONTENT_URI,
                projection,
                selection,
                null,
                null);

        ChatMessage message;

        if (cursor.moveToFirst()) {
            cursor.moveToFirst();
            message = new ChatMessage(Integer.parseInt(cursor.getString(0)), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4));
            cursor.close();
        } else {
            message = null;
        }
        return message;
    }

    public ChatMessage findChatMessage(String date){
        /* BEFORE THE UPDATE
        String query = "Select * FROM " + MESSAGES_TABLE + " WHERE " + COLUMN_ID + " =  \"" + id + "\"";

        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.rawQuery(query, null);

        ChatMessage message;

        if (cursor.moveToFirst()) {
            cursor.moveToFirst();
            message = new ChatMessage(Integer.parseInt(cursor.getString(0)), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4));
            cursor.close();
        } else {
            message = null;
        }
        db.close();
        return message;
        */
        String[] projection = {COLUMN_ID, COLUMN_TEXT, COLUMN_RECEIVER, COLUMN_SENDER, COLUMN_DATE };

        String selection = "date = \"" + date + "\"";

        Cursor cursor = contentResolver.query(
                ChatMessageProvider.CONTENT_URI,
                projection,
                selection,
                null,
                null);

        ChatMessage message;

        if (cursor.moveToFirst()) {
            cursor.moveToFirst();
            message = new ChatMessage(Integer.parseInt(cursor.getString(0)), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4));
            cursor.close();
        } else {
            message = null;
        }
        return message;
    }

    public ArrayList<ChatMessage> getAllMessages(){
        ArrayList<ChatMessage> messages = new ArrayList<>();

        String[] projection = {COLUMN_ID, COLUMN_TEXT, COLUMN_RECEIVER, COLUMN_SENDER, COLUMN_DATE };

        String selection = "";

        Cursor cursor = contentResolver.query(
                ChatMessageProvider.CONTENT_URI,
                projection,
                selection,
                null,
                null);

        while (cursor.moveToFirst()) {
            cursor.moveToFirst();
            messages.add(new ChatMessage(Integer.parseInt(cursor.getString(0)), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4)));
            cursor.close();
        }
        return messages;
    }

    /**
     * Delete a message, if present, with specified id
     * @param id String, the id of the ChatMessage to be deleted
     * @return boolean, true if succesful, false if no message of specified id was found
     */
    public boolean deleteMessage(int id){
        /*
        boolean result = false;

        String query = "Select * FROM " + MESSAGES_TABLE + " WHERE " + COLUMN_ID + " =  \"" + id + "\"";

        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.rawQuery(query, null);

        ChatMessage message = new ChatMessage();

        if (cursor.moveToFirst()) {
            //message.setID(Integer.parseInt(cursor.getString(0)));
            //db.delete(MESSAGES_TABLE, COLUMN_ID + " = ?", new String[] { String.valueOf(message.get_id()) });
            db.delete(MESSAGES_TABLE, COLUMN_ID + " = " + id, null);
            cursor.close();
            result = true;
        }
        db.close();
        return result;
        */

        boolean result = false;

        String selection = "id = \"" + id + "\"";

        int rowsDeleted = contentResolver.delete(ChatMessageProvider.CONTENT_URI, selection, null);

        if (rowsDeleted > 0) {
            count--;
            result = true;
        }

        return result;
    }

    /**
     * Delete a message, if present, with specified date
     * @param date String, the date of the ChatMessage to be deleted
     * @return boolean, true if succesful, false if no message of specified date was found
     */
    public boolean deleteMessage(String date){
        boolean result = false;

        String selection = "date = \"" + date + "\"";

        int rowsDeleted = contentResolver.delete(ChatMessageProvider.CONTENT_URI, selection, null);

        if (rowsDeleted > 0) {
            count--;
            result = true;
        }

        return result;
    }

    /**
     * Get the number of elements (rows) in the MESSAGES_TABLE
     * @return int, the count
     */
    public int getCount(){
        return count;
    }

}
