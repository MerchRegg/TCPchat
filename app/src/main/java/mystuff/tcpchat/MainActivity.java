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
import android.widget.TextView;
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
    private TextView myAddressView;
    private Button sendToServerBtn;
    private Button sendToClientBtn;
    private Button startServerBtn;
    private Button startClientBtn;

    private String myName = "Agilulfo";
    private String clientName;
    private String serverName;
    private final String TAG = "main";
    public static final int DATARETREIVE = 17;
    public static final int DATAOK = 19;
    public static final int DATAERROR = -1;
    public static final String MESSAGE_TO_SEND = "text";
    protected static final String BROADCAST = "MainActivityBroadcast";
    //These are default values for testing.
    private int serverPort = 6789;
    private int clientPort = 6789;
    private String clientIp = NetworkUtils.getIPAddress(true);
    private String myIp;
    private boolean dataFetched = false;
    private boolean serverStarted = false;
    private boolean clientStarted = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        //Erease database
        //getContentResolver().delete(ChatMessagesContentProvider.CONTENT_URI, null, null);

        final EditText editText = (EditText) findViewById(R.id.editText);
        sendToServerBtn = (Button)findViewById(R.id.send_to_server_button);
        sendToClientBtn = (Button)findViewById(R.id.send_to_client_button);
        startServerBtn = (Button)findViewById(R.id.start_server_button);
        startClientBtn = (Button)findViewById(R.id.start_client_button);
        myAddressView = (TextView)findViewById(R.id.my_address_field);
        mList = (ListView)findViewById(R.id.list);

        myIp = NetworkUtils.getIPAddress(true);

        //set the adapter for the list
        mAdapter = new ChatMessageDbAdapter(this);
        mList.setAdapter(mAdapter);

        //create broadcast receiver
        broadcastReceiver = new ServerBroadcastReceiver();
        registerReceiver(broadcastReceiver, new IntentFilter(ServerService.BROADCAST));

        sendToServerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "Sending to server " + serverName);

                String message = editText.getText().toString();

                //add the text in the arrayList
                putMessage(new ChatMessage(-1, message, "C. " + myName, serverName, new Date().toString()));

                //sends the message to the server
                if (mTcpClient != null) {
                    mTcpClient.sendMessage(message);
                }

                //refresh the list
                mAdapter.notifyDataSetChanged();
                editText.setText("");
            }
        });

        sendToClientBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "Sending to client " + clientName);

                String message = editText.getText().toString();

                //add the text in the arrayList
                putMessage(new ChatMessage(-1, message, "S. " + myName, clientName, new Date().toString()));

                //broadcast to server the message to be sent
                broadcastMessage(new Intent(), message, MESSAGE_TO_SEND, BROADCAST);

                //refresh the list
                mAdapter.notifyDataSetChanged();
                editText.setText("");
            }
        });

        startServerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(dataFetched){
                    Log.d(TAG, "Starting server");
                    startServerService(serverPort);
                }
            }
        });

        startClientBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Starting client");
                //connect to server
                checkConnectivity();
                new clientTask().execute("");
                enableSendToClient();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "resume myname: " + myName + " clientName: " + clientName + " serverName: " + serverName);
        if(dataFetched){
            //start a server
            myAddressView.setText("your address is: " + myIp + ":" + serverPort);
            //startServerService(serverPort);
        }
        else{
            Log.d(TAG, "Data not fetched yet, starting GetData");
            startInsertDataActivity(DATARETREIVE);
        }
    }

    /**
     * Starts a new ServerService Service with ServerSocket port specified
     * @param port int, the port of the ServerSocket to be created by the server
     */
    private void startServerService(int port){
        Intent intent = new Intent(this, ServerService.class);
        intent.putExtra("myname", "S. " + myName);
        intent.putExtra("port", port);
        startService(intent);
    }

    /**
     * Stops the ServerService
     */
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

                @Override
                public void receivedServerName(String name) {
                    Log.d(TAG, "Received server name: " + name);
                    serverName = name;
                    publishProgress();
                }
            });
            mTcpClient.setServer(clientIp, clientPort);
            mTcpClient.setName("C. " + myName);
            try {
                mTcpClient.run();
                clientStarted = true;
            } catch (StartClientException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(String[] values) {
            super.onProgressUpdate(values);
            if(values.length < 1){
                sendToServerBtn.setText("Send to " + serverName);
                Log.d(TAG, "Server name set.");
            }
            else {
                Log.d(TAG, "Received message: " + values[0]);
                //add the messaged received from server
                putMessage(new ChatMessage(-1, values[0], serverName, myName, new Date().toString()));
                // notify the adapter that the data set has changed. This means that new message received
                // from server was added to the list
                mAdapter.notifyDataSetChanged();
            }
        }
    }

    /**
     * Adds the values of the given ChatMessage in the messages table in the current database
     * @param message ChatMessage, the message to add in the database
     */
    private void putMessage(ChatMessage message){
        Log.d(TAG, "put message: " + message);
        ContentValues chatMessageValues = new ContentValues();
        chatMessageValues.put(MessagesTable.COLUMN_TEXT, message.getText());
        chatMessageValues.put(MessagesTable.COLUMN_SENDER, message.getSender());
        chatMessageValues.put(MessagesTable.COLUMN_RECEIVER, message.getReceiver());
        chatMessageValues.put(MessagesTable.COLUMN_DATE, message.getDate());
        Log.d(TAG, "put in: " + getContentResolver().insert(ChatMessagesContentProvider.CONTENT_URI, chatMessageValues).toString());
    }

    /**
     * Debug method to print all messages in the messages table in the current database
     */
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

    /**
     * Utility method to check connectivity on the device
     */
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

    /**
     * Receiver of broadcast messages sent by the server
     */
    private class ServerBroadcastReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getBooleanExtra(ServerService.SERVER_STARTED, false)){
                Log.d(TAG, "Server has started, default client could start now");
                enableSendToServer();
            }
            else if(intent.getBooleanExtra(ServerService.RECEIVED_CLIENT_NAME, false)){
                Log.d(TAG, "Received client name: " + (clientName = intent.getStringExtra(ServerService.CLIENT_NAME)));
                sendToClientBtn.setText("Send to " + clientName);
            }
            else{
                Bundle extra = intent.getExtras();
                ChatMessage message = new ChatMessage(-1,
                        extra.getString("text"),
                        extra.getString("sender"),
                        extra.getString("receiver"),
                        extra.getString("date")
                );
                clientName = extra.getString("sender");
                Log.d(TAG, "Received a broadcast message: " + message);
                putMessage(message);
                mAdapter.notifyDataSetChanged();
            }
        }
    }

    /**
     * Starts for result the activity GetData to retreive data from user
     * @param reqType
     */
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
            myName = data.getStringExtra("myName");
            Log.d(TAG, "Set data complete");
            dataFetched = true;
        }
    }

    /**
     * Enables the sendToClientBtn Button and makes the startClientBtn Button invisible
     */
    private void enableSendToClient() {
        clientStarted = true;
        startClientBtn.setVisibility(View.GONE);
        sendToClientBtn.setVisibility(View.VISIBLE);
    }

    /**
     * Enables the sendToServerBtn Button and makes the startServerBtn Button invisible
     */
    private void enableSendToServer() {
        serverStarted = true;
        startServerBtn.setVisibility(View.GONE);
        sendToServerBtn.setVisibility(View.VISIBLE);
    }

    /**
     * Sends a broadcast message with the specified parameters
     * @param intent Intent, the intent to send
     * @param message String, the message to send
     * @param extraName String, the name of the extra where the message will be put
     * @param action String, the action for the broadcast to set in the given intent
     */
    private void broadcastMessage(Intent intent, String message, String extraName, String action){
        Log.d(TAG, "broadcasting to server: " + message);
        intent.setAction(action);
        intent.putExtra(extraName, message);
        sendBroadcast(intent);
    }
}
