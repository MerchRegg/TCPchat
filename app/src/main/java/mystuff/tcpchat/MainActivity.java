package mystuff.tcpchat;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Date;

import mystuff.tcpchat.db.ChatMessage;
import mystuff.tcpchat.db.MyDbHelper;

public class MainActivity extends Activity {
    private ListView mList;
    private ArrayList<String> arrayList;
    private MyCustomAdapter mAdapter;
    private TCPClient mTcpClient;

    private static final String DATABASE_NAME = "chatmessagesDB.db";
    private static String sender = "Client";
    private static String receiver = "Server";
    private MyDbHelper dbHelper;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
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

        //create database helper
        dbHelper = new MyDbHelper(this, DATABASE_NAME, null, 1);

        //connect to server
        new clientTask().execute("");

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String message = editText.getText().toString();

                sendMessage(message);

                //add the text in the arrayList
                arrayList.add("c: " + message);

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
                    printDataBase();
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

    private void sendMessage(String text){
        ChatMessage message = new ChatMessage(text, sender, receiver, new Date().toString());
        dbHelper.addChatMessage(message);


        //add the text in the arrayList
        arrayList.add("c: " + text);

        //sends the message to the server
        if (mTcpClient != null) {
            mTcpClient.sendMessage(text);
        }
    }

    private void printDataBase(){
        ArrayList<ChatMessage> messages = dbHelper.getAllMessages();
        for(ChatMessage m : messages){
            Log.d("main", m.toString());
        }
    }
}
