package mystuff.tcpchat;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Date;

import mystuff.tcpchat.contentprovider.ChatMessagesContentProvider;
import mystuff.tcpchat.database.ChatMessage;
import mystuff.tcpchat.database.MessagesTable;

public class MainActivity extends Activity {
    private ListView mList;
    private ArrayList<String> arrayList;
    private BaseAdapter mAdapter;
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
        /*
        mAdapter = new MyCustomAdapter(this, arrayList);
        */
        mAdapter = new ChatMessageDbAdapter(this);
        mList.setAdapter(mAdapter);

        //start a server
        startServerService();

        //connect to server
        checkConnectivity();
        new clientTask().execute("");

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String message = editText.getText().toString();

                //add the text in the arrayList
                arrayList.add("c: " + message);
                putMessage(new ChatMessage(-1, message, "Client", "Server", new Date().toString()));

                //sends the message to the server
                if (mTcpClient != null) {
                    mTcpClient.sendMessage(message);
                }

                //refresh the list
                mAdapter.notifyDataSetChanged();
                editText.setText("");
            }
        });

    }

    private void startServerService(){
        startService(new Intent(this, ServerService.class));
    }

    private void stopServerService(){
        stopService(new Intent(this, ServerService.class));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mTcpClient.stopClient();
        stopServerService();
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
            mTcpClient.setServer(NetworkUtils.getIPAddress(true), 6789);
            mTcpClient.run();

            return null;
        }

        @Override
        protected void onProgressUpdate(String[] values) {
            super.onProgressUpdate(values);

            Log.d(TAG, "Received message: " + values[0]);
            //in the arrayList we add the messaged received from server
            arrayList.add(values[0]);
            putMessage(new ChatMessage(-1, values[0], "Server", "Client", new Date().toString()));
            // notify the adapter that the data set has changed. This means that new message received
            // from server was added to the list
            mAdapter.notifyDataSetChanged();
        }
    }

    private void putMessage(ChatMessage message){
        Log.d(TAG, "put message: " + message);
        ContentValues chatMessageValues = new ContentValues();
        chatMessageValues.put(MessagesTable.COLUMN_TEXT, message.getText());
        chatMessageValues.put(MessagesTable.COLUMN_SENDER, message.getSender());
        chatMessageValues.put(MessagesTable.COLUMN_RECEIVER, message.getReceiver());
        chatMessageValues.put(MessagesTable.COLUMN_DATE, message.getDate());
        Log.d(TAG, "put in: " + getContentResolver().insert(ChatMessagesContentProvider.CONTENT_URI, chatMessageValues).toString());
        Log.d(TAG, "message: " + message);
    }

    private void printDatabase(){
        Log.d(TAG, "printing database..");
        Cursor cursor = getContentResolver().query(ChatMessagesContentProvider.CONTENT_URI, null, null, null, null);
        if(cursor == null){
            throw new IllegalArgumentException("ERROR IN QUERY MESSAGE TO CONTENT URI");
        }
        cursor.moveToFirst();
        ChatMessage m;
        while(!cursor.isAfterLast()){
            m = new ChatMessage(
                    Integer.parseInt(cursor.getString(0)),
                    cursor.getString(cursor.getColumnIndex(MessagesTable.COLUMN_TEXT)),
                    cursor.getString(cursor.getColumnIndex(MessagesTable.COLUMN_SENDER)),
                    cursor.getString(cursor.getColumnIndex(MessagesTable.COLUMN_RECEIVER)),
                    cursor.getString(cursor.getColumnIndex(MessagesTable.COLUMN_DATE)));
            Log.d(TAG, "DATABASEPRINT: "+ m.toString());
            cursor.moveToNext();
        }
        Log.d(TAG, "database printed.");
    }

    private void checkConnectivity(){
        ConnectivityManager connectivityManager = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected() && networkInfo.isAvailable())
        {
            Toast.makeText(this, "There is an active network connection!", Toast.LENGTH_LONG).show();
        }
        else
        {
            // PROMPT USER THAT NETWORK IS DISCONNECTED

            Toast.makeText(this, "ERROR: There is no active network connection!", Toast.LENGTH_LONG).show();
        }
    }
}
