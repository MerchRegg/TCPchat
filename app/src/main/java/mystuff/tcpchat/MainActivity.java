package mystuff.tcpchat;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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

import java.util.Date;

import mystuff.tcpchat.contentprovider.ChatMessagesContentProvider;
import mystuff.tcpchat.database.ChatMessage;
import mystuff.tcpchat.database.MessagesTable;

public class MainActivity extends Activity {
    private ListView mList;
    private BaseAdapter mAdapter;
    private TCPClient mTcpClient;
    private ServerBroadcastReceiver broadcastReceiver;

    private final String TAG = "main";
    public static final int DATARETREIVE = 17;
    public static final int DATAOK = 19;
    public static final int DATAERROR = -1;
    private int serverPort = 6789;
    private int clientPort = 6789;
    private String clientIp = NetworkUtils.getIPAddress(true);
    private boolean dataFetched = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);


        final EditText editText = (EditText) findViewById(R.id.editText);
        Button sendBtn = (Button)findViewById(R.id.send_button);

        //set the adapter for the list
        mList = (ListView)findViewById(R.id.list);
        mAdapter = new ChatMessageDbAdapter(this);
        mList.setAdapter(mAdapter);

        //create broadcast receiver
        broadcastReceiver = new ServerBroadcastReceiver();
        registerReceiver(broadcastReceiver, new IntentFilter(ServerService.BROADCAST));



        /**
         * NOW this is done when received a broadcast message from the server that says it is started.
        //connect to server
        checkConnectivity();
        new clientTask().execute("");
         */

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String message = editText.getText().toString();

                //add the text in the arrayList
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

    @Override
    protected void onResume() {
        super.onResume();
        if(dataFetched){
            //start a server
            startServerService(serverPort);
        }
        else{
            Log.d(TAG, "Data not fetched yet, starting GetData");
            startInsertDataActivity(DATARETREIVE);
        }
    }

    private void startServerService(int port){
        startService(new Intent(this, ServerService.class).putExtra("port", port));
    }

    private void stopServerService(){
        stopService(new Intent(this, ServerService.class));
    }

    /**
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
        /**
         mTcpClient.stopClient();
         stopServerService();
         */
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
            mTcpClient.setServer(clientIp, clientPort);
            mTcpClient.run();

            return null;
        }

        @Override
        protected void onProgressUpdate(String[] values) {
            super.onProgressUpdate(values);

            Log.d(TAG, "Received message: " + values[0]);
            //add the messaged received from server
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

    private class ServerBroadcastReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getBooleanExtra("ServerStarted", false)){
                Log.d(TAG, "Server has started, starting client");
                //connect to server
                checkConnectivity();
                new clientTask().execute("");
            }
            else{
                Bundle extra = intent.getExtras();
                ChatMessage message = new ChatMessage(-1,
                        extra.getString("text"),
                        extra.getString("sender"),
                        extra.getString("receiver"),
                        extra.getString("date")
                );
                Log.d(TAG, "Received a broadcast message: " + message);
                putMessage(message);
                mAdapter.notifyDataSetChanged();
            }
        }
    }

    private void startInsertDataActivity(int reqType){
        Intent intent = new Intent(this, GetData.class);
        Log.d(TAG, "Starting activity to get data");
        startActivityForResult(intent, reqType);
    }

    @Override
    protected void onActivityResult(int reqCode, int resCode, Intent data){
        Log.d(TAG, "GetData activity has completed task req:" + reqCode + " res:" + resCode);
        if(resCode == DATAOK){
            clientIp = data.getStringExtra("clientIP");
            clientPort = data.getIntExtra("clientPort", clientPort);
            serverPort = data.getIntExtra("serverPort", serverPort);
            Log.d(TAG, "Set data complete");
            dataFetched = true;
        }
    }
}
