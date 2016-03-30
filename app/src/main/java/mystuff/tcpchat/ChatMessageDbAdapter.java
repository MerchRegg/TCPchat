package mystuff.tcpchat;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import mystuff.tcpchat.contentprovider.ChatMessagesContentProvider;
import mystuff.tcpchat.database.ChatMessage;
import mystuff.tcpchat.database.MessagesTable;

/**
 * Created by marco on 30/03/16.
 */
public class ChatMessageDbAdapter  extends BaseAdapter{
    private Context context;
    private LayoutInflater layoutInflater;
    private static String TAG = "dbadapter";

    public ChatMessageDbAdapter(Context context){
        this.context = context;
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        Log.d(TAG, "Created adapter");
    }

    @Override
    public int getCount() {
        Cursor cursor = context.getContentResolver().query(ChatMessagesContentProvider.CONTENT_URI, null, null, null, null);
        Log.d(TAG, "Got count: " + cursor.getCount());
        return (cursor == null) ? -1 : cursor.getCount();
    }

    @Override
    public Object getItem(int position) {
        Cursor cursor = context.getContentResolver().query(ChatMessagesContentProvider.CONTENT_URI, null, null, null, null);
        Log.d(TAG, "Got item at position " + position);
        if(cursor != null && cursor.moveToPosition(position)){
            return new ChatMessage(
                    Integer.parseInt(cursor.getString(0)),
                    cursor.getString(cursor.getColumnIndex(MessagesTable.COLUMN_TEXT)),
                    cursor.getString(cursor.getColumnIndex(MessagesTable.COLUMN_SENDER)),
                    cursor.getString(cursor.getColumnIndex(MessagesTable.COLUMN_RECEIVER)),
                    cursor.getString(cursor.getColumnIndex(MessagesTable.COLUMN_DATE))
            );
        }
        else {
            Log.e(TAG, "Error finding item to position " + position);
            return null;
        }
    }

    @Override
    public long getItemId(int position) {
        Cursor cursor = context.getContentResolver().query(ChatMessagesContentProvider.CONTENT_URI, null, null, null, null);
        Log.d(TAG, "Got item at position " + position);
        if(cursor != null && cursor.moveToPosition(position)){
            return Integer.parseInt(cursor.getString(0));
        }
        else {
            Log.e(TAG, "Error finding item to position " + position);
            return -1;
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //initialize the view
        Log.d(TAG, "Initializing view");
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.list_item, null);
            Log.d(TAG, "Initialized view");
        }

        //get the string item from the position "position" from array list to put it on the TextView
        ChatMessage message = (ChatMessage) getItem(position);
        Log.d(TAG, "Got message " + message.toString());
        if (message != null) {
            TextView itemName = (TextView) convertView.findViewById(R.id.list_item_text_view);
            if (itemName != null) {
                //set the item name on the TextView
                itemName.setText(message.display());
            }
        }

        //this method must return the view corresponding to the data at the specified position.
        return convertView;
    }
}
