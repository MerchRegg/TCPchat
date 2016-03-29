package mystuff.tcpchat;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Date;

import mystuff.tcpchat.contentprovider.ChatMessagesContentProvider;
import mystuff.tcpchat.database.ChatMessage;
import mystuff.tcpchat.database.MessagesTable;

public class MainActivity extends Activity {
    private ListView mList;
    private ArrayList<String> arrayList;
    private MyCustomAdapter mAdapter;
    private TCPClient mTcpClient;

    private final String TAG = "main";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        //array of Strings add to the View
        arrayList = new ArrayList<String>();

        final EditText editText = (EditText) findViewById(R.id.editText);
        Button sendBtn = (Button)findViewById(R.id.send_button);

        //set the adapter for the list
        mList = (ListView)findViewById(R.id.list);
        mAdapter = new MyCustomAdapter(this, arrayList);
        mList.setAdapter(mAdapter);

        //connect to server
        new clientTask().execute("");

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String message = editText.getText().toString();

                //add the text in the arrayList
                arrayList.add("c: " + message);

                //sends the message to the server
                if (mTcpClient != null) {
                    putMessage(message, "Client", "Server", new Date().toString());
                    mTcpClient.sendMessage(message);
                }

                //refresh the list
                mAdapter.notifyDataSetChanged();
                editText.setText("");
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mTcpClient.stopClient();
    }

    /**
     * AsyncTask that starts a new TCPClient
     */
    public class clientTask extends AsyncTask<String,String,TCPClient> {

        @Override
        protected TCPClient doInBackground(String[] message) {

            mTcpClient = new TCPClient(new TCPClient.MessageReceivedListener() {
                @Override
                //here the messageReceived method is implemented
                public void messageReceived(String message) {
                    //this method calls the onProgressUpdate
                    publishProgress(message);
                }
            });
            mTcpClient.run();

            return null;
        }

        @Override
        protected void onProgressUpdate(String[] values) {
            super.onProgressUpdate(values);

            //in the arrayList we add the messaged received from server
            arrayList.add(values[0]);
            // notify the adapter that the data set has changed. This means that new message received
            // from server was added to the list
            mAdapter.notifyDataSetChanged();
        }
    }

    private void putMessage(String text, String sender, String receiver, String date){
        Log.d(TAG, "put message: " + text);
        ContentValues chatMessageValues = new ContentValues();
        chatMessageValues.put(MessagesTable.COLUMN_TEXT, text);
        chatMessageValues.put(MessagesTable.COLUMN_SENDER, sender);
        chatMessageValues.put(MessagesTable.COLUMN_RECEIVER, receiver);
        chatMessageValues.put(MessagesTable.COLUMN_DATE, date);
        Log.d(TAG, "put in: " + getContentResolver().insert(ChatMessagesContentProvider.CONTENT_URI, chatMessageValues).toString());
        printDatabase();
    }

    private void printDatabase(){
        Log.d(TAG, "printing database..");
        Cursor cursor = getContentResolver().query(ChatMessagesContentProvider.CONTENT_URI, null, null, null, null);
        if(cursor == null){
            throw new IllegalArgumentException("ERROR IN QUERY MESSAGE TO CONTENT URI");
        }
        cursor.moveToFirst();
        ChatMessage m;
        while(cursor.isAfterLast()){
            m = new ChatMessage(Integer.parseInt(cursor.getString(0)), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4));
            Log.d(TAG, "DATABASEPRINT: "+ m.toString());
            cursor.moveToNext();
        }
        Log.d(TAG, "database printed.");
    }
}
